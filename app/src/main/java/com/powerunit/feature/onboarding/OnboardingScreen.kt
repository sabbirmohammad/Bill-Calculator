package com.powerunit.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.domain.model.MeterType

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
            .padding(24.dp)
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
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            shape = ButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Next", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SetupWizard(
    state: OnboardingUiState,
    onMeterNameChange: (String) -> Unit,
    onMeterTypeChange: (MeterType) -> Unit,
    onInitialReadingChange: (String) -> Unit,
    onCycleDayChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup Your Meter",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your current meter details to get started. You can change these anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.meterName,
            onValueChange = onMeterNameChange,
            label = { Text("Meter Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Meter Type", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.cycleDay.toString(),
            onValueChange = { str -> str.toIntOrNull()?.let { onCycleDayChange(it) } },
            label = { Text("Billing Cycle Start Day (1 - 28)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            shape = ButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_finish_btn"),
            enabled = !state.isSaving && state.initialReading.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = if (state.isSaving) "Setting up..." else "Get Started",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
