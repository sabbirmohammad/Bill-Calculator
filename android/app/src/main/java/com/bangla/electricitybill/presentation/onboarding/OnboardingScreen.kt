package com.bangla.electricitybill.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.presentation.components.MechanicalMeterDisplay
import com.bangla.electricitybill.presentation.components.tactileScale
import com.bangla.electricitybill.presentation.motion.MotionTokens
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.EnergyEmerald
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.VoltAmber

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Smoothly scroll back to top when step changes
    LaunchedEffect(state.currentStep) {
        scrollState.scrollTo(0)
    }

    val stepTitles = listOf(
        "১. মিটারের নাম",
        "২. বর্তমান ইউনিট",
        "৩. স্ল্যাব ও রেট",
        "৪. অপশনাল চার্জ"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 1) {
                        IconButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Step Indicator Pills
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..4).forEach { step ->
                            val isCurrent = step == state.currentStep
                            val isPassed = step < state.currentStep
                            val indicatorWidth by animateDpAsState(
                                targetValue = if (isCurrent) 28.dp else 8.dp,
                                label = "stepIndicatorWidth"
                            )

                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(indicatorWidth)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isPassed -> EnergyEmerald
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        }
                                    )
                            )
                            if (step < 4) Spacer(modifier = Modifier.width(6.dp))
                        }
                    }

                    // Step Count Badge
                    Text(
                        text = "ধাপ ${state.currentStep}/৪",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Current Step Subtitle Pill
                Text(
                    text = stepTitles.getOrElse(state.currentStep - 1) { "" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.currentStep,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                            animationSpec = MotionTokens.normalTween()
                        ) + fadeIn(animationSpec = MotionTokens.normalTween())).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() },
                                animationSpec = MotionTokens.normalTween()
                            ) + fadeOut(animationSpec = MotionTokens.normalTween())
                        )
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() },
                            animationSpec = MotionTokens.normalTween()
                        ) + fadeIn(animationSpec = MotionTokens.normalTween())).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                                animationSpec = MotionTokens.normalTween()
                            ) + fadeOut(animationSpec = MotionTokens.normalTween())
                        )
                    }
                },
                label = "OnboardingStepContent"
            ) { step ->
                when (step) {
                    // STEP 1: মিটারটার নাম (Meter Name)
                    1 -> Step1MeterNameContent(
                        state = state,
                        onNameChange = viewModel::updateMeterName,
                        onNumberChange = viewModel::updateMeterNumber,
                        onCapacityChange = viewModel::updateBlackCapacity,
                        onNext = { viewModel.nextStep() }
                    )

                    // STEP 2: কারেন্ট ইউনিট কত আছে (Current Units Reading)
                    2 -> Step2CurrentUnitsContent(
                        state = state,
                        onBlackDigitsChange = viewModel::updateBaselineBlackDigits,
                        onRedDigitChange = viewModel::updateBaselineRedDigit,
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )

                    // STEP 3: কয়টা স্ল্যাব চলে এবং কোন স্ল্যাবের রেট কত (Slab Count & Rates)
                    3 -> Step3SlabsAndRatesContent(
                        state = state,
                        onModeChange = viewModel::updateBillingMode,
                        onFlatRateChange = viewModel::updateFlatRate,
                        onSlabRateChange = viewModel::updateSlabRate,
                        onSlabMinChange = viewModel::updateSlabMin,
                        onSlabMaxChange = viewModel::updateSlabMax,
                        onAddSlab = viewModel::addSlab,
                        onRemoveSlab = viewModel::removeSlab,
                        onApplyPreset = viewModel::applyPreset,
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )

                    // STEP 4: আনুষাঙ্গিক সার্ভিস চার্জ, ডিমান্ড চার্জ ও ভ্যাট - অপশনাল
                    4 -> Step4OptionalChargesContent(
                        state = state,
                        onServiceChargeToggle = viewModel::setServiceChargeEnabled,
                        onServiceChargeChange = viewModel::updateServiceCharge,
                        onDemandChargeToggle = viewModel::setDemandChargeEnabled,
                        onDemandChargeChange = viewModel::updateDemandCharge,
                        onMeterRentToggle = viewModel::setMeterRentEnabled,
                        onMeterRentChange = viewModel::updateMeterRent,
                        onVatToggle = viewModel::setVatEnabled,
                        onVatChange = viewModel::updateVatPercentage,
                        onComplete = { viewModel.completeOnboarding(onComplete) },
                        onBack = { viewModel.previousStep() }
                    )
                }
            }
        }
    }
}

