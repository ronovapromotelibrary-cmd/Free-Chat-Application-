package com.example.ui.screens.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.model.ConversationItem
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.data.service.SyncEngine
import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val searchResult: User? = null,
    val isUserNotFound: Boolean = false,
    val isSelfSearch: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val context: Context,
    private val authRepository: AuthRepository = AuthRepository(),
    private val chatRepository: ChatRepository = ChatRepository(context),
    private val userRepository: UserRepository = UserRepository(),
    val syncEngine: SyncEngine = SyncEngine(context)
) : ViewModel() {

    val currentUid: String?
        get() = authRepository.currentUid

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    init {
        syncEngine.start()
        loadUserData()
    }

    override fun onCleared() {
        super.onCleared()
        syncEngine.stop()
    }

    fun loadUserData() {
        val uid = currentUid ?: return
        chatRepository.setupPresence(uid)

        viewModelScope.launch {
            try {
                val profile = authRepository.ensureUserProfile()
                if (profile != null && _currentUser.value == null) {
                    _currentUser.value = profile
                }
            } catch (e: Exception) {
                Log.w("MainViewModel", "Error ensuring user profile initialized", e)
            }
        }

        viewModelScope.launch {
            authRepository.observeCurrentUserProfile()
                .catch { e -> Log.w("MainViewModel", "Error observing user profile", e) }
                .collect { user ->
                    _currentUser.value = user
                }
        }

        viewModelScope.launch {
            chatRepository.observeConversations(uid)
                .catch { e -> Log.w("MainViewModel", "Error observing conversations", e) }
                .collect { list ->
                    _conversations.value = list
                }
        }

        viewModelScope.launch {
            userRepository.observeFriends(uid)
                .catch { e -> Log.w("MainViewModel", "Error observing friends", e) }
                .collect { list ->
                    _friends.value = list
                }
        }

        viewModelScope.launch {
            userRepository.observeBlockedUsers(uid)
                .catch { e -> Log.w("MainViewModel", "Error observing blocked users", e) }
                .collect { set ->
                    _blockedUserIds.value = set
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchState.value = _searchState.value.copy(
            query = query,
            searchResult = null,
            isUserNotFound = false,
            isSelfSearch = false,
            errorMessage = null
        )
    }

    fun searchUserById() {
        val rawId = _searchState.value.query.trim()
        if (rawId.isBlank()) {
            _searchState.value = _searchState.value.copy(errorMessage = "Please enter a Free Chat ID")
            return
        }

        val myFreeChatId = _currentUser.value?.userId ?: ""
        if (rawId.equals(myFreeChatId, ignoreCase = true)) {
            _searchState.value = _searchState.value.copy(
                isSelfSearch = true,
                searchResult = null,
                isUserNotFound = false,
                isSearching = false
            )
            return
        }

        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                isSearching = true,
                searchResult = null,
                isUserNotFound = false,
                isSelfSearch = false,
                errorMessage = null
            )
            try {
                val foundUser = userRepository.searchUserByFreeChatId(rawId)
                if (foundUser != null) {
                    _searchState.value = _searchState.value.copy(
                        isSearching = false,
                        searchResult = foundUser,
                        isUserNotFound = false
                    )
                } else {
                    _searchState.value = _searchState.value.copy(
                        isSearching = false,
                        searchResult = null,
                        isUserNotFound = true
                    )
                }
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    errorMessage = e.localizedMessage ?: "Search failed"
                )
            }
        }
    }

    fun addFriend(friend: User) {
        val myUid = currentUid ?: return
        viewModelScope.launch {
            userRepository.addFriend(myUid, friend.uid)
        }
    }

    fun toggleBlockUser(targetUid: String) {
        val myUid = currentUid ?: return
        viewModelScope.launch {
            if (_blockedUserIds.value.contains(targetUid)) {
                userRepository.unblockUser(myUid, targetUid)
            } else {
                userRepository.blockUser(myUid, targetUid)
            }
        }
    }

    fun updateProfile(name: String, imageUri: Uri?, onComplete: () -> Unit) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            try {
                userRepository.updateProfile(uid, name, imageUri)
                onComplete()
            } catch (e: Exception) {
                // ignore
            } finally {
                _isUpdatingProfile.value = false
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        val uid = currentUid
        viewModelScope.launch {
            if (uid != null) {
                try {
                    chatRepository.setUserOffline(uid)
                } catch (e: Exception) {
                    Log.w("MainViewModel", "Failed to set offline during signout", e)
                }
            }
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                Log.w("MainViewModel", "Error signing out", e)
            }
            _currentUser.value = null
            _conversations.value = emptyList()
            _friends.value = emptyList()
            _blockedUserIds.value = emptySet()
            onSignedOut()
        }
    }
}
