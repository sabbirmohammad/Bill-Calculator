package com.bangla.electricitybill.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.presentation.components.MechanicalMeterDisplay
import com.bangla.electricitybill.presentation.components.tactileScale
import com.bangla.electricitybill.presentation.motion.MotionTokens
import com.bangla.electricitybill.presentation.theme.AccentElectricBlue
import com.bangla.electricitybill.presentation.theme.EnergyEmeraldGreen

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Smoothly reset scroll position to top whenever navigating between steps
    LaunchedEffect(state.currentStep) {
        scrollState.scrollTo(0)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 1) {
                    IconButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Smooth Animated Step indicators (1 to 4)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..4).forEach { step ->
                        val isCurrent = step == state.currentStep
                        val isPassed = step < state.currentStep
                        val indicatorWidth by animateDpAsState(
                            targetValue = if (isCurrent) 28.dp else 10.dp,
                            label = "stepIndicatorWidth"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(indicatorWidth)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isPassed -> EnergyEmeraldGreen
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    }
                                )
                        )
                        if (step < 4) Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Spacer(modifier = Modifier.size(48.dp))
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
                    1 -> WelcomeStepContent(
                        onNext = { viewModel.nextStep() }
                    )
                    2 -> MeterConfigStepContent(
                        state = state,
                        onNameChange = viewModel::updateMeterName,
                        onNumberChange = viewModel::updateMeterNumber,
                        onCapacityChange = viewModel::updateBlackCapacity,
                        onNext = { viewModel.nextStep() }
                    )
                    3 -> TariffConfigStepContent(
                        state = state,
                        onModeChange = viewModel::updateBillingMode,
                        onFlatRateChange = viewModel::updateFlatRate,
                        onSlabRateChange = viewModel::updateSlabRate,
                        onDemandChange = viewModel::updateDemandCharge,
                        onRentChange = viewModel::updateMeterRent,
                        onVatChange = viewModel::updateVatPercentage,
                        onNext = { viewModel.nextStep() }
                    )
                    4 -> BaselineStepContent(
                        state = state,
                        onBlackChange = viewModel::updateBaselineBlackDigits,
                        onRedChange = viewModel::updateBaselineRedDigit,
                        onComplete = { viewModel.completeOnboarding(onComplete) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStepContent(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(AccentElectricBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricMeter,
                contentDescription = null,
                tint = AccentElectricBlue,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "বিদ্যুৎ মিটার ও বিল ক্যালকুলেটর",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "বাংলাদেশের সনাতন এনালগ মেকানিক্যাল কাউন্টার মিটারের নির্ভুল রিডিং ও প্রগ্রেসিভ স্ল্যাব অনুযায়ী বিদ্যুৎ বিল হিসাব করুন।",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Feature highlights with responsive layout
        FeatureItem(
            icon = Icons.Default.Speed,
            title = "কালো ও লাল ডিজিটের সঠিক হিসাব",
            description = "কালো ডিজিট সম্পূর্ণ kWh এবং শেষ ১টি লাল ডিজিট দশমিক (০.১ kWh) নির্দেশ করে।"
        )

        Spacer(modifier = Modifier.height(14.dp))

        FeatureItem(
            icon = Icons.Default.Shield,
            title = "১০০% অফলাইন ও নিরাপদ",
            description = "কোনো ইন্টারনেট সংযোগ বা রেজিস্ট্রেশনের প্রয়োজন নেই। সমস্ত তথ্য আপনার ফোনেই সুরক্ষিত থাকে।"
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .tactileScale(buttonInteractionSource),
            shape = RoundedCornerShape(14.dp),
            interactionSource = buttonInteractionSource
        ) {
            Text(text = "শুরু করুন (Start Setup)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MeterConfigStepContent(
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
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "মিটারের তথ্য কনফিগার করুন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "আপনার বাসার মেকানিক্যাল মিটারের চাকার সংখ্যা নির্ধারণ করুন।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Responsive Meter Preview
        MechanicalMeterDisplay(
            blackDigits = "12348",
            redDigit = "2",
            capacity = state.blackDigitCapacity,
            subtitle = "বাম থেকে ডানে কালো ডিজিট, শেষে লাল দশমিক ডিজিট"
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = state.meterName,
            onValueChange = onNameChange,
            label = { Text("মিটারের নাম (Meter Name)") },
            placeholder = { Text("যেমন: বাসা - মূল মিটার") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.meterNumber,
            onValueChange = onNumberChange,
            label = { Text("মিটার নম্বর (ঐচ্ছিক)") },
            placeholder = { Text("যেমন: 1045892") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "কালো চাকার সংখ্যা (Black Digit Wheels):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .tactileScale(buttonInteractionSource),
            shape = RoundedCornerShape(14.dp),
            interactionSource = buttonInteractionSource
        ) {
            Text("পরবর্তী ধাপ (Next: Tariff)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TariffConfigStepContent(
    state: OnboardingUiState,
    onModeChange: (TariffBillingMode) -> Unit,
    onFlatRateChange: (String) -> Unit,
    onSlabRateChange: (Int, String) -> Unit,
    onDemandChange: (String) -> Unit,
    onRentChange: (String) -> Unit,
    onVatChange: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ট্যারিফ ও বিদ্যুতের মূল্য নির্ধারণ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "প্রয়োজনে যেকোনো স্ল্যাবের ইউনিট মূল্য পরিবর্তন করতে পারেন।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = state.billingMode == TariffBillingMode.SLAB_BASED,
                onClick = { onModeChange(TariffBillingMode.SLAB_BASED) },
                label = { Text("স্ল্যাব ভিত্তিক", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = state.billingMode == TariffBillingMode.FLAT_RATE,
                onClick = { onModeChange(TariffBillingMode.FLAT_RATE) },
                label = { Text("ফ্ল্যাট রেট", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.billingMode == TariffBillingMode.FLAT_RATE) {
            OutlinedTextField(
                value = state.flatRate,
                onValueChange = onFlatRateChange,
                label = { Text("প্রতি ইউনিট মূল্য (৳/kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        } else {
            Text(
                text = "আবাসিক স্ল্যাব দর (সম্পাদনাযোগ্য টেমপ্লেট):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.slabs.forEachIndexed { index, slab ->
                val rangeLabel = if (slab.max != null) "${slab.min} - ${slab.max} ইউনিট" else "${slab.min}+ ইউনিট"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slab.rate,
                        onValueChange = { onSlabRateChange(index, it) },
                        label = { Text("দর (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(115.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional Charges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.demandCharge,
                onValueChange = onDemandChange,
                label = { Text("ডিমান্ড চার্জ (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = state.meterRent,
                onValueChange = onRentChange,
                label = { Text("মিটার ভাড়া (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.vatPercentage,
            onValueChange = onVatChange,
            label = { Text("ভ্যাট শতাংশ (% VAT)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .tactileScale(buttonInteractionSource),
            shape = RoundedCornerShape(14.dp),
            interactionSource = buttonInteractionSource
        ) {
            Text("পরবর্তী ধাপ (Next: Baseline)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BaselineStepContent(
    state: OnboardingUiState,
    onBlackChange: (String) -> Unit,
    onRedChange: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "প্রাথমিক বেসলাইন রিডিং প্রদান",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "এটি আপনার শুরুর রিডিং হিসেবে কাজ করবে। এর ওপর কোনো বিদ্যুৎ বিল বা খরচ ধরা হবে না।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        MechanicalMeterDisplay(
            blackDigits = state.baselineBlackDigits,
            redDigit = state.baselineRedDigit,
            capacity = state.blackDigitCapacity,
            subtitle = "তারিখ: ${DateUtils.formatDateTime(state.baselineDateTime)}"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.baselineBlackDigits,
                onValueChange = onBlackChange,
                label = { Text("কালো ডিজিট (kWh)") },
                placeholder = { Text("12500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(2f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.baselineRedDigit,
                onValueChange = onRedChange,
                label = { Text("লাল (০.১)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onComplete,
            enabled = !state.isCompleting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .tactileScale(buttonInteractionSource),
            shape = RoundedCornerShape(14.dp),
            interactionSource = buttonInteractionSource
        ) {
            if (state.isCompleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সেটআপ সম্পন্ন করুন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
