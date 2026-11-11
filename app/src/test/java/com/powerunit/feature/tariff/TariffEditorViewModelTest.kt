package com.powerunit.feature.tariff

import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class TariffEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val calculateBillUseCase = CalculateBillUseCase()

    private val sampleTariff = Tariff(
        id = 1L,
        name = "DESCO Postpaid",
        effectiveFrom = Instant.fromEpochMilliseconds(1000L),
        vatPercent = BigDecimal("5.0"),
        serviceChargeFlat = BigDecimal("10.00"),
        demandChargeFlat = BigDecimal("35.00"),
        slabs = listOf(
            TariffSlab(1L, 1L, BigDecimal("0"), BigDecimal("75"), BigDecimal("4.85")),
            TariffSlab(2L, 1L, BigDecimal("76"), BigDecimal("200"), BigDecimal("6.63")),
            TariffSlab(3L, 1L, BigDecimal("201"), null, BigDecimal("7.95"))
        ),
        isActive = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { tariffRepository.getActiveTariffSync() } returns sampleTariff
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TariffEditorViewModel {
        return TariffEditorViewModel(
            tariffRepository = tariffRepository,
            calculateBillUseCase = calculateBillUseCase
        )
    }

    @Test
    fun loadActiveTariff_populatesSlabsAndCharges() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DESCO Postpaid", state.tariffName)
        assertEquals(3, state.slabs.size)
        assertEquals("35", state.demandCharge)
        assertEquals("10", state.serviceCharge)
        assertEquals("5", state.vatPercent)
        assertNotNull(state.previewBill)
        assertTrue(state.previewBill!!.totalBill > BigDecimal.ZERO)
    }

    @Test
    fun addSlab_incrementsSlabCount() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addSlab()
        val state = viewModel.uiState.value
        assertEquals(4, state.slabs.size)
        assertEquals(4, state.slabs.last().slabIndex)
    }

    @Test
    fun removeSlab_decrementsSlabCount() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeSlab(1) // Remove slab 2
        val state = viewModel.uiState.value
        assertEquals(2, state.slabs.size)
        assertEquals(1, state.slabs[0].slabIndex)
        assertEquals(2, state.slabs[1].slabIndex)
    }

    @Test
    fun saveTariff_callsRepositoryAndUpdateSuccess() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveTariff()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tariffRepository.updateTariff(any()) }
        assertTrue(viewModel.uiState.value.saveSuccess)
    }
}
