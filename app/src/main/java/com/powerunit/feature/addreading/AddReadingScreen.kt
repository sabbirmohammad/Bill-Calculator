package com.powerunit.feature.addreading

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.InputShape
import com.powerunit.core.designsystem.theme.MeterDisplayLarge
import com.powerunit.core.designsystem.theme.PillShape
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    onNavigateBack: () -> Unit,
    onReadingSaved: (String) -> Unit = {},
    viewModel: AddReadingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            val deltaStr = state.calculatedDelta?.let { Formatters.formatUnits(it) } ?: "0.0"
            onReadingSaved("Saved! $deltaStr units recorded.")
            onNavigateBack()
        }
    }

    if (state.showConfirmDialog) {
        ReadingConfirmDialog(
            message = state.confirmDialogMessage,
            onConfirm = { viewModel.saveReading(forceSave = true) },
            onDismiss = { viewModel.onDismissConfirmDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.readingId != null) "Edit Reading" else "Add Reading",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_reading_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (state.previousReadingValue != null) {
                PreviousReadingCard(
                    value = state.previousReadingValue!!,
                    date = state.previousReadingDate
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            ReadingInputSection(
                input = state.readingInput,
                onInputChange = viewModel::onReadingInputChange,
                delta = state.calculatedDelta
            )

            Spacer(modifier = Modifier.height(20.dp))

            ReadingOptionsSection(
                recordedAt = state.recordedAt,
                isMeterReset = state.isMeterReset,
                onMeterResetChange = viewModel::onMeterResetChange,
                note = state.note,
                onNoteChange = viewModel::onNoteChange
            )

            Spacer(modifier = Modifier.height(28.dp))

            SaveReadingButton(
                canSave = state.readingInput.isNotBlank() &&
                    state.readingInput.toBigDecimalOrNull() != null &&
                    !state.isSaving,
                isSaving = state.isSaving,
                onSave = { viewModel.saveReading() }
            )
        }
    }
}

@Composable
private fun ReadingConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Verify Reading")
            }
        },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Edit Reading")
            }
        }
    )
}

@Composable
private fun PreviousReadingCard(value: BigDecimal, date: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Previous Reading",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${Formatters.formatUnits(value)} kWh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (date != null) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReadingInputSection(
    input: String,
    onInputChange: (String) -> Unit,
    delta: BigDecimal?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Meter Reading",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reading_input_field"),
                textStyle = MeterDisplayLarge.copy(fontSize = 36.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                placeholder = {
                    Text(
                        text = "0.0",
                        style = MeterDisplayLarge.copy(
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                trailingIcon = {
                    Text(
                        text = "kWh",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                singleLine = true,
                shape = InputShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (delta != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val isPositive = delta >= BigDecimal.ZERO
                val deltaPrefix = if (isPositive) "+" else ""
                val deltaText = "$deltaPrefix${Formatters.formatUnits(delta)} kWh"

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(
                            if (isPositive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$deltaText since last reading",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingOptionsSection(
    recordedAt: kotlinx.datetime.Instant,
    isMeterReset: Boolean,
    onMeterResetChange: (Boolean) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Date & Time", style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "${DateUtils.formatDate(recordedAt)} ${DateUtils.formatTime(recordedAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isMeterReset,
            onCheckedChange = onMeterResetChange,
            modifier = Modifier.testTag("meter_reset_checkbox")
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Meter Reset or Replaced",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Check if your meter rolled over to 0 or was replaced with a new unit.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Note (optional, e.g. AC used heavily)") },
        modifier = Modifier.fillMaxWidth(),
        shape = InputShape,
        singleLine = true
    )
}

@Composable
private fun SaveReadingButton(
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("save_reading_btn"),
        enabled = canSave,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isSaving) "Saving..." else "Save Reading",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
