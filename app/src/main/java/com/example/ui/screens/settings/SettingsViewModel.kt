package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthResult
import com.example.data.model.User
import com.example.data.model.UserSettings
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val blockedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    val currentUid: String?
        get() = authRepository.currentUid

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadBlockedUsers()
    }

    private fun loadSettings() {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.observeUserSettings(uid).collect { settings ->
                _uiState.value = _uiState.value.copy(userSettings = settings)
            }
        }
    }

    private fun loadBlockedUsers() {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.observeBlockedUsers(uid).collect { blockedUids ->
                val list = mutableListOf<User>()
                for (bUid in blockedUids) {
                    val u = userRepository.getUser(bUid)
                    if (u != null) list.add(u)
                }
                _uiState.value = _uiState.value.copy(blockedUsers = list)
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val uid = currentUid ?: return
        val newSettings = _uiState.value.userSettings.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            userRepository.updateUserSettings(uid, newSettings)
        }
    }

    fun updateShowOnlineStatus(enabled: Boolean) {
        val uid = currentUid ?: return
        val newSettings = _uiState.value.userSettings.copy(showOnlineStatus = enabled)
        viewModelScope.launch {
            userRepository.updateUserSettings(uid, newSettings)
        }
    }

    fun updateShowLastSeen(enabled: Boolean) {
        val uid = currentUid ?: return
        val newSettings = _uiState.value.userSettings.copy(showLastSeen = enabled)
        viewModelScope.launch {
            userRepository.updateUserSettings(uid, newSettings)
        }
    }

    fun updateReadReceipts(enabled: Boolean) {
        val uid = currentUid ?: return
        val newSettings = _uiState.value.userSettings.copy(readReceipts = enabled)
        viewModelScope.launch {
            userRepository.updateUserSettings(uid, newSettings)
        }
    }

    fun unblockUser(targetUid: String) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            userRepository.unblockUser(uid, targetUid)
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        if (newPass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "New password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val res = authRepository.changePassword(currentPass, newPass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password updated successfully!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun changeEmail(currentPass: String, newEmail: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail.trim()).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val res = authRepository.changeEmail(currentPass, newEmail.trim())) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Verification link sent to $newEmail. Please verify to complete the change."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun deleteAccount(currentPass: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = authRepository.deleteAccount(currentPass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onDeleted()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
