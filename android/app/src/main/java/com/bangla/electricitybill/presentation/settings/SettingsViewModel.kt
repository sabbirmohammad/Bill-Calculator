package com.bangla.electricitybill.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.data.backup.BackupManager
import com.bangla.electricitybill.data.database.AppDatabase
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.data.worker.DailyReadingReminderWorker
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.domain.usecase.HistoricalRecalculationUseCase
import com.bangla.electricitybill.domain.usecase.MeterReplacementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private data class PrefsTuple(
    val lang: String,
    val theme: String,
    val budget: BigDecimal?,
    val reminder: Boolean,
    val time: Pair<Int, Int>
)

data class SettingsUiState(
    val activeMeter: Meter? = null,
    val activeTariff: Tariff? = null,
    val appLanguage: String = "en",
    val appTheme: String = "SYSTEM",
    val monthlyBudget: BigDecimal? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: Pair<Int, Int> = Pair(20, 0),
    val showReplacementDialog: Boolean = false,
    val showTariffDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val feedbackMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val preferencesManager: PreferencesManager,
    private val backupManager: BackupManager,
    private val appDatabase: AppDatabase,
    private val recalculationUseCase: HistoricalRecalculationUseCase,
    private val meterReplacementUseCase: MeterReplacementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val prefsFlow = combine(
                preferencesManager.appLanguage,
                preferencesManager.appTheme,
                preferencesManager.monthlyBudget,
                preferencesManager.reminderEnabled,
                preferencesManager.reminderTime
            ) { lang, theme, budget, reminder, time ->
                PrefsTuple(lang, theme, budget, reminder, time)
            }

            combine(
                meterRepository.getActiveMeterFlow(),
                tariffRepository.getActiveTariffFlow(),
                prefsFlow
            ) { meter, tariff, prefs ->
                _uiState.update {
                    it.copy(
                        activeMeter = meter,
                        activeTariff = tariff,
                        appLanguage = prefs.lang,
                        appTheme = prefs.theme,
                        monthlyBudget = prefs.budget,
                        reminderEnabled = prefs.reminder,
                        reminderTime = prefs.time,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setAppLanguage(lang)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setAppTheme(theme)
        }
    }

    fun setMonthlyBudget(budget: BigDecimal?) {
        viewModelScope.launch {
            preferencesManager.setMonthlyBudget(budget)
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val (hour, minute) = _uiState.value.reminderTime
            preferencesManager.setReminderSettings(enabled, hour, minute)
            if (enabled) {
                DailyReadingReminderWorker.schedule(context, hour, minute)
            } else {
                DailyReadingReminderWorker.cancel(context)
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val enabled = _uiState.value.reminderEnabled
            preferencesManager.setReminderSettings(enabled, hour, minute)
            if (enabled) {
                DailyReadingReminderWorker.schedule(context, hour, minute)
            }
        }
    }

    fun openTariffDialog() = _uiState.update { it.copy(showTariffDialog = true) }
    fun closeTariffDialog() = _uiState.update { it.copy(showTariffDialog = false) }

    fun saveUpdatedTariff(updatedTariff: Tariff) {
        viewModelScope.launch {
            tariffRepository.saveTariff(updatedTariff)
            val meter = _uiState.value.activeMeter
            if (meter != null) {
                recalculationUseCase.recalculateAll(meter.id)
            }
            _uiState.update { it.copy(feedbackMessage = "Tariff updated successfully.", showTariffDialog = false) }
        }
    }

    fun exportCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = backupManager.exportReadingsToCsv()
            onResult(csv)
        }
    }

    fun exportJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            onResult(json)
        }
    }

    fun restoreBackup(jsonString: String) {
        viewModelScope.launch {
            val result = backupManager.restoreFromJson(jsonString)
            if (result.isSuccess) {
                _uiState.update { it.copy(feedbackMessage = "Backup successfully restored.") }
            } else {
                _uiState.update { it.copy(feedbackMessage = "Restore failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openClearDataDialog() = _uiState.update { it.copy(showClearDataDialog = true) }
    fun closeClearDataDialog() = _uiState.update { it.copy(showClearDataDialog = false) }

    fun clearAllApplicationData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                appDatabase.clearAllTables()
                preferencesManager.setOnboardingCompleted(false)
                _uiState.update { it.copy(showClearDataDialog = false, feedbackMessage = "All application data cleared.") }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed to clear data: ${e.message}") }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun replaceMeter(
        newName: String,
        newNumber: String?,
        capacity: Int,
        initialBlack: String,
        initialRed: String
    ) {
        val currentMeter = _uiState.value.activeMeter ?: return
        viewModelScope.launch {
            val res = meterReplacementUseCase.replaceMeter(
                currentMeter = currentMeter,
                newMeterName = newName,
                newMeterNumber = newNumber,
                newBlackCapacity = capacity,
                initialBlackDigits = initialBlack,
                initialRedDigit = initialRed
            )
            if (res.isSuccess) {
                _uiState.update { it.copy(feedbackMessage = "Meter successfully replaced.", showReplacementDialog = false) }
            } else {
                _uiState.update { it.copy(feedbackMessage = "Error: ${res.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openReplacementDialog() = _uiState.update { it.copy(showReplacementDialog = true) }
    fun closeReplacementDialog() = _uiState.update { it.copy(showReplacementDialog = false) }
}
