package com.powerunit.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.GradientHeroCard
import com.powerunit.core.designsystem.component.SectionHeader
import com.powerunit.core.designsystem.component.StatCard
import com.powerunit.core.designsystem.component.TrendChip
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Reading
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    onNavigateToAddReading: () -> Unit,
    onNavigateToBill: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DashboardHeader(
                greeting = state.greeting,
                meterName = state.activeMeter?.name ?: "Main Meter"
            )
        }

        item {
            HeroUsageCard(
                state = state,
                onCardClick = onNavigateToBill
            )
        }

        if (!state.hasRecordedToday) {
            item {
                RecordTodayPromptBanner(onRecordClick = onNavigateToAddReading)
            }
        }

        item {
            QuickStatsSection(state = state)
        }

        item {
            ForecastCardSection(state = state)
        }

        item {
            state.budget?.let { budget ->
                BudgetSection(budget = budget)
            }
        }

        if (state.past14DaysUsage.isNotEmpty()) {
            item {
                SectionHeader(title = "14-Day Usage Trend")
                Spacer(modifier = Modifier.height(8.dp))
                MiniUsageBarChart(dailyUsages = state.past14DaysUsage)
            }
        }

        if (state.recentReadings.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Readings",
                    actionLabel = "See All",
                    onActionClick = onNavigateToHistory
                )
                Spacer(modifier = Modifier.height(8.dp))
                RecentReadingsSection(readings = state.recentReadings)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar & FAB
        }
    }
}

@Composable
private fun DashboardHeader(greeting: String, meterName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "WattWise",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(PillShape)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = meterName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
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
        estimatedBill = billStr,
        budgetPercent = state.budget?.percentageUsed ?: 0f,
        projectionText = cycleInfo,
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Record Today's Reading",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep track of daily power usage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onRecordClick,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
private fun ForecastCardSection(state: DashboardUiState) {
    val forecast = state.forecast ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Month-End Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${Formatters.formatUnits(forecast.expectedUnits)} kWh projected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Estimated final bill: ${Formatters.formatBDT(forecast.expectedBill)} (${Formatters.formatBDT(forecast.lowBill)} - ${Formatters.formatBDT(forecast.highBill)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetSection(budget: BudgetUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(budget.percentageUsed * 100).toInt()}% used",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { budget.percentageUsed },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(PillShape),
                color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${Formatters.formatBDT(budget.currentSpentBDT)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Limit: ${Formatters.formatBDT(budget.monthlyLimitBDT)}",
                    style = MaterialTheme.typography.bodySmall,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val maxUsage = dailyUsages.maxOfOrNull { it.units } ?: BigDecimal("10.0")
        val safeMax = if (maxUsage > BigDecimal.ZERO) maxUsage.toFloat() else 10f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(90.dp),
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
                            .height((70 * barFraction).dp)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentReadingsSection(readings: List<Reading>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            readings.forEachIndexed { index, reading ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${Formatters.formatUnits(reading.readingValue)} kWh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${DateUtils.formatDate(reading.recordedAt)} ${DateUtils.formatTime(reading.recordedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (reading.note != null) {
                        Text(
                            text = reading.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (index < readings.size - 1) {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
