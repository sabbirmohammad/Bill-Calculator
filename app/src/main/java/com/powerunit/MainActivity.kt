package com.powerunit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.ui.PowerUnitAppScaffold
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        observeSecuritySettings()

        setContent {
            WattWiseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PowerUnitAppScaffold()
                }
            }
        }
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
