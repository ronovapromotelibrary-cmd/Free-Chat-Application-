package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MessageStatus
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightPink
import com.example.ui.theme.OnlineBadgeColor
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.PrimaryPinkDark
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SeenTickColor
import com.example.ui.theme.SoftBackground

@Composable
fun PinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "pink_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(testTag),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPink,
            contentColor = PureWhite,
            disabledContainerColor = LightPink.copy(alpha = 0.5f),
            disabledContentColor = PrimaryPinkDark.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PureWhite,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun PinkOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "pink_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryPink
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PrimaryPink,
                letterSpacing = 0.3.sp
            )
        )
    }
}

@Composable
fun FreeChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = "text_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = PrimaryText,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText.copy(alpha = 0.7f))
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) ErrorRed else PrimaryPink,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPink,
                unfocusedBorderColor = DividerColor,
                errorBorderColor = ErrorRed,
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite,
                errorContainerColor = PureWhite,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText
            )
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed),
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = false,
    isOnline: Boolean = false
) {
    Box(modifier = modifier.size(size)) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(LightPink),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = name.trim().take(1).uppercase().ifEmpty { "?" }
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(LightPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.42f).sp
                    )
                )
            }
        }

        if (showOnlineBadge && isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(PureWhite)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(OnlineBadgeColor)
            )
        }
    }
}

@Composable
fun StatusTicks(
    status: String,
    modifier: Modifier = Modifier,
    tintColor: Color? = null
) {
    when (status) {
        MessageStatus.PENDING.name -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Pending",
                modifier = modifier.size(13.dp),
                tint = tintColor ?: SecondaryText.copy(alpha = 0.7f)
            )
        }
        MessageStatus.SENT.name -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                modifier = modifier.size(13.dp),
                tint = tintColor ?: SecondaryText
            )
        }
        MessageStatus.DELIVERED.name -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                modifier = modifier.size(14.dp),
                tint = tintColor ?: SecondaryText
            )
        }
        MessageStatus.SEEN.name -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Seen",
                modifier = modifier.size(14.dp),
                tint = tintColor ?: SeenTickColor
            )
        }
    }
}

@Composable
fun DateHeaderChip(dateText: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SoftBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
        ) {
            Text(
                text = dateText.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PrimaryPink,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(LightPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPink,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                fontSize = 20.sp,
                letterSpacing = (-0.2).sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(22.dp))
            PinkButton(
                text = actionButtonText,
                onClick = onActionClick,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeChatTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    subtitle: String? = null,
    testTag: String = "top_bar"
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PureWhite,
            titleContentColor = PrimaryText,
            actionIconContentColor = PrimaryText
        ),
        modifier = Modifier.testTag(testTag)
    )
}
