package com.powerunit.feature.history

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.EmptyState
import com.powerunit.core.designsystem.component.TrendChip
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.InputShape
import com.powerunit.core.designsystem.theme.PillShape

@Composable
fun HistoryScreen(
    onNavigateToAddReading: () -> Unit,
    onNavigateToEditReading: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryUiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Undo",
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
            onEdit = onNavigateToEditReading
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            HistorySearchAndFilters(
                searchQuery = state.searchQuery,
                activeFilter = state.activeFilter,
                onSearchChange = viewModel::onSearchQueryChange,
                onFilterChange = viewModel::onFilterChange,
                onExportCsv = {
                    viewModel.exportCsv { intent ->
                        context.startActivity(Intent.createChooser(intent, "Export CSV"))
                    }
                },
                onExportPdf = {
                    viewModel.exportPdf { intent ->
                        context.startActivity(Intent.createChooser(intent, "Export PDF"))
                    }
                }
            )

            // Readings List or Empty State
            if (state.monthGroups.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No readings found",
                    description = if (state.searchQuery.isNotBlank()) "No records matched your search query." else "Start tracking daily meter readings to see your consumption history.",
                    actionLabel = if (state.searchQuery.isBlank()) "Add First Reading" else null,
                    onActionClick = if (state.searchQuery.isBlank()) onNavigateToAddReading else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.monthGroups.forEach { group ->
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Month Header Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.monthTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${Formatters.formatUnits(group.totalUnits)} kWh • ${Formatters.formatMoney(group.estimatedBill)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(group.items, key = { it.id }) { item ->
                            ReadingListItem(
                                item = item,
                                onClick = { viewModel.onSelectItemForDetail(item) },
                                onEdit = { onNavigateToEditReading(item.id) },
                                onDelete = { viewModel.deleteReading(item) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingListItem(
    item: ReadingItemUiModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reading_item_${item.id}"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${Formatters.formatUnits(item.readingValue)} kWh",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${Formatters.formatUnits(item.unitsDelta)} units",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (item.note != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badges
                if (item.isEstimated || item.isMeterReset) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.isEstimated) {
                            BadgeChip(label = "Estimated", color = MaterialTheme.colorScheme.tertiary)
                        }
                        if (item.isMeterReset) {
                            BadgeChip(label = "Meter Reset", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun ReadingDetailDialog(
    detail: ReadingItemUiModel,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reading Details") },
        text = {
            Column {
                Text(
                    text = "${Formatters.formatUnits(detail.readingValue)} kWh",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date: ${detail.formattedDate} at ${detail.formattedTime}")
                Text(text = "Units Delta: +${Formatters.formatUnits(detail.unitsDelta)} kWh")
                Text(text = "Estimated Cost: ${Formatters.formatMoney(detail.costEstimate)}")
                if (detail.note != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Note: ${detail.note}", style = MaterialTheme.typography.bodyMedium)
                }
                if (detail.isMeterReset) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Flagged as Meter Reset", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onEdit(detail.id)
                }
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun HistorySearchAndFilters(
    searchQuery: String,
    activeFilter: HistoryFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (HistoryFilter) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(
                    onClick = onExportCsv,
                    modifier = Modifier.testTag("history_export_csv_btn")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                }
                IconButton(
                    onClick = onExportPdf,
                    modifier = Modifier.testTag("history_export_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF Statement")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_field"),
            placeholder = { Text("Search by date, note, or reading...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = InputShape,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = activeFilter == HistoryFilter.ALL,
                    onClick = { onFilterChange(HistoryFilter.ALL) },
                    label = { Text("All") }
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == HistoryFilter.ESTIMATED_ONLY,
                    onClick = { onFilterChange(HistoryFilter.ESTIMATED_ONLY) },
                    label = { Text("Estimated") }
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == HistoryFilter.RESET_ONLY,
                    onClick = { onFilterChange(HistoryFilter.RESET_ONLY) },
                    label = { Text("Resets") }
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == HistoryFilter.HIGH_USAGE,
                    onClick = { onFilterChange(HistoryFilter.HIGH_USAGE) },
                    label = { Text("High Usage") }
                )
            }
        }
    }
}
