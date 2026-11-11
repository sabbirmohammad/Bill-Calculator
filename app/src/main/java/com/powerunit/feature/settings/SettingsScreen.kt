package com.powerunit.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.NotificationManagerCompat
import java.util.Locale
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.powerunit.core.common.Formatters
import java.math.BigDecimal
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.BuildConfig
import com.powerunit.R
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.security.BiometricAuthHelper
import androidx.fragment.app.FragmentActivity
import com.powerunit.domain.model.Meter

@Composable
fun SettingsScreen(
    onNavigateToTariffEditor: () -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDesignGallery: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var clearDataStep by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(uiState.clearDataSuccess) {
        if (uiState.clearDataSuccess) {
            snackbarHostState.showSnackbar("All readings and budgets cleared successfully")
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
                    .padding(horizontal = Dimens.screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
            ) {
                item {
                    Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    MeterSettingsSection(
                        meter = uiState.activeMeter,
                        useBengaliDigits = uiState.useBengaliDigits,
                        isBangla = uiState.language == "bn"
                    )
                }

                item {
                    BillingSettingsSection(
                        cycleDay = uiState.billingCycleDay,
                        useBengaliDigits = uiState.useBengaliDigits,
                        isBangla = uiState.language == "bn",
                        onCycleDayChange = viewModel::setBillingCycleDay,
                        onNavigateToTariff = onNavigateToTariffEditor
                    )
                }

                item {
                    ReminderSettingsSection(
                        enabled = uiState.reminderEnabled,
                        reminderTime = uiState.reminderTime,
                        useBengaliDigits = uiState.useBengaliDigits,
                        isBangla = uiState.language == "bn",
                        onToggle = viewModel::setReminderEnabled,
                        onTimeChange = viewModel::setReminderTime,
                        onSendTestNotification = { viewModel.sendTestNotification(context) }
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
                        lockTimeoutSeconds = uiState.appLockTimeoutSeconds,
                        hideAmounts = uiState.hideAmounts,
                        blockScreenshots = uiState.screenshotBlock,
                        onAppLockToggle = viewModel::setAppLockEnabled,
                        onLockTimeoutChange = viewModel::setAppLockTimeoutSeconds,
                        onHideAmountsToggle = viewModel::setHideAmounts,
                        onBlockScreenshotsToggle = viewModel::setScreenshotBlock
                    )
                }

                item {
                    DataSettingsSection(
                        onBackupClick = onNavigateToBackup,
                        onClearClick = { clearDataStep = 1 }
                    )
                }

                item {
                    UpdateSettingsSection(
                        currentVersion = "v${BuildConfig.VERSION_NAME}",
                        githubRepo = uiState.githubRepo,
                        autoCheck = uiState.autoCheckUpdates,
                        onCheckUpdates = viewModel::checkForUpdates,
                        onToggleAutoCheck = viewModel::setAutoCheckUpdates,
                        onRepoChange = viewModel::setGithubRepo
                    )
                }

                item {
                    AboutSettingsSection(onNavigateToDesignGallery = onNavigateToDesignGallery)
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
                }
            }
        }
    }

    // Step 1: Warning Dialog
    if (clearDataStep == 1) {
        AlertDialog(
            onDismissRequest = { clearDataStep = null },
            title = { Text(stringResource(R.string.settings_clear_data), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.settings_clear_data_step1_desc), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (uiState.appLockEnabled && activity != null && BiometricAuthHelper.canAuthenticate(context)) {
                            BiometricAuthHelper.authenticate(
                                activity = activity,
                                title = "Authorize Data Reset",
                                subtitle = "Verify identity to proceed with wiping data",
                                onSuccess = { clearDataStep = 2 },
                                onError = { clearDataStep = null }
                            )
                        } else {
                            clearDataStep = 2
                        }
                    },
                    modifier = Modifier.testTag("clear_data_continue_btn")
                ) {
                    Text(stringResource(R.string.action_continue), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { clearDataStep = null }) {
                    Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = CardShape,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Step 2: Final Destructive Confirmation Dialog
    if (clearDataStep == 2) {
        AlertDialog(
            onDismissRequest = { clearDataStep = null },
            title = {
                Text(
                    text = stringResource(R.string.settings_clear_data_step2_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = { Text(stringResource(R.string.settings_clear_data_step2_desc), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        clearDataStep = null
                    },
                    modifier = Modifier.testTag("clear_data_final_btn")
                ) {
                    Text(
                        text = stringResource(R.string.action_wipe_everything),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { clearDataStep = null }) {
                    Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = CardShape,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun MeterSettingsSection(
    meter: Meter?,
    useBengaliDigits: Boolean,
    isBangla: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_meter_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Speed, title = stringResource(R.string.settings_section_meter))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meter?.name ?: if (isBangla) "মূল মিটার" else "Main Meter",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val formattedMultiplier = Formatters.formatDouble((meter?.multiplierFactor ?: BigDecimal.ONE).toDouble(), 1, useBengaliDigits)
                    val typeName = meter?.type?.name ?: "POSTPAID"
                    val typeLabel = if (isBangla) {
                        if (typeName == "POSTPAID") "পোস্টপেইড" else "প্রিপেইড"
                    } else typeName
                    val infoText = if (isBangla) {
                        "ধরন: $typeLabel • গুণক: $formattedMultiplier"
                    } else {
                        "Type: $typeLabel • Multiplier: $formattedMultiplier"
                    }
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodyMedium,
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
    useBengaliDigits: Boolean,
    isBangla: Boolean,
    onCycleDayChange: (Int) -> Unit,
    onNavigateToTariff: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_billing_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Receipt, title = stringResource(R.string.settings_section_billing))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCycleDayChange(if (cycleDay >= 28) 1 else cycleDay + 1) }
                    .padding(vertical = Dimens.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_cycle_start_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val formattedDay = Formatters.formatInt(cycleDay, useBengaliDigits)
                val dayText = if (isBangla) "$formattedDay তারিখ" else "Day $formattedDay"
                Text(
                    text = dayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xs))
            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(Dimens.xs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTariff() }
                    .padding(vertical = Dimens.xxs)
                    .testTag("settings_tariff_editor_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_tariff_editor),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_tariff_editor_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
            }
        }
    }
}

@Composable
private fun ReminderSettingsSection(
    enabled: Boolean,
    reminderTime: String,
    useBengaliDigits: Boolean,
    isBangla: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
    onSendTestNotification: () -> Unit
) {
    val context = LocalContext.current
    val areNotificationsEnabled = remember(enabled) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_reminders_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Alarm, title = stringResource(R.string.settings_section_reminders))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            // Permission Warning Banner if notifications are disabled
            if (enabled && !areNotificationsEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.spacingSm),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(Dimens.spacingSm)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.iconSizeSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text(
                                text = stringResource(R.string.settings_notifications_disabled_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacingXxs))
                        Text(
                            text = stringResource(R.string.settings_notifications_disabled_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingXs))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_enable_notifications),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Reminder Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_reminder_toggle),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "প্রতিদিন মিটার রিডিং লেখার তাগিদ দিন" else "Get a daily alert to record meter reading",
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

            if (enabled) {
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(Dimens.spacingXs))

                // Time Picker Row (12h format dial picker)
                val parts = reminderTime.split(":")
                val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 21
                val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            android.app.TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                    onTimeChange(formatted)
                                },
                                initialHour,
                                initialMinute,
                                false // 12-hour AM/PM format
                            ).show()
                        }
                        .padding(vertical = Dimens.xxs)
                        .testTag("reminder_time_picker_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_reminder_time),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val formattedTime = Formatters.formatTime12h(reminderTime, useBengaliDigits, isBangla)
                        Text(
                            text = if (isBangla) "প্রতিদিন $formattedTime" else "Every day at $formattedTime",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Select reminder time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }

                // Debug Send Test Notification Button
                if (BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    OutlinedButton(
                        onClick = onSendTestNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_notification_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingXs))
                        Text(
                            text = stringResource(R.string.settings_send_test_notification),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Palette, title = stringResource(R.string.settings_section_display))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            SettingToggleRow(
                title = stringResource(R.string.settings_theme_mode),
                subtitle = when (theme) {
                    "LIGHT" -> stringResource(R.string.theme_light)
                    "DARK" -> stringResource(R.string.theme_dark)
                    else -> stringResource(R.string.theme_system)
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
                        Text(
                            when (theme) {
                                "LIGHT" -> stringResource(R.string.theme_light)
                                "DARK" -> stringResource(R.string.theme_dark)
                                else -> stringResource(R.string.theme_system)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            SettingToggleRow(
                title = stringResource(R.string.settings_language),
                subtitle = if (language == "bn") "বাংলা (Bangla)" else "English",
                action = {
                    TextButton(onClick = { onLangChange(if (language == "bn") "en" else "bn") }) {
                        Text(if (language == "bn") "English" else "বাংলা", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            SettingToggleRow(
                title = stringResource(R.string.settings_bengali_digits),
                subtitle = "${Formatters.formatUnits(BigDecimal("12345.6"), useBengaliDigits)} kWh • ${Formatters.formatMoney(BigDecimal("2500.00"), true, useBengaliDigits)}",
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
    lockTimeoutSeconds: Int,
    hideAmounts: Boolean,
    blockScreenshots: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    onLockTimeoutChange: (Int) -> Unit,
    onHideAmountsToggle: (Boolean) -> Unit,
    onBlockScreenshotsToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var showTimeoutDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_security_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Lock, title = stringResource(R.string.settings_section_security))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            SettingToggleRow(
                title = stringResource(R.string.settings_app_lock),
                subtitle = stringResource(R.string.settings_app_lock_desc),
                action = {
                    Switch(
                        checked = appLock,
                        onCheckedChange = { desired ->
                            if (activity != null && BiometricAuthHelper.canAuthenticate(context)) {
                                val actionTitle = if (desired) "Enable App Lock" else "Disable App Lock"
                                BiometricAuthHelper.authenticate(
                                    activity = activity,
                                    title = actionTitle,
                                    subtitle = "Verify identity to confirm lock setting",
                                    onSuccess = { onAppLockToggle(desired) },
                                    onError = { /* Auth canceled or failed, do not toggle */ }
                                )
                            } else {
                                onAppLockToggle(desired)
                            }
                        },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }
            )

            if (appLock) {
                HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

                SettingToggleRow(
                    title = stringResource(R.string.settings_lock_timeout),
                    subtitle = when (lockTimeoutSeconds) {
                        30 -> stringResource(R.string.settings_lock_timeout_30s)
                        60 -> stringResource(R.string.settings_lock_timeout_1m)
                        else -> stringResource(R.string.settings_lock_timeout_instant)
                    },
                    action = {
                        TextButton(
                            onClick = { showTimeoutDialog = true },
                            modifier = Modifier.testTag("app_lock_timeout_btn")
                        ) {
                            Text(
                                when (lockTimeoutSeconds) {
                                    30 -> stringResource(R.string.settings_lock_timeout_30s)
                                    60 -> stringResource(R.string.settings_lock_timeout_1m)
                                    else -> stringResource(R.string.settings_lock_timeout_instant)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            }

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            SettingToggleRow(
                title = stringResource(R.string.settings_hide_amounts),
                subtitle = stringResource(R.string.settings_hide_amounts_desc),
                action = {
                    Switch(
                        checked = hideAmounts,
                        onCheckedChange = onHideAmountsToggle,
                        modifier = Modifier.testTag("hide_amounts_switch")
                    )
                }
            )

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            SettingToggleRow(
                title = stringResource(R.string.settings_screenshot_block),
                subtitle = stringResource(R.string.settings_screenshot_block_desc),
                action = {
                    Switch(
                        checked = blockScreenshots,
                        onCheckedChange = onBlockScreenshotsToggle,
                        modifier = Modifier.testTag("block_screenshots_switch")
                    )
                }
            )

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            // Database Encryption Row (Active status)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.xxs)
                    .testTag("db_encryption_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_db_encryption),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_db_encryption_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacingSm))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Hardware-Backed",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text(stringResource(R.string.settings_lock_timeout), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                    listOf(
                        0 to stringResource(R.string.settings_lock_timeout_instant),
                        30 to stringResource(R.string.settings_lock_timeout_30s),
                        60 to stringResource(R.string.settings_lock_timeout_1m)
                    ).forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLockTimeoutChange(seconds)
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = Dimens.spacingSm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (lockTimeoutSeconds == seconds) FontWeight.Bold else FontWeight.Normal,
                                color = if (lockTimeoutSeconds == seconds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = CardShape,
            containerColor = MaterialTheme.colorScheme.surface
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Delete, title = stringResource(R.string.settings_section_data))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackupClick() }
                    .testTag("settings_backup_row")
                    .padding(vertical = Dimens.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Encrypted Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Export and restore encrypted data (.wattwise)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClearClick() }
                    .testTag("settings_clear_data_row")
                    .padding(vertical = Dimens.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_clear_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Reset readings database",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSettingsSection(
    onNavigateToDesignGallery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_about_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.Info, title = stringResource(R.string.settings_section_about))
            Spacer(modifier = Modifier.height(Dimens.xs))

            val buildTypeStr = BuildConfig.BUILD_TYPE.replaceFirstChar { it.uppercase() }
            val versionInfo = "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})"

            Text(
                text = "WattWise",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.xxs))
            Text(
                text = "$versionInfo • $buildTypeStr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Build Date: ${BuildConfig.BUILD_DATE}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(Dimens.xs))
            Text(
                text = stringResource(R.string.settings_privacy_pledge),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(Dimens.xs))
                HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(Dimens.xs))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDesignGallery() }
                        .padding(vertical = Dimens.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.xs))
                        Column {
                            Text(
                                text = "Design System Gallery (Dev)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tokens, typography, and §5 components preview",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            }
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
            modifier = Modifier.size(Dimens.iconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
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
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = Dimens.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}

@Composable
private fun UpdateSettingsSection(
    currentVersion: String,
    githubRepo: String,
    autoCheck: Boolean,
    onCheckUpdates: () -> Unit,
    onToggleAutoCheck: (Boolean) -> Unit,
    onRepoChange: (String) -> Unit
) {
    var showRepoDialog by remember { mutableStateOf(false) }
    var tempRepo by remember(githubRepo) { mutableStateOf(githubRepo) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_updates_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            SectionTitle(icon = Icons.Default.SystemUpdate, title = stringResource(R.string.settings_section_updates))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            // Check for updates row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheckUpdates() }
                    .padding(vertical = Dimens.xxs)
                    .testTag("settings_check_update_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_check_updates),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current: $currentVersion • Tap to check GitHub",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeStandard)
                )
            }

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            // Auto-check toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_auto_update),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Silent check on app launch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoCheck,
                    onCheckedChange = onToggleAutoCheck
                )
            }

            HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)

            // GitHub Repository row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRepoDialog = true }
                    .padding(vertical = Dimens.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_github_repo),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = githubRepo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
            }
        }
    }

    if (showRepoDialog) {
        AlertDialog(
            onDismissRequest = { showRepoDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_github_repo),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter GitHub repository (owner/repo):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    OutlinedTextField(
                        value = tempRepo,
                        onValueChange = { tempRepo = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempRepo.isNotBlank()) {
                            onRepoChange(tempRepo.trim())
                        }
                        showRepoDialog = false
                    }
                ) {
                    Text("Save", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepoDialog = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = CardShape,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

