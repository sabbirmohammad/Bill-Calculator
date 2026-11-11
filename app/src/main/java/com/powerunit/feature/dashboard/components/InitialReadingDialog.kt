package com.powerunit.feature.dashboard.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.designsystem.component.WattWiseDatePickerDialog
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.InputShape
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Composable
fun InitialReadingDialog(
    onSave: (BigDecimal, Instant, String) -> Unit,
    onDismiss: () -> Unit,
    initialInstant: Instant = DateUtils.currentInstant(),
    modifier: Modifier = Modifier
) {
    var readingInput by remember { mutableStateOf("") }
    var selectedInstant by remember { mutableStateOf(initialInstant) }
    var showDatePicker by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (showDatePicker) {
        WattWiseDatePickerDialog(
            initialEpochMillis = DateUtils.instantToUtcEpochMillis(selectedInstant),
            onDateSelected = { selectedLocalDate ->
                selectedInstant = DateUtils.combineDateWithTime(selectedLocalDate, selectedInstant)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    val parsedReading = readingInput.trim().toBigDecimalOrNull()
    val canSave = parsedReading != null && parsedReading >= BigDecimal.ZERO

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
                Text(
                    text = stringResource(R.string.initial_reading_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.xxs)
            ) {
                Text(
                    text = stringResource(R.string.initial_reading_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                OutlinedTextField(
                    value = readingInput,
                    onValueChange = { readingInput = it },
                    label = { Text(stringResource(R.string.initial_reading_input_label)) },
                    placeholder = { Text("0.0") },
                    trailingIcon = {
                        Text(
                            text = "kWh",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = Dimens.spacingSm)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("initial_reading_input"),
                    singleLine = true,
                    shape = InputShape,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                // Modifiable Date Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .clickable { showDatePicker = true }
                        .testTag("initial_reading_date_picker_card"),
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
                                    text = stringResource(R.string.reading_date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = DateUtils.formatDate(selectedInstant),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = ButtonShape,
                            modifier = Modifier.testTag("initial_reading_change_date_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.change_date),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parsedReading?.let {
                        onSave(it, selectedInstant, noteInput)
                    }
                },
                enabled = canSave,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("initial_reading_save_btn")
            ) {
                Text(
                    text = stringResource(R.string.btn_save_initial_reading),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("initial_reading_dismiss_btn")
            ) {
                Text(
                    text = stringResource(R.string.btn_later),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("initial_reading_dialog")
    )
}

@Composable
fun InitialReadingRequiredBanner(
    onSetInitialReadingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("initial_reading_required_banner"),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.iconSizeStandard)
                )
                Text(
                    text = stringResource(R.string.initial_reading_banner_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xxs))

            Text(
                text = stringResource(R.string.initial_reading_banner_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Button(
                onClick = onSetInitialReadingClick,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_set_initial_reading_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.btn_set_initial_reading),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
