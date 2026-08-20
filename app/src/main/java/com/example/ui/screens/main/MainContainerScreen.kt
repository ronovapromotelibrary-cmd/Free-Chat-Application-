package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DividerColor
import com.example.ui.theme.LightPink
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryPinkDark
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainContainerScreen(
    viewModel: MainViewModel,
    onOpenChat: (otherUid: String, convId: String) -> Unit,
    onOpenUserProfile: (targetUid: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val conversations by viewModel.conversations.collectAsState()
    val totalUnread = conversations.sumOf { it.unreadCount }

    val navItems = listOf(
        NavItem("Chats", Icons.Filled.Chat, Icons.Outlined.ChatBubbleOutline, "nav_chats"),
        NavItem("Friends", Icons.Filled.People, Icons.Outlined.PeopleOutline, "nav_friends"),
        NavItem("Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline, "nav_profile")
    )

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.shadow(12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = PureWhite
            ) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            icon = {
                                if (index == 0 && totalUnread > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = PrimaryPink,
                                                contentColor = PureWhite
                                            ) {
                                                Text(
                                                    text = if (totalUnread > 99) "99+" else totalUnread.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title.uppercase(),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryPinkDark,
                                unselectedIconColor = SecondaryText,
                                selectedTextColor = PrimaryPink,
                                unselectedTextColor = SecondaryText,
                                indicatorColor = LightPink
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedIndex) {
                0 -> ChatsScreen(
                    viewModel = viewModel,
                    onOpenChat = onOpenChat,
                    onNavigateToSearch = { selectedIndex = 1 }
                )
                1 -> FriendsScreen(
                    viewModel = viewModel,
                    onOpenChat = onOpenChat,
                    onOpenUserProfile = onOpenUserProfile
                )
                2 -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                    onNavigateToPrivacySettings = onNavigateToPrivacySettings,
                    onLoggedOut = onLoggedOut
                )
            }
        }
    }
}
