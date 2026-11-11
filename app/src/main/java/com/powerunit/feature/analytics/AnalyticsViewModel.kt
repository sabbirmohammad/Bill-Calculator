package com.powerunit.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.PeriodComparison
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import com.powerunit.domain.usecase.CompareWithPreviousUseCase
import com.powerunit.domain.usecase.DetectAnomaliesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class AnalyticsPeriod {
    DAY, WEEK, MONTH, YEAR
}

data class ChartBarData(
    val label: String,
    val units: BigDecimal,
    val cost: BigDecimal,
    val isPeak: Boolean = false,
    val isAnomaly: Boolean = false,
    val fullDate: String = ""
)

data class WeekdayIntensity(
    val dayOfWeek: DayOfWeek,
    val averageUnits: BigDecimal,
    val intensity: Float
)

data class SlabDonutItem(
    val slabName: String,
    val units: BigDecimal,
    val cost: BigDecimal,
    val percentageOfCost: Float,
    val colorIndex: Int
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.DAY,
    val isTableView: Boolean = false,
    val chartData: List<ChartBarData> = emptyList(),
    val totalUnits: BigDecimal = BigDecimal.ZERO,
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val dailyAverage: BigDecimal = BigDecimal.ZERO,
    val peakUsage: ChartBarData? = null,
    val lowestUsage: ChartBarData? = null,
    val comparison: PeriodComparison? = null,
    val weekdayPattern: List<WeekdayIntensity> = emptyList(),
    val slabDonutItems: List<SlabDonutItem> = emptyList(),
    val anomalies: List<DailyUsage> = emptyList(),
    val selectedBarIndex: Int? = null,
    val useBengaliDigits: Boolean = false,
    val isBangla: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository,
    private val tariffRepository: TariffRepository,
    private val settingsPreferences: SettingsPreferences,
    private val calculateDailyUsageUseCase: CalculateDailyUsageUseCase,
    private val calculateBillUseCase: CalculateBillUseCase,
    private val compareWithPreviousUseCase: CompareWithPreviousUseCase,
    private val detectAnomaliesUseCase: DetectAnomaliesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var cachedReadings: List<Reading> = emptyList()
    private var cachedTariff: Tariff? = null
    private var cachedUseBengaliDigits: Boolean = false
    private var cachedIsBangla: Boolean = false

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val meterId = meter?.id ?: 1L

            val prefsFlow = combine(
                settingsPreferences.billingCycleDay,
                settingsPreferences.useBengaliDigits,
                settingsPreferences.appLanguage
            ) { cycleDay, useBnDigits, lang ->
                Triple(cycleDay, useBnDigits, lang == "bn")
            }

            combine(
                readingRepository.getReadings(meterId),
                tariffRepository.getActiveTariff(),
                prefsFlow
            ) { readings, tariff, triple ->
                cachedReadings = readings
                cachedTariff = tariff
                cachedUseBengaliDigits = triple.second
                cachedIsBangla = triple.third
                recomputeState()
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectPeriod(period: AnalyticsPeriod) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            selectedBarIndex = null
        )
        _uiState.value = recomputeState()
    }

    fun toggleTableView() {
        _uiState.value = _uiState.value.copy(isTableView = !_uiState.value.isTableView)
    }

    fun selectBar(index: Int?) {
        _uiState.value = _uiState.value.copy(selectedBarIndex = index)
    }

    private fun recomputeState(): AnalyticsUiState {
        val sortedReadings = cachedReadings.sortedBy { it.recordedAt }
        val rawDailyUsages = calculateDailyUsageUseCase(sortedReadings)
        val dailyUsagesWithAnomalies = detectAnomaliesUseCase(rawDailyUsages)

        val activeTariff = cachedTariff ?: Tariff(
            name = "Default",
            effectiveFrom = DateUtils.currentInstant(),
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true
        )

        val period = _uiState.value.selectedPeriod
        val chartBars = buildChartData(dailyUsagesWithAnomalies, period, activeTariff)

        val totalUnits = chartBars.fold(BigDecimal.ZERO) { acc, bar -> acc + bar.units }
        val billBreakdown = calculateBillUseCase(totalUnits, activeTariff)
        val totalCost = billBreakdown.totalBill

        val daysCount = when (period) {
            AnalyticsPeriod.DAY -> chartBars.size.coerceAtLeast(1)
            AnalyticsPeriod.WEEK -> (chartBars.size * 7).coerceAtLeast(1)
            AnalyticsPeriod.MONTH -> (chartBars.size * 30).coerceAtLeast(1)
            AnalyticsPeriod.YEAR -> (chartBars.size * 365).coerceAtLeast(1)
        }
        val dailyAvg = if (daysCount > 0 && totalUnits > BigDecimal.ZERO) {
            totalUnits.divide(BigDecimal(daysCount), 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val peak = chartBars.maxByOrNull { it.units }
        val nonZeroBars = chartBars.filter { it.units > BigDecimal.ZERO }
        val lowest = nonZeroBars.minByOrNull { it.units } ?: chartBars.minByOrNull { it.units }

        val comparison = computeComparison(chartBars)
        val weekdayPattern = computeWeekdayPattern(dailyUsagesWithAnomalies)
        val slabDonutItems = computeSlabDonut(billBreakdown)
        val anomalies = dailyUsagesWithAnomalies.filter { it.isAnomaly }

        return _uiState.value.copy(
            isLoading = false,
            chartData = chartBars,
            totalUnits = totalUnits,
            totalCost = totalCost,
            dailyAverage = dailyAvg,
            peakUsage = peak,
            lowestUsage = lowest,
            comparison = comparison,
            weekdayPattern = weekdayPattern,
            slabDonutItems = slabDonutItems,
            anomalies = anomalies,
            useBengaliDigits = cachedUseBengaliDigits,
            isBangla = cachedIsBangla
        )
    }

    private fun buildChartData(
        dailyUsages: List<DailyUsage>,
        period: AnalyticsPeriod,
        tariff: Tariff
    ): List<ChartBarData> {
        if (dailyUsages.isEmpty()) return emptyList()

        return when (period) {
            AnalyticsPeriod.DAY -> {
                val recentDays = dailyUsages.takeLast(14)
                val peakVal = recentDays.maxOfOrNull { it.units } ?: BigDecimal.ZERO
                recentDays.map { d ->
                    val cost = calculateBillUseCase(d.units, tariff).totalBill
                    val dayLabel = "${d.date.dayOfMonth} ${d.date.month.name.take(3)}"
                    ChartBarData(
                        label = "${d.date.dayOfMonth}",
                        units = d.units,
                        cost = cost,
                        isPeak = d.units == peakVal && d.units > BigDecimal.ZERO,
                        isAnomaly = d.isAnomaly,
                        fullDate = dayLabel
                    )
                }
            }
            AnalyticsPeriod.WEEK -> {
                val chunks = dailyUsages.chunked(7).takeLast(8)
                val peakVal = chunks.maxOfOrNull { chunk -> chunk.fold(BigDecimal.ZERO) { a, b -> a + b.units } } ?: BigDecimal.ZERO
                chunks.mapIndexed { index, chunk ->
                    val units = chunk.fold(BigDecimal.ZERO) { a, b -> a + b.units }
                    val cost = calculateBillUseCase(units, tariff).totalBill
                    val label = "W${index + 1}"
                    val firstDate = chunk.first().date
                    val lastDate = chunk.last().date
                    val fullDate = "${firstDate.dayOfMonth} ${firstDate.month.name.take(3)} - ${lastDate.dayOfMonth} ${lastDate.month.name.take(3)}"
                    ChartBarData(
                        label = label,
                        units = units,
                        cost = cost,
                        isPeak = units == peakVal && units > BigDecimal.ZERO,
                        fullDate = fullDate
                    )
                }
            }
            AnalyticsPeriod.MONTH -> {
                val groups = dailyUsages.groupBy { "${it.date.year}-${it.date.monthNumber}" }
                val peakVal = groups.values.maxOfOrNull { list -> list.fold(BigDecimal.ZERO) { a, b -> a + b.units } } ?: BigDecimal.ZERO
                groups.entries.toList().takeLast(6).map { entry ->
                    val units = entry.value.fold(BigDecimal.ZERO) { a, b -> a + b.units }
                    val cost = calculateBillUseCase(units, tariff).totalBill
                    val firstDate = entry.value.first().date
                    val label = firstDate.month.name.take(3)
                    ChartBarData(
                        label = label,
                        units = units,
                        cost = cost,
                        isPeak = units == peakVal && units > BigDecimal.ZERO,
                        fullDate = "$label ${firstDate.year}"
                    )
                }
            }
            AnalyticsPeriod.YEAR -> {
                val groups = dailyUsages.groupBy { it.date.year }
                val peakVal = groups.values.maxOfOrNull { list -> list.fold(BigDecimal.ZERO) { a, b -> a + b.units } } ?: BigDecimal.ZERO
                groups.entries.toList().takeLast(4).map { entry ->
                    val units = entry.value.fold(BigDecimal.ZERO) { a, b -> a + b.units }
                    val cost = calculateBillUseCase(units, tariff).totalBill
                    val label = "${entry.key}"
                    ChartBarData(
                        label = label,
                        units = units,
                        cost = cost,
                        isPeak = units == peakVal && units > BigDecimal.ZERO,
                        fullDate = label
                    )
                }
            }
        }
    }

    private fun computeComparison(bars: List<ChartBarData>): PeriodComparison? {
        if (bars.size < 2) return null
        val current = bars.last().units
        val previous = bars[bars.size - 2].units
        return compareWithPreviousUseCase(current, previous)
    }

    private fun computeWeekdayPattern(dailyUsages: List<DailyUsage>): List<WeekdayIntensity> {
        val days = DayOfWeek.entries
        val usageByDay = days.associateWith { day ->
            dailyUsages.filter { it.date.dayOfWeek == day }
        }

        val averages = days.map { day ->
            val list = usageByDay[day] ?: emptyList()
            val avg = if (list.isNotEmpty()) {
                val sum = list.fold(BigDecimal.ZERO) { a, b -> a + b.units }
                sum.divide(BigDecimal(list.size), 1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            Pair(day, avg)
        }

        val maxAvg = averages.maxOfOrNull { it.second } ?: BigDecimal.ZERO
        return averages.map { (day, avg) ->
            val intensity = if (maxAvg > BigDecimal.ZERO) {
                avg.divide(maxAvg, 2, RoundingMode.HALF_UP).toFloat().coerceIn(0.1f, 1.0f)
            } else {
                0.1f
            }
            WeekdayIntensity(
                dayOfWeek = day,
                averageUnits = avg,
                intensity = intensity
            )
        }
    }

    private fun computeSlabDonut(bill: com.powerunit.domain.model.BillBreakdown): List<SlabDonutItem> {
        val totalCost = bill.energyCharge
        return bill.slabCharges.mapIndexed { index, slab ->
            val pct = if (totalCost > BigDecimal.ZERO) {
                slab.amount.divide(totalCost, 4, RoundingMode.HALF_UP).toFloat() * 100f
            } else {
                0f
            }
            val toStr = slab.toUnit?.let { "${it.toInt()} kWh" } ?: "+"
            val name = "Slab ${slab.slabIndex} (${slab.fromUnit.toInt()}–$toStr)"
            SlabDonutItem(
                slabName = name,
                units = slab.unitsBilled,
                cost = slab.amount,
                percentageOfCost = pct,
                colorIndex = index
            )
        }
    }
}
