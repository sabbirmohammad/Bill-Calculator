package com.bangla.electricitybill.presentation.update

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bangla.electricitybill.BuildConfig
import com.bangla.electricitybill.core.update.model.AppUpdateInfo
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.presentation.components.tactileClick
import com.bangla.electricitybill.presentation.theme.AccentCyan
import com.bangla.electricitybill.presentation.theme.AccentElectricBlue
import com.bangla.electricitybill.presentation.theme.EnergyEmeraldGreen
import com.bangla.electricitybill.presentation.theme.VoltAmber
import java.io.File
import java.util.Locale

@Composable
fun ModernUpdateDialog(
    state: UpdateUiState,
    onStartDownload: (AppUpdateInfo) -> Unit,
    onCancelDownload: (AppUpdateInfo) -> Unit,
    onInstallApk: (File) -> Unit,
    onRemindLater: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (state is UpdateUiState.Idle || state is UpdateUiState.Checking || state is UpdateUiState.UpToDate) {
        return
    }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            when (state) {
                is UpdateUiState.Downloading -> onCancelDownload(state.info)
                is UpdateUiState.UpdateAvailable -> onRemindLater(state.info.versionName)
                else -> onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = state !is UpdateUiState.Downloading,
            dismissOnClickOutside = state !is UpdateUiState.Downloading,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is UpdateUiState.UpdateAvailable -> {
                        UpdateAvailableContent(
                            info = state.info,
                            onUpdate = { onStartDownload(state.info) },
                            onRemindLater = { onRemindLater(state.info.versionName) },
                            onOpenBrowser = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(state.info.htmlUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(browserIntent)
                            }
                        )
                    }
                    is UpdateUiState.Downloading -> {
                        DownloadingContent(
                            info = state.info,
                            percent = state.percent,
                            bytesDownloaded = state.bytesDownloaded,
                            totalBytes = state.totalBytes,
                            onCancel = { onCancelDownload(state.info) }
                        )
                    }
                    is UpdateUiState.ReadyToInstall -> {
                        ReadyToInstallContent(
                            info = state.info,
                            onInstall = { onInstallApk(state.apkFile) },
                            onDismiss = onDismiss
                        )
                    }
                    is UpdateUiState.Error -> {
                        ErrorContent(
                            errorMessage = state.message,
                            onDismiss = onDismiss
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    info: AppUpdateInfo,
    onUpdate: () -> Unit,
    onRemindLater: () -> Unit,
    onOpenBrowser: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Animated Header Icon
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(pulseScale)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        AccentCyan.copy(alpha = 0.25f),
                        AccentElectricBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(AccentElectricBlue, AccentCyan)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Title
    Text(
        text = "নতুন সংস্করণ উপলব্ধ!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Version Comparison Chip
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "  ➔  ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )
        Text(
            text = "v${info.versionName}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Changelog Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = VoltAmber
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "কী কী পরিবর্তন এসেছে:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val notes = info.releaseNotes.trim()
            if (notes.isNotEmpty()) {
                notes.lines().forEach { line ->
                    val cleanLine = line.trim()
                    if (cleanLine.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cleanLine.removePrefix("- ").removePrefix("* "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "সাধারণ বাগ সমাধান ও পারফরম্যান্স উন্নয়ন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (info.apkSizeBytes > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        val sizeMb = info.apkSizeBytes / (1024f * 1024f)
        val sizeFormatted = String.format(Locale.US, "%.1f MB", sizeMb)
        Text(
            text = "ফাইল সাইজ: ${BanglaDigitNormalizer.toBangla(sizeFormatted)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action Buttons
    Button(
        onClick = onUpdate,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .tactileClick(onClick = onUpdate),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "এখনই ডাউনলোড ও আপডেট করুন",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onOpenBrowser,
            modifier = Modifier.tactileClick(onClick = onOpenBrowser)
        ) {
            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "গিটহাবে দেখুন", style = MaterialTheme.typography.labelMedium)
        }

        TextButton(
            onClick = onRemindLater,
            modifier = Modifier.tactileClick(onClick = onRemindLater)
        ) {
            Text(
                text = "পরে মনে করিয়ে দিন",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadingContent(
    info: AppUpdateInfo,
    percent: Int,
    bytesDownloaded: Long,
    totalBytes: Long,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "downloadingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animated Download Icon
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(
                color = AccentElectricBlue.copy(alpha = 0.15f * pulseAlpha),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = AccentElectricBlue,
            modifier = Modifier.size(36.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "আপডেট ডাউনলোড হচ্ছে...",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "সংস্করণ v${info.versionName}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Progress Bar
    val progressFraction = if (percent >= 0) (percent / 100f).coerceIn(0f, 1f) else 0f
    LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
        color = AccentCyan,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Numbers display
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dlMb = bytesDownloaded / (1024f * 1024f)
        val totMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else dlMb
        val statString = String.format(Locale.US, "%.1f MB / %.1f MB", dlMb, totMb)

        Text(
            text = BanglaDigitNormalizer.toBangla(statString),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (percent >= 0) "${BanglaDigitNormalizer.toBangla(percent.toString())}%" else "প্রস্তুত হচ্ছে...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AccentCyan
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "ডাউনলোড সম্পন্ন হওয়ার পর সরাসরি ইনস্টলেশন উইন্ডো খুলবে।",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .tactileClick(onClick = onCancel),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(text = "বাতিল করুন", color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ReadyToInstallContent(
    info: AppUpdateInfo,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(
                color = EnergyEmeraldGreen.copy(alpha = 0.15f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EnergyEmeraldGreen,
            modifier = Modifier.size(38.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "ডাউনলোড সম্পন্ন হয়েছে!",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "v${info.versionName} ইনস্টল করার জন্য প্রস্তুত। নিচের বোতামে চাপ দিয়ে আপডেট সম্পন্ন করুন।",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onInstall,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .tactileClick(onClick = onInstall),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EnergyEmeraldGreen
        )
    ) {
        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "এখনই ইনস্টল করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = onDismiss,
        modifier = Modifier.tactileClick(onClick = onDismiss)
    ) {
        Text(
            text = "পরে ইনস্টল করব",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(36.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "আপডেট ত্রুটি",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(com.bangla.electricitybill.core.update.UpdateConfig.getReleasesPageUrl())).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .tactileClick(onClick = {}),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = Icons.Default.OpenInBrowser,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "ব্রাউজারে রিলিজ পেজ খুলুন",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .tactileClick(onClick = onDismiss),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = "ঠিক আছে")
    }
}
