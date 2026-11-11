package com.powerunit.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.DashboardSkeleton
import com.powerunit.core.designsystem.component.GradientHeroCard
import com.powerunit.core.designsystem.component.SectionHeader
import com.powerunit.core.designsystem.component.StatCard
import com.powerunit.core.designsystem.component.UsageRing
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Reading
import com.powerunit.feature.dashboard.components.InitialReadingDialog
import com.powerunit.feature.dashboard.components.InitialReadingRequiredBanner
import com.powerunit.feature.history.ReadingDetailDialog
import com.powerunit.feature.history.ReadingItemUiModel
import com.powerunit.feature.history.ReadingListItem
import com.powerunit.feature.history.toUiModel
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    onNavigateToAddReading: () -> Unit,
    onNavigateToBill: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToEditReading: (Long) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedReadingForDetail by remember { mutableStateOf<ReadingItemUiModel?>(null) }

    selectedReadingForDetail?.let { detail ->
        ReadingDetailDialog(
            detail = detail,
            onDismiss = { selectedReadingForDetail = null },
            onEdit = { id ->
                selectedReadingForDetail = null
                onNavigateToEditReading(id)
            },
            onDelete = {
                selectedReadingForDetail = null
                onNavigateToHistory()
            }
        )
    }

    if (state.isLoading) {
        DashboardSkeleton()
        return
    }

    if (state.showInitialReadingDialog && !state.hasInitialReading) {
        InitialReadingDialog(
            onSave = { value, date, note ->
                viewModel.saveInitialReading(value, date, note)
            },
            onDismiss = {
                viewModel.onDismissInitialReadingDialog()
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        item {
            Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
            DashboardHeader(
                greeting = state.greeting,
                meterName = state.activeMeter?.name ?: "Main Meter"
            )
        }

        if (!state.hasInitialReading) {
            item {
                InitialReadingRequiredBanner(
                    onSetInitialReadingClick = { viewModel.onShowInitialReadingDialog() }
                )
            }
        } else if (!state.hasRecordedToday) {
            item {
                RecordTodayPromptBanner(onRecordClick = onNavigateToAddReading)
            }
        }

        item {
            HeroUsageCard(
                state = state,
                onCardClick = onNavigateToBill
            )
        }

        item {
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            QuickStatsSection(state = state)
        }

        item {
            if (state.budget != null || state.forecast != null) {
                BudgetAndOutlookSection(
                    budget = state.budget,
                    forecast = state.forecast,
                    daysLeft = state.cycleUsage?.daysLeft,
                    hideAmounts = state.hideAmounts
                )
            }
        }

        if (state.past14DaysUsage.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                SectionHeader(title = "14-Day Usage Trend")
                Spacer(modifier = Modifier.height(Dimens.sectionHeaderToContentGap))
                MiniUsageBarChart(dailyUsages = state.past14DaysUsage)
            }
        }

        if (state.recentReadings.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                SectionHeader(
                    title = "Recent Readings",
                    actionLabel = "See All",
                    onActionClick = onNavigateToHistory
                )
                Spacer(modifier = Modifier.height(Dimens.sectionHeaderToContentGap))
                RecentReadingsSection(
                    readings = state.recentReadings,
                    onReadingClick = { item -> selectedReadingForDetail = item }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
        }
    }
}

@Composable
private fun DashboardHeader(greeting: String, meterName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "WattWise",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(PillShape)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimens.chipHorizontalPadding, vertical = Dimens.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = meterName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HeroUsageCard(
    state: DashboardUiState,
    onCardClick: () -> Unit
) {
    val totalUnits = state.cycleUsage?.totalUnitsSoFar ?: BigDecimal.ZERO
    val unitsStr = Formatters.formatUnits(totalUnits)
    val billStr = if (state.hideAmounts) "৳ ••••" else Formatters.formatBDT(state.billBreakdown?.totalBill ?: BigDecimal.ZERO)
    val totalDays = (state.cycleUsage?.daysElapsed ?: 0) + (state.cycleUsage?.daysLeft ?: 0)
    val cycleInfo = state.cycleUsage?.let {
        "Day ${it.daysElapsed} of $totalDays • ${it.daysLeft} days left"
    } ?: "Current Cycle"

    GradientHeroCard(
        monthLabel = "Current Billing Cycle",
        unitsUsed = unitsStr,
        unitSymbol = "kWh",
        estimatedCost = billStr,
        trendPercent = state.usageTrendPercent?.toDouble(),
        cycleDayText = cycleInfo,
        cycleProgress = state.budget?.percentageUsed ?: 0.36f,
        sparklinePoints = null,
        modifier = Modifier
            .testTag("dashboard_hero_card")
            .clickable(onClick = onCardClick)
    )
}

@Composable
private fun RecordTodayPromptBanner(onRecordClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeStandard)
                )
                Spacer(modifier = Modifier.width(Dimens.iconToTextGap))
                Column {
                    Text(
                        text = "Record Today's Reading",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep track of daily power usage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onRecordClick,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
            ) {
                Text("Log", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun QuickStatsSection(state: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Today",
            value = Formatters.formatUnits(state.todayUsage),
            unit = "kWh"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Yesterday",
            value = Formatters.formatUnits(state.yesterdayUsage),
            unit = "kWh",
            trendPercent = state.usageTrendPercent?.toDouble()
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Daily Avg",
            value = Formatters.formatUnits(state.dailyAverage),
            unit = "kWh"
        )
    }
}

@Composable
private fun BudgetAndOutlookSection(
    budget: BudgetUiModel?,
    forecast: com.powerunit.domain.model.MonthEndForecast?,
    daysLeft: Int? = null,
    hideAmounts: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            // Header Row: Title + Dynamic Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimens.xs))
                    Text(
                        text = if (budget != null) stringResource(R.string.budget_and_outlook) else stringResource(R.string.forecast_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Dynamic Status Badge
                if (budget != null) {
                    val isExceeded = budget.isOverBudget
                    val isRisk = forecast != null && forecast.expectedBill > budget.monthlyLimitBDT
                    val (badgeText, badgeColor, textColor) = when {
                        isExceeded -> Triple(
                            stringResource(R.string.forecast_over_budget),
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer
                        )
                        isRisk -> Triple(
                            stringResource(R.string.outlook_at_risk),
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        else -> Triple(
                            stringResource(R.string.forecast_on_track),
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Surface(
                        shape = PillShape,
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = Dimens.xs, vertical = Dimens.xxs),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.sm))

            // Main Content Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                if (budget != null) {
                    UsageRing(
                        progress = budget.percentageUsed,
                        size = 100.dp,
                        strokeWidth = 10.dp,
                        subText = "of budget"
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xxs)
                ) {
                    if (budget != null) {
                        val spentStr = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.currentSpentBDT)
                        Text(
                            text = "Spent: $spentStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (forecast != null) {
                        val projBill = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(forecast.expectedBill)
                        val projUnits = "${Formatters.formatUnits(forecast.expectedUnits)} kWh"
                        Text(
                            text = "Projected: $projBill ($projUnits)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (budget != null) {
                        val limitStr = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.monthlyLimitBDT)
                        Text(
                            text = "Limit: $limitStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Bottom Divider & Insight line
            Spacer(modifier = Modifier.height(Dimens.xs))
            HorizontalDivider(
                thickness = Dimens.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(Dimens.xs))

            val insightText = buildString {
                if (budget != null && forecast != null) {
                    val diff = budget.monthlyLimitBDT.subtract(forecast.expectedBill)
                    if (diff >= BigDecimal.ZERO) {
                        append("Projected ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff)} under budget")
                    } else {
                        append("Projected to exceed budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff.abs())}")
                    }
                } else if (budget != null) {
                    val remaining = budget.monthlyLimitBDT.subtract(budget.currentSpentBDT)
                    if (remaining >= BigDecimal.ZERO) {
                        append("${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(remaining)} remaining")
                    } else {
                        append("Over budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(remaining.abs())}")
                    }
                } else if (forecast != null) {
                    append("Expected bill ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(forecast.expectedBill)}")
                }

                if (daysLeft != null && daysLeft > 0) {
                    if (isNotEmpty()) append(" • ")
                    append("$daysLeft days remaining")
                }
            }

            if (insightText.isNotEmpty()) {
                Text(
                    text = insightText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MiniUsageBarChart(dailyUsages: List<DailyUsage>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        val maxUsage = dailyUsages.maxOfOrNull { it.units } ?: BigDecimal("10.0")
        val safeMax = if (maxUsage > BigDecimal.ZERO) maxUsage.toFloat() else 10f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
                .height(96.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyUsages.forEach { day ->
                val barFraction = (day.units.toFloat() / safeMax).coerceIn(0.05f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height((72 * barFraction).dp)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(Dimens.xxs))
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentReadingsSection(
    readings: List<Reading>,
    onReadingClick: (ReadingItemUiModel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        readings.forEachIndexed { index, reading ->
            val prev = if (index < readings.size - 1) readings[index + 1] else null
            val item = reading.toUiModel(prev)
            ReadingListItem(
                item = item,
                onClick = { onReadingClick(item) }
            )
        }
    }
}
