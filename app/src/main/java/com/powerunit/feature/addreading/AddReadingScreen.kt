package com.powerunit.feature.addreading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.powerunit.core.designsystem.component.WattWiseSwitch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    if (state.showConfirmDialog) {
        ReadingConfirmDialog(
            message = state.confirmDialogMessage,
            onConfirm = { viewModel.saveReading(forceSave = true) },
            onDismiss = { viewModel.onDismissConfirmDialog() }
        )
    }

    // Animated Success Celebration Dialog
    if (state.showSuccessSheet) {
        ReadingSuccessCelebrationDialog(
            units = state.calculatedDelta ?: BigDecimal.ZERO,
            estimatedCost = state.estimatedDeltaCost,
            recordedAt = state.recordedAt,
            onDismiss = {
                viewModel.dismissSuccessSheet()
                val deltaStr = state.calculatedDelta?.let { Formatters.formatUnits(it) } ?: "0.0"
                onReadingSaved("Saved! $deltaStr units recorded.")
                onNavigateBack()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                    ) {
                        Text(
                            text = when {
                                state.isInitialReading -> stringResource(R.string.btn_set_initial_reading)
                                state.readingId != null -> "Edit Reading"
                                else -> "Add Reading"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("add_reading_title")
                        )
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = state.meterName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_reading_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimens.level3,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacingSm)
                ) {
                    val isValid = state.readingInput.isNotBlank() &&
                        state.readingInput.toBigDecimalOrNull() != null &&
                        !state.isSaving

                    val delta = state.calculatedDelta
                    val buttonText = when {
                        state.isSaving -> "Saving..."
                        delta != null && delta > BigDecimal.ZERO -> "Save Reading • +${Formatters.formatUnits(delta)} kWh"
                        else -> "Save Reading"
                    }

                    Button(
                        onClick = { viewModel.saveReading() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightPrimary)
                            .testTag("save_reading_btn"),
                        enabled = isValid,
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.xs))
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
        ) {
            // Initial Reading Card or Previous Reading Bento
            if (state.isInitialReading) {
                InitialReadingInfoCard()
            } else if (state.previousReadingValue != null) {
                ModernPreviousReadingCard(
                    value = state.previousReadingValue!!,
                    date = state.previousReadingDate
                )
            }

            // Modern Current Reading Hero Card with Live Calculation Ticker
            ModernReadingInputSection(
                input = state.readingInput,
                onInputChange = viewModel::onReadingInputChange,
                delta = state.calculatedDelta,
                estimatedCost = state.estimatedDeltaCost,
                isMeterReset = state.isMeterReset
            )

            // Smart Unified Date & Time Card with quick preset pills
            ModernDateTimeCard(
                recordedAt = state.recordedAt,
                onDatePreset = viewModel::onDatePreset,
                onOpenDatePicker = { showDatePicker = true },
                onDateChange = viewModel::onDateChange
            )

            // Quick Note Tags and Custom Note
            ModernQuickNotesSection(
                note = state.note,
                onNoteChange = viewModel::onNoteChange,
                onTagSelect = viewModel::onQuickTagSelect
            )

            // Meter Reset or Replaced Accordion Card
            ModernMeterResetCard(
                isMeterReset = state.isMeterReset,
                onMeterResetChange = viewModel::onMeterResetChange
            )

            Spacer(modifier = Modifier.height(Dimens.spacingLg))
        }
    }
}

/**
 * Modern Previous Reading Card with visual depth and baseline indicator
 */
@Composable
private fun ModernPreviousReadingCard(value: BigDecimal, date: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.iconToTextGap)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconContainerSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeStandard)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.previous_reading_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatUnits(value)} kWh",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (date != null) {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Modern Reading Input Section with Live Delta and Live Cost Ticker
 */
