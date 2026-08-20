package com.example.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.data.model.MessageType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusTicks
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DividerColor
import com.example.ui.theme.LightPink
import com.example.ui.theme.OnlineBadgeColor
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryPinkDark
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground
import com.example.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    viewModel: MainViewModel,
    onOpenChat: (otherUid: String, convId: String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()
    val isOnline by viewModel.syncEngine.isOnline.collectAsState()
    val isSyncing by viewModel.syncEngine.isSyncing.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("chats_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chats",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = PrimaryText,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = LightPink,
                    modifier = Modifier.clickable { onNavigateToSearch() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search ID",
                            tint = PrimaryPinkDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FIND BY ID",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PrimaryPinkDark,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            // Offline / Syncing Banner
            AnimatedVisibility(
                visible = !isOnline || isSyncing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (!isOnline) WarningYellow.copy(alpha = 0.2f) else LightPink)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (!isOnline) WarningYellow else PrimaryPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!isOnline) "You're offline. Messages will send when you're back online." else "Syncing messages...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Chats Yet",
                        description = "Find a friend using their Free Chat ID\nand start your first conversation.",
                        icon = Icons.Default.ChatBubbleOutline,
                        actionButtonText = "Find by Free Chat ID",
                        onActionClick = onNavigateToSearch
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(conversations, key = { it.conversationId }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            currentUid = viewModel.currentUid ?: "",
                            onClick = { onOpenChat(conv.otherUserId, conv.conversationId) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // FAB to start new chat
        FloatingActionButton(
            onClick = onNavigateToSearch,
            containerColor = PrimaryPink,
            contentColor = PureWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("new_chat_fab")
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "New Chat",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ConversationRow(
    conversation: ConversationItem,
    currentUid: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_row_${conversation.conversationId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = conversation.otherUserPhoto,
                name = conversation.otherUserName.ifBlank { "User" },
                size = 52.dp,
                showOnlineBadge = true,
                isOnline = conversation.isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.otherUserName.ifBlank { conversation.otherFreeChatId },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (conversation.lastMessageTime > 0) {
                        Text(
                            text = formatTimestamp(conversation.lastMessageTime),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (conversation.unreadCount > 0) PrimaryPink else SecondaryText,
                                fontSize = 12.sp,
                                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (conversation.lastMessageSenderId == currentUid) {
                            StatusTicks(
                                status = conversation.lastMessageStatus,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        if (conversation.lastMessageType == MessageType.IMAGE.name) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = PrimaryPink,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        Text(
                            text = if (conversation.lastMessageType == MessageType.IMAGE.name) "Photo" else conversation.lastMessage.ifBlank { "Started a chat" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (conversation.unreadCount > 0) PrimaryText else SecondaryText,
                                fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                                .background(PrimaryPink)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val msgCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }

    return when {
        nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR) &&
        nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 48 * 3600 * 1000L && nowCal.get(java.util.Calendar.DAY_OF_YEAR) - msgCal.get(java.util.Calendar.DAY_OF_YEAR) == 1 -> {
            "Yesterday"
        }
        else -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
