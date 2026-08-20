package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LocalConversationEntity
import com.example.data.local.entity.LocalMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM local_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<LocalMessageEntity>>

    @Query("SELECT * FROM local_messages WHERE isPendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getPendingSyncMessages(): List<LocalMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessage(message: LocalMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessages(messages: List<LocalMessageEntity>)

    @Update
    suspend fun updateMessage(message: LocalMessageEntity)

    @Query("DELETE FROM local_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM local_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM local_messages")
    suspend fun clearAll()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM local_conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<LocalConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversation(conversation: LocalConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversations(conversations: List<LocalConversationEntity>)

    @Query("DELETE FROM local_conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM local_conversations")
    suspend fun clearAll()
}
