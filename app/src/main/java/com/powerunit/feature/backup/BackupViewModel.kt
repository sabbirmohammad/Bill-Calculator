package com.powerunit.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val backupFile: File? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun createBackup(passphrase: String) {
        if (passphrase.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passphrase must be at least 4 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val file = backupManager.createEncryptedBackup(passphrase)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    backupFile = file,
                    successMessage = "Encrypted backup created: ${file.name}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to create backup: ${e.message}"
                )
            }
        }
    }

    fun restoreBackup(file: File, passphrase: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val count = backupManager.restoreEncryptedBackup(file, passphrase)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Successfully restored $count readings!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Restore failed: Invalid password or corrupted backup file"
                )
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
