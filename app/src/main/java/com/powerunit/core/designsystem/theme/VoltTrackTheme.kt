package com.powerunit.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Backward compatibility alias for WattWiseTheme.
 */
@Deprecated("Use WattWiseTheme instead", ReplaceWith("WattWiseTheme(darkTheme, dynamicColor, content)"))
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
