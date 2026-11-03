package com.bangla.electricitybill.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Electric Power & Smart Utility Palette (Official Brand Colors)
// Modern Electric Blue & Midnight Navy with Volt Amber & Energy Emerald
// ============================================================

// Primary Electric Sapphire & Navy Brand Palette
val ElectricBluePrimary = Color(0xFF1D4ED8) // Deep Vibrant Electric Blue (600)
val ElectricBlueVibrant = Color(0xFF2563EB) // Bright Electric Blue (500)
val ElectricNavyDeep = Color(0xFF1E3A8A)    // Deep Power Navy (800)
val ElectricNavyMidnight = Color(0xFF0F172A)// Midnight Slate Navy (900)
val ElectricBlueContainer = Color(0xFFEFF6FF)// Soft Ice Blue Container (50)
val ElectricBlueHover = Color(0xFFDBEAFE)    // Interactive Hover Blue (100)
val ElectricBlueBorder = Color(0xFFBFDBFE)   // Delicate Blue Outline (200)

// Hero Gradient for Executive Utility Header (Midnight Navy to Electric Sapphire)
val PowerHeroGradientStart = Color(0xFF0F172A)
val PowerHeroGradientEnd = Color(0xFF1D4ED8)

// Accent Volt - Lightning Bolt, High Peak Load, Energy Alerts
val VoltAmber = Color(0xFFF59E0B)
val VoltAmberLight = Color(0xFFFEF3C7)
val VoltAmberDark = Color(0xFFD97706)
val VoltYellow = Color(0xFFFACC15)

// Semantic Energy Savings & Currency BDT (৳) - Clean High-Legibility Emerald
val EnergyEmerald = Color(0xFF059669)
val EnergyEmeraldLight = Color(0xFFECFDF5)
val EnergyEmeraldDark = Color(0xFF047857)
val SuccessGreen = EnergyEmerald
val SuccessGreenLight = EnergyEmeraldLight
val SuccessGreenDark = EnergyEmeraldDark

// Semantic Feedback - Alerts & Errors
val ErrorRed = Color(0xFFDC2626)
val ErrorRedLight = Color(0xFFFEE2E2)
val ErrorDark = ErrorRed
val ErrorCrimson = ErrorRed
val WarningAmber = VoltAmber
val WarningAmberLight = VoltAmberLight
val InfoBlue = Color(0xFF0284C7)
val InfoBlueLight = Color(0xFFE0F2FE)

// Mechanical Meter Physical Hardware Tokens (Realistic Industrial Bezel)
val MeterBezel = Color(0xFF0F172A)
val MeterFrameBorder = Color(0xFF334155)
val MeterBlackWheelBg = Color(0xFF020617)
val MeterBlackWheelText = Color(0xFFFFFFFF)
val MeterRedWheelBg = Color(0xFFDC2626)
val MeterRedWheelText = Color(0xFFFFFFFF)
val MeterKwhLabel = Color(0xFF94A3B8)

// Light Theme Neutrals - Crisp Slate Surface
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate900 = Color(0xFF0F172A)
val PureWhite = Color(0xFFFFFFFF)

// Dark Theme Neutrals - Deep Obsidian Midnight Grid
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)
val DarkOutlineVariant = Color(0xFF1E293B)
val DarkPrimaryBlue = Color(0xFF3B82F6)
val DarkLightBlue = Color(0xFF1E293B)

// ============================================================
// Dynamic Adaptive Theme Accessors (Light / Dark Mode Safe)
// Prevents invisible black text in dark mode and ensures consistent styling
// ============================================================

val PrimaryText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

val SecondaryText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val AppBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val AppSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val CardOutline: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val AppDivider: Color
    @Composable
    get() = MaterialTheme.colorScheme.outlineVariant

// Backwards-compatibility aliases with dynamic theme routing
val PrimaryPurple: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val DeepPurple: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkPrimaryBlue else ElectricNavyDeep

val LightPurple: Color
    @Composable
    get() = MaterialTheme.colorScheme.primaryContainer

val SoftPurpleHover: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondaryContainer

val PurpleHeroGradientStart: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF090D16) else PowerHeroGradientStart

val PurpleHeroGradientEnd: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF1E3A8A) else PowerHeroGradientEnd

val DarkPrimaryPurple = DarkPrimaryBlue
val DarkLightPurple = DarkLightBlue
val AccentCyan: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val AccentElectricBlue: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkPrimaryBlue else ElectricBluePrimary

val EnergyEmeraldGreen = EnergyEmerald
