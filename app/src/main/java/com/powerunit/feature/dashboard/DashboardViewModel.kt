package com.powerunit.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.entity.BudgetEntity
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MonthEndForecast
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.BudgetRepository
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import com.powerunit.domain.usecase.ForecastMonthEndUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class BudgetUiModel(
    val monthlyLimitBDT: BigDecimal,
    val currentSpentBDT: BigDecimal,
    val percentageUsed: Float,
    val isOverBudget: Boolean
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeMeter: Meter? = null,
    val greeting: String = "Welcome",
    val hasRecordedToday: Boolean = false,
    val cycleUsage: CycleUsage? = null,
    val billBreakdown: BillBreakdown? = null,
    val todayUsage: BigDecimal = BigDecimal.ZERO,
    val yesterdayUsage: BigDecimal = BigDecimal.ZERO,
    val usageTrendPercent: BigDecimal? = null,
    val dailyAverage: BigDecimal = BigDecimal.ZERO,
    val forecast: MonthEndForecast? = null,
    val budget: BudgetUiModel? = null,
    val past14DaysUsage: List<DailyUsage> = emptyList(),
    val recentReadings: List<Reading> = emptyList(),
    val hideAmounts: Boolean = false,
    val hasInitialReading: Boolean = false,
    val showInitialReadingDialog: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository,
    private val tariffRepository: TariffRepository,
    private val budgetRepository: BudgetRepository,
    private val settingsPreferences: SettingsPreferences,
    private val calculateDailyUsageUseCase: CalculateDailyUsageUseCase,
    private val calculateCycleUsageUseCase: CalculateCycleUsageUseCase,
    private val calculateBillUseCase: CalculateBillUseCase,
    private val forecastMonthEndUseCase: ForecastMonthEndUseCase
) : ViewModel() {

    private var userDismissedInitialDialog: Boolean = false
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val meterId = meter?.id ?: 1L

            combine(
                readingRepository.getReadings(meterId),
                tariffRepository.getActiveTariff(),
                settingsPreferences.billingCycleDay,
                settingsPreferences.hideAmounts,
                budgetRepository.getLatestBudget()
            ) { readings, tariff, cycleDay, hideAmounts, budget ->
                computeState(readings, tariff, cycleDay, hideAmounts, meter, budget)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun computeState(
        readings: List<Reading>,
        tariff: Tariff?,
        cycleDay: Int,
        hideAmounts: Boolean,
        meter: Meter?,
        budgetEntity: BudgetEntity? = null
    ): DashboardUiState {
        val now = DateUtils.currentInstant()
        val todayDate = DateUtils.currentDhakaDate()
        val sortedReadings = readings.sortedBy { it.recordedAt }
        val dailyUsages = calculateDailyUsageUseCase(sortedReadings)

        val activeTariff = tariff ?: Tariff(
            name = "Default",
            effectiveFrom = now,
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true
        )

        val cycleUsage = calculateCycleUsageUseCase(
            dailyUsages = dailyUsages,
            cycleStartDay = cycleDay,
            today = todayDate
        )

        val billBreakdown = calculateBillUseCase(
            units = cycleUsage.totalUnitsSoFar,
            tariff = activeTariff
        )

        val forecast = forecastMonthEndUseCase(
            dailyUsages = dailyUsages,
            cycleUsage = cycleUsage,
            tariff = activeTariff
        )

        val (todayUsage, yesterdayUsage, trend) = extractRecentDayMetrics(dailyUsages, todayDate)
        val hasRecordedToday = checkHasRecordedToday(readings, todayDate)

        val past14Days = dailyUsages.takeLast(14)
        val dailyAvg = if (dailyUsages.isNotEmpty()) {
            val sum = dailyUsages.fold(BigDecimal.ZERO) { acc, d -> acc + d.units }
            sum.divide(BigDecimal(dailyUsages.size), 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val budgetModel = computeBudget(billBreakdown.totalBill, budgetEntity)
        val hasInitialReading = sortedReadings.isNotEmpty()
        val showInitialDialog = !hasInitialReading && !userDismissedInitialDialog

        return DashboardUiState(
            isLoading = false,
            activeMeter = meter,
            greeting = computeGreeting(now),
            hasRecordedToday = hasRecordedToday,
            cycleUsage = cycleUsage,
            billBreakdown = billBreakdown,
            todayUsage = todayUsage,
            yesterdayUsage = yesterdayUsage,
            usageTrendPercent = trend,
            dailyAverage = dailyAvg,
            forecast = forecast,
            budget = budgetModel,
            past14DaysUsage = past14Days,
            recentReadings = sortedReadings.takeLast(3).reversed(),
            hideAmounts = hideAmounts,
            hasInitialReading = hasInitialReading,
            showInitialReadingDialog = showInitialDialog
        )
    }

    fun onDismissInitialReadingDialog() {
        userDismissedInitialDialog = true
        _uiState.update { it.copy(showInitialReadingDialog = false) }
    }

    fun onShowInitialReadingDialog() {
        userDismissedInitialDialog = false
        _uiState.update { it.copy(showInitialReadingDialog = true) }
    }

    fun saveInitialReading(
        readingValue: BigDecimal,
        recordedAt: Instant,
        note: String = "Initial Reading"
    ) {
        viewModelScope.launch {
            val activeMeter = meterRepository.getActiveMeterSync()
            val meterId = activeMeter?.id ?: 1L

            if (activeMeter != null) {
                meterRepository.updateMeter(activeMeter.copy(startReading = readingValue))
            }

            val reading = Reading(
                id = 0,
                meterId = meterId,
                readingValue = readingValue.setScale(1, RoundingMode.HALF_UP),
                recordedAt = recordedAt,
                note = note.ifBlank { "Initial Reading" },
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.currentInstant(),
                updatedAt = DateUtils.currentInstant()
            )
            readingRepository.saveReading(reading)

            settingsPreferences.setOnboardingCompleted(true)
            userDismissedInitialDialog = false
            _uiState.update { it.copy(showInitialReadingDialog = false) }
        }
    }

    private fun extractRecentDayMetrics(
        dailyUsages: List<DailyUsage>,
        todayDate: LocalDate
    ): Triple<BigDecimal, BigDecimal, BigDecimal?> {
        val yesterdayDate = LocalDate.fromEpochDays(todayDate.toEpochDays() - 1)

        val todayUsage = dailyUsages.find { it.date == todayDate }?.units ?: BigDecimal.ZERO
        val yesterdayUsage = dailyUsages.find { it.date == yesterdayDate }?.units ?: BigDecimal.ZERO

        val trend = if (yesterdayUsage > BigDecimal.ZERO) {
            todayUsage.subtract(yesterdayUsage)
                .divide(yesterdayUsage, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100.0"))
        } else {
            null
        }

        return Triple(todayUsage, yesterdayUsage, trend)
    }

    private fun checkHasRecordedToday(readings: List<Reading>, todayDate: LocalDate): Boolean {
        val todayStr = DateUtils.formatDate(todayDate, "yyyy-MM-dd")
        return readings.any { DateUtils.formatDate(it.recordedAt, "yyyy-MM-dd") == todayStr }
    }

    private fun computeBudget(currentBill: BigDecimal, budgetEntity: BudgetEntity?): BudgetUiModel {
        val limit = budgetEntity?.amountLimit ?: BigDecimal("2500.00")
        val ratio = if (limit > BigDecimal.ZERO) {
            currentBill.divide(limit, 2, RoundingMode.HALF_UP).toFloat()
        } else {
            0f
        }
        return BudgetUiModel(
            monthlyLimitBDT = limit,
            currentSpentBDT = currentBill,
            percentageUsed = ratio.coerceIn(0f, 1f),
            isOverBudget = currentBill > limit
        )
    }

    private fun computeGreeting(now: Instant): String {
        val hour = DateUtils.formatDate(now, "H").toIntOrNull() ?: 12
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
