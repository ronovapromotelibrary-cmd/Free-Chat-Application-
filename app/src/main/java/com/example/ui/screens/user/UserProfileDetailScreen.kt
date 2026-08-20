package com.example.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Presence
import com.example.data.model.User
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.FreeChatTopBar
import com.example.ui.components.PinkButton
import com.example.ui.components.PinkOutlinedButton
import com.example.ui.components.UserAvatar
import com.example.ui.screens.chat.formatLastSeen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.OnlineBadgeColor
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun UserProfileDetailScreen(
    targetUid: String,
    currentUid: String,
    userRepository: UserRepository,
    chatRepository: ChatRepository,
    onBackClick: () -> Unit,
    onOpenChat: (targetUid: String) -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var presence by remember { mutableStateOf(Presence()) }
    var isBlocked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(targetUid) {
        isLoading = true
        user = userRepository.getUser(targetUid)
        isLoading = false

        chatRepository.observePresence(targetUid).collectLatest {
            presence = it
        }
    }

    LaunchedEffect(currentUid, targetUid) {
        userRepository.observeBlockedUsers(currentUid).collectLatest { blockedSet ->
            isBlocked = blockedSet.contains(targetUid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("user_profile_detail_screen")
    ) {
        FreeChatTopBar(
            title = "Profile",
            onBackClick = onBackClick
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPink)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found", color = SecondaryText)
            }
        } else {
            val u = user!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            photoUrl = u.photoUrl,
                            name = u.name,
                            size = 90.dp,
                            showOnlineBadge = true,
                            isOnline = presence.online
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = u.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (presence.online) "● Online" else if (presence.lastSeen > 0) "Last seen ${formatLastSeen(presence.lastSeen)}" else "Offline",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (presence.online) OnlineBadgeColor else SecondaryText,
                                fontWeight = if (presence.online) FontWeight.Bold else FontWeight.Normal
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Permanent Free Chat ID
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightPink.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = PrimaryPink,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Permanent Free Chat ID",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryPink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = u.userId,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PrimaryPink,
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(u.userId))
                                        isCopied = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isCopied) "Copied!" else "Copy ID",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PinkButton(
                            text = "Send Message",
                            onClick = { onOpenChat(u.uid) },
                            testTag = "user_detail_send_message_button"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PinkOutlinedButton(
                            text = if (isBlocked) "Unblock User" else "Block User",
                            onClick = {
                                scope.launch {
                                    if (isBlocked) {
                                        userRepository.unblockUser(currentUid, targetUid)
                                    } else {
                                        userRepository.blockUser(currentUid, targetUid)
                                    }
                                }
                            },
                            testTag = "user_detail_block_button"
                        )
                    }
                }
            }
        }
    }
}
