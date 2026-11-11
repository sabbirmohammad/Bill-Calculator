package com.powerunit.feature.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.reminder.ReminderNotificationHelper
import com.powerunit.core.worker.WorkManagerScheduler
import com.powerunit.core.update.AppUpdateManager
import com.powerunit.domain.model.Meter
import com.powerunit.domain.repository.MeterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val activeMeter: Meter? = null,
    val billingCycleDay: Int = 1,
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "21:00",
    val themeMode: String = "SYSTEM",
    val language: String = "en",
    val useBengaliDigits: Boolean = false,
    val appLockEnabled: Boolean = false,
    val appLockTimeoutSeconds: Int = 0,
    val hideAmounts: Boolean = false,
    val screenshotBlock: Boolean = false,
    val clearDataSuccess: Boolean = false,
    val githubRepo: String = "sabbirmohammad/Bill-Calculator",
    val autoCheckUpdates: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val settingsPreferences: SettingsPreferences,
    private val workManagerScheduler: WorkManagerScheduler,
    private val database: PowerUnitDatabase,
    val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val activeMeter = meterRepository.getActiveMeterSync()
            _uiState.value = _uiState.value.copy(activeMeter = activeMeter)

            combine(
                settingsPreferences.billingCycleDay,
                settingsPreferences.reminderEnabled,
                settingsPreferences.reminderTime,
                settingsPreferences.themeMode,
                settingsPreferences.appLanguage
            ) { cycleDay, reminderEnabled, reminderTime, theme, lang ->
                _uiState.value.copy(
                    billingCycleDay = cycleDay,
                    reminderEnabled = reminderEnabled,
                    reminderTime = reminderTime,
                    themeMode = theme,
                    language = lang
                )
            }.collect { partialState ->
                _uiState.value = partialState
            }
        }

        viewModelScope.launch {
            combine(
                settingsPreferences.useBengaliDigits,
                settingsPreferences.appLockEnabled,
                settingsPreferences.appLockTimeoutSeconds,
                settingsPreferences.hideAmounts,
                settingsPreferences.screenshotBlock
            ) { bnDigits, appLock, lockTimeout, hideAmt, blockScreen ->
                _uiState.value = _uiState.value.copy(
                    useBengaliDigits = bnDigits,
                    appLockEnabled = appLock,
                    appLockTimeoutSeconds = lockTimeout,
                    hideAmounts = hideAmt,
                    screenshotBlock = blockScreen
                )
            }.collect { }
        }

        viewModelScope.launch {
            combine(
                settingsPreferences.githubRepo,
                settingsPreferences.autoCheckUpdates
            ) { repo, autoCheck ->
                Pair(repo, autoCheck)
            }.collect { (repo, autoCheck) ->
                _uiState.value = _uiState.value.copy(
                    githubRepo = repo,
                    autoCheckUpdates = autoCheck
                )
            }
        }
    }

    fun checkForUpdates() {
        appUpdateManager.checkForUpdates(isManual = true)
    }

    fun setGithubRepo(repo: String) {
        viewModelScope.launch {
            settingsPreferences.setGithubRepo(repo)
        }
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setAutoCheckUpdates(enabled)
        }
    }

    fun setBillingCycleDay(day: Int) {
        viewModelScope.launch {
            settingsPreferences.setBillingCycleDay(day)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setReminderEnabled(enabled)
            if (enabled) {
                workManagerScheduler.scheduleDailyReminder(_uiState.value.reminderTime)
            } else {
                workManagerScheduler.cancelDailyReminder()
            }
        }
    }

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            settingsPreferences.setReminderTime(time)
            if (_uiState.value.reminderEnabled) {
                workManagerScheduler.scheduleDailyReminder(time)
            }
        }
    }

    fun sendTestNotification(context: Context) {
        ReminderNotificationHelper.showReminderNotification(context)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsPreferences.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsPreferences.setAppLanguage(lang)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }
    }

    fun setUseBengaliDigits(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUseBengaliDigits(enabled)
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setAppLockEnabled(enabled)
        }
    }

    fun setAppLockTimeoutSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsPreferences.setAppLockTimeoutSeconds(seconds)
        }
    }

    fun setHideAmounts(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setHideAmounts(enabled)
        }
    }

    fun setScreenshotBlock(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setScreenshotBlock(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.readingDao().deleteAllReadings()
            database.budgetDao().deleteAllBudgets()
            _uiState.value = _uiState.value.copy(clearDataSuccess = true)
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(clearDataSuccess = false)
    }
}
