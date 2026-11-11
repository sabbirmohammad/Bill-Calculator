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
