package com.powerunit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.ui.PowerUnitAppScaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WattWiseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PowerUnitAppScaffold()
                }
            }
        }
    }
}
