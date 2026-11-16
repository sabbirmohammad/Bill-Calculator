package com.powerunit.domain.model

import java.math.BigDecimal

data class SlabCharge(
    val slabIndex: Int,
    val fromUnit: BigDecimal,
    val toUnit: BigDecimal?,
    val unitsBilled: BigDecimal,
    val ratePerUnit: BigDecimal,
    val amount: BigDecimal
)

data class BillBreakdown(
    val slabCharges: List<SlabCharge>,
    val energyCharge: BigDecimal,
    val demandCharge: BigDecimal,
    val serviceCharge: BigDecimal,
    val subtotal: BigDecimal,
    val vatPercent: BigDecimal,
    val vatAmount: BigDecimal,
    val totalBill: BigDecimal,
    val rebateAmount: BigDecimal = BigDecimal.ZERO,
    val otherCharges: BigDecimal = BigDecimal.ZERO
)

data class SplitBillBreakdown(
    val period1Days: Int,
    val period2Days: Int,
    val period1Units: BigDecimal,
    val period2Units: BigDecimal,
    val period1Bill: BillBreakdown,
    val period2Bill: BillBreakdown,
    val totalEnergyCharge: BigDecimal,
    val totalDemandCharge: BigDecimal,
    val totalServiceCharge: BigDecimal,
    val totalOtherCharges: BigDecimal,
    val totalRebateAmount: BigDecimal,
    val totalVatAmount: BigDecimal,
    val totalBill: BigDecimal
)
