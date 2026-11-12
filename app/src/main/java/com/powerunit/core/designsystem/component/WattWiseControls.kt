package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium WattWise unified Switch control.
 *
 * Designed to eliminate ambiguity and low-contrast issues of default M3 switches:
 * - ON: track = primary, thumb = onPrimary (white) with tiny checkmark icon (color-blind safe).
 * - OFF: track = surfaceContainerHighest, 2dp border = outline, prominent 24dp thumb = onSurfaceVariant.
 * - Physics: Spring animation (StiffnessLow, Bouncy).
 * - Haptics: Tactile tick on every toggle.
 * - Accessibility: Meets >= 3:1 non-text contrast against background and within track.
 */
@Composable
fun WattWiseSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current

    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 24.dp
    val thumbPadding = 4.dp

    val thumbOffset: Dp by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WattWiseSwitchThumbOffset"
    )

    val trackColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val trackBorderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        checked -> Color.Transparent
        else -> MaterialTheme.colorScheme.outline
    }

    val thumbColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val iconColor = when {
        !enabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Switch,
                        interactionSource = interactionSource,
                        indication = null
                    ) { newValue ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCheckedChange(newValue)
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Track
        Box(
            modifier = Modifier
                .size(trackWidth, trackHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(trackColor)
                .border(
                    width = if (checked) 0.dp else 2.dp,
                    color = trackBorderColor,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Thumb
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset, y = thumbPadding)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(thumbColor),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Premium WattWise unified Checkbox control with consistent design tokens.
 */
@Composable
fun WattWiseCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current

    Checkbox(
        checked = checked,
        onCheckedChange = if (onCheckedChange != null) {
            { newValue ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(newValue)
            }
        } else null,
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource,
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.outline,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            disabledIndeterminateColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        )
    )
}

/**
 * Premium WattWise unified RadioButton control with consistent design tokens.
 */
@Composable
fun WattWiseRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current

    RadioButton(
        selected = selected,
        onClick = if (onClick != null) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        } else null,
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource,
        colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = MaterialTheme.colorScheme.outline,
            disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledUnselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        )
    )
}
