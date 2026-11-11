package com.powerunit.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design system spacing, sizing, and gap tokens (§4).
 * Grid: 4dp base, 8dp rhythm.
 */
object Dimens {
    // Base rhythm
    val gridBase: Dp = 4.dp
    val gridRhythm: Dp = 8.dp

    // Spacing scale
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
    val huge: Dp = 48.dp

    // Spacing scale aliases
    val spacingXxs: Dp get() = xxs
    val spacingXs: Dp get() = xs
    val spacingSm: Dp get() = sm
    val spacingMd: Dp get() = md
    val spacingLg: Dp get() = lg
    val spacingXl: Dp get() = xl
    val spacingXxl: Dp get() = xxl
    val spacingXxxl: Dp get() = xxxl
    val spacingHuge: Dp get() = huge

    // Elevation shortcuts
    val level0: Dp get() = Elevation.level0
    val level1: Dp get() = Elevation.level1
    val level2: Dp get() = Elevation.level2
    val level3: Dp get() = Elevation.level3
    val level4: Dp get() = Elevation.level4
    val level5: Dp get() = Elevation.level5
    val elevationLevel0: Dp get() = Elevation.level0
    val elevationLevel1: Dp get() = Elevation.level1
    val elevationLevel2: Dp get() = Elevation.level2
    val elevationLevel3: Dp get() = Elevation.level3
    val elevationLevel4: Dp get() = Elevation.level4
    val elevationLevel5: Dp get() = Elevation.level5

    // Context tokens
    val screenHorizontalPadding: Dp = 20.dp
    val contentTopPadding: Dp = 8.dp
    val sectionGap: Dp = 28.dp
    val sectionHeaderToContentGap: Dp = 12.dp
    val cardGap: Dp = 12.dp
    val cardPaddingStandard: Dp = 16.dp
    val cardPaddingLarge: Dp = 20.dp
    val cardTitleToValueGap: Dp = 8.dp
    val cardValueToHintGap: Dp = 4.dp

    // Icons
    val iconContainerSize: Dp = 40.dp
    val iconContainerRadius: Dp = 12.dp
    val iconSizeSmall: Dp = 20.dp
    val iconSizeStandard: Dp = 24.dp
    val iconSizeMini: Dp = 14.dp
    val iconSizeLarge: Dp = 28.dp
    val iconToTextGap: Dp = 12.dp

    // Lists
    val listItemMinHeight: Dp = 64.dp
    val listItemVerticalPadding: Dp = 12.dp
    val dividerInset: Dp = 72.dp
    val dividerThickness: Dp = 1.dp

    // Chips
    val chipHeight: Dp = 32.dp
    val chipHorizontalPadding: Dp = 12.dp
    val chipGap: Dp = 8.dp

    // Buttons
    val buttonHeightPrimary: Dp = 52.dp
    val buttonHeightCompact: Dp = 44.dp
    val buttonHorizontalPadding: Dp = 24.dp

    // Inputs
    val textFieldHeight: Dp = 56.dp
    val textFieldRadius: Dp = 14.dp

    // Keypad (NumberPadField)
    val keypadKeyHeight: Dp = 64.dp
    val keypadKeyRadius: Dp = 20.dp

    // Touch Target
    val minTouchTarget: Dp = 48.dp

    // Navigation & Center Add Button
    val bottomNavBarHeight: Dp = 72.dp
    val centerAddButtonSize: Dp = 60.dp
    val centerAddRaised: Dp = 16.dp
    val notchWidth: Dp = 76.dp
    val notchDepth: Dp = 10.dp
    val centerAddTouchTarget: Dp = 64.dp
    val navIndicatorWidth: Dp = 56.dp
    val navIndicatorHeight: Dp = 32.dp
    val navRailWidth: Dp = 80.dp

    // Scroll padding
    val scrollBottomPadding: Dp = 104.dp

    // Adaptive
    val maxContentWidth: Dp = 640.dp

    // Rings & Donut Charts
    val usageRingDefaultSize: Dp = 120.dp
    val usageRingStrokeWidth: Dp = 12.dp
    val donutChartSize: Dp = 160.dp

    // Bottom sheets
    val bottomSheetDragHandleWidth: Dp = 36.dp
    val bottomSheetDragHandleHeight: Dp = 4.dp
}
