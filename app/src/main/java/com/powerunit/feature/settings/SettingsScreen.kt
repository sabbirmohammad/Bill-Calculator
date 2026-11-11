package com.powerunit.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.domain.model.Meter

@Composable
fun SettingsScreen(
    onNavigateToTariffEditor: () -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.clearDataSuccess) {
        if (uiState.clearDataSuccess) {
            snackbarHostState.showSnackbar("All readings cleared successfully")
            viewModel.clearStatus()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(snackbarHostState)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_lazy_column")
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    MeterSettingsSection(meter = uiState.activeMeter)
                }

                item {
                    BillingSettingsSection(
                        cycleDay = uiState.billingCycleDay,
                        onCycleDayChange = viewModel::setBillingCycleDay,
                        onNavigateToTariff = onNavigateToTariffEditor
                    )
                }

                item {
                    ReminderSettingsSection(
                        enabled = uiState.reminderEnabled,
                        reminderTime = uiState.reminderTime,
                        onToggle = viewModel::setReminderEnabled
                    )
                }

                item {
                    DisplaySettingsSection(
                        theme = uiState.themeMode,
                        language = uiState.language,
                        useBengaliDigits = uiState.useBengaliDigits,
                        onThemeChange = viewModel::setThemeMode,
                        onLangChange = viewModel::setLanguage,
                        onBengaliDigitsToggle = viewModel::setUseBengaliDigits
                    )
                }

                item {
                    SecuritySettingsSection(
                        appLock = uiState.appLockEnabled,
                        hideAmounts = uiState.hideAmounts,
                        blockScreenshots = uiState.screenshotBlock,
                        onAppLockToggle = viewModel::setAppLockEnabled,
                        onHideAmountsToggle = viewModel::setHideAmounts,
                        onBlockScreenshotsToggle = viewModel::setScreenshotBlock
                    )
                }

                item {
                    DataSettingsSection(
                        onBackupClick = onNavigateToBackup,
                        onClearClick = { showClearConfirm = true }
                    )
                }

                item {
                    AboutSettingsSection()
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.settings_clear_data)) },
            text = { Text(stringResource(R.string.settings_clear_data_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MeterSettingsSection(meter: Meter?) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_meter_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Speed, title = stringResource(R.string.settings_section_meter))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meter?.name ?: "Main Meter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Type: ${meter?.type?.name ?: "POSTPAID"} • Multiplier: ${meter?.multiplierFactor ?: 1.0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BillingSettingsSection(
    cycleDay: Int,
    onCycleDayChange: (Int) -> Unit,
    onNavigateToTariff: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_billing_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Receipt, title = stringResource(R.string.settings_section_billing))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCycleDayChange(if (cycleDay >= 28) 1 else cycleDay + 1) }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_cycle_start_day),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Day $cycleDay",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTariff() }
                    .padding(vertical = 4.dp)
                    .testTag("settings_tariff_editor_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_tariff_editor),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.settings_tariff_editor_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReminderSettingsSection(
    enabled: Boolean,
    reminderTime: String,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_reminders_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Alarm, title = stringResource(R.string.settings_section_reminders))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_reminder_toggle),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Time: $reminderTime daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("reminder_switch")
                )
            }
        }
    }
}

@Composable
private fun DisplaySettingsSection(
    theme: String,
    language: String,
    useBengaliDigits: Boolean,
    onThemeChange: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onBengaliDigitsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_display_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Palette, title = stringResource(R.string.settings_section_display))
            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_theme_mode),
                subtitle = when (theme) {
                    "LIGHT" -> "Light"
                    "DARK" -> "Dark"
                    else -> "System Default"
                },
                action = {
                    TextButton(onClick = {
                        val next = when (theme) {
                            "SYSTEM" -> "LIGHT"
                            "LIGHT" -> "DARK"
                            else -> "SYSTEM"
                        }
                        onThemeChange(next)
                    }) {
                        Text(when (theme) {
                            "LIGHT" -> "Light"
                            "DARK" -> "Dark"
                            else -> "System"
                        })
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_language),
                subtitle = if (language == "bn") "বাংলা (Bangla)" else "English",
                action = {
                    TextButton(onClick = { onLangChange(if (language == "bn") "en" else "bn") }) {
                        Text(if (language == "bn") "English" else "বাংলা")
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_bengali_digits),
                subtitle = "১২৩৪৫৬৭৮৯০",
                action = {
                    Switch(
                        checked = useBengaliDigits,
                        onCheckedChange = onBengaliDigitsToggle,
                        modifier = Modifier.testTag("bengali_digits_switch")
                    )
                }
            )
        }
    }
}

@Composable
private fun SecuritySettingsSection(
    appLock: Boolean,
    hideAmounts: Boolean,
    blockScreenshots: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    onHideAmountsToggle: (Boolean) -> Unit,
    onBlockScreenshotsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_security_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Lock, title = stringResource(R.string.settings_section_security))
            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_app_lock),
                subtitle = stringResource(R.string.settings_app_lock_desc),
                action = {
                    Switch(
                        checked = appLock,
                        onCheckedChange = onAppLockToggle,
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_hide_amounts),
                subtitle = "Mask currency on cards",
                action = {
                    Switch(
                        checked = hideAmounts,
                        onCheckedChange = onHideAmountsToggle,
                        modifier = Modifier.testTag("hide_amounts_switch")
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SettingToggleRow(
                title = stringResource(R.string.settings_screenshot_block),
                subtitle = "Protect sensitive financial data",
                action = {
                    Switch(
                        checked = blockScreenshots,
                        onCheckedChange = onBlockScreenshotsToggle,
                        modifier = Modifier.testTag("block_screenshots_switch")
                    )
                }
            )
        }
    }
}

@Composable
private fun DataSettingsSection(
    onBackupClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Delete, title = stringResource(R.string.settings_section_data))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackupClick() }
                    .testTag("settings_backup_row")
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Encrypted Backup & Restore",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Export and restore encrypted data (.wattwise)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClearClick() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_clear_data),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Reset readings database",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Default.Info, title = stringResource(R.string.settings_section_about))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "WattWise v1.0.0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_privacy_pledge),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}
