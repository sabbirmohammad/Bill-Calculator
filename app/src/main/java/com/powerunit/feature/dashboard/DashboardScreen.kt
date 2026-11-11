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
import com.powerunit.core.designsystem.component.MonthlyBudgetAndOutlookCard
import com.powerunit.core.designsystem.component.SectionHeader
import com.powerunit.core.designsystem.component.SetBudgetCtaCard
import com.powerunit.core.designsystem.component.SetBudgetDialog
import com.powerunit.core.designsystem.component.StatCard
import com.powerunit.core.designsystem.component.UsageRing
import com.powerunit.core.designsystem.component.UsageSummaryCard
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
    var showBudgetDialog by remember { mutableStateOf(false) }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentLimit = state.budget?.monthlyLimitBDT,
            onSaveBudget = viewModel::onSaveBudget,
            onDismiss = { showBudgetDialog = false }
        )
    }

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
            val daysElapsed = state.cycleUsage?.daysElapsed ?: 0
            val daysLeft = state.cycleUsage?.daysLeft ?: 0
            val totalDays = daysElapsed + daysLeft
            val currentBudget = state.budget

            if (currentBudget != null && state.hasCustomBudget) {
                MonthlyBudgetAndOutlookCard(
                    budget = currentBudget,
                    forecast = state.forecast,
                    daysElapsed = daysElapsed,
                    totalDays = if (totalDays > 0) totalDays else 30,
                    daysLeft = daysLeft,
                    onEditBudgetClick = { showBudgetDialog = true },
                    hideAmounts = state.hideAmounts,
                    modifier = Modifier.testTag("dashboard_budget_card")
                )
            } else {
                SetBudgetCtaCard(
                    onSetBudgetClick = { showBudgetDialog = true },
                    modifier = Modifier.testTag("dashboard_set_budget_cta")
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
    val daysElapsed = state.cycleUsage?.daysElapsed ?: 0
    val daysLeft = state.cycleUsage?.daysLeft ?: 0
    val totalDays = daysElapsed + daysLeft
    val cycleInfo = if (totalDays > 0) {
        "Day $daysElapsed of $totalDays • $daysLeft days left"
    } else {
        "Current Cycle"
    }
    val cycleProgressFraction = if (totalDays > 0) {
        (daysElapsed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    } else {
        0.36f
    }

    GradientHeroCard(
        monthLabel = "Current Billing Cycle",
        unitsUsed = unitsStr,
        unitSymbol = "kWh",
        estimatedCost = billStr,
        trendPercent = state.usageTrendPercent?.toDouble(),
        cycleDayText = cycleInfo,
        cycleProgress = cycleProgressFraction,
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
    UsageSummaryCard(
        todayUnits = Formatters.formatUnits(state.todayUsage),
        yesterdayUnits = Formatters.formatUnits(state.yesterdayUsage),
        dailyAverageUnits = Formatters.formatUnits(state.dailyAverage),
        unitSymbol = "kWh",
        todayTrendPercent = state.usageTrendPercent?.toDouble(),
        modifier = Modifier.testTag("dashboard_usage_summary_card")
    )
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