@Composable
private fun ModernReadingInputSection(
    input: String,
    onInputChange: (String) -> Unit,
    delta: BigDecimal?,
    estimatedCost: BigDecimal?,
    isMeterReset: Boolean
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
                .padding(Dimens.cardPaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_meter_reading_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reading_input_field"),
                textStyle = NumberLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        text = "0.0",
                        style = NumberLarge.copy(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(end = Dimens.spacingSm)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
                    ) {
                        Text(
                            text = "kWh",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Real-Time Live Calculation Ticker
            AnimatedVisibility(
                visible = delta != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (delta != null) {
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    val isPositive = delta >= BigDecimal.ZERO

                    if (isPositive) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.spacingMd, vertical = Dimens.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(Dimens.iconSizeSmall)
                                )
                                Text(
                                    text = "+${Formatters.formatUnits(delta)} kWh",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                if (estimatedCost != null && estimatedCost > BigDecimal.ZERO) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "≈ ${Formatters.formatMoney(estimatedCost)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    } else if (!isMeterReset) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.spacingMd, vertical = Dimens.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(Dimens.iconSizeSmall)
                                )
                                Text(
                                    text = "Lower than previous reading",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Smart Unified Date & Time Card with quick preset pills
 */
@Composable
private fun ModernDateTimeCard(
    recordedAt: kotlinx.datetime.Instant,
    onDatePreset: (Boolean) -> Unit,
    onOpenDatePicker: () -> Unit,
    onDateChange: (kotlinx.datetime.Instant) -> Unit
) {
    val context = LocalContext.current
    val currentDhakaTime = recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reading_date_time_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
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
                    Box(
                        modifier = Modifier
                            .size(Dimens.iconContainerSize)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.entry_date_and_time),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                        ) {
                            Text(
                                text = DateUtils.formatDate(recordedAt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("reading_date_display")
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
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
                }

                // Edit Icon
                IconButton(
                    onClick = onOpenDatePicker,
                    modifier = Modifier.testTag("change_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            }

            // Quick Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                Surface(
                    onClick = { onDatePreset(true) },
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    onClick = { onDatePreset(false) },
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.yesterday),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
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
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("change_time_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Set Time",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Quick Notes and Custom Note Section
 */
@Composable
private fun ModernQuickNotesSection(
    note: String,
    onNoteChange: (String) -> Unit,
    onTagSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            Text(
                text = stringResource(R.string.quick_notes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Horizontal Scrollable Quick Tags
            val tags = listOf(
                stringResource(R.string.tag_ac),
                stringResource(R.string.tag_guests),
                stringResource(R.string.tag_outage),
                stringResource(R.string.tag_away),
                stringResource(R.string.tag_normal)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                tags.forEach { tag ->
                    val isSelected = note.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagSelect(tag) },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = PillShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = {
                    Text(
                        text = "Add custom note (optional)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = InputShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Modern Switch Card for Meter Reset or Replacement.
 * The entire row is clickable (48dp+ touch target), with label on the left
 * and unified WattWiseSwitch on the right.
 */
@Composable
internal fun ModernMeterResetCard(
    isMeterReset: Boolean,
    onMeterResetChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meter_reset_row")
            .clickable { onMeterResetChange(!isMeterReset) },
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isMeterReset) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.ui.unit.max(48.dp, Dimens.iconContainerSize)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.iconToTextGap)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.iconContainerSize)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.meter_reset_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.meter_reset_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.spacingSm))

                WattWiseSwitch(
                    checked = isMeterReset,
                    onCheckedChange = onMeterResetChange,
                    modifier = Modifier.testTag("meter_reset_switch")
                )
            }

            if (isMeterReset) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
                ) {
                    Text(
                        text = stringResource(R.string.meter_reset_warning_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Animated Celebration Dialog shown after reading is saved
 */
@Composable
private fun ReadingSuccessCelebrationDialog(
    units: BigDecimal,
    estimatedCost: BigDecimal?,
    recordedAt: kotlinx.datetime.Instant,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var animationTrigger by remember { mutableStateOf(false) }

    val scaleAnim = remember { Animatable(0.2f) }
    var countdownProgress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        animationTrigger = true
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = 300f
            )
        )
    }

    // Auto-dismiss in 3 seconds with smooth progress
    LaunchedEffect(Unit) {
        val totalMillis = 3000L
        val interval = 50L
        var elapsed = 0L
        while (elapsed < totalMillis) {
            delay(interval)
            elapsed += interval
            countdownProgress = (totalMillis - elapsed).toFloat() / totalMillis
        }
        onDismiss()
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() }
                .padding(Dimens.screenHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scaleAnim.value)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                ) {
                    // Pulsing Checkmark
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A).copy(alpha = pulseAlpha))
                        )

                        // Center solid circle
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF16A34A), Color(0xFF0EA5A4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.reading_recorded_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Dimens.xxs))
                        Text(
                            text = stringResource(R.string.reading_recorded_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Bento Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spacingMd),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Units Used",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${Formatters.formatUnits(units)} kWh",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Est. Cost",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (estimatedCost != null && estimatedCost > BigDecimal.ZERO) Formatters.formatMoney(estimatedCost) else "৳0.00",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Countdown bar
                    LinearProgressIndicator(
                        progress = { countdownProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF16A34A),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Done Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightPrimary),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
