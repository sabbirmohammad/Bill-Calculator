package com.bangla.electricitybill.domain.usecase

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterEventType
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterReplacementUseCase @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) {

    /**
     * Replaces an existing meter:
     * 1. Closes current meter (status = REPLACED)
     * 2. Registers replacement meter
     * 3. Records baseline reading on new meter
     * 4. Logs METER_REPLACED event
     */
    suspend fun replaceMeter(
        currentMeter: Meter,
        newMeterName: String,
        newMeterNumber: String?,
        newBlackCapacity: Int,
        initialBlackDigits: String,
        initialRedDigit: String,
        replacementDate: LocalDateTime = DateUtils.now()
    ): Result<Meter> {
        return try {
            // 1. Update old meter
            val updatedOldMeter = currentMeter.copy(meterStatus = MeterStatus.REPLACED)
            meterRepository.updateMeter(updatedOldMeter)

            // 2. Create new meter
            val newMeterId = UUID.randomUUID().toString()
            val newMeter = Meter(
                id = newMeterId,
                meterName = newMeterName,
                meterNumber = newMeterNumber,
                blackDigitCapacity = newBlackCapacity,
                decimalPlaces = 1,
                meterStatus = MeterStatus.ACTIVE,
                installedAt = replacementDate,
                createdAt = DateUtils.now()
            )
            meterRepository.saveMeter(newMeter)

            // 3. Record baseline reading
            val initialNormalized = BigDecimal(initialBlackDigits.trim())
                .add(BigDecimal(initialRedDigit.trim()).divide(BigDecimal("10")))

            val baselineReading = MeterReading(
                id = UUID.randomUUID().toString(),
                meterId = newMeterId,
                blackDigits = initialBlackDigits.trim(),
                redDigit = initialRedDigit.trim(),
                normalizedReading = initialNormalized,
                readingDateTime = replacementDate,
                createdAt = DateUtils.now(),
                updatedAt = DateUtils.now(),
                isBaseline = true,
                note = "নতুন মিটারের প্রাথমিক বেসলাইন রিডিং",
                readingSource = ReadingSource.METER_REPLACEMENT
            )
            readingRepository.saveReading(baselineReading)

            // 4. Log event
            val event = MeterEvent(
                id = UUID.randomUUID().toString(),
                meterId = newMeterId,
                eventType = MeterEventType.METER_REPLACED,
                eventDate = replacementDate,
                previousMeterId = currentMeter.id,
                replacementMeterId = newMeterId,
                description = "মিটার প্রতিস্থাপন সম্পন্ন। পূর্ববর্তী মিটার: ${currentMeter.meterName}",
                createdAt = DateUtils.now()
            )
            meterRepository.logMeterEvent(event)

            recalculationUseCase.recalculateAll(newMeterId)
            Result.success(newMeter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
