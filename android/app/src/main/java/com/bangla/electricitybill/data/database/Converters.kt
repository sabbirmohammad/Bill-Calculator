package com.bangla.electricitybill.data.database

import androidx.room.TypeConverter
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.DataCompletenessStatus
import com.bangla.electricitybill.domain.model.MeterEventType
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.PeriodStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? {
        return value?.let { DateUtils.toEpochMilli(it) }
    }

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { DateUtils.fromEpochMilli(it) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromMeterStatus(value: MeterStatus?): String? = value?.name

    @TypeConverter
    fun toMeterStatus(value: String?): MeterStatus? = value?.let { MeterStatus.valueOf(it) }

    @TypeConverter
    fun fromReadingSource(value: ReadingSource?): String? = value?.name

    @TypeConverter
    fun toReadingSource(value: String?): ReadingSource? = value?.let { ReadingSource.valueOf(it) }

    @TypeConverter
    fun fromPeriodStatus(value: PeriodStatus?): String? = value?.name

    @TypeConverter
    fun toPeriodStatus(value: String?): PeriodStatus? = value?.let { PeriodStatus.valueOf(it) }

    @TypeConverter
    fun fromDataCompletenessStatus(value: DataCompletenessStatus?): String? = value?.name

    @TypeConverter
    fun toDataCompletenessStatus(value: String?): DataCompletenessStatus? = value?.let { DataCompletenessStatus.valueOf(it) }

    @TypeConverter
    fun fromMeterEventType(value: MeterEventType?): String? = value?.name

    @TypeConverter
    fun toMeterEventType(value: String?): MeterEventType? = value?.let { MeterEventType.valueOf(it) }

    @TypeConverter
    fun fromTariffBillingMode(value: TariffBillingMode?): String? = value?.name

    @TypeConverter
    fun toTariffBillingMode(value: String?): TariffBillingMode? = value?.let { TariffBillingMode.valueOf(it) }

    @TypeConverter
    fun fromTariffCalculationMethod(value: TariffCalculationMethod?): String? = value?.name

    @TypeConverter
    fun toTariffCalculationMethod(value: String?): TariffCalculationMethod? = value?.let { TariffCalculationMethod.valueOf(it) }

    @TypeConverter
    fun fromAdditionalChargeType(value: AdditionalChargeType?): String? = value?.name

    @TypeConverter
    fun toAdditionalChargeType(value: String?): AdditionalChargeType? = value?.let { AdditionalChargeType.valueOf(it) }

    @TypeConverter
    fun fromChargeCalculationMethod(value: ChargeCalculationMethod?): String? = value?.name

    @TypeConverter
    fun toChargeCalculationMethod(value: String?): ChargeCalculationMethod? = value?.let { ChargeCalculationMethod.valueOf(it) }
}
