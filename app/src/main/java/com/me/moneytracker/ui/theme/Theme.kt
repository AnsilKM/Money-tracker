package com.me.moneytracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

import androidx.core.view.WindowCompat

private val LedgerColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = PaperBackground,
    secondary = BrassDivider,
    onSecondary = InkPrimary,
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = CardSurface,
    onSurface = InkPrimary,
    error = LedgerRed,
    onError = CardSurface,
    surfaceVariant = CardSurface,
    onSurfaceVariant = InkPrimary,
    outline = BrassDivider
)

@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced to false to maintain the strict moneytracker bahi-khata aesthetic
    content: @Composable () -> Unit
) {
    // Always use our moneytracker color scheme as we don't support standard dark mode or dynamic system colors
    val colorScheme = LedgerColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}