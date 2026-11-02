package com.bangla.electricitybill.presentation.bill

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.presentation.components.BillingMonthSelectorCard
import com.bangla.electricitybill.presentation.components.EstimatedBillHeroCard
import com.bangla.electricitybill.presentation.components.FinTechTariffBreakdownCard
import com.bangla.electricitybill.presentation.components.MonthPickerDialog
import com.bangla.electricitybill.presentation.components.MonthlyBillActionButtonsRow
import com.bangla.electricitybill.presentation.components.TariffEditorDialog
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText

@Composable
fun MonthlyBillScreen(
    viewModel: MonthlyBillViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = state.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelectMonth = { viewModel.selectMonth(it) }
        )
    }

    if (state.showEditTariffDialog && state.activeTariff != null) {
        TariffEditorDialog(
            tariff = state.activeTariff!!,
            onDismiss = { viewModel.closeEditTariff() },
            onSaveTariff = { viewModel.saveUpdatedTariff(it) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen 4 Section A: Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Monthly Bill",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }

            // Screen 4 Section B: Month Selector Dropdown Card ("January 2025 ⌄")
            item {
                BillingMonthSelectorCard(
                    selectedMonth = state.selectedMonth,
                    onClick = { showMonthPicker = true },
                    modifier = Modifier.staggeredEntrance(1)
                )
            }

            // Screen 4 Section C: Estimated Bill Hero Card
            item {
                val billFormatted = state.itemizedBill?.let {
                    BigDecimalMath.formatCurrency(it.totalBill)
                } ?: "৳ 0.00"

                val unitsFormatted = "${BigDecimalMath.formatKwh(state.totalUnits)} kWh"

                EstimatedBillHeroCard(
                    billAmountText = billFormatted,
                    totalUnitsText = unitsFormatted,
                    billingPeriodText = state.billingPeriodText,
                    status = state.status,
                    modifier = Modifier.staggeredEntrance(2)
                )
            }

            // Screen 4 Section D & E: Tariff Breakdown Card & Additional Charges
            if (state.itemizedBill != null) {
                item {
                    FinTechTariffBreakdownCard(
                        itemizedBill = state.itemizedBill!!,
                        modifier = Modifier.staggeredEntrance(3)
                    )
                }
            }

            // Screen 4 Section F: Action Buttons ("Edit Tariff" & "Share")
            item {
                MonthlyBillActionButtonsRow(
                    onEditTariffClick = { viewModel.openEditTariff() },
                    onShareClick = {
                        val reportText = viewModel.buildShareReportText()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Electricity Bill"))
                    },
                    modifier = Modifier.staggeredEntrance(4)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
