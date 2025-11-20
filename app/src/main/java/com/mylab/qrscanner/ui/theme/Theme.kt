package com.mylab.qrscanner.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrangeMain,
    onPrimary = Black,
    primaryContainer = OrangeDark,
    onPrimaryContainer = White,
    secondary = OrangeBright,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = BlackSurface,
    onSurface = White,
    surfaceVariant = BlackCard,
    onSurfaceVariant = GrayLight,
    error = Red,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeMain,
    onPrimary = White,
    primaryContainer = OrangeBright,
    onPrimaryContainer = Black,
    secondary = OrangeDark,
    onSecondary = White,
    background = WhiteBackground,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = GrayLight,
    onSurfaceVariant = BlackSurface,
    error = Red,
    onError = White
)

@Composable
fun MyLabQRScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}










