package com.powerunit.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * VoltTrackTheme is the design system entry point specified in §11.
 * Provides light, dark, and optional dynamic color theming.
 */
@Composable
fun VoltTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WattWiseTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
