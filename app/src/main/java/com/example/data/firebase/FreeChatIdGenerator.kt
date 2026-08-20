package com.example.data.firebase

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

object FreeChatIdGenerator {
    private const val CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // Excludes confusing chars 0, 1, I, O
    private const val ID_LENGTH = 8
    private val random = SecureRandom()

    fun generateCandidateId(): String {
        val sb = StringBuilder("FC")
        for (i in 0 until ID_LENGTH) {
            sb.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
        return sb.toString()
    }

    /**
     * Generates a unique Free Chat ID by checking uniqueness against Realtime Database index
     */
    suspend fun generateUniqueFreeChatId(database: FirebaseDatabase): String {
        var attempts = 0
        while (attempts < 10) {
            val candidate = generateCandidateId()
            val normalized = candidate.lowercase().trim()
            val snapshot = database.reference.child("userIdIndex").child(normalized).get().await()
            if (!snapshot.exists()) {
                return candidate
            }
            attempts++
        }
        // Fallback with timestamp salt to guarantee uniqueness
        val timestampSuffix = (System.currentTimeMillis() % 10000).toString()
        return "FC" + generateCandidateId().substring(2, 6) + timestampSuffix
    }
}
