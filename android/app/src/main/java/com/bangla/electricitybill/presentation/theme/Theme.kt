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
    primary = ElectricBluePrimary,
    onPrimary = PureWhite,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = ElectricNavyDeep,
    secondary = ElectricNavyDeep,
    onSecondary = PureWhite,
    secondaryContainer = ElectricBlueHover,
    onSecondaryContainer = ElectricNavyMidnight,
    tertiary = EnergyEmerald,
    onTertiary = PureWhite,
    tertiaryContainer = EnergyEmeraldLight,
    onTertiaryContainer = EnergyEmeraldDark,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = ElectricBlueContainer,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate100
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = DarkBackground,
    primaryContainer = DarkLightBlue,
    onPrimaryContainer = DarkOnSurface,
    secondary = ElectricBlueVibrant,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = EnergyEmerald,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = EnergyEmeraldLight,
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            // In light theme, status bar icons must be dark so they are visible on light background!
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
