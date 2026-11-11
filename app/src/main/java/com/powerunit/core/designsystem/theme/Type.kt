package com.powerunit.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tabular numbers and display styles (§3)
val NumberHero = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 56.sp,
    lineHeight = 60.sp,
    letterSpacing = (-1.0).sp,
    fontFeatureSettings = "tnum"
)

val NumberLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.5).sp,
    fontFeatureSettings = "tnum"
)

val NumberMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.2).sp,
    fontFeatureSettings = "tnum"
)

// Legacy alias for compatibility
val MeterDisplayLarge = NumberHero

@Immutable
data class ExtendedTypography(
    val numberHero: TextStyle = NumberHero,
    val numberLarge: TextStyle = NumberLarge,
    val numberMedium: TextStyle = NumberMedium
)

val LocalExtendedTypography = staticCompositionLocalOf { ExtendedTypography() }

val WattWiseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        fontFeatureSettings = "tnum"
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

object AppTypography {
    val numberHero = NumberHero
    val numberLarge = NumberLarge
    val numberMedium = NumberMedium
    val displayMedium = WattWiseTypography.displayMedium
    val headlineMedium = WattWiseTypography.headlineMedium
    val titleLarge = WattWiseTypography.titleLarge
    val titleMedium = WattWiseTypography.titleMedium
    val bodyLarge = WattWiseTypography.bodyLarge
    val bodyMedium = WattWiseTypography.bodyMedium
    val labelLarge = WattWiseTypography.labelLarge
    val labelMedium = WattWiseTypography.labelMedium
    val labelSmall = WattWiseTypography.labelSmall
}
