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
fun ChangePasswordScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .imePadding()
            .testTag("change_password_screen")
    ) {
        FreeChatTopBar(
            title = "Change Password",
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
                    val displayedError = localError ?: uiState.errorMessage
                    if (!displayedError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightPink)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = displayedError,
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
                            localError = null
                            viewModel.clearMessages()
                        },
                        label = "Current Password",
                        placeholder = "Enter your current password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        testTag = "current_password_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FreeChatTextField(
                        value = newPass,
                        onValueChange = {
                            newPass = it
                            localError = null
                            viewModel.clearMessages()
                        },
                        label = "New Password",
                        placeholder = "At least 6 characters",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        testTag = "new_password_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FreeChatTextField(
                        value = confirmPass,
                        onValueChange = {
                            confirmPass = it
                            localError = null
                            viewModel.clearMessages()
                        },
                        label = "Confirm New Password",
                        placeholder = "Repeat new password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        testTag = "confirm_password_input"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PinkButton(
                        text = "Update Password",
                        onClick = {
                            focusManager.clearFocus()
                            if (newPass != confirmPass) {
                                localError = "New passwords do not match"
                            } else {
                                viewModel.changePassword(currentPass, newPass)
                            }
                        },
                        isLoading = uiState.isLoading,
                        testTag = "save_password_button"
                    )
                }
            }
        }
    }
}
