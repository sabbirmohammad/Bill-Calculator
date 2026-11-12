package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.KeypadKeyShape
import com.powerunit.core.designsystem.theme.NumberHero
import com.powerunit.core.designsystem.theme.NumberLarge
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * NumberPadField (§5.8):
 * - Large numberHero entry, digit-by-digit display with blinking caret, decimal one place
 * - Custom 3x4 number pad (key 64dp tall, radius 20dp, haptic tick)
 * - Under it: live helper "= 8.4 units · ≈ ৳ 78" (titleMedium, teal/secondary)
 * - Error state: red text + icon + suggested fix
 */
@Composable
fun NumberPadField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    errorMessage: String? = null,
    suggestedFix: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val transition = rememberInfiniteTransition(label = "CaretTransition")
    val caretAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CaretBlink"
    )

    fun onKeyPress(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (key) {
            "⌫" -> {
                if (value.isNotEmpty()) {
                    onValueChange(value.dropLast(1))
                }
            }
            "." -> {
                if (!value.contains(".") && value.isNotEmpty()) {
                    onValueChange("$value.")
                } else if (value.isEmpty()) {
                    onValueChange("0.")
                }
            }
            else -> {
                // Decimal allowed up to 1 place
                val parts = value.split(".")
                if (parts.size == 2 && parts[1].length >= 1) {
                    // Already has 1 decimal place, ignore extra digit
                    return
                }
                if (value.length < 9) {
                    onValueChange(value + key)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display area with blinking caret
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spacingMd),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (value.isEmpty()) "0" else value,
                style = NumberHero,
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            // Blinking caret
            Box(
                modifier = Modifier
                    .padding(start = Dimens.xxs)
                    .width(3.dp)
                    .height(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = caretAlpha),
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }

        // Live helper or error message
        if (errorMessage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.screenHorizontalPadding)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Column {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (suggestedFix != null) {
                        Text(
                            text = suggestedFix,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingLg))

        // Custom 3x4 Number Pad (64dp tall, radius 20dp)
        val padRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "⌫")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            padRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimens.keypadKeyHeight)
                                .clip(KeypadKeyShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { onKeyPress(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(Dimens.iconSizeStandard)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = NumberLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "NumberPadField - Light", showBackground = true)
@Composable
private fun PreviewNumberPadFieldLight() {
    WattWiseTheme(darkTheme = false) {
        NumberPadField(
            value = "14828.4",
            onValueChange = {},
            helperText = "= 8.4 units · ≈ ৳ 78"
        )
    }
}

@Preview(name = "NumberPadField - Dark", showBackground = true)
@Composable
private fun PreviewNumberPadFieldDark() {
    WattWiseTheme(darkTheme = true) {
        NumberPadField(
            value = "14828.4",
            onValueChange = {},
            helperText = "= 8.4 units · ≈ ৳ 78"
        )
    }
}

@Preview(name = "NumberPadField - Error", showBackground = true)
@Composable
private fun PreviewNumberPadFieldError() {
    WattWiseTheme {
        NumberPadField(
            value = "14000",
            onValueChange = {},
            errorMessage = "Reading cannot be lower than previous reading (14,820)",
            suggestedFix = "Check if meter digits rolled over or re-enter"
        )
    }
}
