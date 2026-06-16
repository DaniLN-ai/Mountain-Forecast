package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = lightColorScheme(
    primary = BentoBlueIconBg,
    onPrimary = Color.White,
    primaryContainer = BentoBlueBg,
    onPrimaryContainer = BentoBlueText,
    secondary = GlacialSky,
    onSecondary = Color.White,
    secondaryContainer = BentoPurpleBg,
    onSecondaryContainer = BentoPurpleText,
    tertiary = SummitGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF5D4037),
    background = BentoBg,
    onBackground = BentoTextPrimary,
    surface = BentoCardWhite,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoBlueBg,
    onSurfaceVariant = BentoBlueText,
    outline = BentoBorder
)

// Use the same gorgeous Bento scheme for Light as well
private val LightColorScheme = lightColorScheme(
    primary = BentoBlueIconBg,
    onPrimary = Color.White,
    primaryContainer = BentoBlueBg,
    onPrimaryContainer = BentoBlueText,
    secondary = GlacialSky,
    onSecondary = Color.White,
    secondaryContainer = BentoPurpleBg,
    onSecondaryContainer = BentoPurpleText,
    tertiary = SummitGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF5D4037),
    background = BentoBg,
    onBackground = BentoTextPrimary,
    surface = BentoCardWhite,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoBlueBg,
    onSurfaceVariant = BentoBlueText,
    outline = BentoBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to false for the gorgeous light Bento Grid look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.statusBarColor = colorScheme.background.toArgb()
            window?.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
