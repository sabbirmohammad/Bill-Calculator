package com.powerunit.feature.addreading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.ReadingValidationResult
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.ValidateReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class AddReadingUiState(
    val readingId: Long? = null,
    val meterId: Long = 0,
    val readingInput: String = "",
    val previousReadingValue: BigDecimal? = null,
    val previousReadingDate: String? = null,
    val recordedAt: kotlinx.datetime.Instant = DateUtils.currentInstant(),
    val isMeterReset: Boolean = false,
    val isInitialReading: Boolean = false,
    val note: String = "",
    val calculatedDelta: BigDecimal? = null,
    val validationResult: ReadingValidationResult = ReadingValidationResult.Valid,
    val showConfirmDialog: Boolean = false,
    val confirmDialogMessage: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val readingRepository: ReadingRepository,
    private val meterRepository: MeterRepository,
    private val validateReadingUseCase: ValidateReadingUseCase,
    private val settingsPreferences: SettingsPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editReadingId: Long? = savedStateHandle.get<Long>("readingId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(AddReadingUiState(readingId = editReadingId))
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val activeMeter = meterRepository.getActiveMeterSync()
            val meterId = activeMeter?.id ?: 1L
            _uiState.update { it.copy(meterId = meterId) }

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
            } else {
                val latest = readingRepository.getLatestReadingSync(meterId)
                val isInitial = (latest == null)
                val prevVal = latest?.readingValue
                val prevDate = latest?.recordedAt?.let { DateUtils.formatDate(it) }

                _uiState.update {
                    it.copy(
                        isInitialReading = isInitial,
                        previousReadingValue = prevVal,
                        previousReadingDate = prevDate
                    )
                }
            }
        }
    }

    fun onReadingInputChange(input: String) {
        _uiState.update { current ->
            val delta = computeDelta(input, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            val validation = validateInput(input, current.recordedAt, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            current.copy(
                readingInput = input,
                calculatedDelta = delta,
                validationResult = validation
            )
        }
    }

    fun onDateChange(newInstant: kotlinx.datetime.Instant) {
        _uiState.update { current ->
            val delta = computeDelta(current.readingInput, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            val validation = validateInput(current.readingInput, newInstant, current.previousReadingValue, current.isMeterReset, current.isInitialReading)
            current.copy(
                recordedAt = newInstant,
                calculatedDelta = delta,
                validationResult = validation
            )
        }
    }

    fun onMeterResetChange(reset: Boolean) {
        _uiState.update { current ->
            val delta = computeDelta(current.readingInput, current.previousReadingValue, reset, current.isInitialReading)
            val validation = validateInput(current.readingInput, current.recordedAt, current.previousReadingValue, reset, current.isInitialReading)
            current.copy(
                isMeterReset = reset,
                calculatedDelta = delta,
                validationResult = validation
            )
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
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

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
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
