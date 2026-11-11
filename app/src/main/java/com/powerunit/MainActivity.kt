package com.powerunit

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.ui.PowerUnitAppScaffold
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.isAppReady.value
        }

        observeSecuritySettings()

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val appLanguage by mainViewModel.appLanguage.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            val locale = remember(appLanguage) {
                if (appLanguage == "bn") Locale.forLanguageTag("bn-BD") else Locale.ENGLISH
            }
            val configuration = LocalConfiguration.current
            val localizedConfig = remember(appLanguage, configuration) {
                Configuration(configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }

            LaunchedEffect(appLanguage) {
                val conf = resources.configuration
                conf.setLocale(locale)
                conf.setLayoutDirection(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(conf, resources.displayMetrics)
            }

            val isAppLocked by mainViewModel.isAppLocked.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig
            ) {
                WattWiseTheme(darkTheme = isDarkTheme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (isAppLocked) {
                            com.powerunit.core.security.AppLockScreen(
                                onUnlock = mainViewModel::unlockApp
                            )
                        } else {
                            PowerUnitAppScaffold()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.onAppForegrounded(isColdStart = false)
    }

    override fun onStop() {
        super.onStop()
        mainViewModel.onAppBackgrounded()
    }

    private fun observeSecuritySettings() {
        lifecycleScope.launch {
            settingsPreferences.screenshotBlock.collect { block ->
                if (block) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
    }
}
