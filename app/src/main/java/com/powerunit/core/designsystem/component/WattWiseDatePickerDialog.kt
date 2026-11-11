package com.powerunit.core.designsystem.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.powerunit.R
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WattWiseDatePickerDialog(
    initialEpochMillis: Long? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    allowFutureDates: Boolean = false,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochMillis ?: System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return if (allowFutureDates) {
                    true
                } else {
                    // Allow up to current time plus 1 day to cover all timezones
                    utcTimeMillis <= System.currentTimeMillis() + 86400000L
                }
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val javaLocalDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        val selected = LocalDate(
                            year = javaLocalDate.year,
                            monthNumber = javaLocalDate.monthValue,
                            dayOfMonth = javaLocalDate.dayOfMonth
                        )
                        onDateSelected(selected)
                    }
                    onDismiss()
                },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("date_picker_confirm_btn")
            ) {
                Text(text = stringResource(R.string.select_date), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("date_picker_cancel_btn")
            ) {
                Text(text = stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = CardShape,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.testTag("wattwise_date_picker_dialog")
    ) {
        DatePicker(state = datePickerState)
    }
}