// ========================================================================
// STEP 1: মিটারটার নাম চাবে (Screen 1: Meter Name & Info)
// ========================================================================
@Composable
private fun Step1MeterNameContent(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onCapacityChange: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricMeter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "মিটারের নাম দিন",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "আপনার বিদ্যুৎ মিটারের একটি নাম দিন। পরবর্তীতে একাধিক মিটারের ক্ষেত্রে এটি সহজে চিহ্নিত করতে সাহায্য করবে।",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Meter Name Input Field (Required)
        OutlinedTextField(
            value = state.meterName,
            onValueChange = onNameChange,
            label = { Text("মিটারের নাম (Meter Name) *") },
            placeholder = { Text("যেমন: বাসা - মূল মিটার / দোকান / ফ্ল্যাট ৩") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            isError = state.errorMessage != null && state.meterName.isBlank()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Meter Number (Optional)
        OutlinedTextField(
            value = state.meterNumber,
            onValueChange = onNumberChange,
            label = { Text("মিটার নম্বর (ঐচ্ছিক / Optional)") },
            placeholder = { Text("যেমন: 1045892") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Meter Black Wheels Capacity
        Text(
            text = "কালো চাকার সংখ্যা (Black Digit Wheels):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(4, 5, 6, 7).forEach { capacity ->
                FilterChip(
                    selected = state.blackDigitCapacity == capacity,
                    onClick = { onCapacityChange(capacity) },
                    label = {
                        Text(
                            text = "$capacity ডিজিট",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .tactileScale(buttonInteractionSource),
            shape = RoundedCornerShape(14.dp),
            interactionSource = buttonInteractionSource
        ) {
            Text("পরবর্তী ধাপ: বর্তমান ইউনিট (Next)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ========================================================================
// STEP 2: কত কারেন্ট ইউনিট কত আছে (Screen 2: Current Units / Reading)
// ========================================================================
@Composable
private fun Step2CurrentUnitsContent(
    state: OnboardingUiState,
    onBlackDigitsChange: (String) -> Unit,
    onRedDigitChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(VoltAmber.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = VoltAmber,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "বর্তমান কারেন্ট ইউনিট কত আছে?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "বর্তমানে মিটারের চাকা বা ডিসপ্লেতে মোট কত ইউনিট উঠেছে তা নিচে লিখুন। এটি আপনার প্রারম্ভিক বেসলাইন রিডিং হিসেবে সংরক্ষিত থাকবে।",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Mechanical Meter Display
        MechanicalMeterDisplay(
            blackDigits = state.baselineBlackDigits,
            redDigit = state.baselineRedDigit,
            capacity = state.blackDigitCapacity,
            subtitle = "বাম থেকে ডানে কালো ডিজিট (kWh), শেষে লাল দশমিক চাকা"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input Fields Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.baselineBlackDigits,
                onValueChange = onBlackDigitsChange,
                label = { Text("কারেন্ট ইউনিট (kWh) *") },
                placeholder = { Text("12500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(2.2f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.baselineRedDigit,
                onValueChange = onRedDigitChange,
                label = { Text("লাল চাকা (০.১)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Explanatory Note Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "কালো ডিজিটগুলো মোট ব্যবহৃত কিলোওয়াট-ঘণ্টা (kWh) এবং লাল চাকাটি দশমিক (০.১) নির্দেশ করে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    lineHeight = 18.sp
                )
            }
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("পেছনে (Back)", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1.8f)
                    .heightIn(min = 52.dp)
                    .tactileScale(buttonInteractionSource),
                shape = RoundedCornerShape(14.dp),
                interactionSource = buttonInteractionSource
            ) {
                Text("পরবর্তী ধাপ: স্ল্যাব (Next)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ========================================================================
// STEP 3: কয়টা স্ল্যাব চলে এবং কোন স্ল্যাবের রেট কত (Screen 3: Slabs & Rates)
// ========================================================================
@Composable
private fun Step3SlabsAndRatesContent(
    state: OnboardingUiState,
    onModeChange: (TariffBillingMode) -> Unit,
    onFlatRateChange: (String) -> Unit,
    onSlabRateChange: (Int, String) -> Unit,
    onSlabMinChange: (Int, String) -> Unit,
    onSlabMaxChange: (Int, String?) -> Unit,
    onAddSlab: () -> Unit,
    onRemoveSlab: () -> Unit,
    onApplyPreset: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "এলাকার স্ল্যাব সংখ্যা ও রেট",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "আপনার এলাকায় মোট কতটি স্ল্যাব কার্যকর এবং প্রতিটি স্ল্যাবের ইউনিট দর (৳/kWh) কত তা নির্ধারণ করুন।",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Preset Chips
        Text(
            text = "দ্রুত টেমপ্লেট নির্বাচন:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SecondaryText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = state.selectedPreset == "BERC_6",
                onClick = { onApplyPreset("BERC_6") },
                label = { Text("বিইআরসি (৬টি)", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
                selected = state.selectedPreset == "STANDARD_3",
                onClick = { onApplyPreset("STANDARD_3") },
                label = { Text("৩টি স্ল্যাব", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
                selected = state.selectedPreset == "FLAT_1",
                onClick = { onApplyPreset("FLAT_1") },
                label = { Text("ফ্ল্যাট রেট", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.billingMode == TariffBillingMode.FLAT_RATE) {
            // Flat Rate UI
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ফ্ল্যাট রেট নির্ধারণ (কোনো স্ল্যাব নেই)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ব্যবহৃত প্রতি ইউনিটের জন্য এই নির্দিষ্ট দর প্রযোজ্য হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = state.flatRate,
                        onValueChange = onFlatRateChange,
                        label = { Text("প্রতি ইউনিট মূল্য (৳/kWh)") },
                        prefix = { Text("৳ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else {
            // Slabs Count Stepper Control
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, CardOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট স্ল্যাব সংখ্যা: ${state.slabs.size} টি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = "প্রয়োজনে স্ল্যাব যোগ বা বাদ দিতে পারেন",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onRemoveSlab,
                            enabled = state.slabs.size > 1,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove Slab",
                                tint = if (state.slabs.size > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = onAddSlab,
                            enabled = state.slabs.size < 10,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Slab",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Slabs List
            state.slabs.forEachIndexed { index, slab ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "স্ল্যাব ${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val rangeStr = if (slab.max != null) {
                                "${slab.min} – ${slab.max} ইউনিট"
                            } else {
                                "${slab.min}+ ইউনিট (উপরে)"
                            }
                            Text(
                                text = rangeStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedTextField(
                            value = slab.rate,
                            onValueChange = { onSlabRateChange(index, it) },
                            label = { Text("দর (৳/kWh)") },
                            prefix = { Text("৳ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(135.dp),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("পেছনে (Back)", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1.8f)
                    .heightIn(min = 52.dp)
                    .tactileScale(buttonInteractionSource),
                shape = RoundedCornerShape(14.dp),
                interactionSource = buttonInteractionSource
            ) {
                Text("পরবর্তী ধাপ: চার্জ (Next)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ========================================================================
// STEP 4: আনুষাঙ্গিক চার্জসমূহ (সার্ভিস চার্জ, ডিমান্ড চার্জ, ভ্যাট - অপশনাল)
// ========================================================================
@Composable
private fun Step4OptionalChargesContent(
    state: OnboardingUiState,
    onServiceChargeToggle: (Boolean) -> Unit,
    onServiceChargeChange: (String) -> Unit,
    onDemandChargeToggle: (Boolean) -> Unit,
    onDemandChargeChange: (String) -> Unit,
    onMeterRentToggle: (Boolean) -> Unit,
    onMeterRentChange: (String) -> Unit,
    onVatToggle: (Boolean) -> Unit,
    onVatChange: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(EnergyEmerald.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = EnergyEmerald,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "আনুষাঙ্গিক চার্জসমূহ (ঐচ্ছিক)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "সার্ভিস চার্জ, ডিমান্ড চার্জ ও ভ্যাট সম্পূর্ণ অপশনাল। যদি ইনপুট দেন তবে এগুলো সহ বিল হিসাব হবে, আর না দিলে কোনো অতিরিক্ত চার্জ ছাড়াই শুধু ইউনিটের খরচে বিল ক্যালকুলেট হবে।",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Service Charge (সার্ভিস চার্জ - অপশনাল)
        OptionalChargeItemCard(
            title = "সার্ভিস চার্জ",
            subtext = "বিতরণ কোম্পানির মাসিক ফিক্সড সার্ভিস চার্জ",
            isEnabled = state.isServiceChargeEnabled,
            onToggle = onServiceChargeToggle,
            value = state.serviceCharge,
            onValueChange = onServiceChargeChange,
            label = "সার্ভিস চার্জ (৳/মাস)",
            prefix = "৳ ",
            placeholder = "যেমন: 30.00"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Demand Charge (ডিমান্ড চার্জ - অপশনাল)
        OptionalChargeItemCard(
            title = "ডিমান্ড চার্জ",
            subtext = "অনুমোদিত লোডের মাসিক ডিমান্ড চার্জ",
            isEnabled = state.isDemandChargeEnabled,
            onToggle = onDemandChargeToggle,
            value = state.demandCharge,
            onValueChange = onDemandChargeChange,
            label = "ডিমান্ড চার্জ (৳/মাস)",
            prefix = "৳ ",
            placeholder = "যেমন: 42.00"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Meter Rent (মিটার ভাড়া - অপশনাল)
        OptionalChargeItemCard(
            title = "মিটার ভাড়া",
            subtext = "বিতরণ কোম্পানির মাসিক মিটার রেন্ট",
            isEnabled = state.isMeterRentEnabled,
            onToggle = onMeterRentToggle,
            value = state.meterRent,
            onValueChange = onMeterRentChange,
            label = "মিটার ভাড়া (৳/মাস)",
            prefix = "৳ ",
            placeholder = "যেমন: 10.00"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. VAT % (ভ্যাট - অপশনাল)
        OptionalChargeItemCard(
            title = "সরকারি ভ্যাট (%)",
            subtext = "বিদ্যুৎ বিলের ওপর প্রযোজ্য ভ্যাট শতাংশ",
            isEnabled = state.isVatEnabled,
            onToggle = onVatToggle,
            value = state.vatPercentage,
            onValueChange = onVatChange,
            label = "ভ্যাট শতাংশ (%)",
            suffix = "%",
            placeholder = "যেমন: 5.00"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Live Mode Notice Banner
        val hasAnyOptionalCharge = (state.isServiceChargeEnabled && state.serviceCharge.isNotBlank()) ||
                (state.isDemandChargeEnabled && state.demandCharge.isNotBlank()) ||
                (state.isMeterRentEnabled && state.meterRent.isNotBlank()) ||
                (state.isVatEnabled && state.vatPercentage.isNotBlank())

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasAnyOptionalCharge) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                } else {
                    EnergyEmerald.copy(alpha = 0.1f)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (hasAnyOptionalCharge) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else EnergyEmerald.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (hasAnyOptionalCharge) MaterialTheme.colorScheme.primary else EnergyEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (hasAnyOptionalCharge) {
                        "✓ সক্রিয় অতিরিক্ত চার্জ: ব্যবহৃত বিদ্যুৎ খরচের সাথে আপনার কনফিগার করা চার্জসমূহ যোগ হয়ে মোট বিল হিসাব করা হবে।"
                    } else {
                        "✓ কোনো অতিরিক্ত চার্জ যোগ করা হয়নি: শুধুমাত্র ব্যবহৃত ইউনিটের মূল্যের ওপর সম্পূর্ণ বিদ্যুৎ বিল হিসাব করা হবে।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryText,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("পেছনে (Back)", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onComplete,
                enabled = !state.isCompleting,
                modifier = Modifier
                    .weight(2f)
                    .heightIn(min = 52.dp)
                    .tactileScale(buttonInteractionSource),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EnergyEmerald
                ),
                interactionSource = buttonInteractionSource
            ) {
                if (state.isCompleting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "মিটার সেটআপ সম্পন্ন করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Reusable Optional Charge Card with Toggle and Input
@Composable
private fun OptionalChargeItemCard(
    title: String,
    subtext: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    suffix: String? = null,
    placeholder: String = ""
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(ঐচ্ছিক)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(label) },
                        placeholder = { Text(placeholder) },
                        prefix = prefix?.let { { Text(it) } },
                        suffix = suffix?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}
