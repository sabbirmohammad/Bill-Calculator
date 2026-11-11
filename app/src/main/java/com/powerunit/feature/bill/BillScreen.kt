package com.powerunit.feature.bill

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.component.BillSkeleton
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.NumberLarge
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.core.designsystem.theme.VoltTrackTheme
import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.SlabCharge
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bill_breakdown), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("bill_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportPdfStatement { intent ->
                                context.startActivity(Intent.createChooser(intent, "Share Bill Statement"))
                            }
                        },
                        modifier = Modifier.testTag("bill_export_pdf_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Bill Statement",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            BillSkeleton(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
        ) {
            BillSummaryCard(
                bill = state.actualBill,
                units = state.actualCycleUsage?.totalUnitsSoFar ?: BigDecimal.ZERO,
                isPaid = state.isPastCyclePaid,
                onTogglePaid = viewModel::togglePaidStatus
            )

            state.actualBill?.let { bill ->
                ItemizedReceiptCard(bill = bill)
            }

            WhatIfSimulatorCard(
                simulatedUnits = state.simulatedUnits,
                simulatedBill = state.simulatedBill,
                deltaVsActual = state.billDeltaVsActual,
                onUnitsChange = viewModel::onSimulatedUnitsChange
            )

            Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
        }
    }
}

@Composable
private fun BillSummaryCard(
    bill: BillBreakdown?,
    units: BigDecimal,
    isPaid: Boolean,
    onTogglePaid: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_summary_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Estimated Bill",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                FilterChip(
                    selected = isPaid,
                    onClick = onTogglePaid,
                    label = { Text(if (isPaid) "Paid" else "Unpaid", style = MaterialTheme.typography.labelMedium) },
                    leadingIcon = if (isPaid) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(Dimens.iconSizeSmall)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(Dimens.cardTitleToValueGap))

            Text(
                text = Formatters.formatBDT(bill?.totalBill ?: BigDecimal.ZERO),
                style = NumberLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(Dimens.cardValueToHintGap))

            Text(
                text = "${Formatters.formatUnits(units)} kWh consumed this billing cycle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ItemizedReceiptCard(bill: BillBreakdown) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_itemized_receipt_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingLarge)
        ) {
            // Receipt Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.xs))
                Column {
                    Text(
                        text = stringResource(R.string.itemized_receipt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.energy_charges),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            // Modern Slab Breakdown Table
            SlabBreakdownTable(slabCharges = bill.slabCharges)

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            // Charges & Breakdown Summary Box
            AdditionalChargesCard(bill = bill)
        }
    }
}

@Composable
private fun SlabBreakdownTable(
    slabCharges: List<SlabCharge>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.table_header_slab),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.35f)
                )
                Text(
                    text = stringResource(R.string.table_header_units),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = stringResource(R.string.table_header_rate),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.85f)
                )
                Text(
                    text = stringResource(R.string.table_header_amount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.05f)
                )
            }

            // Table Body Rows
            slabCharges.forEachIndexed { index, slab ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                SlabTableRow(slab = slab)
            }
        }
    }
}

@Composable
private fun SlabTableRow(slab: SlabCharge) {
    val isActive = slab.unitsBilled > BigDecimal.ZERO
    val fromStr = slab.fromUnit.stripTrailingZeros().toPlainString()
    val toStr = slab.toUnit?.stripTrailingZeros()?.toPlainString()
    val rangeStr = if (toStr != null) "$fromStr–$toStr" else "> $fromStr"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
            )
            .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Col 1: Slab Badge & Range
        Column(
            modifier = Modifier.weight(1.35f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.slab_number, slab.slabIndex),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "$rangeStr kWh",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Col 2: Units Billed
        Column(
            modifier = Modifier.weight(0.9f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = Formatters.formatUnits(slab.unitsBilled),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.End
            )
            Text(
                text = "kWh",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
        }

        // Col 3: Rate Per Unit
        Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = Formatters.formatBDT(slab.ratePerUnit),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            Text(
                text = stringResource(R.string.rate_per_unit),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
        }

        // Col 4: Amount
        Text(
            text = Formatters.formatBDT(slab.amount),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFeatureSettings = "tnum"
            ),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.05f)
        )
    }
}

