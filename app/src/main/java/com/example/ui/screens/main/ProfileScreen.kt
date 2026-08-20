package com.example.ui.screens.main

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FreeChatTextField
import com.example.ui.components.PinkButton
import com.example.ui.components.PinkOutlinedButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCopiedToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentUser == null || currentUser?.userId.isNullOrBlank()) {
            viewModel.loadUserData()
        }
    }

    LaunchedEffect(showCopiedToast) {
        if (showCopiedToast) {
            delay(2500)
            showCopiedToast = false
        }
    }

    val freeChatId = currentUser?.userId?.takeIf { it.isNotBlank() && !it.contains("-") }

    val copyIdToClipboard: () -> Unit = {
        val idToCopy = freeChatId ?: currentUser?.userId
        if (!idToCopy.isNullOrBlank() && !idToCopy.contains("---")) {
            clipboardManager.setText(AnnotatedString(idToCopy))
            try {
                val sysClipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Free Chat ID", idToCopy)
                sysClipboard?.setPrimaryClip(clip)
            } catch (_: Exception) {}
            showCopiedToast = true
            Toast.makeText(context, "Copied ID: $idToCopy", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Loading Free Chat ID...", Toast.LENGTH_SHORT).show()
            viewModel.loadUserData()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("profile_screen")
    ) {
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
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryText,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                )
            )

            IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("profile_settings_icon")) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PrimaryText
                )
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        UserAvatar(
                            photoUrl = currentUser?.photoUrl,
                            name = currentUser?.name ?: "User",
                            size = 86.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(PrimaryPink)
                                .clickable {
                                    editName = currentUser?.name ?: ""
                                    showEditDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit photo",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentUser?.name.orEmpty().ifBlank { "User" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentUser?.email.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Permanent Free Chat ID Card Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { copyIdToClipboard() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightPink.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PrimaryPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Permanent Free Chat ID",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = PrimaryPink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = freeChatId ?: (currentUser?.userId?.takeIf { it.isNotBlank() } ?: "Tap to Load ID"),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryText,
                                    fontSize = 24.sp,
                                    letterSpacing = 2.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (showCopiedToast) PrimaryPink.copy(alpha = 0.85f) else PrimaryPink,
                                    modifier = Modifier
                                        .clickable { copyIdToClipboard() }
                                        .testTag("copy_free_chat_id_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showCopiedToast) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy ID",
                                            tint = PureWhite,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (showCopiedToast) "Copied!" else "Copy ID",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PureWhite,
                                    modifier = Modifier
                                        .clickable {
                                            val idToShare = freeChatId ?: currentUser?.userId.orEmpty()
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Add me on Free Chat! My permanent Free Chat ID is: $idToShare"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Free Chat ID"))
                                        }
                                        .testTag("share_id_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = PrimaryPink,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Share",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = PrimaryPink,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PinkOutlinedButton(
                        text = "Edit Profile",
                        onClick = {
                            editName = currentUser?.name ?: ""
                            selectedImageUri = null
                            showEditDialog = true
                        },
                        testTag = "edit_profile_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Menu Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Message alerts & tones",
                        onClick = onNavigateToNotificationSettings
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy",
                        subtitle = "Online status, last seen, read receipts",
                        onClick = onNavigateToPrivacySettings
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        title = "Account Settings",
                        subtitle = "Password, security & deletion",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out",
                        subtitle = "Sign out from this device",
                        iconColor = ErrorRed,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of Free Chat on this device?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLoggedOut()
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Edit Profile Modal Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdatingProfile) showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(LightPink)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            UserAvatar(
                                photoUrl = selectedImageUri.toString(),
                                name = editName,
                                size = 76.dp
                            )
                        } else {
                            UserAvatar(
                                photoUrl = currentUser?.photoUrl,
                                name = currentUser?.name ?: "User",
                                size = 76.dp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PureWhite.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = PrimaryPink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FreeChatTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Display Name",
                        placeholder = "Enter your name",
                        testTag = "edit_name_input"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your Free Chat ID (${currentUser?.userId}) is permanent and cannot be modified.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                PinkButton(
                    text = "Save",
                    onClick = {
                        viewModel.updateProfile(editName, selectedImageUri) {
                            showEditDialog = false
                        }
                    },
                    isLoading = isUpdatingProfile,
                    modifier = Modifier.width(120.dp),
                    testTag = "save_profile_button"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isUpdatingProfile
                ) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: androidx.compose.ui.graphics.Color = PrimaryPink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    fontSize = 15.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = SecondaryText.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}
