package com.bangla.electricitybill.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.usecase.DashboardState
import com.bangla.electricitybill.domain.usecase.GetDashboardDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import javax.inject.Inject

data class RecentReadingUiModel(
    val reading: MeterReading,
    val dateLabel: String,
    val readingValue: String,
    val unitsDelta: String?
)

data class DashboardScreenUiState(
    val dashboardData: DashboardState? = null,
    val selectedMonth: YearMonth = YearMonth.from(DateUtils.now()),
    val monthConsumption: BigDecimal = BigDecimal.ZERO,
    val monthEstimatedBill: BigDecimal = BigDecimal.ZERO,
    val daysTracked: Int = 0,
    val unitsPerDay: BigDecimal = BigDecimal.ZERO,
    val costPerDay: BigDecimal = BigDecimal.ZERO,
    val monthlyItemizedBill: ItemizedBill? = null,
    val recentReadingsList: List<RecentReadingUiModel> = emptyList(),
    val isBangla: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.from(DateUtils.now()))

    val uiState: StateFlow<DashboardScreenUiState> = combine(
        getDashboardDataUseCase.getDashboardDataFlow(),
        preferencesManager.appLanguage,
        _selectedMonth
    ) { data, lang, selMonth ->
        val meter = data.activeMeter
        val tariff = data.activeTariff
        val allReadings = data.recentReadings // Note: recentReadings in DashboardState has up to latest or all sorted
        val isBn = lang == "bn"

        // Filter readings for the selected month
        val monthReadings = allReadings.filter { YearMonth.from(it.readingDateTime) == selMonth }
            .sortedBy { it.readingDateTime }

        val totalUnits: BigDecimal
        val daysTracked: Int

        if (monthReadings.size >= 2) {
            val first = monthReadings.first()
            val last = monthReadings.last()
            val isRollover = monthReadings.any { it.readingSource == ReadingSource.ROLLOVER } ||
                last.normalizedReading < first.normalizedReading
            val deltaRes = ConsumptionCalculator.calculateConsumption(
                previousReading = first.normalizedReading,
                currentReading = last.normalizedReading,
                isRollover = isRollover,
                blackDigitCapacity = meter?.blackDigitCapacity ?: 5
            )
            totalUnits = deltaRes.getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
            daysTracked = monthReadings.map { it.readingDateTime.toLocalDate() }.distinct().size
        } else if (monthReadings.size == 1 && data.previousReading != null &&
            YearMonth.from(data.previousReading.readingDateTime) == selMonth) {
            val deltaRes = ConsumptionCalculator.calculateConsumption(
                previousReading = data.previousReading.normalizedReading,
                currentReading = monthReadings.first().normalizedReading,
                blackDigitCapacity = meter?.blackDigitCapacity ?: 5
            )
            totalUnits = deltaRes.getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
            daysTracked = 1
        } else if (selMonth == YearMonth.from(DateUtils.now()) && data.forecast != null) {
            totalUnits = data.forecast.currentConsumption
            daysTracked = (data.forecast.elapsedDays).toInt().coerceAtLeast(1)
        } else {
            totalUnits = BigDecimal.ZERO
            daysTracked = monthReadings.map { it.readingDateTime.toLocalDate() }.distinct().size
        }

        // Calculate itemized bill
        val itemized = if (tariff != null && totalUnits > BigDecimal.ZERO) {
            ProgressiveTariffEngine.calculateMonthlyBill(
                monthlyConsumption = totalUnits,
                tariff = tariff
            )
        } else null

        val totalBill = itemized?.totalBill ?: BigDecimal.ZERO

        val unitsPerDay = if (daysTracked > 0) {
            totalUnits.divide(BigDecimal(daysTracked), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        val costPerDay = if (daysTracked > 0) {
            totalBill.divide(BigDecimal(daysTracked), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        // Build recent readings with deltas
        val sortedDesc = allReadings.sortedByDescending { it.readingDateTime }
        val recentUiList = mutableListOf<RecentReadingUiModel>()
        val today = DateUtils.today()

        for (i in 0 until sortedDesc.size.coerceAtMost(6)) {
            val curr = sortedDesc[i]
            val prev = if (i + 1 < sortedDesc.size) sortedDesc[i + 1] else null

            val dateLabel = when (curr.readingDateTime.toLocalDate()) {
                today -> "Today, ${DateUtils.formatDate(curr.readingDateTime)}"
                today.minusDays(1) -> "Yesterday"
                else -> DateUtils.formatDate(curr.readingDateTime)
            }

            val readingFormatted = "${curr.blackDigits}.${curr.redDigit} kWh"

            val deltaStr = if (prev != null) {
                val isRollover = curr.readingSource == ReadingSource.ROLLOVER || curr.normalizedReading < prev.normalizedReading
                val res = ConsumptionCalculator.calculateConsumption(
                    previousReading = prev.normalizedReading,
                    currentReading = curr.normalizedReading,
                    isRollover = isRollover,
                    blackDigitCapacity = meter?.blackDigitCapacity ?: 5
                )
                val d = res.getOrDefault(BigDecimal.ZERO)
                if (d > BigDecimal.ZERO) d.setScale(1, RoundingMode.HALF_UP).toPlainString() else null
            } else null

            recentUiList.add(
                RecentReadingUiModel(
                    reading = curr,
                    dateLabel = dateLabel,
                    readingValue = readingFormatted,
                    unitsDelta = deltaStr
                )
            )
        }

        DashboardScreenUiState(
            dashboardData = data,
            selectedMonth = selMonth,
            monthConsumption = totalUnits,
            monthEstimatedBill = totalBill,
            daysTracked = daysTracked,
            unitsPerDay = unitsPerDay,
            costPerDay = costPerDay,
            monthlyItemizedBill = itemized,
            recentReadingsList = recentUiList,
            isBangla = isBn,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardScreenUiState()
    )

    fun selectMonth(month: YearMonth) {
        _selectedMonth.update { month }
    }
}
