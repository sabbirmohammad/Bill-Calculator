package com.powerunit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.repository.MeterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val meterRepository: MeterRepository
) : ViewModel() {

    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    val themeMode: StateFlow<String> = settingsPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "SYSTEM")

    val appLanguage: StateFlow<String> = settingsPreferences.appLanguage
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    val onboardingCompleted: StateFlow<Boolean> = settingsPreferences.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appLockEnabled: StateFlow<Boolean> = settingsPreferences.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appLockTimeoutSeconds: StateFlow<Int> = settingsPreferences.appLockTimeoutSeconds
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var lastBackgroundTimestamp = 0L

    fun onAppForegrounded(isColdStart: Boolean = false) {
        if (!appLockEnabled.value) {
            _isAppLocked.value = false
            return
        }

        if (isColdStart) {
            _isAppLocked.value = true
            return
        }

        val now = System.currentTimeMillis()
        val timeoutMs = appLockTimeoutSeconds.value * 1000L
        if (lastBackgroundTimestamp > 0L && (now - lastBackgroundTimestamp) >= timeoutMs) {
            _isAppLocked.value = true
        }
    }

    fun onAppBackgrounded() {
        if (appLockEnabled.value) {
            lastBackgroundTimestamp = System.currentTimeMillis()
        }
    }

    fun unlockApp() {
        _isAppLocked.value = false
        lastBackgroundTimestamp = 0L
    }

    init {
        viewModelScope.launch {
            // Hard timeout of 1500ms so splash screen can never hang
            val timeoutJob = launch {
                delay(1500)
                _isAppReady.value = true
            }

            try {
                // Wait for the first emission of preferences and initial active meter
                combine(
                    settingsPreferences.themeMode,
                    settingsPreferences.appLanguage,
                    settingsPreferences.onboardingCompleted,
                    settingsPreferences.appLockEnabled,
                    meterRepository.getActiveMeter()
                ) { _ ->
                    true
                }.first()

                timeoutJob.cancel()
                _isAppReady.value = true
            } catch (_: Exception) {
                _isAppReady.value = true
            }
        }
    }
}
