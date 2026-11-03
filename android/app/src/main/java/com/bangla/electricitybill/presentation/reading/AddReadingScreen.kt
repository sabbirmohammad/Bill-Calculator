package com.bangla.electricitybill.presentation.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.presentation.components.AddReadingDateCard
import com.bangla.electricitybill.presentation.components.AddReadingTopBar
import com.bangla.electricitybill.presentation.components.MechanicalMeterGraphic
import com.bangla.electricitybill.presentation.components.MeterDigitInputSection
import com.bangla.electricitybill.presentation.components.ReadingSummaryCard
import com.bangla.electricitybill.presentation.components.tactileScale
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.WarningAmber
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    viewModel: AddReadingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val saveInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) {
            onNavigateBack()
        }
    }

    // Meter Rollover Confirmation Dialog
    if (state.showRolloverDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRolloverDialog() },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber) },
            title = { Text(text = "Meter Rollover Detected", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "The entered reading (${state.rolloverValue} kWh) is lower than the previous reading (${state.previousReading?.normalizedReading} kWh). Did your mechanical meter roll over past its maximum capacity?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissRolloverDialog()
                        viewModel.saveReading(isRolloverConfirmed = true)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Confirm Rollover")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRolloverDialog() }) {
                    Text("Cancel & Fix")
                }
            }
        )
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.readingDateTime.atZone(ZoneId.of("Asia/Dhaka")).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("Asia/Dhaka"))
                                .toLocalDate()
                            viewModel.updateReadingDateTime(localDate.atTime(12, 0))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            AddReadingTopBar(
                onNavigateBack = onNavigateBack,
                title = if (state.isBangla) "মিটার রিডিং যোগ করুন" else "Add Meter Reading"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Screen 2 Section B: Date Selection Card
            val formattedDate = DateUtils.formatDate(state.readingDateTime)
            val dateLabel = when (state.readingDateTime.toLocalDate()) {
                DateUtils.today() -> if (state.isBangla) "আজ, ${com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(formattedDate)}" else "Today, $formattedDate"
                else -> if (state.isBangla) com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(formattedDate) else formattedDate
            }
            val badgeLabel = if (state.isBangla) {
                if (state.isAutoDetectedDate) "স্বয়ংক্রিয় তারিখ" else "কাস্টম তারিখ"
            } else {
                if (state.isAutoDetectedDate) "Auto-detected" else "Custom Date"
            }

            AddReadingDateCard(
                dateText = dateLabel,
                badgeText = badgeLabel,
                onClick = { showDatePicker = true },
                modifier = Modifier.staggeredEntrance(0)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Screen 2 Section C: Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = if (state.isBangla) "বর্তমান মিটার রিডিং লিখুন" else "Enter Current Meter Reading",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Screen 2 Section C: Mechanical Meter Instruction Graphic
            val capacity = state.activeMeter?.blackDigitCapacity ?: 5
            val fallbackBlack = state.previousReading?.blackDigits ?: "0".repeat(capacity)
            val fallbackRed = state.previousReading?.redDigit ?: "0"
            MechanicalMeterGraphic(
                blackDigits = state.blackDigits.ifEmpty { fallbackBlack },
                redDigit = state.redDigit.ifEmpty { fallbackRed },
                capacity = capacity,
                modifier = Modifier.staggeredEntrance(2)
            )

            // Screen 2 Section D: Reading Input Component (Black Digits + Red Decimal Digit)
            MeterDigitInputSection(
                blackDigits = state.blackDigits,
                redDigit = state.redDigit,
                onBlackDigitsChange = viewModel::updateBlackDigits,
                onRedDigitChange = viewModel::updateRedDigit,
                modifier = Modifier.staggeredEntrance(3),
                isBangla = state.isBangla
            )

            // Validation Message Display
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Screen 2 Section E: Live Calculation Preview & Reading Summary Card
            val prevText = state.previousReading?.let {
                if (state.isBangla) {
                    "${com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(it.blackDigits)}.${com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(it.redDigit)} kWh"
                } else {
                    "${it.blackDigits}.${it.redDigit} kWh"
                }
            } ?: (if (state.isBangla) "কোনো পূর্ববর্তী রিডিং নেই" else "No previous reading")

            val unitsTodayText = state.previewDeltaUnits?.let {
                BigDecimalMath.formatUnits(it, state.isBangla)
            }

            val costTodayText = state.previewEnergyCost?.let {
                BigDecimalMath.formatCurrency(it, state.isBangla)
            }

            ReadingSummaryCard(
                previousReadingText = prevText,
                unitsTodayText = unitsTodayText,
                costTodayText = costTodayText,
                modifier = Modifier.staggeredEntrance(4),
                isBangla = state.isBangla
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Screen 2 Section G: Save Button
            Button(
                onClick = { viewModel.saveReading() },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .tactileScale(saveInteractionSource)
                    .staggeredEntrance(5),
                shape = RoundedCornerShape(16.dp),
                interactionSource = saveInteractionSource
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = if (state.isBangla) "রিডিং সংরক্ষণ করুন" else "Save Reading",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
