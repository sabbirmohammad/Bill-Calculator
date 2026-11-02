package com.bangla.electricitybill.domain.usecase

import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.MeterReadingParser
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.IntervalConsumptionResult
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class SaveReadingValidationResult {
    data class Success(val reading: MeterReading, val intervalResult: IntervalConsumptionResult?) : SaveReadingValidationResult()
    data class RequiresRolloverConfirmation(val readingValue: BigDecimal, val previousReadingValue: BigDecimal) : SaveReadingValidationResult()
    data class Error(val message: String) : SaveReadingValidationResult()
}

@Singleton
class SaveReadingUseCase @Inject constructor(
    private val readingRepository: MeterReadingRepository,
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val billingPeriodRepository: BillingPeriodRepository,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) {

    suspend fun saveReading(
        meterId: String,
        blackDigits: String,
        redDigit: String,
        readingDateTime: LocalDateTime = DateUtils.now(),
        isRolloverConfirmed: Boolean = false,
        note: String? = null
    ): SaveReadingValidationResult {
        // 1. Parse reading
        val parseResult = MeterReadingParser.parse(blackDigits, redDigit)
        if (parseResult.isFailure) {
            return SaveReadingValidationResult.Error(parseResult.exceptionOrNull()?.message ?: "অবৈধ রিডিং (Invalid reading)")
        }
        val normalizedReading = parseResult.getOrThrow()

        // 2. Future-dated check (with 5 min grace)
        val now = DateUtils.now()
        if (readingDateTime.isAfter(now.plusMinutes(5))) {
            return SaveReadingValidationResult.Error("ভবিষ্যতের তারিখের রিডিং গ্রহণযোগ্য নয় (Future dated readings are prohibited)")
        }

        // 3. Get previous and next reading for chronological validation
        val previousReading = readingRepository.getPreviousReading(meterId, readingDateTime)
        val nextReading = readingRepository.getNextReading(meterId, readingDateTime)
        val activeMeter = meterRepository.getActiveMeter()
            ?: return SaveReadingValidationResult.Error("কোনো সক্রিয় মিটার পাওয়া যায়নি (No active meter found)")

        // 4. Chronological validation
        if (previousReading != null && normalizedReading < previousReading.normalizedReading) {
            if (!isRolloverConfirmed) {
                return SaveReadingValidationResult.RequiresRolloverConfirmation(
                    readingValue = normalizedReading,
                    previousReadingValue = previousReading.normalizedReading
                )
            }
        }

        if (nextReading != null && normalizedReading > nextReading.normalizedReading) {
            return SaveReadingValidationResult.Error(
                "পরবর্তী রিডিং (${nextReading.normalizedReading}) এর চেয়ে বড় রিডিং হতে পারবে না (Reading cannot be greater than subsequent reading)"
            )
        }

        val readingSource = if (isRolloverConfirmed) ReadingSource.ROLLOVER else ReadingSource.MANUAL

        val reading = MeterReading(
            id = UUID.randomUUID().toString(),
            meterId = meterId,
            blackDigits = blackDigits.trim(),
            redDigit = redDigit.trim(),
            normalizedReading = normalizedReading,
            readingDateTime = readingDateTime,
            createdAt = now,
            updatedAt = now,
            isBaseline = false,
            note = note,
            readingSource = readingSource
        )

        // Save reading
        readingRepository.saveReading(reading)

        // Calculate interval preview result if previous exists
        var intervalResult: IntervalConsumptionResult? = null
        if (previousReading != null) {
            val consumptionResult = ConsumptionCalculator.calculateConsumption(
                previousReading = previousReading.normalizedReading,
                currentReading = normalizedReading,
                isRollover = isRolloverConfirmed,
                blackDigitCapacity = activeMeter.blackDigitCapacity
            )

            if (consumptionResult.isSuccess) {
                val consumption = consumptionResult.getOrThrow()
                val elapsedDays = ConsumptionCalculator.calculateElapsedDays(previousReading, reading)
                val avgDaily = ConsumptionCalculator.calculateAverageDaily(consumption, elapsedDays)
                val isConsecutive = elapsedDays == 1L

                val activeTariff = tariffRepository.getActiveTariff()
                val energyCost = if (activeTariff != null) {
                    ProgressiveTariffEngine.calculateDifferentialEnergyCost(
                        previousCumulativeConsumption = BigDecimal.ZERO, // will be refreshed by recalculateAll
                        newCumulativeConsumption = consumption,
                        tariff = activeTariff
                    )
                } else {
                    BigDecimal.ZERO
                }

                intervalResult = IntervalConsumptionResult(
                    previousReading = previousReading,
                    currentReading = reading,
                    consumptionUnits = consumption,
                    elapsedDays = elapsedDays,
                    averageDailyConsumption = avgDaily,
                    intervalEnergyCost = energyCost,
                    isConsecutiveDaily = isConsecutive,
                    label = if (isConsecutive) "আজকের ব্যবহার" else "পূর্ববর্তী রিডিং থেকে ব্যবহার"
                )
            }
        }

        // Trigger historical cascade recalculation for monthly summaries and billing periods
        recalculationUseCase.recalculateAll(meterId)

        return SaveReadingValidationResult.Success(reading, intervalResult)
    }
}
