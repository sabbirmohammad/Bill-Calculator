package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.powerunit.R
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.PillShape
import java.util.Locale

/**
 * Premium WattWise-styled TimePicker dialog.
 *
 * Implements Material 3 TimePicker / TimeInput inside a 28dp rounded dialog:
 * - Dial and selector themed with primary and primaryContainer tokens
 * - 12-hour format display with AM/PM segmented toggle
 * - Keyboard input mode toggle for rapid numeric entry
 * - Pill-shaped action buttons
 * - Adaptive layout with vertical scroll for 200% font scale
 * - Formatted summary supporting Bengali numerals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WattWiseTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    useBengaliDigits: Boolean = false,
    isBangla: Boolean = false
) {
    val parts = initialTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 21
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var showKeyboardInput by remember { mutableStateOf(false) }

    val formattedTime = Formatters.formatTime12h(
        timeStr = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute),
        useBengaliDigits = useBengaliDigits,
        isBangla = isBangla
    )

    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
        selectorColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.surface,
        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        periodSelectorUnselectedContainerColor = Color.Transparent,
        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("wattwise_time_picker_dialog"),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.spacingSm))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_reminder_time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = { showKeyboardInput = !showKeyboardInput },
                    modifier = Modifier.testTag("time_picker_toggle_input_mode")
                ) {
                    Icon(
                        imageVector = if (showKeyboardInput) Icons.Default.Schedule else Icons.Default.Keyboard,
                        contentDescription = if (showKeyboardInput) "Switch to Clock Dial" else "Switch to Keyboard Input",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showKeyboardInput) {
                    TimeInput(
                        state = timePickerState,
                        colors = timePickerColors,
                        modifier = Modifier.padding(vertical = Dimens.spacingSm)
                    )
                } else {
                    TimePicker(
                        state = timePickerState,
                        colors = timePickerColors,
                        modifier = Modifier.padding(vertical = Dimens.spacingSm)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formatted = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onConfirm(formatted)
                },
                shape = PillShape,
                modifier = Modifier.testTag("time_picker_confirm_btn")
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("time_picker_cancel_btn")
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
