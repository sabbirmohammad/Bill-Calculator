package com.bangla.electricitybill.presentation.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.DeepPurple
import com.bangla.electricitybill.presentation.theme.ErrorRed
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.SuccessGreen
import com.bangla.electricitybill.presentation.theme.WarningAmber
import com.bangla.electricitybill.presentation.theme.WarningAmberLight

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onAddReadingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedItemForDetails by remember { mutableStateOf<HistoryItem?>(null) }

    if (state.showDeleteConfirmDialog && state.readingToDelete != null) {
        val reading = state.readingToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
            title = { Text(text = if (state.isBangla) "মিটার রিডিং মুছবেন?" else "Delete Meter Reading?", fontWeight = FontWeight.Bold) },
            text = {
                val formattedDt = DateUtils.formatDateTime(reading.readingDateTime)
                val displayDt = if (state.isBangla) BanglaDigitNormalizer.toBangla(formattedDt) else formattedDt
                val readingText = if (state.isBangla) {
                    "${BanglaDigitNormalizer.toBangla(reading.blackDigits)}.${BanglaDigitNormalizer.toBangla(reading.redDigit)} kWh"
                } else {
                    "${reading.blackDigits}.${reading.redDigit} kWh"
                }
                Text(
                    text = if (state.isBangla) {
                        "$displayDt তারিখে রেকর্ডকৃত রিডিংটি ($readingText) মুছে ফেললে পরবর্তী সকল হিসাব এবং বিলিং সময়সীমা স্বয়ংক্রিয়ভাবে পুনর্গণনা করা হবে।"
                    } else {
                        "Deleting reading ($readingText) recorded on $displayDt will automatically recalculate subsequent intervals and billing periods."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteReading() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (state.isBangla) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(if (state.isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (selectedItemForDetails != null) {
        ReadingDetailsDialog(
            item = selectedItemForDetails!!,
            isBangla = state.isBangla,
            onDismiss = { selectedItemForDetails = null },
            onDelete = {
                val itemToDelete = selectedItemForDetails!!.reading
                selectedItemForDetails = null
                viewModel.promptDeleteReading(itemToDelete)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0)
                ) {
                    Text(
                        text = if (state.isBangla) "মিটার রিডিং সমূহ" else "Readings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }

            if (state.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CardOutline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(LightPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (state.isBangla) "এখনও কোনো মিটার রিডিং রেকর্ড করা হয়নি।" else "No readings recorded yet.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(state.items) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index + 1)
                            .clickable { selectedItemForDetails = item },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, CardOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Speed icon + Reading Value & Date
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(LightPurple.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    val readingVal = if (state.isBangla) {
                                        "${BanglaDigitNormalizer.toBangla(item.reading.blackDigits)}.${BanglaDigitNormalizer.toBangla(item.reading.redDigit)} kWh"
                                    } else {
                                        "${item.reading.blackDigits}.${item.reading.redDigit} kWh"
                                    }
                                    Text(
                                        text = readingVal,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText,
                                        fontSize = 15.sp
                                    )

                                    val formattedDate = DateUtils.formatDateTime(item.reading.readingDateTime)
                                    Text(
                                        text = if (state.isBangla) BanglaDigitNormalizer.toBangla(formattedDate) else formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Right: Delta Units + Cost OR Baseline Badge
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                if (item.reading.isBaseline) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LightPurple)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (state.isBangla) "ভিত্তি রিডিং" else "Baseline",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple,
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    if (item.deltaUnits != null) {
                                        val deltaFormatted = if (state.isBangla) {
                                            "+${BanglaDigitNormalizer.toBangla(item.deltaUnits.toString())} ইউনিট"
                                        } else {
                                            "+${item.deltaUnits} units"
                                        }
                                        Text(
                                            text = deltaFormatted,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            fontSize = 13.sp
                                        )
                                    }

                                    if (item.energyCost != null) {
                                        Text(
                                            text = if (state.isBangla) BigDecimalMath.formatCurrency(item.energyCost, true) else BigDecimalMath.formatCurrency(item.energyCost),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryText,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Modern Reading Details Popup Dialog with Deletion Option
 */
@Composable
fun ReadingDetailsDialog(
    item: HistoryItem,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Title + Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (isBangla) "মিটার রিডিংয়ের বিবরণ" else "Reading Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Main Reading Hero Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightPurple.copy(alpha = 0.35f))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isBangla) "রেকর্ডকৃত রিডিং" else "Recorded Reading",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryText
                        )
                        val readingVal = if (isBangla) {
                            "${BanglaDigitNormalizer.toBangla(item.reading.blackDigits)}.${BanglaDigitNormalizer.toBangla(item.reading.redDigit)} kWh"
                        } else {
                            "${item.reading.blackDigits}.${item.reading.redDigit} kWh"
                        }
                        Text(
                            text = readingVal,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        val formattedDate = DateUtils.formatDateTime(item.reading.readingDateTime)
                        Text(
                            text = if (isBangla) BanglaDigitNormalizer.toBangla(formattedDate) else formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }

                // Detailed Breakdown List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.reading.isBaseline) {
                        DetailRow(
                            label = if (isBangla) "রিডিংয়ের ধরন" else "Reading Type",
                            value = if (isBangla) "ভিত্তি রিডিং (শুরুর পয়েন্ট)" else "Baseline (Initial Reading)"
                        )
                    } else {
                        if (item.deltaUnits != null) {
                            val deltaFormatted = if (isBangla) {
                                "+${BanglaDigitNormalizer.toBangla(item.deltaUnits.toString())} ইউনিট"
                            } else {
                                "+${item.deltaUnits} units"
                            }
                            DetailRow(
                                label = if (isBangla) "ব্যবহৃত বিদ্যুৎ" else "Consumed Units",
                                value = deltaFormatted,
                                valueColor = SuccessGreen
                            )
                        }

                        if (item.energyCost != null) {
                            DetailRow(
                                label = if (isBangla) "আনুমানিক জ্বালানি খরচ" else "Estimated Energy Cost",
                                value = if (isBangla) BigDecimalMath.formatCurrency(item.energyCost, true) else BigDecimalMath.formatCurrency(item.energyCost)
                            )
                        }

                        if (item.elapsedDays != null) {
                            DetailRow(
                                label = if (isBangla) "সময়ের ব্যবধান" else "Interval",
                                value = if (isBangla) "${BanglaDigitNormalizer.toBangla(item.elapsedDays.toString())} দিন" else "${item.elapsedDays} days"
                            )
                        }
                    }

                    if (item.isMissingDatesWarning && item.elapsedDays != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningAmberLight)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "${BanglaDigitNormalizer.toBangla(item.elapsedDays.toString())} দিনের ব্যবধান (মাঝের দিনগুলোর রিডিং নেই)" else "${item.elapsedDays} days interval (intermediate dates missing)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action Buttons: Delete and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!item.reading.isBaseline) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRed.copy(alpha = 0.12f),
                                contentColor = ErrorRed
                            ),
                            elevation = null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "মুছুন" else "Delete",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = if (isBangla) "বন্ধ করুন" else "Close",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = PrimaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
