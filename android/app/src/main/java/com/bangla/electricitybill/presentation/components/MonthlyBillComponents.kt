package com.bangla.electricitybill.presentation.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.PeriodStatus
import com.bangla.electricitybill.domain.model.SlabChargeBreakdown
import com.bangla.electricitybill.presentation.theme.AppDivider
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.SuccessGreen
import com.bangla.electricitybill.presentation.theme.SuccessGreenLight
import com.bangla.electricitybill.presentation.theme.WarningAmber
import com.bangla.electricitybill.presentation.theme.WarningAmberLight
import java.math.BigDecimal

/**
 * Screen 4 — Estimated Bill Primary Card
 */
@Composable
fun EstimatedBillHeroCard(
    billAmountText: String,
    totalUnitsText: String,
    billingPeriodText: String,
    status: PeriodStatus = PeriodStatus.IN_PROGRESS,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top: Circular Teal/Mint Document Badge + "Estimated Bill" + Amount
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)), // soft mint teal
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontWeight = FontWeight.Medium
                        )

                        // Status Chip
                        val (statusText, statusBg, statusColor) = when (status) {
                            PeriodStatus.COMPLETED -> Triple("Completed", SuccessGreenLight, SuccessGreen)
                            PeriodStatus.PARTIAL_TRACKING -> Triple("Partial", WarningAmberLight, WarningAmber)
                            PeriodStatus.ESTIMATED -> Triple("Estimated", LightPurple, PrimaryPurple)
                            else -> Triple("In Progress", LightPurple, PrimaryPurple)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = billAmountText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText,
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = AppDivider)
            Spacer(modifier = Modifier.height(14.dp))

            // Total Units Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Text(
                    text = totalUnitsText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Billing Period Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billing Period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Text(
                    text = billingPeriodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }
    }
}

/**
 * Screen 4 — Detailed Tariff Breakdown Card with Highlighted Green Total Pill
 */
@Composable
fun FinTechTariffBreakdownCard(
    itemizedBill: ItemizedBill,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tariff Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Slab Rows
            itemizedBill.slabBreakdowns.forEach { breakdown ->
                FinTechSlabRow(breakdown = breakdown, isBangla = isBangla)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Additional Charges (Demand Charge, Meter Rent, VAT, etc.)
            if (itemizedBill.demandCharge > BigDecimal.ZERO) {
                ChargeRow(label = "Demand Charge", amount = itemizedBill.demandCharge, isBangla = isBangla)
                Spacer(modifier = Modifier.height(6.dp))
            }
            if (itemizedBill.meterRent > BigDecimal.ZERO) {
                ChargeRow(label = "Meter Rent", amount = itemizedBill.meterRent, isBangla = isBangla)
                Spacer(modifier = Modifier.height(6.dp))
            }
            if (itemizedBill.vatAmount > BigDecimal.ZERO) {
                ChargeRow(label = "VAT (5%)", amount = itemizedBill.vatAmount, isBangla = isBangla)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted Green Total Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuccessGreenLight)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val unitsStr = BigDecimalMath.formatKwh(itemizedBill.totalUnits, isBangla)
                    Text(
                        text = "Total ($unitsStr units)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    val billFormatted = BigDecimalMath.formatCurrency(itemizedBill.totalBill, isBangla)
                    Text(
                        text = billFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun FinTechSlabRow(
    breakdown: SlabChargeBreakdown,
    isBangla: Boolean
) {
    val rangeText = if (breakdown.slab.maximumUnits != null) {
        "${breakdown.slab.minimumUnits.toInt()} – ${breakdown.slab.maximumUnits.toInt()} units"
    } else {
        "${breakdown.slab.minimumUnits.toInt()}+ units"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1.3f)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = rangeText,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryText,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "@ ৳ ${breakdown.rate}",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        val costFormatted = BigDecimalMath.formatCurrency(breakdown.cost, isBangla)
        Text(
            text = "= $costFormatted",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun ChargeRow(
    label: String,
    amount: BigDecimal,
    isBangla: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText
        )
        Text(
            text = BigDecimalMath.formatCurrency(amount, isBangla),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText
        )
    }
}

/**
 * Screen 4 — Compact Action Buttons ("Edit Tariff" & "Share")
 */
@Composable
fun MonthlyBillActionButtonsRow(
    onEditTariffClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Edit Tariff Button
        OutlinedButton(
            onClick = onEditTariffClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CardOutline),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Tariff",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }

        // Share Button
        OutlinedButton(
            onClick = onShareClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CardOutline),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }
    }
}
