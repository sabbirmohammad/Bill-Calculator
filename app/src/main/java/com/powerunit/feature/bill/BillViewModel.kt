package com.powerunit.feature.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.export.ExportManager
import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class BillUiState(
    val isLoading: Boolean = true,
    val currentTariff: Tariff? = null,
    val actualCycleUsage: CycleUsage? = null,
    val actualBill: BillBreakdown? = null,
    val simulatedUnits: BigDecimal = BigDecimal("150.0"),
    val simulatedBill: BillBreakdown? = null,
    val billDeltaVsActual: BigDecimal? = null,
    val isPastCyclePaid: Boolean = false,
    val useBengaliDigits: Boolean = false,
    val isBangla: Boolean = false,
    val hideAmounts: Boolean = false
)

@HiltViewModel
class BillViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository,
    private val tariffRepository: TariffRepository,
    private val settingsPreferences: SettingsPreferences,
    private val calculateDailyUsageUseCase: CalculateDailyUsageUseCase,
    private val calculateCycleUsageUseCase: CalculateCycleUsageUseCase,
    private val calculateBillUseCase: CalculateBillUseCase,
    private val exportManager: ExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState.asStateFlow()

    init {
        loadBillData()
    }

    fun exportPdfStatement(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val bill = _uiState.value.actualBill ?: return@launch
            val cycle = _uiState.value.actualCycleUsage
            val file = exportManager.exportBillToPdf(bill, meter, cycle)
            val intent = exportManager.getShareIntent(file, "application/pdf")
            onReady(intent)
        }
    }

    private fun loadBillData() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val meterId = meter?.id ?: 1L

            val prefs = combine(
                settingsPreferences.billingCycleDay,
                settingsPreferences.useBengaliDigits,
                settingsPreferences.appLanguage,
                settingsPreferences.hideAmounts
            ) { cycleDay, useBnDigits, lang, hideAmounts ->
                listOf(cycleDay, useBnDigits, lang == "bn", hideAmounts)
            }

            combine(
                readingRepository.getReadings(meterId),
                tariffRepository.getActiveTariff(),
                prefs
            ) { readings, tariff, prefsList ->
                val cycleDay = prefsList[0] as Int
                val useBnDigits = prefsList[1] as Boolean
                val isBangla = prefsList[2] as Boolean
                val hideAmounts = prefsList[3] as Boolean
                val now = DateUtils.currentInstant()
                val today = DateUtils.currentDhakaDate()
                val sorted = readings.sortedBy { it.recordedAt }
                val dailyUsages = calculateDailyUsageUseCase(sorted)

                val activeTariff = tariff ?: Tariff(
                    name = "Default",
                    effectiveFrom = now,
                    vatPercent = BigDecimal("5.0"),
                    serviceChargeFlat = BigDecimal("10.00"),
                    demandChargeFlat = BigDecimal("35.00"),
                    isActive = true
                )

                val cycleUsage = calculateCycleUsageUseCase(dailyUsages, cycleDay, today)
                val actualBill = calculateBillUseCase(cycleUsage.totalUnitsSoFar, activeTariff)
                val initialSimUnits = if (cycleUsage.totalUnitsSoFar > BigDecimal.ZERO) {
                    cycleUsage.totalUnitsSoFar
                } else {
                    BigDecimal("100.0")
                }
                val simulatedBill = calculateBillUseCase(initialSimUnits, activeTariff)

                BillUiState(
                    isLoading = false,
                    currentTariff = activeTariff,
                    actualCycleUsage = cycleUsage,
                    actualBill = actualBill,
                    simulatedUnits = initialSimUnits,
                    simulatedBill = simulatedBill,
                    billDeltaVsActual = BigDecimal.ZERO,
                    useBengaliDigits = useBnDigits,
                    isBangla = isBangla,
                    hideAmounts = hideAmounts
                )
            }.collect { newState ->
                _uiState.update { current ->
                    if (current.isLoading) newState
                    else current.copy(
                        currentTariff = newState.currentTariff,
                        actualCycleUsage = newState.actualCycleUsage,
                        actualBill = newState.actualBill,
                        useBengaliDigits = newState.useBengaliDigits,
                        isBangla = newState.isBangla,
                        hideAmounts = newState.hideAmounts
                    )
                }
            }
        }
    }

    fun onSimulatedUnitsChange(newUnits: BigDecimal) {
        val rounded = newUnits.setScale(0, RoundingMode.HALF_UP)
        val tariff = _uiState.value.currentTariff ?: return
        val newSimulatedBill = calculateBillUseCase(rounded, tariff)
        val actualBill = _uiState.value.actualBill

        val delta = if (actualBill != null) {
            newSimulatedBill.totalBill.subtract(actualBill.totalBill)
        } else {
            BigDecimal.ZERO
        }

        _uiState.update {
            it.copy(
                simulatedUnits = rounded,
                simulatedBill = newSimulatedBill,
                billDeltaVsActual = delta
            )
        }
    }

    fun togglePaidStatus() {
        _uiState.update { it.copy(isPastCyclePaid = !it.isPastCyclePaid) }
    }
}
