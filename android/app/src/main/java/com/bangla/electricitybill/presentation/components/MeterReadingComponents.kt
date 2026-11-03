package com.bangla.electricitybill.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.presentation.theme.AppDivider
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.DeepPurple
import com.bangla.electricitybill.presentation.theme.ErrorRed
import com.bangla.electricitybill.presentation.theme.ErrorRedLight
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.SuccessGreen

/**
 * Clean Top Bar for Add Meter Reading screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingTopBar(
    onNavigateBack: () -> Unit,
    title: String = "Add Meter Reading",
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

/**
 * Screen 2 — Date Selection Card ("Today, Jan 18, 2025", "Auto-detected")
 */
@Composable
fun AddReadingDateCard(
    dateText: String,
    badgeText: String = "Auto-detected",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            }

            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple
            )
        }
    }
}

/**
 * Authentic Mechanical Meter Dial Graphic as seen in the reference image:
 * Black dial with white embossed numbers for black digits and a distinct red wheel for the decimal digit.
 */
@Composable
fun MechanicalMeterGraphic(
    blackDigits: String,
    redDigit: String,
    capacity: Int = 5,
    modifier: Modifier = Modifier
) {
    val paddedBlack = blackDigits.padStart(capacity, '0').takeLast(capacity)
    val safeRed = if (redDigit.isNotEmpty()) redDigit.take(1) else "0"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Mechanical Bezel Frame
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF171A21))
                    .border(2.dp, Color(0xFF333842), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Black whole-number wheels
                    paddedBlack.forEach { char ->
                        MechanicalWheel(
                            digit = char.toString(),
                            isRed = false
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Single Red decimal wheel
                    MechanicalWheel(
                        digit = safeRed,
                        isRed = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "kWh",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MechanicalWheel(
    digit: String,
    isRed: Boolean
) {
    val wheelGradient = if (isRed) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF991B1B),
                Color(0xFFDC2626),
                Color(0xFF7F1D1D)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E222A),
                Color(0xFF0F1217),
                Color(0xFF05070A)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(28.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(wheelGradient)
            .border(
                1.dp,
                if (isRed) Color(0xFFEF4444) else Color(0xFF2E3440),
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Center rotation seam line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.15f))
        )

        Text(
            text = digit,
            fontFamily = FontFamily.Monospace,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Screen 2 — Interactive Distinct Input Component:
 * Separate inputs for Black Digits and Red Decimal Digit with live reconstructed display.
 */
@Composable
fun MeterDigitInputSection(
    blackDigits: String,
    redDigit: String,
    onBlackDigitsChange: (String) -> Unit,
    onRedDigitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Complete Reading readout display (e.g. "2,568.6 kWh")
        val formattedWhole = if (blackDigits.isNotEmpty()) {
            try {
                "%,d".format(blackDigits.toLong())
            } catch (e: Exception) {
                blackDigits
            }
        } else "0"
        val formattedRed = if (redDigit.isNotEmpty()) redDigit else "0"

        val displayWhole = if (isBangla) com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(formattedWhole) else formattedWhole
        val displayRed = if (isBangla) com.bangla.electricitybill.core.util.BanglaDigitNormalizer.toBangla(formattedRed) else formattedRed

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "$displayWhole.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontSize = 32.sp
            )
            Text(
                text = displayRed,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ErrorRed,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "kWh",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = SecondaryText,
                fontSize = 20.sp
            )
        }

        // Educational hint text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isBangla) {
                    "মিটারের পুরো রিডিং লিখুন (শেষের লাল ঘরটি দশমিক মান নির্দেশ করে)"
                } else {
                    "Enter the full reading from your meter (last red digit is the decimal, e.g. 2568.6)"
                },
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Two separate inputs: Black Digits (large) + Red Decimal Digit (red tinted)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Black Digits Input Field
            Column(modifier = Modifier.weight(2.2f)) {
                Text(
                    text = if (isBangla) "কালো ডিজিট" else "BLACK DIGITS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (blackDigits.isEmpty()) {
                        Text(
                            text = if (isBangla) "১২৩৪৮" else "12348",
                            style = MaterialTheme.typography.titleLarge,
                            color = SecondaryText.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BasicTextField(
                        value = blackDigits,
                        onValueChange = onBlackDigitsChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        ),
                        cursorBrush = SolidColor(PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Red Decimal Digit Input Field
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "লাল ডিজিট" else "RED DIGIT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ErrorRedLight)
                        .border(1.5.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (redDigit.isEmpty()) {
                        Text(
                            text = "2",
                            style = MaterialTheme.typography.titleLarge,
                            color = ErrorRed.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    BasicTextField(
                        value = redDigit,
                        onValueChange = { if (it.length <= 1) onRedDigitChange(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(ErrorRed),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Screen 2 — Live Reading Summary Card:
 * Titled "Reading Summary" in bold purple.
 * Displays Previous Reading, Units Today, and Cost Today.
 */
@Composable
fun ReadingSummaryCard(
    previousReadingText: String,
    unitsTodayText: String?,
    costTodayText: String?,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = if (isBangla) "রিডিং সারসংক্ষেপ" else "Reading Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Previous Reading
            SummaryItemRow(
                icon = Icons.Default.Speed,
                iconColor = DeepPurple,
                label = if (isBangla) "পূর্ববর্তী রিডিং" else "Previous Reading",
                value = previousReadingText,
                valueColor = PrimaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Units Today
            val unitsDisplay = if (unitsTodayText != null) {
                if (isBangla) "+$unitsTodayText" else "+$unitsTodayText units"
            } else "—"
            SummaryItemRow(
                icon = Icons.Default.Bolt,
                iconColor = PrimaryPurple,
                label = if (isBangla) "আজকের ব্যবহূত ইউনিট" else "Units Today",
                value = unitsDisplay,
                valueColor = SuccessGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Cost Today
            SummaryItemRow(
                icon = Icons.Default.Layers,
                iconColor = PrimaryPurple,
                label = if (isBangla) "আজকের আনুমানিক খরচ" else "Cost Today",
                value = costTodayText ?: "—",
                valueColor = PrimaryText
            )
        }
    }
}

@Composable
private fun SummaryItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
