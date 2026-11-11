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
}
