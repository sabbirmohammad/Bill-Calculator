package com.powerunit.domain.usecase

import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculateBillUseCaseTest {

    private lateinit var useCase: CalculateBillUseCase
    private lateinit var bdTariff: Tariff

    @Before
    fun setUp() {
        useCase = CalculateBillUseCase()

        // Standard Bangladesh Residential LT-A Tariff template
        // Slab 1: 0 - 75 units @ ৳4.35
        // Slab 2: 76 - 200 units @ ৳6.63 (capacity 125)
        // Slab 3: 201 - 300 units @ ৳6.95 (capacity 100)
        // Slab 4: 301 - 400 units @ ৳7.34 (capacity 100)
        // Slab 5: 401 - 600 units @ ৳11.51 (capacity 200)
        // Slab 6: 601+ units @ ৳13.26
        // Demand charge: ৳35.00
        // Service charge: ৳10.00
        // VAT: 5.0%
        val slabs = listOf(
            TariffSlab(id = 1, tariffId = 1, fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.35")),
            TariffSlab(id = 2, tariffId = 1, fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
            TariffSlab(id = 3, tariffId = 1, fromUnit = BigDecimal("201"), toUnit = BigDecimal("300"), ratePerUnit = BigDecimal("6.95")),
            TariffSlab(id = 4, tariffId = 1, fromUnit = BigDecimal("301"), toUnit = BigDecimal("400"), ratePerUnit = BigDecimal("7.34")),
            TariffSlab(id = 5, tariffId = 1, fromUnit = BigDecimal("401"), toUnit = BigDecimal("600"), ratePerUnit = BigDecimal("11.51")),
            TariffSlab(id = 6, tariffId = 1, fromUnit = BigDecimal("601"), toUnit = null, ratePerUnit = BigDecimal("13.26"))
        )

        bdTariff = Tariff(
            id = 1,
            name = "Residential LT-A",
            effectiveFrom = Instant.fromEpochMilliseconds(0),
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true,
            slabs = slabs
        )
    }

    @Test
    fun calculateBill_zeroUnits_chargesOnlyFixedAndVat() {
        // 0 units: energy charge = 0
        // subtotal = 0 + 35 + 10 = 45.00
        // VAT 5% of 45.00 = 2.25
        // total = 47.25
        val result = useCase(BigDecimal.ZERO, bdTariff)

        assertEquals(BigDecimal("0.00"), result.energyCharge)
        assertEquals(BigDecimal("35.00"), result.demandCharge)
        assertEquals(BigDecimal("10.00"), result.serviceCharge)
        assertEquals(BigDecimal("45.00"), result.subtotal)
        assertEquals(BigDecimal("2.25"), result.vatAmount)
        assertEquals(BigDecimal("47.25"), result.totalBill)
    }

    @Test
    fun calculateBill_workedExample_50UnitsInSlab1() {
        // 50 units in Slab 1 (0..75):
        // 50 * 4.35 = 217.50
        // subtotal = 217.50 + 35 + 10 = 262.50
        // VAT 5% = 262.50 * 0.05 = 13.125 -> 13.13
        // total = 262.50 + 13.13 = 275.63
        val result = useCase(BigDecimal("50"), bdTariff)

        assertEquals(BigDecimal("217.50"), result.energyCharge)
        assertEquals(BigDecimal("262.50"), result.subtotal)
        assertEquals(BigDecimal("13.13"), result.vatAmount)
        assertEquals(BigDecimal("275.63"), result.totalBill)
        assertEquals(BigDecimal("50.0"), result.slabCharges[0].unitsBilled)
        assertEquals(BigDecimal("0.0"), result.slabCharges[1].unitsBilled)
    }

    @Test
    fun calculateBill_workedExample_exactSlab1Boundary_75Units() {
        // 75 units:
        // Slab 1: 75 * 4.35 = 326.25
        // subtotal = 326.25 + 45 = 371.25
        // VAT 5% = 18.56
        // total = 389.81
        val result = useCase(BigDecimal("75"), bdTariff)

        assertEquals(BigDecimal("326.25"), result.energyCharge)
        assertEquals(BigDecimal("371.25"), result.subtotal)
        assertEquals(BigDecimal("18.56"), result.vatAmount)
        assertEquals(BigDecimal("389.81"), result.totalBill)
    }

    @Test
    fun calculateBill_workedExample_250UnitsCrossingMultipleSlabs() {
        // 250 units:
        // Slab 1 (0..75): 75 * 4.35 = 326.25
        // Slab 2 (76..200): 125 * 6.63 = 828.75
        // Slab 3 (201..300): 50 * 6.95 = 347.50
        // Energy charge = 326.25 + 828.75 + 347.50 = 1502.50
        // Subtotal = 1502.50 + 45.00 = 1547.50
        // VAT 5% = 77.375 -> 77.38
        // Total = 1547.50 + 77.38 = 1624.88
        val result = useCase(BigDecimal("250"), bdTariff)

        assertEquals(BigDecimal("1502.50"), result.energyCharge)
        assertEquals(BigDecimal("1547.50"), result.subtotal)
        assertEquals(BigDecimal("77.38"), result.vatAmount)
        assertEquals(BigDecimal("1624.88"), result.totalBill)

        assertEquals(BigDecimal("75.0"), result.slabCharges[0].unitsBilled)
        assertEquals(BigDecimal("125.0"), result.slabCharges[1].unitsBilled)
        assertEquals(BigDecimal("50.0"), result.slabCharges[2].unitsBilled)
        assertEquals(BigDecimal("0.0"), result.slabCharges[3].unitsBilled)
    }

    @Test
    fun calculateBill_workedExample_topSlabExceeded_700Units() {
        // 700 units:
        // Slab 1: 75 * 4.35 = 326.25
        // Slab 2: 125 * 6.63 = 828.75
        // Slab 3: 100 * 6.95 = 695.00
        // Slab 4: 100 * 7.34 = 734.00
        // Slab 5: 200 * 11.51 = 2302.00
        // Slab 6 (601+): 100 * 13.26 = 1326.00
        // Energy charge = 326.25 + 828.75 + 695.00 + 734.00 + 2302.00 + 1326.00 = 6212.00
        // Subtotal = 6212.00 + 45.00 = 6257.00
        // VAT 5% = 312.85
        // Total = 6569.85
        val result = useCase(BigDecimal("700"), bdTariff)

        assertEquals(BigDecimal("6212.00"), result.energyCharge)
        assertEquals(BigDecimal("6257.00"), result.subtotal)
        assertEquals(BigDecimal("312.85"), result.vatAmount)
        assertEquals(BigDecimal("6569.85"), result.totalBill)
        assertEquals(BigDecimal("100.0"), result.slabCharges[5].unitsBilled)
    }

    @Test
    fun getSlabForUnits_identifiesCorrectSlabAndRate() {
        val slab0 = useCase.getSlabForUnits(BigDecimal.ZERO, bdTariff)
        assertEquals(1, slab0.slabNumber)
        assertEquals(BigDecimal("4.35"), slab0.ratePerUnit)

        val slab50 = useCase.getSlabForUnits(BigDecimal("50"), bdTariff)
        assertEquals(1, slab50.slabNumber)
        assertEquals(BigDecimal("4.35"), slab50.ratePerUnit)

        val slab75 = useCase.getSlabForUnits(BigDecimal("75"), bdTariff)
        assertEquals(1, slab75.slabNumber)

        val slab76 = useCase.getSlabForUnits(BigDecimal("76"), bdTariff)
        assertEquals(2, slab76.slabNumber)
        assertEquals(BigDecimal("6.63"), slab76.ratePerUnit)

        val slab200 = useCase.getSlabForUnits(BigDecimal("200"), bdTariff)
        assertEquals(2, slab200.slabNumber)

        val slab250 = useCase.getSlabForUnits(BigDecimal("250"), bdTariff)
        assertEquals(3, slab250.slabNumber)
        assertEquals(BigDecimal("6.95"), slab250.ratePerUnit)

        val slab350 = useCase.getSlabForUnits(BigDecimal("350"), bdTariff)
        assertEquals(4, slab350.slabNumber)
        assertEquals(BigDecimal("7.34"), slab350.ratePerUnit)

        val slab500 = useCase.getSlabForUnits(BigDecimal("500"), bdTariff)
        assertEquals(5, slab500.slabNumber)
        assertEquals(BigDecimal("11.51"), slab500.ratePerUnit)

        val slab750 = useCase.getSlabForUnits(BigDecimal("750"), bdTariff)
        assertEquals(6, slab750.slabNumber)
        assertEquals(BigDecimal("13.26"), slab750.ratePerUnit)
    }

    @Test
    fun calculateEnergyCharge_computesProgressiveEnergyCharge() {
        val energy50 = useCase.calculateEnergyCharge(BigDecimal("50"), bdTariff)
        assertEquals(BigDecimal("217.50"), energy50)

        val energy75 = useCase.calculateEnergyCharge(BigDecimal("75"), bdTariff)
        assertEquals(BigDecimal("326.25"), energy75)

        val energy250 = useCase.calculateEnergyCharge(BigDecimal("250"), bdTariff)
        assertEquals(BigDecimal("1502.50"), energy250)

        // Incremental cost between 75 and 250 units
        val deltaCost = energy250 - energy75
        // 125 * 6.63 (828.75) + 50 * 6.95 (347.50) = 1176.25
        assertEquals(BigDecimal("1176.25"), deltaCost)
    }

    @Test
    fun calculateBill_exactBoundaries_200UnitsAnd300Units() {
        // Exact 200 units boundary:
        // Slab 1 (75): 75 * 4.35 = 326.25
        // Slab 2 (125): 125 * 6.63 = 828.75
        // Energy charge = 1155.00
        // Subtotal = 1155.00 + 45 = 1200.00
        // VAT 5% = 60.00
        // Total = 1260.00
        val result200 = useCase(BigDecimal("200"), bdTariff)
        assertEquals(BigDecimal("1155.00"), result200.energyCharge)
        assertEquals(BigDecimal("1200.00"), result200.subtotal)
        assertEquals(BigDecimal("60.00"), result200.vatAmount)
        assertEquals(BigDecimal("1260.00"), result200.totalBill)
        assertEquals(BigDecimal("75.0"), result200.slabCharges[0].unitsBilled)
        assertEquals(BigDecimal("125.0"), result200.slabCharges[1].unitsBilled)
        assertEquals(BigDecimal("0.0"), result200.slabCharges[2].unitsBilled)

        // Exact 300 units boundary:
        // Slab 1: 326.25, Slab 2: 828.75, Slab 3 (100): 100 * 6.95 = 695.00
        // Energy charge = 1155.00 + 695.00 = 1850.00
        // Subtotal = 1850.00 + 45 = 1895.00
        // VAT 5% = 94.75
        // Total = 1989.75
        val result300 = useCase(BigDecimal("300"), bdTariff)
        assertEquals(BigDecimal("1850.00"), result300.energyCharge)
        assertEquals(BigDecimal("1895.00"), result300.subtotal)
        assertEquals(BigDecimal("94.75"), result300.vatAmount)
        assertEquals(BigDecimal("1989.75"), result300.totalBill)
    }

    @Test
    fun getUnitsToNextSlab_computesRemainingUnitsAccurately() {
        // At 50 units (in Slab 1 with upper bound 75), units to next slab = 25.0
        val remainingAt50 = useCase.getUnitsToNextSlab(BigDecimal("50.0"), bdTariff)
        assertEquals(BigDecimal("25.0"), remainingAt50)

        // At 75 units (boundary), units to next slab = 0.0
        val remainingAt75 = useCase.getUnitsToNextSlab(BigDecimal("75.0"), bdTariff)
        assertEquals(BigDecimal("0.0"), remainingAt75)

        // At 150 units (in Slab 2 with upper bound 200), units to next slab = 50.0
        val remainingAt150 = useCase.getUnitsToNextSlab(BigDecimal("150.0"), bdTariff)
        assertEquals(BigDecimal("50.0"), remainingAt150)

        // At 700 units (in unlimited Slab 6), returns null
        val remainingAt700 = useCase.getUnitsToNextSlab(BigDecimal("700.0"), bdTariff)
        assertEquals(null, remainingAt700)
    }

    @Test
    fun calculateBill_withRebateAndOtherCharges() {
        // 100 units with 1% prepaid rebate and ৳40 meter rent
        val tariffWithRebate = bdTariff.copy(
            rebatePercent = BigDecimal("1.0"),
            otherCharges = BigDecimal("40.00")
        )
        // Energy charge for 100 units:
        // Slab 1 (75): 75 * 4.35 = 326.25
        // Slab 2 (25): 25 * 6.63 = 165.75
        // Total energy = 492.00
        // Rebate 1% = 4.92 -> net energy = 487.08
        // Subtotal = 487.08 + 35 (demand) + 10 (service) + 40 (meter rent) = 572.08
        // VAT 5% of 572.08 = 28.604 -> 28.60
        // Total = 572.08 + 28.60 = 600.68
        val result = useCase(BigDecimal("100"), tariffWithRebate)
        assertEquals(BigDecimal("492.00"), result.energyCharge)
        assertEquals(BigDecimal("4.92"), result.rebateAmount)
        assertEquals(BigDecimal("40.00"), result.otherCharges)
        assertEquals(BigDecimal("572.08"), result.subtotal)
        assertEquals(BigDecimal("28.60"), result.vatAmount)
        assertEquals(BigDecimal("600.68"), result.totalBill)
    }

    @Test
    fun calculateSplitBill_handlesMidCycleTariffChange() {
        val oldTariff = bdTariff
        val newTariff = bdTariff.copy(
            demandChargeFlat = BigDecimal("40.00"),
            slabs = listOf(
                TariffSlab(id = 1, tariffId = 1, fromUnit = BigDecimal("0"), toUnit = null, ratePerUnit = BigDecimal("7.00"))
            )
        )

        // 30 days total: 10 days under old tariff, 20 days under new tariff. Total units = 300.0
        // Period 1 units = 300 * 10 / 30 = 100.0
        // Period 2 units = 300 - 100 = 200.0
        val split = useCase.calculateSplitBill(
            totalUnits = BigDecimal("300.0"),
            firstTariff = oldTariff,
            firstPeriodDays = 10,
            secondTariff = newTariff,
            secondPeriodDays = 20
        )

        assertEquals(BigDecimal("100.0"), split.period1Units)
        assertEquals(BigDecimal("200.0"), split.period2Units)
        assertEquals(BigDecimal("300.0"), split.period1Units + split.period2Units)
        org.junit.Assert.assertTrue(split.totalBill > BigDecimal.ZERO)
    }
}
