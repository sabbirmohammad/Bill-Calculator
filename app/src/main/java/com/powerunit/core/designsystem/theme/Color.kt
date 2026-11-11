package com.powerunit.core.designsystem.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Color Tokens
val PrimaryLight = Color(0xFF4F46E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE4E3FF)
val OnPrimaryContainerLight = Color(0xFF1B1670)

val SecondaryLight = Color(0xFF0EA5A4)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD3F5F3)
val OnSecondaryContainerLight = Color(0xFF053F3E)

val TertiaryLight = Color(0xFFF59E0B)
val OnTertiaryLight = Color(0xFF1F1300)
val TertiaryContainerLight = Color(0xFFFEEFC7)
val OnTertiaryContainerLight = Color(0xFF3D2600)

val BackgroundLight = Color(0xFFF6F7FB)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFAFBFE)
val SurfaceContainerLight = Color(0xFFF1F3FA)
val SurfaceVariantLight = Color(0xFFECEEF6)
val OnSurfaceLight = Color(0xFF0F172A)
val OnSurfaceVariantLight = Color(0xFF5B6478)

val OutlineLight = Color(0xFFD5D9E6)
val OutlineVariantLight = Color(0xFFE7EAF3)

val SuccessLight = Color(0xFF16A34A)
val WarningLight = Color(0xFFF59E0B)
val ErrorLight = Color(0xFFDC2626)
val InfoLight = Color(0xFF0284C7)
val ScrimLight = Color(0x660F172A) // 40%

// Dark Color Tokens (True Dark)
val PrimaryDark = Color(0xFF8B93FF)
val OnPrimaryDark = Color(0xFF14123D)
val PrimaryContainerDark = Color(0xFF2C2A8F)
val OnPrimaryContainerDark = Color(0xFFE4E3FF)

val SecondaryDark = Color(0xFF2DD4BF)
val OnSecondaryDark = Color(0xFF00201D)
val SecondaryContainerDark = Color(0xFF0F4F4B)
val OnSecondaryContainerDark = Color(0xFFD3F5F3)

val TertiaryDark = Color(0xFFFBBF24)
val OnTertiaryDark = Color(0xFF2B1A00)
val TertiaryContainerDark = Color(0xFF5C3C00)
val OnTertiaryContainerDark = Color(0xFFFEEFC7)

val BackgroundDark = Color(0xFF0B1020)
val SurfaceDark = Color(0xFF131A2E)
val SurfaceContainerLowDark = Color(0xFF0F1527)
val SurfaceContainerDark = Color(0xFF18213A)
val SurfaceVariantDark = Color(0xFF1B2440)
val OnSurfaceDark = Color(0xFFE6E9F5)
val OnSurfaceVariantDark = Color(0xFFA3ABC2)

val OutlineDark = Color(0xFF2A3556)
val OutlineVariantDark = Color(0xFF202A47)

val SuccessDark = Color(0xFF4ADE80)
val WarningDark = Color(0xFFFBBF24)
val ErrorDark = Color(0xFFF87171)
val InfoDark = Color(0xFF38BDF8)
val ScrimDark = Color(0x660B1020)

// Gradients
val HeroGradientLight = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4F46E5),
        Color(0xFF7C3AED),
        Color(0xFF06B6D4)
    ),
    start = Offset.Zero,
    end = Offset.Infinite
)

val HeroGradientDark = Brush.linearGradient(
    colors = listOf(
        Color(0xFF3730A3),
        Color(0xFF5B21B6),
        Color(0xFF0E7490)
    ),
    start = Offset.Zero,
    end = Offset.Infinite
)

val SoftAccentLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE4E3FF),
        Color(0xFFFFFFFF)
    )
)

val SoftAccentDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF2C2A8F),
        Color(0xFF131A2E)
    )
)

val SuccessMomentGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF16A34A),
        Color(0xFF0EA5A4)
    )
)

val CenterAddButtonGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4F46E5),
        Color(0xFF7C3AED)
    )
)

// Chart Palette (Colorblind-safe)
val ChartColor1 = Color(0xFF4F46E5)
val ChartColor2 = Color(0xFF0EA5A4)
val ChartColor3 = Color(0xFFF59E0B)
val ChartColor4 = Color(0xFFEC4899)
val ChartColor5 = Color(0xFF64748B)

val ChartColors = listOf(ChartColor1, ChartColor2, ChartColor3, ChartColor4, ChartColor5)

// Usage meter threshold colors
val UsageLow = Color(0xFF0EA5A4)      // 0-70% teal
val UsageMedium = Color(0xFFF59E0B)   // 70-90% amber
val UsageHigh = Color(0xFFDC2626)     // 90%+ red
