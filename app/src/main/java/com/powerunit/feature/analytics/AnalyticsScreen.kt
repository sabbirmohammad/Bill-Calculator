package com.powerunit.feature.analytics

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.EmptyState
import com.powerunit.core.designsystem.component.TrendChip
import com.powerunit.domain.model.DailyUsage
import com.powerunit.feature.analytics.components.SlabDonutChart
import com.powerunit.feature.analytics.components.UsageBarAndLineChart
import com.powerunit.feature.analytics.components.WeekdayHeatmap
import com.powerunit.core.designsystem.theme.CardShape
import java.math.BigDecimal

@Composable
fun AnalyticsScreen(
    onNavigateToAddReading: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AnalyticsContent(
                uiState = uiState,
                onPeriodSelected = viewModel::selectPeriod,
                onToggleTable = viewModel::toggleTableView,
                onBarSelected = viewModel::selectBar,
                onNavigateToAddReading = onNavigateToAddReading
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    onToggleTable: () -> Unit,
    onBarSelected: (Int?) -> Unit,
    onNavigateToAddReading: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_scroll_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AnalyticsHeader(
                isTableView = uiState.isTableView,
                onToggleTable = onToggleTable
            )
        }

        item {
            PeriodSelectorRow(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        }

        if (uiState.chartData.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.BarChart,
                    title = stringResource(R.string.analytics_empty_title),
                    description = stringResource(R.string.analytics_empty_desc),
                    actionLabel = "Record Reading",
                    onActionClick = onNavigateToAddReading,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            item {
                AnalyticsSummaryGrid(
                    totalUnits = uiState.totalUnits,
                    totalCost = uiState.totalCost,
                    dailyAverage = uiState.dailyAverage,
                    peakUsage = uiState.peakUsage
                )
            }

            uiState.comparison?.let { comp ->
                item {
                    PeriodComparisonCard(comp = comp)
                }
            }

            item {
                UsageBarAndLineChart(
                    bars = uiState.chartData,
                    selectedBarIndex = uiState.selectedBarIndex,
                    onBarSelected = onBarSelected,
                    isTableView = uiState.isTableView
                )
            }

            if (uiState.weekdayPattern.isNotEmpty()) {
                item {
                    WeekdayHeatmap(pattern = uiState.weekdayPattern)
                }
            }

            if (uiState.slabDonutItems.isNotEmpty()) {
                item {
                    SlabDonutChart(
                        items = uiState.slabDonutItems,
                        totalUnits = uiState.totalUnits
                    )
                }
            }

            item {
                AnomaliesSection(anomalies = uiState.anomalies)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AnalyticsHeader(
    isTableView: Boolean,
    onToggleTable: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.analytics_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Consumption trends & cost insights",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onToggleTable,
            modifier = Modifier.testTag("analytics_toggle_table_btn")
        ) {
            Icon(
                imageVector = if (isTableView) Icons.Default.BarChart else Icons.Default.TableChart,
                contentDescription = if (isTableView) {
                    stringResource(R.string.analytics_toggle_chart)
                } else {
                    stringResource(R.string.analytics_toggle_table)
                },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PeriodSelectorRow(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                val periodName = when (period) {
                    AnalyticsPeriod.DAY -> stringResource(R.string.analytics_period_day)
                    AnalyticsPeriod.WEEK -> stringResource(R.string.analytics_period_week)
                    AnalyticsPeriod.MONTH -> stringResource(R.string.analytics_period_month)
                    AnalyticsPeriod.YEAR -> stringResource(R.string.analytics_period_year)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 8.dp)
                        .testTag("period_tab_${period.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = periodName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSummaryGrid(
    totalUnits: BigDecimal,
    totalCost: BigDecimal,
    dailyAverage: BigDecimal,
    peakUsage: ChartBarData?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile(
                title = stringResource(R.string.analytics_total_usage),
                value = "${Formatters.formatUnits(totalUnits)} kWh",
                icon = Icons.Default.Bolt,
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                title = stringResource(R.string.analytics_total_cost),
                value = Formatters.formatBDT(totalCost),
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile(
                title = stringResource(R.string.analytics_daily_average),
                value = "${Formatters.formatUnits(dailyAverage)} kWh/d",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                title = stringResource(R.string.analytics_peak_usage),
                value = peakUsage?.let { "${Formatters.formatUnits(it.units)} kWh" } ?: "—",
                icon = Icons.Default.WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PeriodComparisonCard(
    comp: com.powerunit.domain.model.PeriodComparison
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_comparison_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.analytics_comparison_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current vs prior period (${Formatters.formatUnits(comp.currentUnits)} vs ${Formatters.formatUnits(comp.previousUnits)} kWh)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TrendChip(percent = comp.percentChange)
        }
    }
}

@Composable
private fun AnomaliesSection(anomalies: List<DailyUsage>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_anomalies_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.analytics_anomalies_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (anomalies.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_no_anomalies),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                anomalies.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = DateUtils.formatDate(item.date),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "High spike (> 2σ above mean)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "${Formatters.formatUnits(item.units)} kWh",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
