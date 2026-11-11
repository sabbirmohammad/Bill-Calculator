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
    val totalBill: BigDecimal
)
