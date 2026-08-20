package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FreeChatTopBar
import com.example.ui.theme.DividerColor
import com.example.ui.theme.LightPink
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("about_screen")
    ) {
        FreeChatTopBar(title = "About Free Chat", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LightPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Logo",
                    tint = PrimaryPink,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Free Chat",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )

            Text(
                text = "Version 1.0.0 (Official Release)",
                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Free Chat is an offline-first, real-time 1-to-1 private messaging application built for seamless communication.\n\nEvery user receives a unique, permanent Free Chat ID upon registration that never expires and protects personal phone numbers from being exposed.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryText,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("privacy_policy_screen")
    ) {
        FreeChatTopBar(title = "Privacy Policy", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PolicySection("1. Information We Collect", "Free Chat only collects your display name, registered email, and avatar for identification. Your email is kept private and never shared with other users.")
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicySection("2. Permanent Free Chat IDs", "Your Free Chat ID is a pseudonymized identifier created to allow friends to connect with you without sharing phone numbers or personal emails.")
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicySection("3. Direct 1-to-1 Messaging", "Messages are delivered in real-time. Delivery and read receipt preferences can be toggled at any time in Privacy Settings.")
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicySection("4. Data Deletion", "You can permanently delete your account and all associated messages at any time directly through Account Settings.")
                }
            }
        }
    }
}

@Composable
fun HelpSupportScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("help_support_screen")
    ) {
        FreeChatTopBar(title = "Help & Support", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PolicySection("How do I find friends?", "Go to the Friends tab, enter your friend's permanent Free Chat ID (e.g. FC8K4M2P7X), and tap Search. You can message them immediately.")
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicySection("Can I change my Free Chat ID?", "No, Free Chat IDs are permanent, unique identifiers assigned upon account registration to ensure consistent 1-to-1 routing.")
                    Spacer(modifier = Modifier.height(16.dp))
                    PolicySection("How does offline messaging work?", "When you send a message while offline, it is stored in your local database with a pending clock icon (◷) and automatically syncs to Firebase once connectivity is restored.")
                }
            }
        }
    }
}

@Composable
fun PolicySection(title: String, body: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = PrimaryText,
                lineHeight = 20.sp
            )
        )
    }
}
