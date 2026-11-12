package com.powerunit.feature.tariff

import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.model.TariffVersion
import com.powerunit.domain.repository.TariffRecalculationResult
import com.powerunit.domain.repository.TariffRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class TariffEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)

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
        isActive = true,
        effectiveFromCycle = "2026-11"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { tariffRepository.getActiveTariffSync() } returns sampleTariff
        coEvery { tariffRepository.seedDefaultsIfNeeded() } returns Unit
        every { settingsPreferences.appLockEnabled } returns flowOf(false)
        every { settingsPreferences.billingCycleDay } returns flowOf(1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TariffEditorViewModel {
        return TariffEditorViewModel(
            tariffRepository = tariffRepository,
            settingsPreferences = settingsPreferences
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
        assertFalse(state.isDirty)
        assertTrue(state.validationErrors.isEmpty())
    }

    @Test
    fun addSlab_incrementsSlabCountAndMaintainsIndices() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addSlab()
        val state = viewModel.uiState.value
        assertEquals(4, state.slabs.size)
        assertEquals(4, state.slabs.last().slabIndex)
        assertTrue(state.isDirty)
    }

    @Test
    fun removeSlab_decrementsSlabCountAndEnsuresOpenEndedLast() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeSlab(1) // Remove slab 2
        val state = viewModel.uiState.value
        assertEquals(2, state.slabs.size)
        assertEquals(1, state.slabs[0].slabIndex)
        assertEquals(2, state.slabs[1].slabIndex)
        assertTrue(state.slabs.last().toUnit.isBlank()) // Open ended
        assertTrue(state.isDirty)
    }

    @Test
    fun validateTariff_catchesVatOutOfRange() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateVatPercent("105.0")
        val errors = viewModel.validateTariff()
        assertTrue(errors.any { it.contains("VAT must be a valid percentage") })

        viewModel.updateVatPercent("-2.0")
        val negativeErrors = viewModel.validateTariff()
        assertTrue(negativeErrors.any { it.contains("VAT must be a valid percentage") })
    }

    @Test
    fun validateTariff_catchesNegativeCharges() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDemandCharge("-10")
        val errors = viewModel.validateTariff()
        assertTrue(errors.any { it.contains("Demand charge must be a non-negative") })

        viewModel.updateServiceCharge("-5")
        val serviceErrors = viewModel.validateTariff()
        assertTrue(serviceErrors.any { it.contains("Service charge must be a non-negative") })
    }

    @Test
    fun validateTariff_catchesOverlappingSlabs() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Make slab 2 overlap with slab 1 (slab 1 ends at 75, slab 2 starts at 50)
        viewModel.updateSlab(1, from = "50", to = "200", rate = "6.63")
        val errors = viewModel.validateTariff()
        assertTrue(errors.any { it.contains("overlap") })
    }

    @Test
    fun validateTariff_catchesGapsBetweenSlabs() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Slab 1 ends at 75, set slab 2 to start at 100 (gap from 76 to 99)
        viewModel.updateSlab(1, from = "100", to = "200", rate = "6.63")
        val errors = viewModel.validateTariff()
        assertTrue(errors.any { it.contains("Gap detected") })
    }

    @Test
    fun validateTariff_catchesInvertedSlabBounds() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // From 100, To 50
        viewModel.updateSlab(0, from = "100", to = "50", rate = "4.85")
        val errors = viewModel.validateTariff()
        assertTrue(errors.any { it.contains("must be greater than") })
    }

    @Test
    fun onSaveClicked_withErrors_showsValidationErrors() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateVatPercent("150.0")
        viewModel.onSaveClicked()

        assertTrue(viewModel.uiState.value.validationErrors.isNotEmpty())
        assertFalse(viewModel.uiState.value.showMonthPicker)
    }

    @Test
    fun onSaveClicked_withValidChanges_showsMonthPicker() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDemandCharge("45.00")
        viewModel.onSaveClicked()

        assertTrue(viewModel.uiState.value.showMonthPicker)
    }

    @Test
    fun onEffectiveMonthSelected_showsConfirmDialogWithDiff() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDemandCharge("45.00")
        viewModel.onEffectiveMonthSelected(2026, 11)

        assertFalse(viewModel.uiState.value.showMonthPicker)
        assertTrue(viewModel.uiState.value.showConfirmDialog)
        assertEquals("2026-11", viewModel.uiState.value.selectedEffectiveCycle)
        assertTrue(viewModel.uiState.value.diffItems.any { it.label == "Demand Charge" })
    }

    @Test
    fun onConfirmSave_savesTariffVersionAndRecalculates_showsSuccessAndUndo() = runTest(testDispatcher) {
        coEvery {
            tariffRepository.saveTariffVersionAndRecalculate(any(), any())
        } returns TariffRecalculationResult(
            versionId = 2L,
            effectiveFromCycle = "2026-11",
            monthsRecalculated = 1,
            previousVersionId = 1L
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDemandCharge("45.00")
        viewModel.onEffectiveMonthSelected(2026, 11)
        viewModel.onConfirmSave()
        testDispatcher.scheduler.advanceUntilIdle()

        val captured = slot<TariffVersion>()
        coVerify { tariffRepository.saveTariffVersionAndRecalculate(capture(captured), any()) }

        assertEquals(BigDecimal("45.00"), captured.captured.demandCharge)
        assertEquals("2026-11", captured.captured.effectiveFromCycle)
        assertTrue(viewModel.uiState.value.saveSuccess)
        assertEquals(2L, viewModel.uiState.value.undoVersionId)
        assertTrue(viewModel.uiState.value.successMessage?.contains("1 months recalculated") == true)
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun undoLastSave_callsRevertTariffVersion() = runTest(testDispatcher) {
        coEvery {
            tariffRepository.saveTariffVersionAndRecalculate(any(), any())
        } returns TariffRecalculationResult(
            versionId = 2L,
            effectiveFromCycle = "2026-11",
            monthsRecalculated = 1,
            previousVersionId = 1L
        )
        coEvery { tariffRepository.revertTariffVersion(2L) } returns true

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDemandCharge("45.00")
        viewModel.onEffectiveMonthSelected(2026, 11)
        viewModel.onConfirmSave()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2L, viewModel.uiState.value.undoVersionId)

        viewModel.undoLastSave()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tariffRepository.revertTariffVersion(2L) }
        assertEquals(null, viewModel.uiState.value.undoVersionId)
    }
}
