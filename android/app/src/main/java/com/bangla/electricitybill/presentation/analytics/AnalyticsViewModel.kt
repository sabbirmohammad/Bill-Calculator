package com.bangla.electricitybill.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.backup.BackupManager
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.presentation.components.DailyChartPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AnalyticsUiState(
    val selectedFilter: String = "This Month",
    val monthTitle: String = "This Month Usage",
    val totalRecordedUnits: BigDecimal = BigDecimal.ZERO,
    val averageDailyUnits: BigDecimal = BigDecimal.ZERO,
    val estimatedBill: BigDecimal = BigDecimal.ZERO,
    val percentageDiff: Int? = null,
    val isDiffDecrease: Boolean = true,
    val previousMonthUnits: BigDecimal? = null,
    val chartPoints: List<DailyChartPoint> = emptyList(),
    val readings: List<MeterReading> = emptyList(),
    val monthlyPeriods: List<BillingPeriod> = emptyList(),
    val activeTariff: Tariff? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val billingPeriodRepository: BillingPeriodRepository,
    private val tariffRepository: TariffRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("This Month")

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeter() ?: return@launch
            combine(
                readingRepository.getReadingsFlow(meter.id),
                billingPeriodRepository.getBillingPeriodsFlow(meter.id),
                tariffRepository.getActiveTariffFlow(),
                _selectedFilter
            ) { readings, periods, tariff, filter ->
                val sorted = readings.sortedBy { it.readingDateTime }
                val now = DateUtils.now()
                val currentYearMonth = YearMonth.from(now)
                val targetYearMonth = when (filter) {
                    "Last Month" -> currentYearMonth.minusMonths(1)
                    else -> currentYearMonth
                }

                // Month Title
                val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
                val monthTitle = "${targetYearMonth.format(monthFormatter)} Usage"

                // Filter readings for target month
                val targetReadings = sorted.filter { YearMonth.from(it.readingDateTime) == targetYearMonth }
                val prevMonthReadings = sorted.filter { YearMonth.from(it.readingDateTime) == targetYearMonth.minusMonths(1) }

                // Calculate interval consumptions in target period
                val chartPoints = mutableListOf<DailyChartPoint>()
                var totalUnits = BigDecimal.ZERO

                for (i in 1 until sorted.size) {
                    val prev = sorted[i - 1]
                    val curr = sorted[i]
                    if (YearMonth.from(curr.readingDateTime) == targetYearMonth) {
                        val isRollover = curr.readingSource == ReadingSource.ROLLOVER || curr.normalizedReading < prev.normalizedReading
                        val deltaRes = ConsumptionCalculator.calculateConsumption(
                            previousReading = prev.normalizedReading,
                            currentReading = curr.normalizedReading,
                            isRollover = isRollover,
                            blackDigitCapacity = meter.blackDigitCapacity
                        )
                        val delta = deltaRes.getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
                        totalUnits = totalUnits.add(delta)

                        val dayNum = curr.readingDateTime.dayOfMonth
                        val label = when (dayNum) {
                            1 -> "Jan 1"
                            else -> dayNum.toString()
                        }

                        chartPoints.add(
                            DailyChartPoint(
                                dayNumber = dayNum,
                                units = delta.toFloat(),
                                dateLabel = label
                            )
                        )
                    }
                }

                // If only 1 reading exists for target month and we have a previous reading in previous month:
                if (targetReadings.size == 1 && prevMonthReadings.isNotEmpty()) {
                    val prev = prevMonthReadings.last()
                    val curr = targetReadings.first()
                    val deltaRes = ConsumptionCalculator.calculateConsumption(
                        previousReading = prev.normalizedReading,
                        currentReading = curr.normalizedReading,
                        blackDigitCapacity = meter.blackDigitCapacity
                    )
                    val delta = deltaRes.getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
                    totalUnits = totalUnits.add(delta)
                    chartPoints.add(
                        DailyChartPoint(
                            dayNumber = curr.readingDateTime.dayOfMonth,
                            units = delta.toFloat(),
                            dateLabel = curr.readingDateTime.dayOfMonth.toString()
                        )
                    )
                }

                // Calculate Previous Month units for comparison
                var prevTotalUnits = BigDecimal.ZERO
                for (i in 1 until sorted.size) {
                    val prev = sorted[i - 1]
                    val curr = sorted[i]
                    if (YearMonth.from(curr.readingDateTime) == targetYearMonth.minusMonths(1)) {
                        val isRollover = curr.readingSource == ReadingSource.ROLLOVER || curr.normalizedReading < prev.normalizedReading
                        val deltaRes = ConsumptionCalculator.calculateConsumption(
                            previousReading = prev.normalizedReading,
                            currentReading = curr.normalizedReading,
                            isRollover = isRollover,
                            blackDigitCapacity = meter.blackDigitCapacity
                        )
                        prevTotalUnits = prevTotalUnits.add(deltaRes.getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO))
                    }
                }

                // Comparison percentage
                var percentageDiff: Int? = null
                var isDecrease = true
                if (prevTotalUnits > BigDecimal.ZERO && totalUnits > BigDecimal.ZERO) {
                    val diff = totalUnits.subtract(prevTotalUnits)
                    val pct = diff.divide(prevTotalUnits, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toInt()
                    percentageDiff = Math.abs(pct)
                    isDecrease = diff < BigDecimal.ZERO
                }

                // Days tracked
                val daysCount = targetReadings.map { it.readingDateTime.toLocalDate() }.distinct().size.coerceAtLeast(1)
                val avgDaily = if (daysCount > 0) {
                    totalUnits.divide(BigDecimal(daysCount), 2, RoundingMode.HALF_UP)
                } else BigDecimal.ZERO

                // Estimated Bill
                val itemized = if (tariff != null && totalUnits > BigDecimal.ZERO) {
                    ProgressiveTariffEngine.calculateMonthlyBill(totalUnits, tariff)
                } else null
                val estBill = itemized?.totalBill ?: BigDecimal.ZERO

                _uiState.update {
                    it.copy(
                        selectedFilter = filter,
                        monthTitle = monthTitle,
                        totalRecordedUnits = totalUnits,
                        averageDailyUnits = avgDaily,
                        estimatedBill = estBill,
                        percentageDiff = percentageDiff,
                        isDiffDecrease = isDecrease,
                        previousMonthUnits = if (prevTotalUnits > BigDecimal.ZERO) prevTotalUnits else null,
                        chartPoints = chartPoints,
                        readings = sorted,
                        monthlyPeriods = periods,
                        activeTariff = tariff,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun selectFilter(filter: String) {
        _selectedFilter.update { filter }
    }

    fun exportCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = backupManager.exportReadingsToCsv()
            onResult(csv)
        }
    }

    fun exportJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            onResult(json)
        }
    }
}
