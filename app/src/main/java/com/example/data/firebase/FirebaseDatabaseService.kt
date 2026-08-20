package com.example.data.firebase

import android.util.Log
import com.example.data.model.ConversationItem
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.Presence
import com.example.data.model.User
import com.example.data.model.UserSettings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseDbService"

        fun getDeterministicConversationId(uidA: String, uidB: String): String {
            return if (uidA < uidB) {
                "${uidA}_${uidB}"
            } else {
                "${uidB}_${uidA}"
            }
        }
    }

    suspend fun ensureUserInitialized(uid: String, email: String, name: String): User {
        val existing = getUser(uid)
        if (existing != null && existing.userId.isNotBlank() && !existing.userId.contains("-")) {
            return existing
        }
        val freeChatId = FreeChatIdGenerator.generateUniqueFreeChatId(database)
        val user = User(
            uid = uid,
            userId = freeChatId,
            name = if (!existing?.name.isNullOrBlank()) existing!!.name else (if (name.isNotBlank()) name else "User"),
            email = if (!existing?.email.isNullOrBlank()) existing!!.email else email,
            photoUrl = existing?.photoUrl ?: "",
            createdAt = if (existing != null && existing.createdAt > 0) existing.createdAt else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveUserProfile(user)
        return user
    }

    suspend fun saveUserProfile(user: User) {
        val userRef = database.reference.child("users").child(user.uid)
        val userMap = mapOf(
            "uid" to user.uid,
            "userId" to user.userId,
            "name" to user.name,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
            "createdAt" to if (user.createdAt > 0) user.createdAt else ServerValue.TIMESTAMP,
            "updatedAt" to ServerValue.TIMESTAMP
        )
        userRef.setValue(userMap).await()

        // Create normalized index for lookup
        val normalizedId = user.userId.lowercase().trim()
        val indexRef = database.reference.child("userIdIndex").child(normalizedId)
        indexRef.setValue(mapOf("uid" to user.uid)).await()

        // Initialize default user settings if not exists
        val settingsRef = database.reference.child("userSettings").child(user.uid)
        val settingsSnap = settingsRef.get().await()
        if (!settingsSnap.exists()) {
            settingsRef.setValue(
                mapOf(
                    "notificationsEnabled" to true,
                    "showOnlineStatus" to true,
                    "showLastSeen" to true,
                    "readReceipts" to true
                )
            ).await()
        }
    }

    suspend fun getUser(uid: String): User? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = database.reference.child("users").child(uid).get().await()
            if (snapshot.exists()) {
                mapSnapshotToUser(snapshot)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $uid", e)
            null
        }
    }

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val ref = database.reference.child("users").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    trySend(mapSnapshotToUser(snapshot))
                } else {
                    trySend(null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeUser cancelled: ${error.message}")
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun searchUserByFreeChatId(rawFreeChatId: String): User? {
        val normalized = rawFreeChatId.lowercase().trim()
        if (normalized.isBlank()) return null

        val indexSnap = database.reference.child("userIdIndex").child(normalized).get().await()
        if (!indexSnap.exists()) return null

        val uid = indexSnap.child("uid").getValue(String::class.java) ?: return null
        return getUser(uid)
    }

    fun observeUserConversations(currentUid: String): Flow<List<ConversationItem>> = callbackFlow {
        val ref = database.reference.child("userConversations").child(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ConversationItem>()
                for (child in snapshot.children) {
                    val conversationId = child.key ?: continue
                    val otherUserId = child.child("otherUserId").getValue(String::class.java) ?: ""
                    val otherUserName = child.child("otherUserName").getValue(String::class.java) ?: ""
                    val otherUserPhoto = child.child("otherUserPhoto").getValue(String::class.java) ?: ""
                    val otherFreeChatId = child.child("otherFreeChatId").getValue(String::class.java) ?: ""
                    val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: ""
                    val lastMessageType = child.child("lastMessageType").getValue(String::class.java) ?: MessageType.TEXT.name
                    val lastMessageSenderId = child.child("lastMessageSenderId").getValue(String::class.java) ?: ""
                    val lastMessageTime = child.child("lastMessageTime").getValue(Long::class.java) ?: 0L
                    val lastMessageStatus = child.child("lastMessageStatus").getValue(String::class.java) ?: MessageStatus.SENT.name
                    val unreadCount = (child.child("unreadCount").getValue(Long::class.java) ?: 0L).toInt()

                    list.add(
                        ConversationItem(
                            conversationId = conversationId,
                            otherUserId = otherUserId,
                            otherUserName = otherUserName,
                            otherUserPhoto = otherUserPhoto,
                            otherFreeChatId = otherFreeChatId,
                            lastMessage = lastMessage,
                            lastMessageType = lastMessageType,
                            lastMessageSenderId = lastMessageSenderId,
                            lastMessageTime = lastMessageTime,
                            lastMessageStatus = lastMessageStatus,
                            unreadCount = unreadCount
                        )
                    )
                }
                // Sort by lastMessageTime descending
                trySend(list.sortedByDescending { it.lastMessageTime })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeUserConversations cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val ref = database.reference.child("messages").child(conversationId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = mapSnapshotToMessage(child)
                    if (message != null) {
                        list.add(message)
                    }
                }
                trySend(list.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeMessages cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(
        message: Message,
        sender: User,
        recipient: User
    ) {
        val conversationId = message.conversationId
        val messageRef = database.reference.child("messages").child(conversationId).child(message.id)

        val messageData = mapOf(
            "id" to message.id,
            "conversationId" to conversationId,
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "type" to message.type,
            "text" to message.text,
            "imageUrl" to message.imageUrl,
            "thumbnailUrl" to message.thumbnailUrl,
            "timestamp" to ServerValue.TIMESTAMP,
            "sentAt" to ServerValue.TIMESTAMP,
            "deliveredAt" to 0L,
            "readAt" to 0L,
            "status" to MessageStatus.SENT.name
        )

        // Write message
        messageRef.setValue(messageData).await()

        // Update root conversation
        val conversationRef = database.reference.child("conversations").child(conversationId)
        val convData = mapOf(
            "participants" to mapOf(message.senderId to true, message.receiverId to true),
            "lastMessage" to if (message.type == MessageType.IMAGE.name) "📷 Photo" else message.text,
            "lastMessageType" to message.type,
            "lastMessageSenderId" to message.senderId,
            "lastMessageTime" to ServerValue.TIMESTAMP,
            "updatedAt" to ServerValue.TIMESTAMP
        )
        conversationRef.updateChildren(convData).await()

        // Update sender's userConversation index
        val senderConvRef = database.reference.child("userConversations").child(message.senderId).child(conversationId)
        val senderConvData = mapOf(
            "otherUserId" to recipient.uid,
            "otherUserName" to recipient.name,
            "otherUserPhoto" to recipient.photoUrl,
            "otherFreeChatId" to recipient.userId,
            "lastMessage" to if (message.type == MessageType.IMAGE.name) "📷 Photo" else message.text,
            "lastMessageType" to message.type,
            "lastMessageSenderId" to message.senderId,
            "lastMessageTime" to ServerValue.TIMESTAMP,
            "lastMessageStatus" to MessageStatus.SENT.name,
            "unreadCount" to 0
        )
        senderConvRef.updateChildren(senderConvData).await()

        // Update recipient's userConversation index (increment unread count atomically)
        val receiverConvRef = database.reference.child("userConversations").child(message.receiverId).child(conversationId)
        val receiverSnap = receiverConvRef.get().await()
        val currentUnread = (receiverSnap.child("unreadCount").getValue(Long::class.java) ?: 0L) + 1

        val receiverConvData = mapOf(
            "otherUserId" to sender.uid,
            "otherUserName" to sender.name,
            "otherUserPhoto" to sender.photoUrl,
            "otherFreeChatId" to sender.userId,
            "lastMessage" to if (message.type == MessageType.IMAGE.name) "📷 Photo" else message.text,
            "lastMessageType" to message.type,
            "lastMessageSenderId" to message.senderId,
            "lastMessageTime" to ServerValue.TIMESTAMP,
            "lastMessageStatus" to MessageStatus.DELIVERED.name,
            "unreadCount" to currentUnread
        )
        receiverConvRef.updateChildren(receiverConvData).await()
    }

    suspend fun markMessagesAsSeen(conversationId: String, currentUid: String) {
        val messagesRef = database.reference.child("messages").child(conversationId)
        val snapshot = messagesRef.get().await()
        for (child in snapshot.children) {
            val receiverId = child.child("receiverId").getValue(String::class.java)
            val status = child.child("status").getValue(String::class.java)
            if (receiverId == currentUid && status != MessageStatus.SEEN.name) {
                child.ref.updateChildren(
                    mapOf(
                        "status" to MessageStatus.SEEN.name,
                        "readAt" to ServerValue.TIMESTAMP
                    )
                )
            }
        }
        // Reset unread count for this user
        database.reference.child("userConversations").child(currentUid).child(conversationId).child("unreadCount").setValue(0)
    }

    suspend fun markMessageDelivered(conversationId: String, messageId: String, currentUid: String) {
        val messageRef = database.reference.child("messages").child(conversationId).child(messageId)
        val snap = messageRef.get().await()
        if (snap.exists()) {
            val receiverId = snap.child("receiverId").getValue(String::class.java)
            val status = snap.child("status").getValue(String::class.java)
            if (receiverId == currentUid && status == MessageStatus.SENT.name) {
                messageRef.updateChildren(
                    mapOf(
                        "status" to MessageStatus.DELIVERED.name,
                        "deliveredAt" to ServerValue.TIMESTAMP
                    )
                )
            }
        }
    }

    suspend fun deleteMessage(conversationId: String, messageId: String) {
        database.reference.child("messages").child(conversationId).child(messageId).removeValue().await()
    }

    fun setTyping(conversationId: String, uid: String, isTyping: Boolean) {
        val ref = database.reference.child("typing").child(conversationId).child(uid)
        if (isTyping) {
            ref.setValue(true)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun observeTyping(conversationId: String, otherUid: String): Flow<Boolean> = callbackFlow {
        val ref = database.reference.child("typing").child(conversationId).child(otherUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists() && snapshot.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeTyping cancelled: ${error.message}")
                trySend(false)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observePresence(uid: String): Flow<Presence> = callbackFlow {
        if (uid.isBlank()) {
            trySend(Presence(false, 0L))
            awaitClose { }
            return@callbackFlow
        }
        val ref = database.reference.child("presence").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.child("online").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                trySend(Presence(online, lastSeen))
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observePresence cancelled: ${error.message}")
                trySend(Presence(false, 0L))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setupUserPresence(uid: String) {
        val connectedRef = database.reference.child(".info/connected")
        val userPresenceRef = database.reference.child("presence").child(uid)

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userPresenceRef.child("online").setValue(true)
                    userPresenceRef.child("online").onDisconnect().setValue(false)
                    userPresenceRef.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "setupUserPresence cancelled: ${error.message}")
            }
        })
    }

    suspend fun setOffline(uid: String) {
        try {
            val userPresenceRef = database.reference.child("presence").child(uid)
            userPresenceRef.updateChildren(
                mapOf(
                    "online" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error setting offline", e)
        }
    }

    suspend fun blockUser(currentUid: String, blockedUid: String) {
        database.reference.child("blockedUsers").child(currentUid).child(blockedUid).setValue(true).await()
    }

    suspend fun unblockUser(currentUid: String, blockedUid: String) {
        database.reference.child("blockedUsers").child(currentUid).child(blockedUid).removeValue().await()
    }

    fun observeBlockedUsers(currentUid: String): Flow<Set<String>> = callbackFlow {
        val ref = database.reference.child("blockedUsers").child(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val set = mutableSetOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { set.add(it) }
                }
                trySend(set)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeBlockedUsers cancelled: ${error.message}")
                trySend(emptySet())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addFriend(currentUid: String, friendUid: String) {
        database.reference.child("friends").child(currentUid).child(friendUid).setValue(
            mapOf("createdAt" to ServerValue.TIMESTAMP)
        ).await()
    }

    fun observeFriends(currentUid: String): Flow<List<User>> = callbackFlow {
        val ref = database.reference.child("friends").child(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendUids = snapshot.children.mapNotNull { it.key }
                if (friendUids.isEmpty()) {
                    trySend(emptyList())
                    return
                }
                // Fetch each user
                val usersRef = database.reference.child("users")
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersSnap: DataSnapshot) {
                        val list = mutableListOf<User>()
                        for (uid in friendUids) {
                            val userChild = usersSnap.child(uid)
                            if (userChild.exists()) {
                                mapSnapshotToUser(userChild)?.let { list.add(it) }
                            }
                        }
                        trySend(list)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "observeFriends users lookup cancelled: ${error.message}")
                        trySend(emptyList())
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeFriends cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeUserSettings(uid: String): Flow<UserSettings> = callbackFlow {
        val ref = database.reference.child("userSettings").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = UserSettings(
                    notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean::class.java) ?: true,
                    showOnlineStatus = snapshot.child("showOnlineStatus").getValue(Boolean::class.java) ?: true,
                    showLastSeen = snapshot.child("showLastSeen").getValue(Boolean::class.java) ?: true,
                    readReceipts = snapshot.child("readReceipts").getValue(Boolean::class.java) ?: true
                )
                trySend(settings)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeUserSettings cancelled: ${error.message}")
                trySend(UserSettings())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateUserSettings(uid: String, settings: UserSettings) {
        val ref = database.reference.child("userSettings").child(uid)
        ref.setValue(
            mapOf(
                "notificationsEnabled" to settings.notificationsEnabled,
                "showOnlineStatus" to settings.showOnlineStatus,
                "showLastSeen" to settings.showLastSeen,
                "readReceipts" to settings.readReceipts
            )
        ).await()
    }

    suspend fun updateUserProfileName(uid: String, name: String, photoUrl: String = "") {
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "updatedAt" to ServerValue.TIMESTAMP
        )
        if (photoUrl.isNotBlank()) {
            updates["photoUrl"] = photoUrl
        }
        database.reference.child("users").child(uid).updateChildren(updates).await()
    }

    suspend fun saveFcmToken(uid: String, deviceId: String, token: String) {
        database.reference.child("userDevices").child(uid).child(deviceId).setValue(
            mapOf(
                "fcmToken" to token,
                "platform" to "android",
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    suspend fun deleteUserData(uid: String, normalizedFreeChatId: String) {
        // Clean up user profile, index, settings, presence, friends, devices
        try {
            database.reference.child("users").child(uid).removeValue().await()
            if (normalizedFreeChatId.isNotBlank()) {
                database.reference.child("userIdIndex").child(normalizedFreeChatId).removeValue().await()
            }
            database.reference.child("presence").child(uid).removeValue().await()
            database.reference.child("userSettings").child(uid).removeValue().await()
            database.reference.child("friends").child(uid).removeValue().await()
            database.reference.child("userConversations").child(uid).removeValue().await()
            database.reference.child("blockedUsers").child(uid).removeValue().await()
            database.reference.child("userDevices").child(uid).removeValue().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning user data", e)
        }
    }

    private fun mapSnapshotToUser(snapshot: DataSnapshot): User? {
        val uid = snapshot.child("uid").getValue(String::class.java) ?: snapshot.key ?: return null
        val userId = snapshot.child("userId").getValue(String::class.java) ?: ""
        val name = snapshot.child("name").getValue(String::class.java) ?: ""
        val email = snapshot.child("email").getValue(String::class.java) ?: ""
        val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
        val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L
        val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L

        return User(
            uid = uid,
            userId = userId,
            name = name,
            email = email,
            photoUrl = photoUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun mapSnapshotToMessage(snapshot: DataSnapshot): Message? {
        val id = snapshot.child("id").getValue(String::class.java) ?: snapshot.key ?: return null
        val conversationId = snapshot.child("conversationId").getValue(String::class.java) ?: ""
        val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
        val receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: ""
        val type = snapshot.child("type").getValue(String::class.java) ?: MessageType.TEXT.name
        val text = snapshot.child("text").getValue(String::class.java) ?: ""
        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
        val thumbnailUrl = snapshot.child("thumbnailUrl").getValue(String::class.java) ?: ""
        val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
        val sentAt = snapshot.child("sentAt").getValue(Long::class.java) ?: 0L
        val deliveredAt = snapshot.child("deliveredAt").getValue(Long::class.java) ?: 0L
        val readAt = snapshot.child("readAt").getValue(Long::class.java) ?: 0L
        val status = snapshot.child("status").getValue(String::class.java) ?: MessageStatus.SENT.name

        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            type = type,
            text = text,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl,
            timestamp = timestamp,
            sentAt = sentAt,
            deliveredAt = deliveredAt,
            readAt = readAt,
            status = status
        )
    }
}
