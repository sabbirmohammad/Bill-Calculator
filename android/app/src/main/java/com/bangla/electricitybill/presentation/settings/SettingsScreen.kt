package com.bangla.electricitybill.presentation.settings

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.BuildConfig
import com.bangla.electricitybill.presentation.components.TariffEditorDialog
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.update.AppUpdateViewModel
import com.bangla.electricitybill.presentation.update.UpdateUiState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateViewModel: AppUpdateViewModel? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val updateState by updateViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(UpdateUiState.Idle) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(updateState) {
        when (val uState = updateState) {
            is UpdateUiState.UpToDate -> {
                snackbarHostState.showSnackbar("Your app is on the latest version (v${BuildConfig.VERSION_NAME})")
                updateViewModel?.dismiss()
            }
            is UpdateUiState.Error -> {
                snackbarHostState.showSnackbar(uState.message)
            }
            else -> Unit
        }
    }

    // Tariff Editor Dialog
    if (state.showTariffDialog && state.activeTariff != null) {
        TariffEditorDialog(
            tariff = state.activeTariff!!,
            onDismiss = { viewModel.closeTariffDialog() },
            onSaveTariff = { viewModel.saveUpdatedTariff(it) }
        )
    }

    // Clear Data Confirmation Dialog
    if (state.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeClearDataDialog() },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This action will permanently delete all recorded meter readings, billing periods, and custom configuration. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllApplicationData(onComplete = {})
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Clear Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Meter Replacement Dialog
    if (state.showReplacementDialog) {
        var newName by remember { mutableStateOf("Main Meter") }
        var newNumber by remember { mutableStateOf("") }
        var capacity by remember { mutableStateOf(5) }
        var initialBlack by remember { mutableStateOf("00000") }
        var initialRed by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { viewModel.closeReplacementDialog() },
            title = { Text("Replace Electricity Meter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Historical records of previous meter will be archived, and new meter tracking will begin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Meter Name") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newNumber,
                        onValueChange = { newNumber = it },
                        label = { Text("Meter Number (Optional)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = initialBlack,
                            onValueChange = { initialBlack = it },
                            label = { Text("Initial Black Digits") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = initialRed,
                            onValueChange = { initialRed = it.take(1) },
                            label = { Text("Red") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.replaceMeter(
                            newName = newName,
                            newNumber = newNumber.ifBlank { null },
                            capacity = capacity,
                            initialBlack = initialBlack,
                            initialRed = initialRed
                        )
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Replacement")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeReplacementDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 9: Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }

            // Section 9.1: Meter Settings
            item {
                val meter = state.activeMeter
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(1),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Meter Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Name: ${meter?.meterName ?: "Default Main Meter"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryText
                        )
                        if (!meter?.meterNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Meter Number: ${meter?.meterNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Configuration: ${meter?.blackDigitCapacity ?: 5} Black Digits + 1 Red Decimal Digit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.openReplacementDialog() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChangeCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Meter Replacement Wizard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 9.2: Electricity Tariff Configuration
            item {
                val tariff = state.activeTariff
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(2),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Electricity Tariff",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LightPurple)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Slab-Based (LT-A)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Configured with progressive step rates, monthly demand charge, meter rent, and 5% VAT.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { viewModel.openTariffDialog() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Tariff & Unit Prices", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        }
                    }
                }
            }

            // Section 9.3: Billing & Preferences
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(3),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Language:", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.appLanguage == "en",
                                onClick = { viewModel.setLanguage("en") },
                                label = { Text("English") },
                                shape = RoundedCornerShape(10.dp)
                            )
                            FilterChip(
                                selected = state.appLanguage == "bn",
                                onClick = { viewModel.setLanguage("bn") },
                                label = { Text("বাংলা (Bangla)") },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Theme:", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.appTheme == "SYSTEM",
                                onClick = { viewModel.setTheme("SYSTEM") },
                                label = { Text("System") },
                                shape = RoundedCornerShape(10.dp)
                            )
                            FilterChip(
                                selected = state.appTheme == "LIGHT",
                                onClick = { viewModel.setTheme("LIGHT") },
                                label = { Text("Light") },
                                shape = RoundedCornerShape(10.dp)
                            )
                            FilterChip(
                                selected = state.appTheme == "DARK",
                                onClick = { viewModel.setTheme("DARK") },
                                label = { Text("Dark") },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Daily Reminder Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Reading Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Text(
                                    text = "Daily notification at 8:00 PM to record meter digits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            Switch(
                                checked = state.reminderEnabled,
                                onCheckedChange = { viewModel.toggleReminder(it) }
                            )
                        }
                    }
                }
            }

            // Section 9.4: Data Management & Backup
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(4),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Data Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Export your reading history or clear application data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.exportCsv { csvContent ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, csvContent)
                                            type = "text/csv"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share CSV Export"))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.exportJson { jsonContent ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, jsonContent)
                                            type = "application/json"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share JSON Backup"))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup JSON", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.openClearDataDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Application Data", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Section 9.5: About App & OTA Update
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(5),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightPurple.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, CardOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "About Application",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = LightPurple
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Native Android Electricity Meter Reading & Bill Calculator with exact BigDecimal progressive slab tariff calculation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { updateViewModel?.checkForUpdates(isManual = true) },
                            enabled = updateState !is UpdateUiState.Checking && updateState !is UpdateUiState.Downloading,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (updateState is UpdateUiState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryPurple
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking for updates...", color = PrimaryPurple)
                            } else {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check for Updates", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
