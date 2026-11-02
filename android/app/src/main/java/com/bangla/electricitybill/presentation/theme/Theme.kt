package com.bangla.electricitybill.presentation.theme

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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = AppSurface,
    primaryContainer = LightPurple,
    onPrimaryContainer = DeepPurple,
    secondary = DeepPurple,
    onSecondary = AppSurface,
    secondaryContainer = SoftPurpleHover,
    onSecondaryContainer = PrimaryPurple,
    tertiary = SuccessGreen,
    onTertiary = AppSurface,
    tertiaryContainer = SuccessGreenLight,
    onTertiaryContainer = SuccessGreenDark,
    error = ErrorRed,
    onError = AppSurface,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,
    background = AppBackground,
    onBackground = PrimaryText,
    surface = AppSurface,
    onSurface = PrimaryText,
    surfaceVariant = LightPurple,
    onSurfaceVariant = SecondaryText,
    outline = CardOutline,
    outlineVariant = AppDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryPurple,
    onPrimary = DarkBackground,
    primaryContainer = DarkLightPurple,
    onPrimaryContainer = DarkOnSurface,
    secondary = PrimaryPurple,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = SuccessGreen,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkLightPurple,
    onTertiaryContainer = SuccessGreenLight,
    error = ErrorRed,
    onError = DarkBackground,
    errorContainer = DarkSurfaceVariant,
    onErrorContainer = ErrorRedLight,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun ElectricityBillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = if (darkTheme) colorScheme.background.toArgb() else DeepPurple.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false // White text over purple/dark header
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
