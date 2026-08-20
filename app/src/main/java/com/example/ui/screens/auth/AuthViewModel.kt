package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthResult
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isEmailVerified: Boolean = false,
    val registeredUser: User? = null,
    val loggedInUser: FirebaseUser? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun register(name: String, email: String, pass: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.length < 2) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid name (at least 2 characters)")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.register(trimmedName, trimmedEmail, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredUser = result.data,
                        successMessage = "Verification email sent to $trimmedEmail"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.login(trimmedEmail, pass)) {
                is AuthResult.Success -> {
                    val isVerified = result.data.isEmailVerified
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = isVerified,
                        isEmailVerified = isVerified,
                        loggedInUser = result.data,
                        errorMessage = if (!isVerified) "Please verify your email before using Free Chat." else null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun checkEmailVerification(onVerified: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val isVerified = authRepository.reloadUser()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isEmailVerified = isVerified
            )
            if (isVerified) {
                onVerified()
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Email not verified yet. Please check your inbox and spam folder."
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.resendVerificationEmail()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Verification email resent successfully!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val trimmed = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.sendPasswordResetEmail(trimmed)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset email sent! Check your inbox."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
