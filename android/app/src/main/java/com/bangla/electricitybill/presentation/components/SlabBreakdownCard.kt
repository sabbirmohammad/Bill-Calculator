package com.bangla.electricitybill.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.SlabChargeBreakdown
import com.bangla.electricitybill.presentation.motion.MotionTokens
import com.bangla.electricitybill.presentation.motion.bounceClick
import java.math.BigDecimal

@Composable
fun SlabBreakdownCard(
    itemizedBill: ItemizedBill,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = MotionTokens.SpringSnappy,
        label = "slabChevronRotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isBangla) "বিদ্যুৎ বিলের বিস্তারিত বিবরণ" else "Detailed Bill Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Toggle Breakdown",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = BigDecimalMath.formatKwh(itemizedBill.totalUnits, isBangla),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = MotionTokens.fastTween()) + expandVertically(animationSpec = MotionTokens.normalTween()),
                exit = fadeOut(animationSpec = MotionTokens.fastTween()) + shrinkVertically(animationSpec = MotionTokens.fastTween())
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Progressive Slab table header with concentric 10dp radius
                    if (itemizedBill.slabBreakdowns.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "ধাপ (Slab)" else "Slab Range",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = if (isBangla) "ইউনিট ও দর" else "Units @ Rate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.6f)
                            )
                            Text(
                                text = if (isBangla) "টাকা" else "Amount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        itemizedBill.slabBreakdowns.forEach { breakdown ->
                            SlabRow(breakdown = breakdown, isBangla = isBangla)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Energy Cost Line
                    BillItemRow(
                        label = if (isBangla) "১. মোট এনার্জি চার্জ" else "1. Total Energy Charge",
                        amount = itemizedBill.energyCost,
                        isBold = true,
                        isBangla = isBangla
                    )

                    // Additional Charges
                    if (itemizedBill.demandCharge > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "২. ডিমান্ড চার্জ" else "2. Demand Charge",
                            amount = itemizedBill.demandCharge,
                            isBangla = isBangla
                        )
                    }
                    if (itemizedBill.meterRent > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "৩. মিটার ভাড়া" else "3. Meter Rent",
                            amount = itemizedBill.meterRent,
                            isBangla = isBangla
                        )
                    }
                    if (itemizedBill.serviceCharge > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "৪. সার্ভিস চার্জ" else "4. Service Charge",
                            amount = itemizedBill.serviceCharge,
                            isBangla = isBangla
                        )
                    }
                    if (itemizedBill.otherCharges > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "৫. অন্যান্য ফি" else "5. Other Charges",
                            amount = itemizedBill.otherCharges,
                            isBangla = isBangla
                        )
                    }
                    if (itemizedBill.vatAmount > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "৬. ভ্যাট (VAT)" else "6. VAT",
                            amount = itemizedBill.vatAmount,
                            isBangla = isBangla
                        )
                    }
                    if (itemizedBill.discountAmount > BigDecimal.ZERO) {
                        BillItemRow(
                            label = if (isBangla) "৭. রিবেট / ছাড়" else "7. Rebate / Discount",
                            amount = itemizedBill.discountAmount.negate(),
                            isBangla = isBangla,
                            textColor = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Estimated Bill Hero Container (Concentric 12dp pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "সর্বমোট আনুমানিক বিল" else "Total Estimated Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = BigDecimalMath.formatCurrency(itemizedBill.totalBill, isBangla),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SlabRow(
    breakdown: SlabChargeBreakdown,
    isBangla: Boolean
) {
    val maxText = if (breakdown.slab.maximumUnits != null) {
        "${breakdown.slab.minimumUnits} - ${breakdown.slab.maximumUnits}"
    } else {
        "${breakdown.slab.minimumUnits}+"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = maxText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = "${BigDecimalMath.formatKwh(breakdown.unitsInSlab, isBangla)} @ ৳${breakdown.rate}",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.6f)
        )
        Text(
            text = BigDecimalMath.formatCurrency(breakdown.cost, isBangla),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun BillItemRow(
    label: String,
    amount: BigDecimal,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified,
    isBangla: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = BigDecimalMath.formatCurrency(amount, isBangla),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
