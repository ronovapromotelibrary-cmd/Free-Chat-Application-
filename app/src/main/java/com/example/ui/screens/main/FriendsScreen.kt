package com.example.ui.screens.main

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FreeChatTextField
import com.example.ui.components.PinkButton
import com.example.ui.components.PinkOutlinedButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.OnlineBadgeColor
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground

@Composable
fun FriendsScreen(
    viewModel: MainViewModel,
    onOpenChat: (otherUid: String, convId: String) -> Unit,
    onOpenUserProfile: (targetUid: String) -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("friends_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Friends & Discovery",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryText,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search Box Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Search by Free Chat ID",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Enter a permanent Free Chat ID (e.g. FC8K4M2P7X)",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FreeChatTextField(
                            value = searchState.query,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = "Enter Free Chat ID...",
                            leadingIcon = Icons.Default.Search,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    viewModel.searchUserById()
                                }
                            ),
                            testTag = "search_user_input"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PinkButton(
                            text = if (searchState.isSearching) "Searching..." else "Search ID",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.searchUserById()
                            },
                            isLoading = searchState.isSearching,
                            testTag = "search_user_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Results Section
            if (searchState.searchResult != null) {
                val foundUser = searchState.searchResult!!
                val isBlocked = blockedUserIds.contains(foundUser.uid)

                item {
                    Text(
                        text = "Search Result",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            UserAvatar(
                                photoUrl = foundUser.photoUrl,
                                name = foundUser.name,
                                size = 68.dp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = foundUser.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PrimaryText
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LightPink
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = foundUser.userId,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPink
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy ID",
                                        tint = PrimaryPink,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(foundUser.userId))
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                PinkButton(
                                    text = "Message",
                                    onClick = {
                                        viewModel.addFriend(foundUser)
                                        onOpenChat(foundUser.uid, "")
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "message_found_user_button"
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                PinkOutlinedButton(
                                    text = if (isBlocked) "Unblock" else "Block",
                                    onClick = { viewModel.toggleBlockUser(foundUser.uid) },
                                    modifier = Modifier.width(100.dp),
                                    testTag = "block_found_user_button"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (searchState.isUserNotFound) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "User Not Found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This Free Chat ID is not registered with Free Chat.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (searchState.isSelfSearch) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightPink)
                    ) {
                        Text(
                            text = "That's your own Free Chat ID! You can share it with others.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryPink,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Saved / Recent Friends Section
            item {
                Text(
                    text = "Friends (${friends.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }

            if (friends.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Friends Added Yet",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Search a Free Chat ID above to connect with real friends.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(friends, key = { it.uid }) { friend ->
                    FriendRowItem(
                        friend = friend,
                        onMessageClick = { onOpenChat(friend.uid, "") },
                        onProfileClick = { onOpenUserProfile(friend.uid) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FriendRowItem(
    friend: User,
    onMessageClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick)
            .testTag("friend_row_${friend.uid}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = friend.photoUrl,
                name = friend.name,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Text(
                    text = "ID: ${friend.userId}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PrimaryPink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            IconButton(
                onClick = onMessageClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LightPink)
                    .size(36.dp)
                    .testTag("chat_friend_${friend.uid}")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Message",
                    tint = PrimaryPink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
