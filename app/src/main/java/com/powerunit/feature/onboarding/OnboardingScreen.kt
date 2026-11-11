package com.powerunit.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.DateUtils
import com.powerunit.core.designsystem.component.WattWiseDatePickerDialog
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.domain.model.MeterType
import kotlinx.datetime.Instant

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isCompleted) {
        onFinished()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.spacingXl)
    ) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingContent"
        ) { currentStep ->
            when (currentStep) {
                0 -> WelcomeSlide(
                    icon = Icons.Default.ElectricBolt,
                    title = "Track Electricity Daily",
                    description = "Take control of your power bills. Simply record your meter reading once a day to see exact units used and costs in BDT ৳.",
                    onNext = { viewModel.onNextStep() }
                )
                1 -> WelcomeSlide(
                    icon = Icons.Default.Insights,
                    title = "Accurate Slab Calculations",
                    description = "Automatic calculation based on official Bangladesh residential tariff slabs. See your bill so far and projected month-end totals.",
                    onNext = { viewModel.onNextStep() }
                )
                2 -> WelcomeSlide(
                    icon = Icons.Default.Savings,
                    title = "Stay Within Budget",
                    description = "100% offline, privacy-first tracker. Set monthly unit limits, get budget warnings, and prevent surprise electricity bills.",
                    onNext = { viewModel.onNextStep() }
                )
                else -> SetupWizard(
                    state = state,
                    onMeterNameChange = { viewModel.onMeterNameChange(it) },
                    onMeterTypeChange = { viewModel.onMeterTypeChange(it) },
                    onInitialReadingChange = { viewModel.onInitialReadingChange(it) },
                    onInitialReadingDateChange = { viewModel.onInitialReadingDateChange(it) },
                    onCycleDayChange = { viewModel.onCycleDayChange(it) },
                    onFinish = { viewModel.completeOnboarding() }
                )
            }
        }
    }
}

@Composable
private fun WelcomeSlide(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.huge)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingXxl))

        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimens.spacingMd)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeightPrimary)
                .testTag("onboarding_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Next", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SetupWizard(
    state: OnboardingUiState,
    onMeterNameChange: (String) -> Unit,
    onMeterTypeChange: (MeterType) -> Unit,
    onInitialReadingChange: (String) -> Unit,
    onInitialReadingDateChange: (Instant) -> Unit,
    onCycleDayChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        WattWiseDatePickerDialog(
            initialEpochMillis = DateUtils.instantToUtcEpochMillis(state.initialReadingDate),
            onDateSelected = { selectedLocalDate ->
                val updated = DateUtils.combineDateWithTime(selectedLocalDate, state.initialReadingDate)
                onInitialReadingDateChange(updated)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup Your Meter",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimens.xs))

        Text(
            text = "Enter your current meter details to get started. You can change these anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.spacingXl))

        OutlinedTextField(
            value = state.meterName,
            onValueChange = onMeterNameChange,
            label = { Text("Meter Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        Text(text = "Meter Type", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.xs)) {
            FilterChip(
                selected = state.meterType == MeterType.POSTPAID,
                onClick = { onMeterTypeChange(MeterType.POSTPAID) },
                label = { Text("Postpaid (Monthly Bill)") }
            )
            FilterChip(
                selected = state.meterType == MeterType.PREPAID,
                onClick = { onMeterTypeChange(MeterType.PREPAID) },
                label = { Text("Prepaid Card") }
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        OutlinedTextField(
            value = state.initialReading,
            onValueChange = onInitialReadingChange,
            label = { Text("Current Meter Reading (kWh)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("initial_reading_field"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .testTag("onboarding_date_card"),
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingMd, vertical = Dimens.spacingSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Column {
                        Text(
                            text = "Reading Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatDate(state.initialReadingDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = ButtonShape,
                    modifier = Modifier.testTag("onboarding_change_date_btn")
                ) {
                    Text(text = "Change Date", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        OutlinedTextField(
            value = state.cycleDay.toString(),
            onValueChange = { str -> str.toIntOrNull()?.let { onCycleDayChange(it) } },
            label = { Text("Billing Cycle Start Day (1 - 28)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingXxl))

        Button(
            onClick = onFinish,
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeightPrimary)
                .testTag("onboarding_finish_btn"),
            enabled = !state.isSaving && state.initialReading.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = if (state.isSaving) "Setting up..." else "Get Started",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
