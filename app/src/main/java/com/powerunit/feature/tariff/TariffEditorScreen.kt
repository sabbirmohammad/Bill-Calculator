package com.powerunit.feature.tariff

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.domain.model.BillBreakdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffEditorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TariffEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Tariff rates updated successfully!")
            viewModel.clearSaveSuccess()
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
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tariff_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            TariffEditorContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun TariffEditorContent(
    uiState: TariffEditorUiState,
    viewModel: TariffEditorViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        item {
            Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
            OutlinedTextField(
                value = uiState.tariffName,
                onValueChange = viewModel::updateTariffName,
                label = { Text(stringResource(R.string.tariff_name), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tariff_name_input"),
                singleLine = true
            )
        }

        item {
            SlabsSection(
                slabs = uiState.slabs,
                onUpdateSlab = viewModel::updateSlab,
                onAddSlab = viewModel::addSlab,
                onRemoveSlab = viewModel::removeSlab
            )
        }

        item {
            FixedChargesSection(
                demandCharge = uiState.demandCharge,
                serviceCharge = uiState.serviceCharge,
                vatPercent = uiState.vatPercent,
                onUpdateDemand = viewModel::updateDemandCharge,
                onUpdateService = viewModel::updateServiceCharge,
                onUpdateVat = viewModel::updateVatPercent
            )
        }

        item {
            LivePreviewSection(
                testUnits = uiState.testUnits,
                previewBill = uiState.previewBill,
                onUpdateTestUnits = viewModel::updateTestUnits
            )
        }

        item {
            Button(
                onClick = viewModel::saveTariff,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightPrimary)
                    .testTag("save_tariff_btn"),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = "Save Tariff Rates",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
        }
    }
}

@Composable
private fun SlabsSection(
    slabs: List<EditableSlab>,
    onUpdateSlab: (Int, String, String, String) -> Unit,
    onAddSlab: () -> Unit,
    onRemoveSlab: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_slabs_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Text(
                text = stringResource(R.string.tariff_slabs_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            slabs.forEachIndexed { index, slab ->
                SlabRow(
                    index = index,
                    slab = slab,
                    canDelete = slabs.size > 1,
                    onUpdateSlab = onUpdateSlab,
                    onRemoveSlab = onRemoveSlab
                )
                Spacer(modifier = Modifier.height(Dimens.xs))
            }

            Spacer(modifier = Modifier.height(Dimens.xs))

            OutlinedButton(
                onClick = onAddSlab,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_slab_btn"),
                shape = ButtonShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xxs))
                Text(stringResource(R.string.tariff_add_slab), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SlabRow(
    index: Int,
    slab: EditableSlab,
    canDelete: Boolean,
    onUpdateSlab: (Int, String, String, String) -> Unit,
    onRemoveSlab: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = slab.fromUnit,
            onValueChange = { onUpdateSlab(index, it, slab.toUnit, slab.ratePerUnit) },
            label = { Text("From") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = slab.toUnit,
            onValueChange = { onUpdateSlab(index, slab.fromUnit, it, slab.ratePerUnit) },
            label = { Text("To") },
            placeholder = { Text("∞") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = slab.ratePerUnit,
            onValueChange = { onUpdateSlab(index, slab.fromUnit, slab.toUnit, it) },
            label = { Text("৳/kWh") },
            modifier = Modifier.weight(1.2f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        if (canDelete) {
            IconButton(onClick = { onRemoveSlab(index) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Slab",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
            }
        }
    }
}

@Composable
private fun FixedChargesSection(
    demandCharge: String,
    serviceCharge: String,
    vatPercent: String,
    onUpdateDemand: (String) -> Unit,
    onUpdateService: (String) -> Unit,
    onUpdateVat: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Text(
                text = stringResource(R.string.tariff_fixed_charges_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                OutlinedTextField(
                    value = demandCharge,
                    onValueChange = onUpdateDemand,
                    label = { Text(stringResource(R.string.demand_charge)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = serviceCharge,
                    onValueChange = onUpdateService,
                    label = { Text(stringResource(R.string.service_charge)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xs))

            OutlinedTextField(
                value = vatPercent,
                onValueChange = onUpdateVat,
                label = { Text(stringResource(R.string.tariff_vat_percent)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LivePreviewSection(
    testUnits: String,
    previewBill: BillBreakdown?,
    onUpdateTestUnits: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tariff_live_preview_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.tariff_live_preview_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedTextField(
                value = testUnits,
                onValueChange = onUpdateTestUnits,
                label = { Text(stringResource(R.string.tariff_test_units)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tariff_test_units_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            previewBill?.let { bill ->
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                HorizontalDivider(thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(Dimens.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Energy Charges (${testUnits} kWh)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatBDT(bill.energyCharge),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fixed Charges & VAT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatBDT(bill.demandCharge + bill.serviceCharge + bill.vatAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.xxs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Preview Total Payable",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Formatters.formatBDT(bill.totalBill),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
