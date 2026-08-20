package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FreeChatColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = PureWhite,
    primaryContainer = LightPink,
    onPrimaryContainer = DeepPurple,
    secondary = PrimaryPinkDark,
    onSecondary = PureWhite,
    secondaryContainer = SoftPink,
    onSecondaryContainer = PrimaryText,
    tertiary = SuccessGreen,
    onTertiary = PureWhite,
    background = SoftBackground,
    onBackground = PrimaryText,
    surface = PureWhite,
    onSurface = PrimaryText,
    surfaceVariant = SoftPink,
    onSurfaceVariant = SecondaryText,
    outline = DividerColor,
    outlineVariant = Color(0xFFE6E1E5),
    error = ErrorRed,
    onError = PureWhite
)

@Composable
fun FreeChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FreeChatColorScheme,
        typography = Typography,
        content = content
    )
}

