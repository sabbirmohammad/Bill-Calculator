package com.bangla.electricitybill.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.presentation.theme.AppDivider
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.DeepPurple
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.PurpleHeroGradientEnd
import com.bangla.electricitybill.presentation.theme.PurpleHeroGradientStart
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.SuccessGreen
import com.bangla.electricitybill.presentation.theme.VoltAmber
import java.math.BigDecimal
import java.time.YearMonth

/**
 * Screen 1 — Modern Purple Gradient Hero Header
 */
@Composable
fun HomeHeader(
    appNameLine1: String = "Electricity Bill",
    appNameLine2: String = "Calculator",
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(PurpleHeroGradientStart, PurpleHeroGradientEnd)
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circle lightning icon with gold accent
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = VoltAmber,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = appNameLine1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = appNameLine2,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Notification Bell
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Screen 1 — Billing Month Selector Card
 */
@Composable
fun BillingMonthSelectorCard(
    selectedMonth: YearMonth,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val formattedMonth = DateUtils.formatMonthYear(selectedMonth)
                Text(
                    text = if (isBangla) BanglaDigitNormalizer.toBangla(formattedMonth) else formattedMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Month",
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, LightPurple, RoundedCornerShape(10.dp))
                    .background(LightPurple.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Screen 1 — Monthly Consumption Card (Hero Card with Rising Mini Bar Chart)
 */
@Composable
fun MonthlyConsumptionCard(
    consumptionKwh: BigDecimal,
    estimatedBill: BigDecimal,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false,
    subtitle: String = "This Month (so far)"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Header badge with lightning bolt
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Consumption Number
                val kwhStr = BigDecimalMath.formatKwh(consumptionKwh, isBangla)
                val unitWord = if (isBangla) "ইউনিট" else "Units"
                Text(
                    text = "$kwhStr $unitWord",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    fontSize = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isBangla) "আনুমানিক বিল" else "Estimated Bill",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Green Amount
                val billStr = BigDecimalMath.formatCurrency(estimatedBill, isBangla)
                Text(
                    text = billStr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mini Progressive Bar Chart Visualization
            MiniProgressiveBarChart(
                modifier = Modifier
                    .width(72.dp)
                    .height(84.dp)
            )
        }
    }
}

/**
 * Mini bar chart graphic shown on the right side of the Hero Consumption card
 */
@Composable
fun MiniProgressiveBarChart(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barCount = 5
        val barRatios = listOf(0.35f, 0.48f, 0.62f, 0.78f, 0.95f)
        val spacing = 6.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val chartHeight = size.height

        barRatios.forEachIndexed { i, ratio ->
            val bHeight = chartHeight * ratio
            val x = i * (barWidth + spacing)
            val y = chartHeight - bHeight
            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    PrimaryPurple,
                    LightPurple
                ),
                startY = y,
                endY = chartHeight
            )

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, bHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

/**
 * Screen 1 — Quick Statistics (Row of 3 cards: Days, Units/Day, Avg/Day)
 */
@Composable
fun QuickStatisticsRow(
    daysTracked: Int,
    unitsPerDay: BigDecimal,
    costPerDay: BigDecimal,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.CalendarToday,
            value = if (isBangla) BanglaDigitNormalizer.toBangla(daysTracked.toString()) else daysTracked.toString(),
            label = if (isBangla) "দিন" else "Days",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.Bolt,
            value = BigDecimalMath.formatKwh(unitsPerDay, isBangla),
            label = if (isBangla) "ইউনিট/দিন" else "Units/Day",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.Layers,
            value = BigDecimalMath.formatCurrency(costPerDay, isBangla),
            label = if (isBangla) "গড়/দিন" else "Avg/Day",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                maxLines = 1
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Screen 1 & 2 — Full-Width Primary Action Button ("+ Add Today's Reading")
 */
@Composable
fun AddReadingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Add Today's Reading"
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = PrimaryPurple.copy(alpha = 0.35f),
                ambientColor = PrimaryPurple.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(PrimaryPurple, DeepPurple)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .tactileScale(interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Screen 1 — Compact Recent Reading Row
 */
@Composable
fun RecentReadingRow(
    dateLabel: String,
    readingValue: String,
    unitsDelta: String?,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1.2f)
            )

            Text(
                text = readingValue,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.4f)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (unitsDelta != null) {
                    Text(
                        text = "+$unitsDelta units",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppDivider)
            )
        }
    }
}
