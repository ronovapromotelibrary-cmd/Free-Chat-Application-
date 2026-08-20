package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.components.FreeChatTextField
import com.example.ui.components.FreeChatTopBar
import com.example.ui.components.PinkButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBackground
import com.example.ui.theme.SuccessGreen

@Composable
fun ChangeEmailScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var currentPass by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .imePadding()
            .testTag("change_email_screen")
    ) {
        FreeChatTopBar(
            title = "Change Email",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightPink)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (!uiState.successMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.successMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    FreeChatTextField(
                        value = currentPass,
                        onValueChange = {
                            currentPass = it
                            viewModel.clearMessages()
                        },
                        label = "Current Password",
                        placeholder = "Enter your current password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        testTag = "change_email_password_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FreeChatTextField(
                        value = newEmail,
                        onValueChange = {
                            newEmail = it
                            viewModel.clearMessages()
                        },
                        label = "New Email",
                        placeholder = "Enter new email address",
                        leadingIcon = Icons.Default.Email,
                        testTag = "change_email_new_input"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PinkButton(
                        text = "Update Email",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.changeEmail(currentPass, newEmail)
                        },
                        isLoading = uiState.isLoading,
                        testTag = "save_email_button"
                    )
                }
            }
        }
    }
}
