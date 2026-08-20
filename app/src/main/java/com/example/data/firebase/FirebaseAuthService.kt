package com.example.data.firebase

import com.example.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): AuthResult<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("Account creation failed")

            // Generate unique permanent Free Chat ID
            val freeChatId = FreeChatIdGenerator.generateUniqueFreeChatId(com.google.firebase.database.FirebaseDatabase.getInstance())

            val user = User(
                uid = firebaseUser.uid,
                userId = freeChatId,
                name = name.trim(),
                email = email.trim(),
                photoUrl = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save user profile and ID index in Realtime Database
            dbService.saveUserProfile(user)

            // Send verification email
            firebaseUser.sendEmailVerification().await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Registration failed"))
        }
    }

    suspend fun login(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Login failed")
            // Reload user to get fresh emailVerified status
            user.reload().await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Invalid email or password"))
        }
    }

    suspend fun reloadUser(): Boolean {
        return try {
            auth.currentUser?.reload()?.await()
            auth.currentUser?.isEmailVerified == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun resendVerificationEmail(): AuthResult<Unit> {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("No user logged in")
            user.sendEmailVerification().await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Failed to resend verification email"))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Failed to send password reset email"))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit> {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("No user logged in")
            val email = user.email ?: return AuthResult.Error("No email associated")

            // Re-authenticate
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Failed to change password. Please verify current password."))
        }
    }

    suspend fun changeEmail(currentPassword: String, newEmail: String): AuthResult<Unit> {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("No user logged in")
            val oldEmail = user.email ?: return AuthResult.Error("No email associated")

            val credential = EmailAuthProvider.getCredential(oldEmail, currentPassword)
            user.reauthenticate(credential).await()

            user.verifyBeforeUpdateEmail(newEmail.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Failed to initiate email change"))
        }
    }

    suspend fun deleteAccount(currentPassword: String): AuthResult<Unit> {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("No user logged in")
            val email = user.email ?: return AuthResult.Error("No email associated")

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            val uid = user.uid
            val profile = dbService.getUser(uid)
            val normalizedId = profile?.userId?.lowercase()?.trim() ?: ""

            // Delete database references
            dbService.deleteUserData(uid, normalizedId)

            // Delete Firebase user
            user.delete().await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthException(e, "Failed to delete account. Please verify current password."))
        }
    }

    private fun mapAuthException(e: Exception, defaultMessage: String): String {
        val msg = e.message.orEmpty()
        return when {
            e is com.google.firebase.auth.FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) ->
                "The email address is already in use by another account."
            e is com.google.firebase.auth.FirebaseAuthWeakPasswordException || msg.contains("password should be at least", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."
            e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                msg.contains("badly formatted", ignoreCase = true) ||
                msg.contains("invalid credential", ignoreCase = true) ||
                msg.contains("incorrect", ignoreCase = true) ||
                msg.contains("malformed", ignoreCase = true) ||
                msg.contains("expired", ignoreCase = true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Incorrect email or password. Please try again."
            e is com.google.firebase.auth.FirebaseAuthInvalidUserException || msg.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email. Please create an account first."
            msg.contains("too-many-requests", ignoreCase = true) || msg.contains("blocked all requests", ignoreCase = true) ->
                "Access temporarily blocked due to many failed attempts. Please try again later or reset your password."
            msg.contains("network", ignoreCase = true) ->
                "Network error. Please check your internet connection."
            e.localizedMessage.isNullOrBlank() -> defaultMessage
            else -> e.localizedMessage ?: defaultMessage
        }
    }

    fun signOut() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // best effort mark offline
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .reference.child("presence").child(uid)
                    .updateChildren(
                        mapOf(
                            "online" to false,
                            "lastSeen" to com.google.firebase.database.ServerValue.TIMESTAMP
                        )
                    )
            } catch (e: Exception) {
                // ignore
            }
        }
        auth.signOut()
    }
}
