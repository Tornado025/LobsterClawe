package com.lobsterclawe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = White,
    primaryContainer = TealLight,
    onPrimaryContainer = TealText,
    background = Gray50,
    surface = White,
    outline = Gray200,
    onBackground = Gray900,
    onSurface = Gray900,
    onSurfaceVariant = Gray500
)

@Composable
fun LobsterClawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
