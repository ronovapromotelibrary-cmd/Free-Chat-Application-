package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.MessageStatus

@Entity(tableName = "local_messages")
data class LocalMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: String = MessageType.TEXT.name,
    val text: String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sentAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val readAt: Long = 0L,
    val status: String = MessageStatus.PENDING.name,
    val isPendingSync: Boolean = false
) {
    fun toMessage(): Message {
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

    companion object {
        fun fromMessage(message: Message, isPendingSync: Boolean = false): LocalMessageEntity {
            return LocalMessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                receiverId = message.receiverId,
                type = message.type,
                text = message.text,
                imageUrl = message.imageUrl,
                thumbnailUrl = message.thumbnailUrl,
                timestamp = message.timestamp,
                sentAt = message.sentAt,
                deliveredAt = message.deliveredAt,
                readAt = message.readAt,
                status = message.status,
                isPendingSync = isPendingSync
            )
        }
    }
}

@Entity(tableName = "local_conversations")
data class LocalConversationEntity(
    @PrimaryKey val conversationId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserPhoto: String,
    val otherFreeChatId: String,
    val lastMessage: String,
    val lastMessageType: String,
    val lastMessageSenderId: String,
    val lastMessageTime: Long,
    val lastMessageStatus: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val lastSeen: Long
)
