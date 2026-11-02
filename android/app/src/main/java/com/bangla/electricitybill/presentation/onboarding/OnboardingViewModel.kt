package com.bangla.electricitybill.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.domain.engine.MeterReadingParser
import com.bangla.electricitybill.domain.model.AdditionalCharge
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterEventType
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import com.bangla.electricitybill.domain.model.TariffSlab
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.domain.usecase.HistoricalRecalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class EditableSlabState(
    val min: String,
    val max: String?,
    val rate: String
)

data class OnboardingUiState(
    val currentStep: Int = 1,
    // Step 2: Meter Config
    val meterName: String = "বাসা - মূল মিটার",
    val meterNumber: String = "",
    val blackDigitCapacity: Int = 5,
    // Step 3: Billing Config
    val billingMode: TariffBillingMode = TariffBillingMode.SLAB_BASED,
    val flatRate: String = "8.00",
    val slabs: List<EditableSlabState> = listOf(
        EditableSlabState("0", "75", "5.26"),
        EditableSlabState("75", "200", "7.20"),
        EditableSlabState("200", "300", "7.59"),
        EditableSlabState("300", "400", "8.02"),
        EditableSlabState("400", "600", "12.67"),
        EditableSlabState("600", null, "14.61")
    ),
    val demandCharge: String = "42.00",
    val meterRent: String = "10.00",
    val vatPercentage: String = "5.00",
    // Step 4: Baseline Reading
    val baselineBlackDigits: String = "12500",
    val baselineRedDigit: String = "0",
    val baselineDateTime: LocalDateTime = DateUtils.now(),
    val reconstructedBaseline: BigDecimal? = BigDecimal("12500.0"),
    val errorMessage: String? = null,
    val isCompleting: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val readingRepository: MeterReadingRepository,
    private val preferencesManager: PreferencesManager,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current == 2) {
            // Validate step 2
            if (_uiState.value.meterName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "মিটারে একটি নাম দিন (Meter name is required)") }
                return
            }
        }
        if (current < 4) {
            _uiState.update { it.copy(currentStep = current + 1, errorMessage = null) }
        }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.update { it.copy(currentStep = current - 1, errorMessage = null) }
        }
    }

    fun updateMeterName(name: String) = _uiState.update { it.copy(meterName = name) }
    fun updateMeterNumber(number: String) = _uiState.update { it.copy(meterNumber = number) }
    fun updateBlackCapacity(capacity: Int) = _uiState.update { it.copy(blackDigitCapacity = capacity) }

    fun updateBillingMode(mode: TariffBillingMode) = _uiState.update { it.copy(billingMode = mode) }
    fun updateFlatRate(rate: String) = _uiState.update { it.copy(flatRate = rate) }

    fun updateSlabRate(index: Int, newRate: String) {
        _uiState.update { state ->
            val updated = state.slabs.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(rate = newRate)
            }
            state.copy(slabs = updated)
        }
    }

    fun updateDemandCharge(charge: String) = _uiState.update { it.copy(demandCharge = charge) }
    fun updateMeterRent(rent: String) = _uiState.update { it.copy(meterRent = rent) }
    fun updateVatPercentage(vat: String) = _uiState.update { it.copy(vatPercentage = vat) }

    fun updateBaselineBlackDigits(digits: String) {
        val normalized = BanglaDigitNormalizer.normalize(digits).filter { it.isDigit() }
        _uiState.update { state ->
            val result = MeterReadingParser.parse(normalized, state.baselineRedDigit)
            state.copy(
                baselineBlackDigits = normalized,
                reconstructedBaseline = result.getOrNull(),
                errorMessage = null
            )
        }
    }

    fun updateBaselineRedDigit(digit: String) {
        val normalized = BanglaDigitNormalizer.normalize(digit).filter { it.isDigit() }.take(1)
        _uiState.update { state ->
            val result = MeterReadingParser.parse(state.baselineBlackDigits, normalized)
            state.copy(
                baselineRedDigit = normalized,
                reconstructedBaseline = result.getOrNull(),
                errorMessage = null
            )
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val parseResult = MeterReadingParser.parse(state.baselineBlackDigits, state.baselineRedDigit)
        if (parseResult.isFailure) {
            _uiState.update { it.copy(errorMessage = parseResult.exceptionOrNull()?.message) }
            return
        }

        val baselineReadingValue = parseResult.getOrThrow()
        _uiState.update { it.copy(isCompleting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val meterId = UUID.randomUUID().toString()
                val tariffId = UUID.randomUUID().toString()
                val now = DateUtils.now()

                // 1. Create and save meter
                val meter = Meter(
                    id = meterId,
                    meterName = state.meterName.ifBlank { "বাসা - মূল মিটার" },
                    meterNumber = state.meterNumber.ifBlank { null },
                    meterType = "MECHANICAL_COUNTER",
                    blackDigitCapacity = state.blackDigitCapacity,
                    decimalPlaces = 1,
                    meterStatus = MeterStatus.ACTIVE,
                    installedAt = state.baselineDateTime,
                    createdAt = now
                )
                meterRepository.saveMeter(meter)

                // 2. Create and save tariff with slabs and charges
                val slabs = state.slabs.mapIndexed { idx, s ->
                    TariffSlab(
                        id = UUID.randomUUID().toString(),
                        tariffId = tariffId,
                        minimumUnits = BigDecimal(s.min),
                        maximumUnits = s.max?.let { BigDecimal(it) },
                        ratePerUnit = BigDecimal(s.rate.ifBlank { "0.0" }),
                        displayOrder = idx + 1
                    )
                }

                val charges = mutableListOf<AdditionalCharge>()
                if (state.demandCharge.isNotBlank()) {
                    charges.add(
                        AdditionalCharge(
                            id = UUID.randomUUID().toString(),
                            tariffId = tariffId,
                            chargeType = AdditionalChargeType.DEMAND_CHARGE,
                            calculationMethod = ChargeCalculationMethod.FIXED_MONTHLY,
                            amount = BigDecimal(state.demandCharge),
                            isEnabled = true,
                            vatApplicable = true
                        )
                    )
                }
                if (state.meterRent.isNotBlank()) {
                    charges.add(
                        AdditionalCharge(
                            id = UUID.randomUUID().toString(),
                            tariffId = tariffId,
                            chargeType = AdditionalChargeType.METER_RENT,
                            calculationMethod = ChargeCalculationMethod.FIXED_MONTHLY,
                            amount = BigDecimal(state.meterRent),
                            isEnabled = true,
                            vatApplicable = true
                        )
                    )
                }
                if (state.vatPercentage.isNotBlank()) {
                    charges.add(
                        AdditionalCharge(
                            id = UUID.randomUUID().toString(),
                            tariffId = tariffId,
                            chargeType = AdditionalChargeType.VAT,
                            calculationMethod = ChargeCalculationMethod.PERCENTAGE_OF_TOTAL,
                            amount = BigDecimal(state.vatPercentage),
                            isEnabled = true,
                            vatApplicable = false
                        )
                    )
                }

                val tariff = Tariff(
                    id = tariffId,
                    tariffName = "আবাসিক ট্যারিফ (Residential LT-A)",
                    billingMode = state.billingMode,
                    calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
                    effectiveFrom = now,
                    effectiveUntil = null,
                    currency = "BDT",
                    isActive = true,
                    flatRate = if (state.billingMode == TariffBillingMode.FLAT_RATE) BigDecimal(state.flatRate) else null,
                    slabs = slabs,
                    additionalCharges = charges
                )
                tariffRepository.saveTariff(tariff)

                // 3. Save Baseline reading
                val baselineReading = MeterReading(
                    id = UUID.randomUUID().toString(),
                    meterId = meterId,
                    blackDigits = state.baselineBlackDigits,
                    redDigit = state.baselineRedDigit,
                    normalizedReading = baselineReadingValue,
                    readingDateTime = state.baselineDateTime,
                    createdAt = now,
                    updatedAt = now,
                    isBaseline = true,
                    note = "প্রাথমিক বেসলাইন রিডিং (Initial Baseline)",
                    readingSource = ReadingSource.INITIAL_BASELINE
                )
                readingRepository.saveReading(baselineReading)

                // 4. Log initialization event
                meterRepository.logMeterEvent(
                    MeterEvent(
                        id = UUID.randomUUID().toString(),
                        meterId = meterId,
                        eventType = MeterEventType.INITIALIZATION,
                        eventDate = state.baselineDateTime,
                        description = "মিটার ইনিশিয়ালাইজেশন ও বেসলাইন রিডিং সম্পন্ন ($baselineReadingValue kWh)",
                        createdAt = now
                    )
                )

                // 5. Initialize period and calculate
                recalculationUseCase.recalculateAll(meterId)

                // Mark onboarding completed
                preferencesManager.setOnboardingCompleted(true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCompleting = false, errorMessage = "ত্রুটি: ${e.message}") }
            }
        }
    }
}
