package com.imaniapp.uganda.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGreenLight,
    onPrimary = TextOnPrimary,
    primaryContainer = IslamicGreenDark,
    onPrimaryContainer = IslamicWhite,
    secondary = IslamicGold,
    onSecondary = TextPrimary,
    secondaryContainer = IslamicGoldDark,
    onSecondaryContainer = IslamicWhite,
    tertiary = IslamicBeige,
    onTertiary = TextPrimary,
    background = Color(0xFF121212),
    onBackground = IslamicWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = IslamicWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = IslamicCream,
    error = ErrorRed,
    onError = IslamicWhite
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = TextOnPrimary,
    primaryContainer = IslamicGreenLight,
    onPrimaryContainer = TextPrimary,
    secondary = IslamicGold,
    onSecondary = TextPrimary,
    secondaryContainer = IslamicGoldLight,
    onSecondaryContainer = TextPrimary,
    tertiary = IslamicBeige,
    onTertiary = TextPrimary,
    background = IslamicWhite,
    onBackground = TextPrimary,
    surface = IslamicWhite,
    onSurface = TextPrimary,
    surfaceVariant = IslamicCream,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = IslamicWhite
)

@Composable
fun ImaniAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain Islamic theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 