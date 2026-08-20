package com.example.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadChatImage(
        conversationId: String,
        messageId: String,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {}
    ): String {
        val ref = storage.reference.child("chat_images").child(conversationId).child("$messageId.jpg")
        val uploadTask = ref.putFile(imageUri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            onProgress(progress)
        }

        uploadTask.await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): String {
        val ref = storage.reference.child("profile_photos").child("$uid.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
