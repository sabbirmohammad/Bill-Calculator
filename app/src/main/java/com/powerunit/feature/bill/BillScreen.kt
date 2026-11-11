package com.powerunit.feature.bill

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.MeterDisplayLarge
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.domain.model.BillBreakdown
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
                title = { Text("Bill Breakdown", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("bill_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        Icon(Icons.Default.Share, contentDescription = "Share Bill Statement")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            Spacer(modifier = Modifier.height(24.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
                    label = { Text(if (isPaid) "Paid" else "Unpaid") },
                    leadingIcon = if (isPaid) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Formatters.formatBDT(bill?.totalBill ?: BigDecimal.ZERO),
                style = MeterDisplayLarge.copy(fontSize = 38.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

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
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Itemized Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Energy Charges (Slabs)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            bill.slabCharges.forEach { slab ->
                val rangeStr = if (slab.toUnit != null) "${slab.fromUnit}–${slab.toUnit} units" else "> ${slab.fromUnit} units"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Slab ${slab.slabIndex} ($rangeStr @ ${Formatters.formatBDT(slab.ratePerUnit)})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${Formatters.formatUnits(slab.unitsBilled)} kWh",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Formatters.formatBDT(slab.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            ReceiptRow(title = "Energy Charge Subtotal", amount = Formatters.formatBDT(bill.energyCharge))
            ReceiptRow(title = "Demand Charge", amount = Formatters.formatBDT(bill.demandCharge))
            ReceiptRow(title = "Service Charge", amount = Formatters.formatBDT(bill.serviceCharge))
            ReceiptRow(title = "VAT (5%)", amount = Formatters.formatBDT(bill.vatAmount))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Payable",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Formatters.formatBDT(bill.totalBill),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(title: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "What-If Bill Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Slide to simulate how changes in your monthly units affect your final bill.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        fontWeight = FontWeight.Bold,
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                Spacer(modifier = Modifier.height(8.dp))
                val isSaving = deltaVsActual < BigDecimal.ZERO
                val prefix = if (isSaving) "- " else "+ "
                val absDelta = deltaVsActual.abs()
                val deltaText = "$prefix${Formatters.formatBDT(absDelta)}"

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(
                            if (isSaving) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSaving) "Potential Savings: $deltaText" else "Estimated Increase: $deltaText",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
