package com.bangla.electricitybill.data.backup

import com.bangla.electricitybill.data.entity.AdditionalChargeEntity
import com.bangla.electricitybill.data.entity.BillingPeriodEntity
import com.bangla.electricitybill.data.entity.MeterEntity
import com.bangla.electricitybill.data.entity.MeterEventEntity
import com.bangla.electricitybill.data.entity.MeterReadingEntity
import com.bangla.electricitybill.data.entity.MonthlySummaryEntity
import com.bangla.electricitybill.data.entity.TariffEntity
import com.bangla.electricitybill.data.entity.TariffSlabEntity

data class AppBackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val meters: List<MeterEntity>,
    val meterReadings: List<MeterReadingEntity>,
    val billingPeriods: List<BillingPeriodEntity>,
    val tariffs: List<TariffEntity>,
    val tariffSlabs: List<TariffSlabEntity>,
    val additionalCharges: List<AdditionalChargeEntity>,
    val monthlySummaries: List<MonthlySummaryEntity>,
    val meterEvents: List<MeterEventEntity>
)
