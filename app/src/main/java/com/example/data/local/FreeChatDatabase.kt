package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.entity.LocalConversationEntity
import com.example.data.local.entity.LocalMessageEntity

@Database(
    entities = [LocalMessageEntity::class, LocalConversationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FreeChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: FreeChatDatabase? = null

        fun getDatabase(context: Context): FreeChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreeChatDatabase::class.java,
                    "free_chat_local_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
