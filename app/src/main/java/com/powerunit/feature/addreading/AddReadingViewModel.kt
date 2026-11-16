package com.powerunit.feature.addreading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.DEFAULT_BANGLADESH_TARIFF
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.ReadingValidationResult
import com.powerunit.domain.model.SlabInfo
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import com.powerunit.domain.usecase.ValidateReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class AddReadingUiState(
    val readingId: Long? = null,
    val meterId: Long = 0,
    val meterName: String = "Home Meter",
    val readingInput: String = "",
    val previousReadingValue: BigDecimal? = null,
    val previousReadingDate: String? = null,
    val recordedAt: kotlinx.datetime.Instant = DateUtils.currentInstant(),
    val isMeterReset: Boolean = false,
    val isInitialReading: Boolean = false,
    val note: String = "",
    val calculatedDelta: BigDecimal? = null,
    val estimatedDeltaCost: BigDecimal? = null,
    val slabNumber: Int? = null,
    val slabLabel: String? = null,
    val slabRate: BigDecimal? = null,
    val cumulativeMonthUnits: BigDecimal? = null,
    val validationResult: ReadingValidationResult = ReadingValidationResult.Valid,
    val showConfirmDialog: Boolean = false,
    val confirmDialogMessage: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showSuccessSheet: Boolean = false
)

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val readingRepository: ReadingRepository,
    private val meterRepository: MeterRepository,
    private val validateReadingUseCase: ValidateReadingUseCase,
    private val settingsPreferences: SettingsPreferences,
    private val tariffRepository: TariffRepository,
    private val calculateBillUseCase: CalculateBillUseCase,
    private val calculateCycleUsageUseCase: CalculateCycleUsageUseCase = CalculateCycleUsageUseCase(),
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editReadingId: Long? = savedStateHandle.get<Long>("readingId")?.takeIf { it > 0 }

    private var cachedTariff: Tariff? = null
    private var monthUnitsSoFar: BigDecimal = BigDecimal.ZERO
    private var isBangla: Boolean = false
    private var useBengaliDigits: Boolean = false

    private val _uiState = MutableStateFlow(AddReadingUiState(readingId = editReadingId))
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val activeMeter = meterRepository.getActiveMeterSync()
            val meterId = activeMeter?.id ?: 1L
            val meterName = activeMeter?.name ?: "Home Meter"
            cachedTariff = tariffRepository.getActiveTariffSync() ?: DEFAULT_BANGLADESH_TARIFF
            if (cachedTariff?.slabs.isNullOrEmpty()) {
                cachedTariff = DEFAULT_BANGLADESH_TARIFF
            }
            try {
                isBangla = settingsPreferences.appLanguage.first().equals("bn", ignoreCase = true) ||
                    settingsPreferences.useBengaliDigits.first()
                useBengaliDigits = settingsPreferences.useBengaliDigits.first()
            } catch (_: Exception) {}

            _uiState.update { it.copy(meterId = meterId, meterName = meterName) }

            if (editReadingId != null) {
                val existing = readingRepository.getReadingById(editReadingId)
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            readingInput = existing.readingValue.toPlainString(),
                            recordedAt = existing.recordedAt,
                            isMeterReset = existing.isMeterReset,
                            note = existing.note ?: "",
                            isInitialReading = false
                        )
                    }
                }
            }

            updateContextForDate(_uiState.value.recordedAt, meterId)
        }
    }

    private suspend fun updateContextForDate(targetInstant: kotlinx.datetime.Instant, meterId: Long) {
        val allReadings = try {
            readingRepository.getReadings(meterId).first()
        } catch (_: Exception) {
            emptyList()
        }
        val readings = if (editReadingId != null) {
            allReadings.filter { it.id != editReadingId }
        } else {
            allReadings
        }
        val sortedAsc = readings.sortedBy { it.recordedAt }

        // Find the immediate predecessor to targetInstant
        val prevReading = sortedAsc.filter { it.recordedAt <= targetInstant }.maxByOrNull { it.recordedAt }
        val fallbackLatest = if (sortedAsc.isEmpty()) {
            try { readingRepository.getLatestReadingSync(meterId) } catch (_: Exception) { null }
        } else null
        val effectivePrev = prevReading ?: fallbackLatest
        val isInitial = (effectivePrev == null && sortedAsc.isEmpty() && editReadingId == null)
        val prevVal = effectivePrev?.readingValue
        val prevDate = effectivePrev?.recordedAt?.let { DateUtils.formatDate(it) }

        // Determine billing cycle range for targetInstant
        val cycleStartDay = try {
            settingsPreferences.billingCycleDay.first()
        } catch (_: Exception) {
            1
        }
        val ldt = targetInstant.toLocalDateTime(DateUtils.DHAKA_ZONE)
        val (cycleStartDate, cycleEndDate) = calculateCycleUsageUseCase.computeCycleRange(cycleStartDay, ldt.date)

        // Sum units consumed in the cycle strictly before targetInstant
        var cycleUnits = BigDecimal.ZERO
        for (i in sortedAsc.indices) {
            val r = sortedAsc[i]
            val prev = if (i > 0) sortedAsc[i - 1] else null
            val delta = if (r.isMeterReset) {
                r.readingValue
            } else if (prev != null) {
                (r.readingValue - prev.readingValue).coerceAtLeast(BigDecimal.ZERO)
            } else {
                BigDecimal.ZERO
            }
            val readingDate = r.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).date
            if (readingDate in cycleStartDate..cycleEndDate && r.recordedAt < targetInstant) {
                cycleUnits += delta
            }
        }
        monthUnitsSoFar = cycleUnits

        _uiState.update { current ->
            val delta = computeDelta(current.readingInput, prevVal, current.isMeterReset, isInitial)
            val validation = validateInput(current.readingInput, targetInstant, prevVal, current.isMeterReset, isInitial)
            val (cost, slabInfo) = computeEstimatedCostAndSlab(delta)
            val slabLabel = slabInfo?.let { Formatters.formatSlabLabel(it.slabNumber, isBangla, useBengaliDigits) }
            val cumulativeUnits = if (delta != null && delta > BigDecimal.ZERO) {
                monthUnitsSoFar + delta
            } else if (monthUnitsSoFar > BigDecimal.ZERO) {
                monthUnitsSoFar
            } else null

            current.copy(
                isInitialReading = isInitial,
                previousReadingValue = prevVal,
                previousReadingDate = prevDate,
                calculatedDelta = delta,
                estimatedDeltaCost = cost,
                slabNumber = slabInfo?.slabNumber,
                slabLabel = slabLabel,
                slabRate = slabInfo?.ratePerUnit,
                cumulativeMonthUnits = cumulativeUnits,
                validationResult = validation
            )
        }
    }

    fun onReadingInputChange(input: String) {
        _uiState.update { current ->
            val delta = computeDelta(input, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            val validation = validateInput(input, current.recordedAt, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            val (cost, slabInfo) = computeEstimatedCostAndSlab(delta)
            val slabLabel = slabInfo?.let { Formatters.formatSlabLabel(it.slabNumber, isBangla, useBengaliDigits) }
            val cumulativeUnits = if (delta != null && delta > BigDecimal.ZERO) {
                monthUnitsSoFar + delta
            } else if (monthUnitsSoFar > BigDecimal.ZERO) {
                monthUnitsSoFar
            } else null

            current.copy(
                readingInput = input,
                calculatedDelta = delta,
                estimatedDeltaCost = cost,
                slabNumber = slabInfo?.slabNumber,
                slabLabel = slabLabel,
                slabRate = slabInfo?.ratePerUnit,
                cumulativeMonthUnits = cumulativeUnits,
                validationResult = validation
            )
        }
    }

    fun onDateChange(newInstant: kotlinx.datetime.Instant) {
        _uiState.update { it.copy(recordedAt = newInstant) }
        viewModelScope.launch {
            val meterId = _uiState.value.meterId
            updateContextForDate(newInstant, meterId)
        }
    }

    fun onDatePreset(isToday: Boolean) {
        val now = DateUtils.currentInstant()
        val newInstant = if (isToday) {
            now
        } else {
            val localDate = DateUtils.toLocalDate(now)
            val javaDate = java.time.LocalDate.of(localDate.year, localDate.monthNumber, localDate.dayOfMonth).minusDays(1)
            val yesterdayDate = kotlinx.datetime.LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
            DateUtils.combineDateWithTime(yesterdayDate, now)
        }
        onDateChange(newInstant)
    }

    fun onMeterResetChange(reset: Boolean) {
        _uiState.update { current ->
            val delta = computeDelta(current.readingInput, current.previousReadingValue, reset, current.isInitialReading)
            val validation = validateInput(current.readingInput, current.recordedAt, current.previousReadingValue, reset, current.isInitialReading)
            val (cost, slabInfo) = computeEstimatedCostAndSlab(delta)
            val slabLabel = slabInfo?.let { Formatters.formatSlabLabel(it.slabNumber, isBangla, useBengaliDigits) }
            val cumulativeUnits = if (delta != null && delta > BigDecimal.ZERO) {
                monthUnitsSoFar + delta
            } else if (monthUnitsSoFar > BigDecimal.ZERO) {
                monthUnitsSoFar
            } else null

            current.copy(
                isMeterReset = reset,
                calculatedDelta = delta,
                estimatedDeltaCost = cost,
                slabNumber = slabInfo?.slabNumber,
                slabLabel = slabLabel,
                slabRate = slabInfo?.ratePerUnit,
                cumulativeMonthUnits = cumulativeUnits,
                validationResult = validation
            )
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onQuickTagSelect(tag: String) {
        _uiState.update { current ->
            val newNote = if (current.note.contains(tag)) {
                current.note.replace(tag, "").replace(" ,", ",").replace(", ,", ",").trim().trim(',', ' ')
            } else if (current.note.isBlank()) {
                tag
            } else {
                "${current.note.trim()}, $tag"
            }
            current.copy(note = newNote)
        }
    }

    fun onDismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun dismissSuccessSheet() {
        _uiState.update { it.copy(showSuccessSheet = false) }
    }

    private fun computeEstimatedCostAndSlab(delta: BigDecimal?): Pair<BigDecimal?, SlabInfo?> {
        val tariff = cachedTariff ?: DEFAULT_BANGLADESH_TARIFF
        if (delta == null || delta <= BigDecimal.ZERO) {
            val currentSlab = calculateBillUseCase.getSlabForUnits(monthUnitsSoFar, tariff)
            return Pair(null, currentSlab)
        }
        return try {
            val costBefore = calculateBillUseCase.calculateEnergyCharge(monthUnitsSoFar, tariff)
            val costAfter = calculateBillUseCase.calculateEnergyCharge(monthUnitsSoFar + delta, tariff)
            val incrementalCost = (costAfter - costBefore).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
            val slabAfter = calculateBillUseCase.getSlabForUnits(monthUnitsSoFar + delta, tariff)
            Pair(incrementalCost, slabAfter)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    private fun computeEstimatedCost(delta: BigDecimal?): BigDecimal? {
        return computeEstimatedCostAndSlab(delta).first
    }

    fun saveReading(forceSave: Boolean = false) {
        val state = _uiState.value
        val parsed = state.readingInput.toBigDecimalOrNull() ?: return

        val validation = validateInput(
            state.readingInput,
            state.recordedAt,
            state.previousReadingValue,
            state.isMeterReset,
            state.isInitialReading
        )

        if (!forceSave) {
            when (validation) {
                is ReadingValidationResult.LowerThanPreviousWarning -> {
                    _uiState.update {
                        it.copy(
                            showConfirmDialog = true,
                            confirmDialogMessage = validation.message
                        )
                    }
                    return
                }
                is ReadingValidationResult.HighConsumptionWarning -> {
                    _uiState.update {
                        it.copy(
                            showConfirmDialog = true,
                            confirmDialogMessage = validation.message
                        )
                    }
                    return
                }
                is ReadingValidationResult.FutureDateError -> {
                    return // Blocked
                }
                ReadingValidationResult.Valid -> {
                    // Continue to save
                }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showConfirmDialog = false) }

            val activeMeter = meterRepository.getActiveMeterSync()
            val effectiveMeterId = if (state.meterId > 0) state.meterId else (activeMeter?.id ?: 1L)

            val reading = Reading(
                id = state.readingId ?: 0,
                meterId = effectiveMeterId,
                readingValue = parsed.setScale(1, RoundingMode.HALF_UP),
                recordedAt = state.recordedAt,
                note = state.note.ifBlank { if (state.isInitialReading) "Initial Reading" else null },
                isEstimated = false,
                isMeterReset = state.isMeterReset,
                createdAt = DateUtils.currentInstant(),
                updatedAt = DateUtils.currentInstant()
            )

            if (state.readingId != null) {
                readingRepository.updateReading(reading)
            } else {
                readingRepository.saveReading(reading)
                if (state.isInitialReading) {
                    if (activeMeter != null) {
                        meterRepository.updateMeter(activeMeter.copy(startReading = parsed))
                    }
                    settingsPreferences.setOnboardingCompleted(true)
                }
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true, showSuccessSheet = true) }
        }
    }

    private fun computeDelta(
        input: String,
        previous: BigDecimal?,
        isReset: Boolean,
        isInitial: Boolean = false
    ): BigDecimal? {
        if (isInitial || previous == null) return null
        val currentVal = input.toBigDecimalOrNull() ?: return null
        return if (isReset) {
            currentVal
        } else {
            (currentVal - previous).setScale(1, RoundingMode.HALF_UP)
        }
    }

    private fun validateInput(
        input: String,
        recordedAt: kotlinx.datetime.Instant,
        previous: BigDecimal?,
        isReset: Boolean,
        isInitial: Boolean = false
    ): ReadingValidationResult {
        val currentVal = input.toBigDecimalOrNull() ?: return ReadingValidationResult.Valid
        return validateReadingUseCase(
            newReadingValue = currentVal,
            recordedAt = recordedAt,
            currentInstant = DateUtils.currentInstant(),
            previousReadingValue = if (isInitial) null else previous,
            isMeterReset = isReset,
            average30DayUnits = if (isInitial) null else BigDecimal("10.0")
        )
    }
}
