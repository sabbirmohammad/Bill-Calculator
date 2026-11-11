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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.WattWiseDatePickerDialog
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.InputShape
import com.powerunit.core.designsystem.theme.NumberLarge
import com.powerunit.core.designsystem.theme.PillShape
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    onNavigateBack: () -> Unit,
    onReadingSaved: (String) -> Unit = {},
    viewModel: AddReadingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        WattWiseDatePickerDialog(
            initialEpochMillis = DateUtils.instantToUtcEpochMillis(state.recordedAt),
            onDateSelected = { selectedLocalDate ->
                val updated = DateUtils.combineDateWithTime(selectedLocalDate, state.recordedAt)
                viewModel.onDateChange(updated)
            },
            onDismiss = { showDatePicker = false }
        )
    }

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
                        text = when {
                            state.isInitialReading -> stringResource(R.string.btn_set_initial_reading)
                            state.readingId != null -> "Edit Reading"
                            else -> "Add Reading"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("add_reading_title")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_reading_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
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
                .padding(Dimens.screenHorizontalPadding)
        ) {
            if (state.isInitialReading) {
                InitialReadingInfoCard()
                Spacer(modifier = Modifier.height(Dimens.spacingLg))
            } else if (state.previousReadingValue != null) {
                PreviousReadingCard(
                    value = state.previousReadingValue!!,
                    date = state.previousReadingDate
                )
                Spacer(modifier = Modifier.height(Dimens.spacingLg))
            }

            ReadingInputSection(
                input = state.readingInput,
                onInputChange = viewModel::onReadingInputChange,
                delta = state.calculatedDelta
            )

            Spacer(modifier = Modifier.height(Dimens.spacingLg))

            ReadingOptionsSection(
                recordedAt = state.recordedAt,
                isMeterReset = state.isMeterReset,
                onMeterResetChange = viewModel::onMeterResetChange,
                note = state.note,
                onNoteChange = viewModel::onNoteChange,
                onOpenDatePicker = { showDatePicker = true },
                onDateChange = viewModel::onDateChange
            )

            Spacer(modifier = Modifier.height(Dimens.sectionGap))

            SaveReadingButton(
                canSave = state.readingInput.isNotBlank() &&
                    state.readingInput.toBigDecimalOrNull() != null &&
                    !state.isSaving,
                isSaving = state.isSaving,
                onSave = { viewModel.saveReading() }
            )

            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
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
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(text = "Verify Reading", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm & Save", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Edit Reading", style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun InitialReadingInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("initial_reading_info_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeStandard)
            )
            Column {
                Text(
                    text = stringResource(R.string.initial_reading_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.initial_reading_info_banner),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun PreviousReadingCard(value: BigDecimal, date: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (date != null) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
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
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Meter Reading",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reading_input_field"),
                textStyle = NumberLarge.copy(textAlign = TextAlign.Center),
                placeholder = {
                    Text(
                        text = "0.0",
                        style = NumberLarge.copy(
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                trailingIcon = {
                    Text(
                        text = "kWh",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = Dimens.spacingSm)
                    )
                },
                singleLine = true,
                shape = InputShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (delta != null) {
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                val isPositive = delta >= BigDecimal.ZERO
                val deltaPrefix = if (isPositive) "+" else ""
                val deltaText = "$deltaPrefix${Formatters.formatUnits(delta)} kWh"

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(
                            if (isPositive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = Dimens.spacingMd, vertical = Dimens.xxs)
                ) {
                    Text(
                        text = "$deltaText since last reading",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
    onNoteChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onDateChange: (kotlinx.datetime.Instant) -> Unit
) {
    val context = LocalContext.current
    val currentDhakaTime = recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onOpenDatePicker() }
            .testTag("reading_date_time_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.iconToTextGap)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.reading_date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatDate(recordedAt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("reading_date_display")
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenDatePicker,
                    shape = ButtonShape,
                    modifier = Modifier.testTag("change_date_button")
                ) {
                    Text(
                        text = stringResource(R.string.change_date),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.iconToTextGap)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.reading_time),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatTime(recordedAt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("reading_time_display")
                        )
                    }
                }

                TextButton(
                    onClick = {
                        android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val updated = DateUtils.combineWithTime(
                                    date = DateUtils.toLocalDate(recordedAt),
                                    hour = hourOfDay,
                                    minute = minute
                                )
                                onDateChange(updated)
                            },
                            currentDhakaTime.hour,
                            currentDhakaTime.minute,
                            false
                        ).show()
                    },
                    modifier = Modifier.testTag("change_time_button")
                ) {
                    Text(
                        text = stringResource(R.string.change_time),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.spacingMd))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimens.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isMeterReset,
            onCheckedChange = onMeterResetChange,
            modifier = Modifier.testTag("meter_reset_checkbox")
        )
        Spacer(modifier = Modifier.width(Dimens.xs))
        Column {
            Text(
                text = "Meter Reset or Replaced",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Check if your meter rolled over to 0 or was replaced with a new unit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(Dimens.spacingMd))

    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Note (optional, e.g. AC used heavily)", style = MaterialTheme.typography.bodyMedium) },
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
            .height(Dimens.buttonHeightPrimary)
            .testTag("save_reading_btn"),
        enabled = canSave,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.xs))
        Text(
            text = if (isSaving) "Saving..." else "Save Reading",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
