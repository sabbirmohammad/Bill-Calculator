package com.powerunit.feature.tariff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.powerunit.core.designsystem.component.WattWiseCheckbox
import com.powerunit.core.designsystem.component.WattWiseRadioButton
import com.powerunit.core.designsystem.theme.PillShape
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.security.BiometricAuthHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffEditorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TariffEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var activeSlabIndexToEdit by remember { mutableStateOf<Int?>(null) }
    var showAddSlabSheet by remember { mutableStateOf(false) }
    var showEditChargesSheet by remember { mutableStateOf(false) }

    val handleBack = {
        if (viewModel.onBackRequested()) {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    LaunchedEffect(uiState.saveSuccess, uiState.successMessage) {
        if (uiState.saveSuccess) {
            val msg = uiState.successMessage ?: "Tariff rates updated successfully!"
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (uiState.undoVersionId != null) "Undo" else null,
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoLastSave()
            }
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tariff_editor_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.testTag("tariff_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::openHistorySheet,
                        modifier = Modifier.testTag("tariff_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.tariff_history_title),
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimens.screenHorizontalPadding)
                    .testTag("tariff_editor_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
            ) {
                item {
                    Spacer(modifier = Modifier.height(Dimens.xs))
                    TariffSummaryCard(
                        uiState = uiState,
                        onEditCharges = { showEditChargesSheet = true }
                    )
                }

                if (uiState.validationErrors.isNotEmpty()) {
                    item {
                        ValidationErrorsCard(errors = uiState.validationErrors)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tariff_slabs_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("tariff_slabs_card")
                        ) {
                            Text(
                                text = "${uiState.slabs.size} slabs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                itemsIndexed(uiState.slabs) { index, slab ->
                    SlabItemCard(
                        index = index,
                        slab = slab,
                        canDelete = uiState.slabs.size > 1,
                        onEdit = { activeSlabIndexToEdit = index },
                        onDelete = { viewModel.removeSlab(index) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { showAddSlabSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightPrimary)
                            .testTag("add_slab_btn"),
                        shape = ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.xs))
                        Text(
                            text = stringResource(R.string.tariff_add_slab),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.xs))
                    Button(
                        onClick = viewModel::onSaveClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightPrimary)
                            .testTag("save_tariff_btn"),
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimens.xs))
                        Text(
                            text = stringResource(R.string.tariff_save_changes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (activeSlabIndexToEdit != null) {
        val idx = activeSlabIndexToEdit!!
        val slab = uiState.slabs.getOrNull(idx)
        if (slab != null) {
            SlabEditorBottomSheet(
                slab = slab,
                isNew = false,
                onDismiss = { activeSlabIndexToEdit = null },
                onSave = { from, to, rate ->
                    viewModel.updateSlab(idx, from, to, rate)
                    activeSlabIndexToEdit = null
                }
            )
        }
    }

    if (showAddSlabSheet) {
        val lastTo = uiState.slabs.lastOrNull()?.toUnit?.toDoubleOrNull()?.toInt()
        val defaultFrom = if (lastTo != null) "${lastTo + 1}" else "0"
        val emptySlab = EditableSlab(
            slabIndex = uiState.slabs.size + 1,
            fromUnit = defaultFrom,
            toUnit = "",
            ratePerUnit = "8.00"
        )
        SlabEditorBottomSheet(
            slab = emptySlab,
            isNew = true,
            onDismiss = { showAddSlabSheet = false },
            onSave = { from, to, rate ->
                viewModel.addSlab(from, to, rate)
                showAddSlabSheet = false
            }
        )
    }

    if (showEditChargesSheet) {
        FixedChargesBottomSheet(
            tariffName = uiState.tariffName,
            demandCharge = uiState.demandCharge,
            serviceCharge = uiState.serviceCharge,
            vatPercent = uiState.vatPercent,
            onDismiss = { showEditChargesSheet = false },
            onSave = { name, demand, service, vat ->
                viewModel.updateTariffName(name)
                viewModel.updateDemandCharge(demand)
                viewModel.updateServiceCharge(service)
                viewModel.updateVatPercent(vat)
                showEditChargesSheet = false
            }
        )
    }

    // Apply From Which Month Dialog
    if (uiState.showMonthPicker) {
        ApplyFromMonthDialog(
            initialCycle = uiState.selectedEffectiveCycle,
            onDismiss = viewModel::dismissMonthPicker,
            onMonthSelected = viewModel::onEffectiveMonthSelected
        )
    }

    // Confirmation & Diff Dialog on Save
    if (uiState.showConfirmDialog) {
        SaveTariffConfirmationDialog(
            effectiveCycle = uiState.selectedEffectiveCycle,
            diffItems = uiState.diffItems,
            onDismiss = viewModel::dismissConfirmDialog,
            onConfirm = {
                if (uiState.isAppLockEnabled && activity != null && BiometricAuthHelper.canAuthenticate(context)) {
                    BiometricAuthHelper.authenticate(
                        activity = activity,
                        title = "Confirm Tariff Changes",
                        subtitle = "Authenticate to apply modified rates",
                        onSuccess = {
                            viewModel.onConfirmSave()
                        },
                        onError = { err ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(err)
                            }
                        }
                    )
                } else {
                    viewModel.onConfirmSave()
                }
            }
        )
    }

    // Recalculating Progress Dialog
    if (uiState.isRecalculating) {
        RecalculationProgressDialog()
    }

    // Unsaved Changes Guard Dialog
    if (uiState.showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissUnsavedDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.tariff_unsaved_title))
            },
            text = {
                Text(stringResource(R.string.tariff_unsaved_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissUnsavedDialog()
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.tariff_discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Button(
                    onClick = viewModel::dismissUnsavedDialog,
                    shape = ButtonShape
                ) {
                    Text(stringResource(R.string.tariff_keep_editing))
                }
            }
        )
    }

    // History Bottom Sheet
    if (uiState.showHistorySheet) {
        TariffHistoryBottomSheet(
            versions = uiState.historyVersions,
            onDismiss = viewModel::dismissHistorySheet,
            onSelectVersion = viewModel::loadVersionIntoEditor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TariffSummaryCard(
    uiState: TariffEditorUiState,
    onEditCharges: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_summary_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tariff_summary_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.tariffName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("tariff_name_input")
                    )
                }

                IconButton(
                    onClick = onEditCharges,
                    modifier = Modifier.testTag("edit_charges_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.tariff_edit_charges),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TariffChip(label = "BDT (৳)", isHighlighted = true)
                TariffChip(label = "VAT: ${uiState.vatPercent}%")
                TariffChip(label = "Demand: ৳${uiState.demandCharge}/mo")
                TariffChip(label = "Service: ৳${uiState.serviceCharge}/mo")
            }
        }
    }
}

@Composable
private fun TariffChip(label: String, isHighlighted: Boolean = false) {
    Surface(
        shape = CircleShape,
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SlabItemCard(
    index: Int,
    slab: EditableSlab,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_slab_row_$index"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.spacingSm))

                Column {
                    val rangeText = if (slab.toUnit.isBlank()) {
                        "${slab.fromUnit}+ kWh (Unlimited)"
                    } else {
                        "${slab.fromUnit} – ${slab.toUnit} kWh"
                    }
                    Text(
                        text = rangeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "৳${slab.ratePerUnit} / kWh",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_slab_btn_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit slab",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_slab_btn_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete slab",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationErrorsCard(errors: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_validation_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = "Tariff Configuration Issues",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(Dimens.xxs))
            errors.forEach { err ->
                Text(
                    text = "• $err",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlabEditorBottomSheet(
    slab: EditableSlab,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fromText by remember { mutableStateOf(slab.fromUnit) }
    var toText by remember { mutableStateOf(slab.toUnit) }
    var isOpenEnded by remember { mutableStateOf(slab.toUnit.isBlank()) }
    var rateText by remember { mutableStateOf(slab.ratePerUnit) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(bottom = Dimens.xxl),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            Text(
                text = if (isNew) stringResource(R.string.tariff_add_slab) else stringResource(R.string.tariff_edit_slab),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                OutlinedTextField(
                    value = fromText,
                    onValueChange = { fromText = it },
                    label = { Text("From (kWh)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (isOpenEnded) "" else toText,
                    onValueChange = { toText = it },
                    label = { Text("To (kWh)") },
                    enabled = !isOpenEnded,
                    placeholder = { Text("∞") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isOpenEnded = !isOpenEnded }
            ) {
                WattWiseCheckbox(
                    checked = isOpenEnded,
                    onCheckedChange = { isOpenEnded = it }
                )
                Spacer(modifier = Modifier.width(Dimens.xxs))
                Text(
                    text = stringResource(R.string.tariff_open_ended),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("Rate (৳/kWh)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimens.xs))

            Button(
                onClick = {
                    val finalTo = if (isOpenEnded) "" else toText.trim()
                    onSave(fromText.trim(), finalTo, rateText.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightPrimary)
                    .testTag("slab_sheet_save_btn"),
                shape = ButtonShape
            ) {
                Text(
                    text = if (isNew) "Add Slab" else "Update Slab",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FixedChargesBottomSheet(
    tariffName: String,
    demandCharge: String,
    serviceCharge: String,
    vatPercent: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(tariffName) }
    var demand by remember { mutableStateOf(demandCharge) }
    var service by remember { mutableStateOf(serviceCharge) }
    var vat by remember { mutableStateOf(vatPercent) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(bottom = Dimens.xxl),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            Text(
                text = stringResource(R.string.tariff_edit_charges),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.tariff_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tariff_plan_name_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                OutlinedTextField(
                    value = demand,
                    onValueChange = { demand = it },
                    label = { Text(stringResource(R.string.demand_charge)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text(stringResource(R.string.service_charge)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = vat,
                onValueChange = { vat = it },
                label = { Text(stringResource(R.string.tariff_vat_percent)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimens.xs))

            Button(
                onClick = { onSave(name.trim(), demand.trim(), service.trim(), vat.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightPrimary)
                    .testTag("charges_sheet_save_btn"),
                shape = ButtonShape
            ) {
                Text(
                    text = "Update Plan & Charges",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ApplyFromMonthDialog(
    initialCycle: String,
    onDismiss: () -> Unit,
    onMonthSelected: (year: Int, month: Int) -> Unit
) {
    val initialParts = initialCycle.split("-")
    val defaultYear = initialParts.getOrNull(0)?.toIntOrNull() ?: com.powerunit.core.common.DateUtils.currentDhakaDate().year
    val defaultMonth = initialParts.getOrNull(1)?.toIntOrNull() ?: com.powerunit.core.common.DateUtils.currentDhakaDate().monthNumber

    var selectedYear by remember { mutableStateOf(defaultYear) }
    var selectedMonth by remember { mutableStateOf(defaultMonth) }

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.tariff_apply_month_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                Text(
                    text = stringResource(R.string.tariff_apply_month_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear = (selectedYear - 1).coerceAtLeast(defaultYear - 5) }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                    }
                    Text(
                        text = "$selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { selectedYear = (selectedYear + 1).coerceAtMost(defaultYear + 1) }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthNum = row * 3 + col + 1
                                val isSelected = monthNum == selectedMonth
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable { selectedMonth = monthNum },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = monthNames[monthNum - 1],
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMonthSelected(selectedYear, selectedMonth) },
                shape = PillShape,
                modifier = Modifier.testTag("apply_month_confirm_btn")
            ) {
                Text("Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SaveTariffConfirmationDialog(
    effectiveCycle: String,
    diffItems: List<TariffDiffItem>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val monthName = com.powerunit.core.common.DateUtils.formatCycleMonth(effectiveCycle)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.tariff_diff_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Effective Month: $monthName",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Past bills from $monthName to now will be recalculated automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (diffItems.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = CardShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spacingSm),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            diffItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${item.oldValue} → ${item.newValue}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = PillShape,
                modifier = Modifier.testTag("confirm_save_tariff_btn")
            ) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun RecalculationProgressDialog() {
    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.tariff_recalculating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TariffHistoryBottomSheet(
    versions: List<com.powerunit.domain.model.TariffVersion>,
    onDismiss: () -> Unit,
    onSelectVersion: (com.powerunit.domain.model.TariffVersion) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(bottom = Dimens.xxl),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            Text(
                text = stringResource(R.string.tariff_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (versions.isEmpty()) {
                Text(
                    text = "No recorded tariff history versions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                versions.forEachIndexed { idx, v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.cardPaddingStandard),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val monthStr = com.powerunit.core.common.DateUtils.formatCycleMonth(v.effectiveFromCycle)
                                Text(
                                    text = "Version: $monthStr (${v.effectiveFromCycle})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${v.slabs.size} slabs • VAT: ${v.vatPercent}% • Demand: ৳${v.demandCharge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (idx > 0) {
                                Button(
                                    onClick = { onSelectVersion(v) },
                                    shape = ButtonShape
                                ) {
                                    Text(stringResource(R.string.tariff_revert_btn))
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
