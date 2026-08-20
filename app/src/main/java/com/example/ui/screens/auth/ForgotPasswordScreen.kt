package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FreeChatTextField
import com.example.ui.components.PinkButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftBackground
import com.example.ui.theme.SuccessGreen

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .testTag("forgot_password_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LightPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Forgot Password",
                    tint = PrimaryPink,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter your registered email address to receive a password reset link.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SecondaryText,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
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
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearMessages()
                        },
                        label = "Email",
                        placeholder = "Enter your email",
                        leadingIcon = Icons.Default.Email,
                        testTag = "forgot_email_input"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PinkButton(
                        text = "Send Reset Link",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.sendPasswordReset(email)
                        },
                        isLoading = uiState.isLoading,
                        testTag = "send_reset_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.testTag("back_to_login_btn")
            ) {
                Text(
                    text = "Back to Login",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
