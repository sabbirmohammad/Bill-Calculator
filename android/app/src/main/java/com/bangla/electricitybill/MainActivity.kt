package com.bangla.electricitybill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.presentation.navigation.AppNavigation
import com.bangla.electricitybill.presentation.navigation.Screen
import com.bangla.electricitybill.presentation.theme.ElectricityBillTheme
import com.bangla.electricitybill.presentation.update.AppUpdateViewModel
import com.bangla.electricitybill.presentation.update.ModernUpdateDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val updateViewModel: AppUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by preferencesManager.appTheme.collectAsState(initial = "SYSTEM")
            val appLanguage by preferencesManager.appLanguage.collectAsState(initial = "en")
            val updateState by updateViewModel.uiState.collectAsState()
            val context = LocalContext.current

            val isBangla = appLanguage == "bn"

            // Check for updates automatically in the background
            LaunchedEffect(Unit) {
                updateViewModel.checkForUpdates(isManual = false)
            }

            val isDark = when (appTheme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            ElectricityBillTheme(darkTheme = isDark) {
                AppNavigation(
                    startDestination = Screen.Splash.route,
                    updateViewModel = updateViewModel,
                    isBangla = isBangla
                )

                // Modern Update Prompt & Download Progress Dialog
                ModernUpdateDialog(
                    state = updateState,
                    onStartDownload = { updateViewModel.startDownload(it) },
                    onCancelDownload = { updateViewModel.cancelDownload(it) },
                    onInstallApk = { updateViewModel.installApk(context, it) },
                    onRemindLater = { updateViewModel.remindLater(it) },
                    onDismiss = { updateViewModel.dismiss() }
                )
            }
        }
    }
}
