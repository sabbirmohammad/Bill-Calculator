package com.bangla.electricitybill.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.domain.repository.MeterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashNavigationState {
    object Checking : SplashNavigationState()
    object NavigateToDashboard : SplashNavigationState()
    object PromptAddMeter : SplashNavigationState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Checking)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    init {
        checkMeterAndStatus()
    }

    fun checkMeterAndStatus() {
        viewModelScope.launch {
            _navigationState.value = SplashNavigationState.Checking
            // Gentle branded delay so the user experiences the splash brand animation smoothly
            delay(1000)

            val activeMeter = meterRepository.getActiveMeter()
            val isOnboardingCompleted = preferencesManager.isOnboardingCompleted.first()

            if (activeMeter != null && isOnboardingCompleted) {
                _navigationState.value = SplashNavigationState.NavigateToDashboard
            } else {
                _navigationState.value = SplashNavigationState.PromptAddMeter
            }
        }
    }
}
