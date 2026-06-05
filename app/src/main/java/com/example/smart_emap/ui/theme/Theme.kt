package com.example.smart_emap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartEmapLightScheme = lightColorScheme(
    primary = LoginColors.Primary,
    onPrimary = Color.White,
    primaryContainer = LoginColors.Accent,
    secondary = LoginColors.PrimaryDark,
    background = LoginColors.RightPanelBg,
    surface = Color.White,
    onBackground = LoginColors.TitleDark,
    onSurface = LoginColors.TitleDark,
    onSurfaceVariant = LoginColors.TextMuted,
)

@Composable
fun SmartEMAPTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SmartEmapLightScheme,
        typography = Typography,
        content = content,
    )
}
