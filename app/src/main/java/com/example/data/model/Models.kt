package com.example.data.model

data class User(
    val uid: String = "",
    val userId: String = "", // e.g. FC8K4M2P7X (Permanent Free Chat ID)
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class Presence(
    val online: Boolean = false,
    val lastSeen: Long = 0L
)

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val readReceipts: Boolean = true
)

enum class MessageType {
    TEXT,
    IMAGE
}

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    SEEN,
    FAILED
}

data class Message(
    val id: String = "", // Unique local / server message ID
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: String = MessageType.TEXT.name,
    val text: String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val timestamp: Long = 0L,
    val sentAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val readAt: Long = 0L,
    val status: String = MessageStatus.SENT.name
)

data class ConversationItem(
    val conversationId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserPhoto: String = "",
    val otherFreeChatId: String = "",
    val lastMessage: String = "",
    val lastMessageType: String = MessageType.TEXT.name,
    val lastMessageSenderId: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageStatus: String = MessageStatus.SENT.name,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)
