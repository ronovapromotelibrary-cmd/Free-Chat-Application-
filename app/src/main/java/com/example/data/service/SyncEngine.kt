package com.example.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.local.FreeChatDatabase
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.model.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncEngine(
    private val context: Context,
    private val database: FreeChatDatabase = FreeChatDatabase.getDatabase(context),
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService()
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
            syncPendingMessages()
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Initial connectivity check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isOnline.value = online
        if (online) {
            syncPendingMessages()
        }
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun syncPendingMessages() {
        scope.launch {
            if (_isSyncing.value) return@launch
            _isSyncing.value = true
            try {
                val pendingList = database.messageDao().getPendingSyncMessages()
                for (localMsg in pendingList) {
                    val message = localMsg.toMessage()
                    val sender = dbService.getUser(message.senderId)
                    val recipient = dbService.getUser(message.receiverId)

                    if (sender != null && recipient != null) {
                        try {
                            dbService.sendMessage(message, sender, recipient)
                            database.messageDao().insertOrUpdateMessage(
                                localMsg.copy(
                                    status = MessageStatus.SENT.name,
                                    isPendingSync = false
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("SyncEngine", "Failed to sync message ${message.id}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncEngine", "Sync error", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
