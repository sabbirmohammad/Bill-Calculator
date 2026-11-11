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
import com.powerunit.core.designsystem.component.AnalyticsSkeleton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.EmptyState
import com.powerunit.core.designsystem.component.SegmentedTabs
import com.powerunit.core.designsystem.component.TrendChip
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.IconWellShape
import com.powerunit.domain.model.DailyUsage
import com.powerunit.feature.analytics.components.SlabDonutChart
import com.powerunit.feature.analytics.components.UsageBarAndLineChart
import com.powerunit.feature.analytics.components.WeekdayHeatmap
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
            AnalyticsSkeleton()
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
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .testTag("analytics_scroll_content"),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        item {
            Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
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
                    modifier = Modifier.padding(Dimens.spacingXl)
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
            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Consumption trends & cost insights",
                style = MaterialTheme.typography.bodyMedium,
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeStandard)
            )
        }
    }
}

@Composable
private fun PeriodSelectorRow(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    val periods = AnalyticsPeriod.entries
    val periodLabels = periods.map { period ->
        when (period) {
            AnalyticsPeriod.DAY -> stringResource(R.string.analytics_period_day)
            AnalyticsPeriod.WEEK -> stringResource(R.string.analytics_period_week)
            AnalyticsPeriod.MONTH -> stringResource(R.string.analytics_period_month)
            AnalyticsPeriod.YEAR -> stringResource(R.string.analytics_period_year)
        }
    }
    val selectedIndex = periods.indexOf(selectedPeriod).coerceAtLeast(0)

    SegmentedTabs(
        tabs = periodLabels,
        selectedIndex = selectedIndex,
        onTabSelected = { index -> onPeriodSelected(periods[index]) },
        tabTestTags = listOf("period_tab_day", "period_tab_week", "period_tab_month", "period_tab_year")
    )
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
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
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
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.cardTitleToValueGap))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconContainerSize)
                        .background(MaterialTheme.colorScheme.secondaryContainer, IconWellShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.iconToTextGap))
                Column {
                    Text(
                        text = stringResource(R.string.analytics_comparison_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current vs prior period (${Formatters.formatUnits(comp.currentUnits)} vs ${Formatters.formatUnits(comp.previousUnits)} kWh)",
                        style = MaterialTheme.typography.bodyMedium,
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.analytics_anomalies_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

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
                            .padding(vertical = Dimens.xxs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = DateUtils.formatDate(item.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "High spike (> 2σ above mean)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "${Formatters.formatUnits(item.units)} kWh",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
