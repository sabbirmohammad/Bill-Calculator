package com.powerunit.core.database

import com.powerunit.core.database.converter.BigDecimalConverter
import com.powerunit.core.database.converter.InstantConverter
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ConverterTest {

    private val bigDecimalConverter = BigDecimalConverter()
    private val instantConverter = InstantConverter()

    @Test
    fun bigDecimalConverter_roundTrip() {
        val original = BigDecimal("12345.6789")
        val str = bigDecimalConverter.fromBigDecimal(original)
        val recovered = bigDecimalConverter.toBigDecimal(str)

        assertEquals(original, recovered)
        assertNull(bigDecimalConverter.fromBigDecimal(null))
        assertNull(bigDecimalConverter.toBigDecimal(null))
    }

    @Test
    fun instantConverter_roundTrip() {
        val original = Instant.fromEpochMilliseconds(1710000000000L)
        val epoch = instantConverter.fromInstant(original)
        val recovered = instantConverter.toInstant(epoch)

        assertEquals(original, recovered)
        assertNull(instantConverter.fromInstant(null))
        assertNull(instantConverter.toInstant(null))
    }
}
