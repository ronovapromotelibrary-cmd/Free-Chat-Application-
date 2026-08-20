package com.example.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.components.DateHeaderChip
import com.example.ui.components.PinkButton
import com.example.ui.components.StatusTicks
import com.example.ui.components.UserAvatar
import com.example.ui.screens.main.formatTimestamp
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.OnlineBadgeColor
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onOpenUserProfile: (targetUid: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var selectedMessageForActions by remember { mutableStateOf<Message?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectImageForPreview(uri)
        }
    }

    // Scroll to bottom when messages update
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Chat Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { uiState.otherUser?.let { onOpenUserProfile(it.uid) } }
                        .padding(vertical = 4.dp)
                ) {
                    UserAvatar(
                        photoUrl = uiState.otherUser?.photoUrl,
                        name = uiState.otherUser?.name.orEmpty().ifBlank { "User" },
                        size = 40.dp,
                        showOnlineBadge = true,
                        isOnline = uiState.otherPresence.online
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = uiState.otherUser?.name.orEmpty().ifBlank { uiState.otherUser?.userId ?: "Chat" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val statusSubtitle = when {
                            uiState.isOtherTyping -> "typing..."
                            uiState.otherPresence.online -> "● Online"
                            uiState.otherPresence.lastSeen > 0 -> "Last seen ${formatLastSeen(uiState.otherPresence.lastSeen)}"
                            else -> "Offline"
                        }

                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.isOtherTyping) PrimaryPink else if (uiState.otherPresence.online) OnlineBadgeColor else SecondaryText,
                                fontWeight = if (uiState.isOtherTyping || uiState.otherPresence.online) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }
            },
            actions = {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("chat_menu_button")) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = PrimaryText
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(PureWhite)
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile") },
                        onClick = {
                            showMenu = false
                            uiState.otherUser?.let { onOpenUserProfile(it.uid) }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryPink)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (uiState.isBlocked) "Unblock User" else "Block User") },
                        onClick = {
                            showMenu = false
                            viewModel.toggleBlock()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Block, contentDescription = null, tint = ErrorRed)
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureWhite,
                titleContentColor = PrimaryText
            )
        )

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // Upload progress bar
        AnimatedVisibility(visible = uiState.isUploadingImage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightPink)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryPink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uploading image ${uiState.uploadProgress}%...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryPink,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LightPink.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Say Hello to ${uiState.otherUser?.name.orEmpty().ifBlank { "User" }}! 👋",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryPink,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    var lastDateHeader = ""

                    items(uiState.messages, key = { it.id }) { message ->
                        val messageDate = formatDateHeader(message.timestamp)
                        if (messageDate != lastDateHeader) {
                            lastDateHeader = messageDate
                            DateHeaderChip(dateText = messageDate)
                        }

                        val isOutgoing = message.senderId == uiState.currentUser?.uid
                        MessageBubble(
                            message = message,
                            isOutgoing = isOutgoing,
                            onLongClick = { selectedMessageForActions = message }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (uiState.isOtherTyping) {
                        item {
                            TypingBubble(senderName = uiState.otherUser?.name.orEmpty().ifBlank { "User" })
                        }
                    }
                }
            }
        }

        // Composer or Blocked State
        if (uiState.isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have blocked this user. Unblock to send messages.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Composer Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LightPink)
                            .testTag("attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Photo",
                            tint = PrimaryPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            viewModel.onUserTyping(it)
                        },
                        placeholder = {
                            Text(
                                text = "Write a message...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText.copy(alpha = 0.7f))
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = DividerColor,
                            focusedContainerColor = SoftBackground,
                            unfocusedContainerColor = SoftBackground,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText
                        ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendTextMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendTextMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) PrimaryPink else LightPink)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) PureWhite else PrimaryPink.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Image Preview Dialog before Sending
    if (uiState.selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { viewModel.selectImageForPreview(null) },
            title = {
                Text(
                    text = "Send Image",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = uiState.selectedImageUri,
                        contentDescription = "Selected image preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            confirmButton = {
                PinkButton(
                    text = "Send",
                    onClick = {
                        uiState.selectedImageUri?.let { viewModel.sendImageMessage(it) }
                    },
                    modifier = Modifier.width(100.dp),
                    testTag = "confirm_send_image_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectImageForPreview(null) }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Message Actions Menu Bottom Sheet / Dialog
    if (selectedMessageForActions != null) {
        val selectedMsg = selectedMessageForActions!!
        AlertDialog(
            onDismissRequest = { selectedMessageForActions = null },
            title = {
                Text(
                    text = "Message Options",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
            },
            text = {
                Column {
                    if (selectedMsg.type == MessageType.TEXT.name && selectedMsg.text.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Free Chat Message", selectedMsg.text))
                                    selectedMessageForActions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PrimaryPink)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy Text", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val msg = selectedMsg
                                selectedMessageForActions = null
                                viewModel.showMessageInfo(msg)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryPink)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Message Info", style = MaterialTheme.typography.bodyLarge)
                    }

                    if (selectedMsg.senderId == uiState.currentUser?.uid) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDeleteConfirmDialog = true
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Message", style = MaterialTheme.typography.bodyLarge.copy(color = ErrorRed))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMessageForActions = null }) {
                    Text("Close", color = SecondaryText)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Confirmation
    if (showDeleteConfirmDialog && selectedMessageForActions != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Message?") },
            text = { Text("Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMessageForActions?.id?.let { viewModel.deleteMessage(it) }
                        showDeleteConfirmDialog = false
                        selectedMessageForActions = null
                    }
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Message Info Dialog (Section 55 Requirements)
    if (uiState.selectedMessageForInfo != null) {
        val infoMsg = uiState.selectedMessageForInfo!!
        AlertDialog(
            onDismissRequest = { viewModel.showMessageInfo(null) },
            title = {
                Text(
                    text = "Message Info",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (infoMsg.type == MessageType.TEXT.name) {
                        Text(
                            text = "\"${infoMsg.text}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = PrimaryText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftBackground)
                                .padding(12.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    InfoTimestampRow(
                        label = "Sent",
                        timestamp = if (infoMsg.sentAt > 0) infoMsg.sentAt else infoMsg.timestamp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoTimestampRow(
                        label = "Delivered",
                        timestamp = infoMsg.deliveredAt
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoTimestampRow(
                        label = "Seen",
                        timestamp = infoMsg.readAt
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showMessageInfo(null) }) {
                    Text("Close", color = PrimaryPink, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun InfoTimestampRow(label: String, timestamp: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        )
        Text(
            text = if (timestamp > 0) {
                SimpleDateFormat("MMM d, yyyy  h:mm:ss a", Locale.getDefault()).format(Date(timestamp))
            } else {
                "—"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = PrimaryText,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        val bubbleShape = if (isOutgoing) {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
        }

        Surface(
            shape = bubbleShape,
            color = if (isOutgoing) PrimaryPink else PureWhite,
            border = if (isOutgoing) null else androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                if (message.type == MessageType.IMAGE.name && message.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Chat image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isOutgoing) PureWhite else PrimaryText,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            fontWeight = if (isOutgoing) FontWeight.Medium else FontWeight.Normal
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isOutgoing) LightPink.copy(alpha = 0.85f) else SecondaryText.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        StatusTicks(
                            status = message.status,
                            tintColor = PureWhite.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubble(senderName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = PureWhite,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$senderName is typing...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PrimaryPink,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

fun formatDateHeader(timestamp: Long): String {
    if (timestamp <= 0) return "Today"
    val now = System.currentTimeMillis()
    val msgCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }

    return when {
        nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR) &&
        nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> "Today"
        nowCal.get(java.util.Calendar.DAY_OF_YEAR) - msgCal.get(java.util.Calendar.DAY_OF_YEAR) == 1 &&
        nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatLastSeen(lastSeen: Long): String {
    if (lastSeen <= 0) return "recently"
    val diffMinutes = (System.currentTimeMillis() - lastSeen) / (60 * 1000)
    return when {
        diffMinutes < 1 -> "just now"
        diffMinutes < 60 -> "$diffMinutes minutes ago"
        diffMinutes < 24 * 60 -> "${diffMinutes / 60} hours ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(lastSeen))
    }
}
