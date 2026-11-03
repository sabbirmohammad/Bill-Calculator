package com.bangla.electricitybill.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.presentation.components.AnalyticsSummaryCardsRow
import com.bangla.electricitybill.presentation.components.AnalyticsUsageHeroCard
import com.bangla.electricitybill.presentation.components.InteractiveConsumptionBarChart
import com.bangla.electricitybill.presentation.components.NavigableSectionTile
import com.bangla.electricitybill.presentation.components.SegmentedDateFilters
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToReadings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen 3 Section A: Title Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = if (state.isBangla) "বিদ্যুৎ ব্যবহার বিশ্লেষণ" else "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }

            // Screen 3 Section B: Segmented Date Filters
            item {
                SegmentedDateFilters(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { viewModel.selectFilter(it) },
                    modifier = Modifier.staggeredEntrance(1),
                    isBangla = state.isBangla
                )
            }

            // Screen 3 Section C: Monthly Consumption Hero Card
            item {
                AnalyticsUsageHeroCard(
                    monthTitle = state.monthTitle,
                    totalUnits = state.totalRecordedUnits,
                    percentageDiff = state.percentageDiff,
                    isDiffDecrease = state.isDiffDecrease,
                    previousUnits = state.previousMonthUnits,
                    modifier = Modifier.staggeredEntrance(2),
                    isBangla = state.isBangla
                )
            }

            // Screen 3 Section D: Consumption Interactive Canvas Chart
            item {
                InteractiveConsumptionBarChart(
                    dataPoints = state.chartPoints,
                    modifier = Modifier.staggeredEntrance(3)
                )
            }

            // Screen 3 Section E: Analytics Summary Cards
            item {
                val dailyUnitsFormatted = BigDecimalMath.formatUnits(state.averageDailyUnits, state.isBangla)
                val billFormatted = BigDecimalMath.formatCurrency(state.estimatedBill, state.isBangla)

                AnalyticsSummaryCardsRow(
                    dailyAverageUnits = dailyUnitsFormatted,
                    estimatedBillText = billFormatted,
                    modifier = Modifier.staggeredEntrance(4),
                    isBangla = state.isBangla
                )
            }

            // Screen 3 Section F: Navigable Sections ("Usage Trend", "Daily Readings")
            item {
                NavigableSectionTile(
                    icon = Icons.Default.ShowChart,
                    title = if (state.isBangla) "ব্যবহারের গতিধারা" else "Usage Trend",
                    onClick = onNavigateToReadings,
                    modifier = Modifier.staggeredEntrance(5)
                )
            }

            item {
                NavigableSectionTile(
                    icon = Icons.Default.CalendarMonth,
                    title = if (state.isBangla) "দৈনিক রিডিং তালিকা" else "Daily Readings",
                    onClick = onNavigateToReadings,
                    modifier = Modifier.staggeredEntrance(6)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
