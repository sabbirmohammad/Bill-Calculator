package com.powerunit.core.designsystem.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.component.ChartCard
import com.powerunit.core.designsystem.component.EmptyState
import com.powerunit.core.designsystem.component.GradientHeroCard
import com.powerunit.core.designsystem.component.HeroCardSkeleton
import com.powerunit.core.designsystem.component.InsightCard
import com.powerunit.core.designsystem.component.NumberPadField
import com.powerunit.core.designsystem.component.ReadingListItem
import com.powerunit.core.designsystem.component.SectionHeader
import com.powerunit.core.designsystem.component.SegmentedTabs
import com.powerunit.core.designsystem.component.StatCard
import com.powerunit.core.designsystem.component.StatCardSkeleton
import com.powerunit.core.designsystem.component.TariffSlabRow
import com.powerunit.core.designsystem.component.TrendChip
import com.powerunit.core.designsystem.component.UsageRing
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.ChartColors
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.update.AppUpdateInstaller
import com.powerunit.core.update.model.UpdateInfo
import com.powerunit.core.update.model.UpdateState
import com.powerunit.core.update.ui.AppUpdateBottomSheet
import com.powerunit.core.designsystem.theme.NumberHero
import com.powerunit.core.designsystem.theme.NumberLarge
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.UsageHigh
import com.powerunit.core.designsystem.theme.UsageLow
import com.powerunit.core.designsystem.theme.UsageMedium
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * Design Gallery Screen (§11.3):
 * Debug screen showcasing all design system tokens, type scale, spacing, and components.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesignGalleryScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WattWise Design System Gallery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap)
        ) {
            // In-App Update Animated UI Demonstration
            val context = LocalContext.current
            var simulatedUpdateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(Dimens.cardPaddingStandard)) {
                    Text(
                        text = "Interactive In-App Update Simulation",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimens.xxs))
                    Text(
                        text = "Test download percentage animations, radial progress, and install transitions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))

                    val sampleInfo = UpdateInfo(
                        currentVersion = "1.0.0",
                        newVersion = "1.0.1",
                        releaseName = "v1.0.1 - GitHub Auto Update & Smooth Animations",
                        releaseNotes = "• Added real-time in-app updater connected to GitHub Releases.\n• High-framerate radial download ring with speed indicator.\n• Automatic APK package installer integration.\n• Enhanced Bengali numerals and design system polish.",
                        downloadUrl = "https://example.com/WattWise.apk",
                        apkSize = 25165824L,
                        publishedAt = "2026-11-11"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                    ) {
                        OutlinedButton(
                            onClick = { simulatedUpdateState = UpdateState.Checking },
                            modifier = Modifier.weight(1f),
                            shape = ButtonShape
                        ) {
                            Text("1. Checking", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { simulatedUpdateState = UpdateState.Available(sampleInfo) },
                            modifier = Modifier.weight(1f),
                            shape = ButtonShape
                        ) {
                            Text("2. Available", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                    ) {
                        Button(
                            onClick = {
                                simulatedUpdateState = UpdateState.Downloading(
                                    info = sampleInfo,
                                    progress = 0.65f,
                                    downloadedBytes = 16357785L,
                                    totalBytes = 25165824L,
                                    speedBytesPerSec = 2516582L
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = ButtonShape
                        ) {
                            Text("3. Downloading (65%)", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val dummyFile = java.io.File(context.cacheDir, "sample.apk").apply {
                                    if (!exists()) {
                                        createNewFile()
                                        writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
                                    }
                                }
                                simulatedUpdateState = UpdateState.ReadyToInstall(
                                    apkFile = dummyFile,
                                    info = sampleInfo
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = ButtonShape
                        ) {
                            Text("4. Ready (100%)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            AppUpdateBottomSheet(
                state = simulatedUpdateState,
                onStartDownload = { info ->
                    simulatedUpdateState = UpdateState.Downloading(
                        info = info,
                        progress = 0.45f,
                        downloadedBytes = 11324620L,
                        totalBytes = info.apkSize,
                        speedBytesPerSec = 3145728L
                    )
                },
                onCancelDownload = { simulatedUpdateState = UpdateState.Idle },
                onInstall = { simulatedUpdateState = UpdateState.Idle },
                onDismiss = { simulatedUpdateState = UpdateState.Idle },
                installer = AppUpdateInstaller(context)
            )

            // 1. Color Tokens Section
            SectionHeader(title = "1. Color Tokens")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
                ColorSwatch("PrimaryContainer", MaterialTheme.colorScheme.primaryContainer)
                ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
                ColorSwatch("SecondaryContainer", MaterialTheme.colorScheme.secondaryContainer)
                ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
                ColorSwatch("TertiaryContainer", MaterialTheme.colorScheme.tertiaryContainer)
                ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
                ColorSwatch("SurfaceContainer", MaterialTheme.colorScheme.surfaceContainer)
                ColorSwatch("Usage Low", UsageLow)
                ColorSwatch("Usage Mid", UsageMedium)
                ColorSwatch("Usage High", UsageHigh)
            }

            // 2. Typography Scale Section
            SectionHeader(title = "2. Typography Scale")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.cardPaddingStandard),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text("numberHero: 148.4 kWh", style = NumberHero)
                    Text("numberLarge: ৳ 1,280.50", style = NumberLarge)
                    Text("numberMedium: 8.4 units", style = NumberMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.xxs))
                    Text("displayMedium", style = MaterialTheme.typography.displayMedium)
                    Text("headlineMedium", style = MaterialTheme.typography.headlineMedium)
                    Text("titleLarge Section Header", style = MaterialTheme.typography.titleLarge)
                    Text("titleMedium Card Title", style = MaterialTheme.typography.titleMedium)
                    Text("bodyLarge Primary Body Text", style = MaterialTheme.typography.bodyLarge)
                    Text("bodyMedium Secondary Body Text", style = MaterialTheme.typography.bodyMedium)
                    Text("labelLarge Buttons & Tabs", style = MaterialTheme.typography.labelLarge)
                    Text("labelMedium Captions & Units", style = MaterialTheme.typography.labelMedium)
                    Text("labelSmall Nav Labels", style = MaterialTheme.typography.labelSmall)
                }
            }

            // 3. Components Showcase
            SectionHeader(title = "3. Components (§5)")

            // GradientHeroCard
            Text("GradientHeroCard", style = MaterialTheme.typography.titleMedium)
            GradientHeroCard(
                monthLabel = "Current Billing Cycle",
                unitsUsed = "142.8",
                unitSymbol = "kWh",
                estimatedCost = "৳ 1,280.50 (Projected)",
                trendPercent = -5.4,
                cycleDayText = "Day 14 of 30 · 48% through cycle",
                cycleProgress = 0.48f,
                sparklinePoints = listOf(10f, 12f, 15f, 13f, 17f, 14f, 16f)
            )

            // StatCards
            Text("StatCard Bento Grid", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.cardGap)
            ) {
                StatCard(
                    title = "Avg daily",
                    value = "8.4",
                    unit = "units",
                    icon = Icons.Filled.ElectricBolt,
                    trendPercent = -4.2,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Peak day",
                    value = "14.2",
                    unit = "units",
                    icon = Icons.Filled.Lightbulb,
                    trendPercent = 8.1,
                    modifier = Modifier.weight(1f)
                )
            }

            // UsageRing
            Text("UsageRing", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UsageRing(progress = 0.55f, centerText = "55%")
                UsageRing(progress = 0.78f, centerText = "78%")
                UsageRing(progress = 0.95f, centerText = "95%")
            }

            // TrendChip
            Text("TrendChip", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                TrendChip(percent = -8.4)
                TrendChip(percent = 0.0)
                TrendChip(percent = 12.3)
            }

            // SegmentedTabs
            Text("SegmentedTabs", style = MaterialTheme.typography.titleMedium)
            SegmentedTabs(
                tabs = listOf("Day", "Week", "Month", "Year"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // ReadingListItem
            Text("ReadingListItem", style = MaterialTheme.typography.titleMedium)
            ReadingListItem(
                dayString = "14",
                monthString = "Nov",
                meterValue = "14,828.4 kWh",
                deltaUnitsText = "+8.4 units",
                costText = "৳ 78.50",
                hasPhoto = true,
                hasNote = true,
                isEstimated = false
            )

            // TariffSlabRow
            Text("TariffSlabRow", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    TariffSlabRow("0 - 75 units", "৳ 4.63", "75", "৳ 347.25")
                    TariffSlabRow("76 - 200 units", "৳ 6.31", "42", "৳ 265.02")
                    TariffSlabRow("Total", "", "117", "৳ 612.27", isTotalRow = true)
                }
            }

            // InsightCard
            Text("InsightCard", style = MaterialTheme.typography.titleMedium)
            InsightCard(
                message = "You used 12% less electricity than this time last month!",
                onDismiss = {}
            )

            // ChartCard
            Text("ChartCard", style = MaterialTheme.typography.titleMedium)
            ChartCard(
                title = "Last 7 Days Usage",
                semanticSummary = "Past 7 days usage shows an average of 8.2 units per day.",
                tabs = listOf("Day", "Week", "Month", "Year"),
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                onViewAsTableClick = {}
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Interactive Chart (140dp)", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Skeleton Shimmers
            Text("Skeleton Shimmer", style = MaterialTheme.typography.titleMedium)
            HeroCardSkeleton()
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.cardGap)) {
                StatCardSkeleton(modifier = Modifier.weight(1f))
                StatCardSkeleton(modifier = Modifier.weight(1f))
            }

            // EmptyState
            Text("EmptyState", style = MaterialTheme.typography.titleMedium)
            EmptyState(
                icon = Icons.Filled.ElectricBolt,
                title = "No recent alerts",
                description = "All appliances and meters operating normally within expected power ranges.",
                actionLabel = "Refresh status",
                onActionClick = {}
            )

            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Dimens.xxs)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(Dimens.xxs))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(name = "Design Gallery - Light", showBackground = true)
@Composable
private fun PreviewDesignGalleryLight() {
    WattWiseTheme(darkTheme = false) {
        DesignGalleryScreen()
    }
}

@Preview(name = "Design Gallery - Dark", showBackground = true)
@Composable
private fun PreviewDesignGalleryDark() {
    WattWiseTheme(darkTheme = true) {
        DesignGalleryScreen()
    }
}
