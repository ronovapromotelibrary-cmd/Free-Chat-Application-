package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.firebase.AuthResult
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.firebase.FirebaseStorageService
import com.example.data.local.FreeChatDatabase
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.model.ConversationItem
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.Presence
import com.example.data.model.User
import com.example.data.model.UserSettings
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class AuthRepository(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService()
) {
    val currentUser: FirebaseUser?
        get() = authService.currentUser

    val currentUid: String?
        get() = authService.currentUid

    suspend fun register(name: String, email: String, password: String): AuthResult<User> =
        authService.register(name, email, password)

    suspend fun login(email: String, password: String): AuthResult<FirebaseUser> =
        authService.login(email, password)

    suspend fun reloadUser(): Boolean =
        authService.reloadUser()

    suspend fun resendVerificationEmail(): AuthResult<Unit> =
        authService.resendVerificationEmail()

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> =
        authService.sendPasswordResetEmail(email)

    suspend fun changePassword(current: String, newPass: String): AuthResult<Unit> =
        authService.changePassword(current, newPass)

    suspend fun changeEmail(current: String, newEmail: String): AuthResult<Unit> =
        authService.changeEmail(current, newEmail)

    suspend fun deleteAccount(current: String): AuthResult<Unit> =
        authService.deleteAccount(current)

    fun signOut() = authService.signOut()

    suspend fun getCurrentUserProfile(): User? {
        val uid = currentUid ?: return null
        return dbService.getUser(uid)
    }

    suspend fun ensureUserProfile(): User? {
        val firebaseUser = authService.currentUser ?: return null
        val email = firebaseUser.email.orEmpty()
        val displayName = firebaseUser.displayName.orEmpty().ifBlank {
            if (email.contains("@")) email.substringBefore("@") else "User"
        }
        return dbService.ensureUserInitialized(firebaseUser.uid, email, displayName)
    }

    fun observeCurrentUserProfile(): Flow<User?> {
        val uid = currentUid ?: return kotlinx.coroutines.flow.flowOf(null)
        return dbService.observeUser(uid)
    }
}

class ChatRepository(
    private val context: Context,
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService(),
    private val storageService: FirebaseStorageService = FirebaseStorageService(),
    private val localDb: FreeChatDatabase = FreeChatDatabase.getDatabase(context)
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun observeConversations(currentUid: String): Flow<List<ConversationItem>> =
        dbService.observeUserConversations(currentUid)

    fun observeMessages(conversationId: String): Flow<List<Message>> {
        // Listen to Firebase and sync to Room cache
        val firebaseFlow = dbService.observeMessages(conversationId)
        scope.launch {
            firebaseFlow.collect { msgs ->
                localDb.messageDao().insertOrUpdateMessages(
                    msgs.map { LocalMessageEntity.fromMessage(it, isPendingSync = false) }
                )
            }
        }
        return firebaseFlow
    }

    fun getLocalMessagesFlow(conversationId: String): Flow<List<Message>> =
        localDb.messageDao().getMessagesForConversation(conversationId).map { list ->
            list.map { it.toMessage() }
        }

    suspend fun sendTextMessage(
        sender: User,
        recipient: User,
        text: String
    ): Message {
        val conversationId = FirebaseDatabaseService.getDeterministicConversationId(sender.uid, recipient.uid)
        val messageId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"

        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = sender.uid,
            receiverId = recipient.uid,
            type = MessageType.TEXT.name,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            sentAt = System.currentTimeMillis(),
            status = MessageStatus.PENDING.name
        )

        // Store in local DB first (Local-First UX)
        localDb.messageDao().insertOrUpdateMessage(
            LocalMessageEntity.fromMessage(message, isPendingSync = true)
        )

        try {
            dbService.sendMessage(message, sender, recipient)
            // Update local status to SENT
            localDb.messageDao().insertOrUpdateMessage(
                LocalMessageEntity.fromMessage(message.copy(status = MessageStatus.SENT.name), isPendingSync = false)
            )
        } catch (e: Exception) {
            // Keep as isPendingSync = true for background sync engine
        }

        return message
    }

    suspend fun sendImageMessage(
        sender: User,
        recipient: User,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {}
    ): Message {
        val conversationId = FirebaseDatabaseService.getDeterministicConversationId(sender.uid, recipient.uid)
        val messageId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"

        // Upload to Storage
        val downloadUrl = storageService.uploadChatImage(
            conversationId = conversationId,
            messageId = messageId,
            imageUri = imageUri,
            onProgress = onProgress
        )

        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = sender.uid,
            receiverId = recipient.uid,
            type = MessageType.IMAGE.name,
            text = "",
            imageUrl = downloadUrl,
            timestamp = System.currentTimeMillis(),
            sentAt = System.currentTimeMillis(),
            status = MessageStatus.SENT.name
        )

        localDb.messageDao().insertOrUpdateMessage(
            LocalMessageEntity.fromMessage(message, isPendingSync = false)
        )

        dbService.sendMessage(message, sender, recipient)
        return message
    }

    suspend fun markMessagesAsSeen(conversationId: String, currentUid: String) {
        dbService.markMessagesAsSeen(conversationId, currentUid)
    }

    suspend fun deleteMessage(conversationId: String, messageId: String) {
        localDb.messageDao().deleteMessageById(messageId)
        dbService.deleteMessage(conversationId, messageId)
    }

    fun setTyping(conversationId: String, uid: String, isTyping: Boolean) {
        dbService.setTyping(conversationId, uid, isTyping)
    }

    fun observeTyping(conversationId: String, otherUid: String): Flow<Boolean> =
        dbService.observeTyping(conversationId, otherUid)

    fun observePresence(uid: String): Flow<Presence> =
        dbService.observePresence(uid)

    fun setupPresence(uid: String) =
        dbService.setupUserPresence(uid)

    suspend fun setUserOffline(uid: String) =
        dbService.setOffline(uid)
}

class UserRepository(
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService(),
    private val storageService: FirebaseStorageService = FirebaseStorageService()
) {
    suspend fun getUser(uid: String): User? = dbService.getUser(uid)

    fun observeUser(uid: String): Flow<User?> = dbService.observeUser(uid)

    suspend fun searchUserByFreeChatId(rawId: String): User? =
        dbService.searchUserByFreeChatId(rawId)

    suspend fun updateProfile(uid: String, name: String, imageUri: Uri?): String {
        var photoUrl = ""
        if (imageUri != null) {
            photoUrl = storageService.uploadProfilePhoto(uid, imageUri)
        }
        dbService.updateUserProfileName(uid, name, photoUrl)
        return photoUrl
    }

    suspend fun blockUser(currentUid: String, blockedUid: String) =
        dbService.blockUser(currentUid, blockedUid)

    suspend fun unblockUser(currentUid: String, blockedUid: String) =
        dbService.unblockUser(currentUid, blockedUid)

    fun observeBlockedUsers(currentUid: String): Flow<Set<String>> =
        dbService.observeBlockedUsers(currentUid)

    suspend fun addFriend(currentUid: String, friendUid: String) =
        dbService.addFriend(currentUid, friendUid)

    fun observeFriends(currentUid: String): Flow<List<User>> =
        dbService.observeFriends(currentUid)

    fun observeUserSettings(uid: String): Flow<UserSettings> =
        dbService.observeUserSettings(uid)

    suspend fun updateUserSettings(uid: String, settings: UserSettings) =
        dbService.updateUserSettings(uid, settings)
}
