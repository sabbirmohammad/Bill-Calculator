package com.powerunit.feature.backup

import com.powerunit.core.backup.BackupManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val backupManager = mockk<BackupManager>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createBackup_withShortPassphrase_setsErrorMessage() = runTest(testDispatcher) {
        val viewModel = BackupViewModel(backupManager)

        viewModel.createBackup("123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Passphrase must be at least 4 characters", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.backupFile)
    }

    @Test
    fun createBackup_successful_updatesStateWithFile() = runTest(testDispatcher) {
        val fakeFile = File("fake_backup.wattwise")
        coEvery { backupManager.createEncryptedBackup("validPass123") } returns fakeFile

        val viewModel = BackupViewModel(backupManager)
        viewModel.createBackup("validPass123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.backupFile)
        assertEquals(fakeFile, state.backupFile)
        assertTrue(state.successMessage?.contains("Encrypted backup created") == true)
    }

    @Test
    fun restoreBackup_successful_setsSuccessMessage() = runTest(testDispatcher) {
        val fakeFile = File("fake_backup.wattwise")
        coEvery { backupManager.restoreEncryptedBackup(fakeFile, "validPass123") } returns 5

        val viewModel = BackupViewModel(backupManager)
        viewModel.restoreBackup(fakeFile, "validPass123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Successfully restored 5 readings!", state.successMessage)
    }

    @Test
    fun restoreBackup_failure_setsErrorMessage() = runTest(testDispatcher) {
        val fakeFile = File("fake_backup.wattwise")
        coEvery {
            backupManager.restoreEncryptedBackup(fakeFile, "wrongPass")
        } throws IllegalArgumentException("Decryption error")

        val viewModel = BackupViewModel(backupManager)
        viewModel.restoreBackup(fakeFile, "wrongPass")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Restore failed") == true)
    }
}
