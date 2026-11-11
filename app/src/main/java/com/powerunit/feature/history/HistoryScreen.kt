package com.powerunit.feature.history

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.EmptyState
import com.powerunit.core.designsystem.component.HistorySkeleton
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.feature.history.component.HistoryHeaderAndYearSelector
import com.powerunit.feature.history.component.HistoryMonthChipsRow
import com.powerunit.feature.history.component.MonthSummaryCard
import java.math.BigDecimal

@Composable
fun HistoryScreen(
    onNavigateToAddReading: () -> Unit,
    onNavigateToEditReading: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isBangla = state.appLanguage.equals("bn", ignoreCase = true) || state.useBengaliDigits

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryUiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = if (isBangla) "পূর্বাবস্থায় ফেরত" else "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }
        }
    }

    state.selectedDetailItem?.let { detail ->
        ReadingDetailDialog(
            detail = detail,
            onDismiss = { viewModel.onSelectItemForDetail(null) },
            onEdit = onNavigateToEditReading,
            onDelete = { item -> viewModel.deleteReading(item) },
            useBengaliDigits = state.useBengaliDigits,
            isBangla = isBangla
        )
    }

    if (state.isLoading) {
        HistorySkeleton()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryHeaderAndYearSelector(
                selectedYear = state.selectedYear,
                availableYears = state.availableYears,
                useBengaliDigits = state.useBengaliDigits,
                onPreviousYear = viewModel::onPreviousYear,
                onNextYear = viewModel::onNextYear,
                onExportPdf = {
                    viewModel.exportPdf { intent ->
                        context.startActivity(Intent.createChooser(intent, "Export PDF"))
                    }
                }
            )

            HistoryMonthChipsRow(
                chips = state.monthChips,
                selectedMonth = state.selectedMonth,
                onSelectMonth = viewModel::onSelectMonth
            )

            if (state.items.isEmpty()) {
                val monthName = if (isBangla) {
                    banglaFullMonths.getOrElse(state.selectedMonth - 1) { "মাস" }
                } else {
                    englishFullMonths.getOrElse(state.selectedMonth - 1) { "Month" }
                }
                val yearStr = if (state.useBengaliDigits) {
                    Formatters.toBengaliDigits(state.selectedYear.toString())
                } else {
                    state.selectedYear.toString()
                }

                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = if (isBangla) "$monthName $yearStr-এ কোনো রিডিং নেই" else "No readings in $monthName $yearStr",
                    description = if (isBangla) {
                        "এই মাসের বিদ্যুৎ খরচ ট্র্যাক করতে আপনার মিটারের রিডিং যুক্ত করুন।"
                    } else {
                        "Add your meter reading to track electricity usage and billing for this month."
                    },
                    actionLabel = if (isBangla) "রিডিং যোগ করুন" else "Add Reading",
                    onActionClick = onNavigateToAddReading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.screenHorizontalPadding,
                        end = Dimens.screenHorizontalPadding,
                        top = 4.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
                ) {
                    state.monthSummary?.let { summary ->
                        item(key = "month_summary_${state.selectedYear}_${state.selectedMonth}") {
                            MonthSummaryCard(
                                summary = summary,
                                useBengaliDigits = state.useBengaliDigits,
                                isBangla = isBangla
                            )
                        }
                    }

                    items(state.items, key = { it.id }) { item ->
                        SwipeableReadingItem(
                            item = item,
                            onClick = { viewModel.onSelectItemForDetail(item) },
                            onEdit = onNavigateToEditReading,
                            onDelete = { viewModel.deleteReading(item) },
                            useBengaliDigits = state.useBengaliDigits
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Dimens.spacingSm)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableReadingItem(
    item: ReadingItemUiModel,
    onClick: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (ReadingItemUiModel) -> Unit,
    useBengaliDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete(item)
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit(item.id)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.testTag("swipeable_reading_item_${item.id}"),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        ReadingListItem(
            item = item,
            onClick = onClick,
            useBengaliDigits = useBengaliDigits
        )
    }
}

@Composable
fun ReadingListItem(
    item: ReadingItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useBengaliDigits: Boolean = false,
    isBangla: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reading_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.size(width = 46.dp, height = 50.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.dayString.ifEmpty { "•" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.monthString.ifEmpty { "DATE" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = Formatters.formatUnits(item.readingValue, useBengaliDigits),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "kWh",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.unitsDelta > BigDecimal.ZERO) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "+${Formatters.formatUnits(item.unitsDelta, useBengaliDigits)} units",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (item.isMeterReset) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Initial reading",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.isEstimated) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Estimated",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!item.note.isNullOrBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = item.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (item.costEstimate > BigDecimal.ZERO) {
                    Text(
                        text = Formatters.formatMoney(item.costEstimate, includeSymbol = true, useBengaliDigits = useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = item.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingDetailDialog(
    detail: ReadingItemUiModel,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (ReadingItemUiModel) -> Unit,
    useBengaliDigits: Boolean = false,
    isBangla: Boolean = false
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isBangla) "রিডিং মুছুন" else "Delete Reading") },
            text = { Text(if (isBangla) "আপনি কি নিশ্চিত যে এই রিডিংটি মুছতে চান?" else "Are you sure you want to delete this reading? This will update daily usage calculations.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDelete(detail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "রিডিং বিবরণ" else "Reading Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${Formatters.formatUnits(detail.readingValue, useBengaliDigits)} kWh",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (detail.unitsDelta > BigDecimal.ZERO) "+${Formatters.formatUnits(detail.unitsDelta, useBengaliDigits)} units consumed" else "Initial reading",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (detail.costEstimate > BigDecimal.ZERO) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Est. Cost: ${Formatters.formatMoney(detail.costEstimate, includeSymbol = true, useBengaliDigits = useBengaliDigits)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                DetailInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = if (isBangla) "তারিখ ও সময়" else "Date & Time",
                    value = "${detail.formattedDate} at ${detail.formattedTime}"
                )

                if (!detail.note.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        label = if (isBangla) "নোট" else "Note",
                        value = detail.note
                    )
                }

                if (detail.isEstimated) {
                    DetailInfoRow(
                        icon = Icons.Default.Info,
                        label = if (isBangla) "অবস্থা" else "Status",
                        value = if (isBangla) "আনুমানিক রিডিং" else "Estimated Reading"
                    )
                }

                if (detail.isMeterReset) {
                    DetailInfoRow(
                        icon = Icons.Default.WarningAmber,
                        label = if (isBangla) "বিজ্ঞপ্তি" else "Notice",
                        value = if (isBangla) "মিটার রিসেট চিহ্নিত" else "Flagged as Meter Reset"
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "মুছুন" else "Delete", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onEdit(detail.id)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "সম্পাদনা" else "Edit", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