@Composable
private fun AdditionalChargesCard(bill: BillBreakdown) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(Dimens.cardPaddingStandard)
    ) {
        ReceiptRow(
            title = stringResource(R.string.energy_subtotal),
            amount = Formatters.formatBDT(bill.energyCharge),
            isHighlighted = true
        )
        ReceiptRow(
            title = stringResource(R.string.demand_charge),
            amount = Formatters.formatBDT(bill.demandCharge)
        )
        ReceiptRow(
            title = stringResource(R.string.service_charge),
            amount = Formatters.formatBDT(bill.serviceCharge)
        )
        ReceiptRow(
            title = stringResource(R.string.vat),
            amount = Formatters.formatBDT(bill.vatAmount)
        )

        Spacer(modifier = Modifier.height(Dimens.xs))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(Dimens.xs))

        // Total Payable highlight box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.total_payable),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.total_payable_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = Formatters.formatBDT(bill.totalBill),
                    style = NumberMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    title: String,
    amount: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Dimens.xs))
        Text(
            text = amount,
            style = (if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
                .copy(fontFeatureSettings = "tnum"),
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun WhatIfSimulatorCard(
    simulatedUnits: BigDecimal,
    simulatedBill: BillBreakdown?,
    deltaVsActual: BigDecimal?,
    onUnitsChange: (BigDecimal) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("what_if_simulator_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingLarge)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = "What-If Bill Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xxs))

            Text(
                text = "Slide to simulate how changes in your monthly units affect your final bill.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Simulated Usage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatUnits(simulatedUnits)} kWh",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                simulatedBill?.let { sim ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Simulated Bill",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatBDT(sim.totalBill),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.xs))

            Slider(
                value = simulatedUnits.toFloat(),
                onValueChange = { onUnitsChange(BigDecimal(it.toDouble())) },
                valueRange = 0f..800f,
                steps = 79,
                modifier = Modifier.testTag("what_if_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            if (deltaVsActual != null && deltaVsActual.compareTo(BigDecimal.ZERO) != 0) {
                Spacer(modifier = Modifier.height(Dimens.xs))
                val isSaving = deltaVsActual < BigDecimal.ZERO
                val prefix = if (isSaving) "- " else "+ "
                val absDelta = deltaVsActual.abs()
                val deltaText = "$prefix${Formatters.formatBDT(absDelta)}"

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(
                            if (isSaving) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs)
                ) {
                    Text(
                        text = if (isSaving) "Potential Savings: $deltaText" else "Estimated Increase: $deltaText",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(name = "Itemized Receipt - Light", showBackground = true)
@Composable
private fun PreviewItemizedReceiptLight() {
    VoltTrackTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            ItemizedReceiptCard(bill = sampleBillBreakdown)
        }
    }
}

@Preview(name = "Itemized Receipt - Dark", showBackground = true)
@Composable
private fun PreviewItemizedReceiptDark() {
    VoltTrackTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            ItemizedReceiptCard(bill = sampleBillBreakdown)
        }
    }
}

@Preview(name = "Itemized Receipt - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewItemizedReceiptFontScale() {
    VoltTrackTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ItemizedReceiptCard(bill = sampleBillBreakdown)
        }
    }
}

private val sampleBillBreakdown = BillBreakdown(
    slabCharges = listOf(
        SlabCharge(slabIndex = 1, fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), unitsBilled = BigDecimal("75.0"), ratePerUnit = BigDecimal("4.85"), amount = BigDecimal("363.75")),
        SlabCharge(slabIndex = 2, fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), unitsBilled = BigDecimal("42.0"), ratePerUnit = BigDecimal("6.63"), amount = BigDecimal("278.46")),
        SlabCharge(slabIndex = 3, fromUnit = BigDecimal("201"), toUnit = BigDecimal("300"), unitsBilled = BigDecimal("0.0"), ratePerUnit = BigDecimal("6.95"), amount = BigDecimal("0.00")),
        SlabCharge(slabIndex = 4, fromUnit = BigDecimal("301"), toUnit = BigDecimal("400"), unitsBilled = BigDecimal("0.0"), ratePerUnit = BigDecimal("7.34"), amount = BigDecimal("0.00")),
        SlabCharge(slabIndex = 5, fromUnit = BigDecimal("401"), toUnit = BigDecimal("600"), unitsBilled = BigDecimal("0.0"), ratePerUnit = BigDecimal("11.51"), amount = BigDecimal("0.00")),
        SlabCharge(slabIndex = 6, fromUnit = BigDecimal("601"), toUnit = null, unitsBilled = BigDecimal("0.0"), ratePerUnit = BigDecimal("13.26"), amount = BigDecimal("0.00"))
    ),
    energyCharge = BigDecimal("642.21"),
    demandCharge = BigDecimal("35.00"),
    serviceCharge = BigDecimal("10.00"),
    subtotal = BigDecimal("687.21"),
    vatPercent = BigDecimal("5.0"),
    vatAmount = BigDecimal("34.36"),
    totalBill = BigDecimal("721.57")
)
