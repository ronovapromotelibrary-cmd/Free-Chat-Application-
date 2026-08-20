package com.example.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.model.Message
import com.example.data.model.Presence
import com.example.data.model.User
import com.example.data.model.UserSettings
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversationId: String = "",
    val currentUser: User? = null,
    val otherUser: User? = null,
    val otherPresence: Presence = Presence(),
    val isOtherTyping: Boolean = false,
    val isBlocked: Boolean = false,
    val isRecipientBlockingMe: Boolean = false,
    val messages: List<Message> = emptyList(),
    val isUploadingImage: Boolean = false,
    val uploadProgress: Int = 0,
    val selectedMessageForInfo: Message? = null,
    val selectedImageUri: Uri? = null,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val context: Context,
    private val otherUid: String,
    private val authRepository: AuthRepository = AuthRepository(),
    private val chatRepository: ChatRepository = ChatRepository(context),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val currentUid = authRepository.currentUid ?: ""
    private val conversationId = FirebaseDatabaseService.getDeterministicConversationId(currentUid, otherUid)

    private val _uiState = MutableStateFlow(ChatUiState(conversationId = conversationId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var typingJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (currentUid.isBlank() || otherUid.isBlank()) return

        viewModelScope.launch {
            authRepository.observeCurrentUserProfile()
                .catch { e -> Log.w("ChatViewModel", "Error observing current user", e) }
                .collect { user ->
                    _uiState.value = _uiState.value.copy(currentUser = user)
                }
        }

        viewModelScope.launch {
            userRepository.observeUser(otherUid)
                .catch { e -> Log.w("ChatViewModel", "Error observing other user", e) }
                .collect { other ->
                    _uiState.value = _uiState.value.copy(otherUser = other)
                }
        }

        viewModelScope.launch {
            chatRepository.observePresence(otherUid)
                .catch { e -> Log.w("ChatViewModel", "Error observing presence", e) }
                .collect { presence ->
                    _uiState.value = _uiState.value.copy(otherPresence = presence)
                }
        }

        viewModelScope.launch {
            chatRepository.observeTyping(conversationId, otherUid)
                .catch { e -> Log.w("ChatViewModel", "Error observing typing", e) }
                .collect { isTyping ->
                    _uiState.value = _uiState.value.copy(isOtherTyping = isTyping)
                }
        }

        viewModelScope.launch {
            userRepository.observeBlockedUsers(currentUid)
                .catch { e -> Log.w("ChatViewModel", "Error observing blocked users", e) }
                .collect { blockedSet ->
                    _uiState.value = _uiState.value.copy(isBlocked = blockedSet.contains(otherUid))
                }
        }

        // Observe real-time messages and mark seen
        viewModelScope.launch {
            chatRepository.observeMessages(conversationId)
                .catch { e -> Log.w("ChatViewModel", "Error observing messages", e) }
                .collect { msgList ->
                    _uiState.value = _uiState.value.copy(messages = msgList)
                    try {
                        chatRepository.markMessagesAsSeen(conversationId, currentUid)
                    } catch (e: Exception) {
                        Log.w("ChatViewModel", "Error marking messages seen", e)
                    }
                }
        }
    }

    fun onUserTyping(text: String) {
        val isTyping = text.isNotBlank()
        chatRepository.setTyping(conversationId, currentUid, isTyping)

        typingJob?.cancel()
        if (isTyping) {
            typingJob = viewModelScope.launch {
                delay(3000)
                chatRepository.setTyping(conversationId, currentUid, false)
            }
        }
    }

    fun sendTextMessage(text: String) {
        val sender = _uiState.value.currentUser ?: return
        val recipient = _uiState.value.otherUser ?: return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        chatRepository.setTyping(conversationId, currentUid, false)

        viewModelScope.launch {
            chatRepository.sendTextMessage(sender, recipient, trimmed)
        }
    }

    fun selectImageForPreview(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun sendImageMessage(imageUri: Uri) {
        val sender = _uiState.value.currentUser ?: return
        val recipient = _uiState.value.otherUser ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, uploadProgress = 0, selectedImageUri = null)
            try {
                chatRepository.sendImageMessage(sender, recipient, imageUri) { progress ->
                    _uiState.value = _uiState.value.copy(uploadProgress = progress)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Image upload failed")
            } finally {
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(conversationId, messageId)
        }
    }

    fun showMessageInfo(message: Message?) {
        _uiState.value = _uiState.value.copy(selectedMessageForInfo = message)
    }

    fun toggleBlock() {
        viewModelScope.launch {
            if (_uiState.value.isBlocked) {
                userRepository.unblockUser(currentUid, otherUid)
            } else {
                userRepository.blockUser(currentUid, otherUid)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.setTyping(conversationId, currentUid, false)
    }
}
