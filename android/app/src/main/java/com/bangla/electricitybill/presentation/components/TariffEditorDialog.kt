package com.bangla.electricitybill.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffSlab
import java.math.BigDecimal

@Composable
fun TariffEditorDialog(
    tariff: Tariff,
    onDismiss: () -> Unit,
    onSaveTariff: (Tariff) -> Unit
) {
    val slabRates = remember {
        mutableStateMapOf<String, String>().apply {
            tariff.slabs.forEach { slab ->
                put(slab.id, slab.ratePerUnit.toPlainString())
            }
        }
    }

    var demandCharge by remember {
        val charge = tariff.additionalCharges.find { it.chargeType == AdditionalChargeType.DEMAND_CHARGE }
        mutableStateOf(charge?.amount?.toPlainString() ?: "42.00")
    }

    var meterRent by remember {
        val charge = tariff.additionalCharges.find { it.chargeType == AdditionalChargeType.METER_RENT }
        mutableStateOf(charge?.amount?.toPlainString() ?: "10.00")
    }

    var vatPercentage by remember {
        val charge = tariff.additionalCharges.find { it.chargeType == AdditionalChargeType.VAT }
        mutableStateOf(charge?.amount?.toPlainString() ?: "5.00")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Electricity Tariff",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configure unit prices for each consumption slab (BDT / kWh):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                tariff.slabs.forEach { slab ->
                    val rangeLabel = if (slab.maximumUnits != null) {
                        "${slab.minimumUnits.toInt()} – ${slab.maximumUnits.toInt()} Units"
                    } else {
                        "${slab.minimumUnits.toInt()}+ Units"
                    }

                    OutlinedTextField(
                        value = slabRates[slab.id] ?: "",
                        onValueChange = { slabRates[slab.id] = it },
                        label = { Text(rangeLabel) },
                        prefix = { Text("৳ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Additional Charges:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = demandCharge,
                    onValueChange = { demandCharge = it },
                    label = { Text("Demand Charge (Monthly)") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = meterRent,
                    onValueChange = { meterRent = it },
                    label = { Text("Meter Rent (Monthly)") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vatPercentage,
                    onValueChange = { vatPercentage = it },
                    label = { Text("VAT Percentage (%)") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedSlabs = tariff.slabs.map { slab ->
                        val rateStr = slabRates[slab.id]?.ifBlank { "0" } ?: "0"
                        slab.copy(ratePerUnit = BigDecimal(rateStr))
                    }

                    val updatedCharges = tariff.additionalCharges.map { charge ->
                        when (charge.chargeType) {
                            AdditionalChargeType.DEMAND_CHARGE -> charge.copy(amount = BigDecimal(demandCharge.ifBlank { "0" }))
                            AdditionalChargeType.METER_RENT -> charge.copy(amount = BigDecimal(meterRent.ifBlank { "0" }))
                            AdditionalChargeType.VAT -> charge.copy(amount = BigDecimal(vatPercentage.ifBlank { "0" }))
                            else -> charge
                        }
                    }

                    val updatedTariff = tariff.copy(
                        slabs = updatedSlabs,
                        additionalCharges = updatedCharges
                    )
                    onSaveTariff(updatedTariff)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
