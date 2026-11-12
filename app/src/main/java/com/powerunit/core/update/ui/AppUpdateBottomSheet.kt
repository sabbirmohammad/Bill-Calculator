package com.powerunit.core.update.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.powerunit.BuildConfig
import com.powerunit.R
import com.powerunit.core.designsystem.theme.BottomSheetShape
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.NumberLarge
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.core.designsystem.theme.SuccessDark
import com.powerunit.core.designsystem.theme.SuccessLight
import com.powerunit.core.update.AppUpdateInstaller
import com.powerunit.core.update.model.UpdateInfo
import com.powerunit.core.update.model.UpdatePhase
import com.powerunit.core.update.model.UpdateState
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateBottomSheet(
    state: UpdateState,
    onStartDownload: (UpdateInfo) -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: (File) -> Unit,
    onDismiss: () -> Unit,
    installer: AppUpdateInstaller,
    modifier: Modifier = Modifier,
    onRetry: (UpdateInfo?) -> Unit = {}
) {
    if (state is UpdateState.Idle) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = {
            if (state !is UpdateState.Downloading) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.level3,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = Dimens.spacingSm, bottom = Dimens.xs)
                    .size(width = Dimens.bottomSheetDragHandleWidth, height = Dimens.bottomSheetDragHandleHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        modifier = modifier.testTag("app_update_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingLg)
                .padding(bottom = Dimens.spacingXl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Key AnimatedContent ONLY on UpdatePhase to prevent recomposition storms / flickering during download
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    fadeIn(tween(200)).togetherWith(fadeOut(tween(150)))
                },
                label = "UpdatePhaseTransition"
            ) { phase ->
                when (phase) {
                    UpdatePhase.CHECKING -> {
                        CheckingContent()
                    }
                    UpdatePhase.UP_TO_DATE -> {
                        UpToDateContent(
                            currentVersion = BuildConfig.VERSION_NAME,
                            onDismiss = onDismiss
                        )
                    }
                    UpdatePhase.AVAILABLE -> {
                        val info = (state as? UpdateState.Available)?.info
                        if (info != null) {
                            UpdateAvailableContent(
                                info = info,
                                onUpdate = { onStartDownload(info) },
                                onDismiss = onDismiss
                            )
                        }
                    }
                    UpdatePhase.DOWNLOADING -> {
                        val downloadingState = state as? UpdateState.Downloading
                        if (downloadingState != null) {
                            DownloadingContent(
                                info = downloadingState.info,
                                progress = downloadingState.progress,
                                downloadedBytes = downloadingState.downloadedBytes,
                                totalBytes = downloadingState.totalBytes,
                                speed = downloadingState.speedBytesPerSec,
                                onCancel = onCancelDownload
                            )
                        }
                    }
                    UpdatePhase.READY_TO_INSTALL -> {
                        val readyState = state as? UpdateState.ReadyToInstall
                        if (readyState != null) {
                            ReadyToInstallContent(
                                info = readyState.info,
                                installer = installer,
                                context = context,
                                onInstall = { onInstall(readyState.apkFile) }
                            )
                        }
                    }
                    UpdatePhase.ERROR -> {
                        val errorState = state as? UpdateState.Error
                        ErrorContent(
                            message = errorState?.message ?: "An unexpected error occurred",
                            canRetry = errorState?.canRetry ?: true,
                            onRetry = { onRetry(errorState?.failedInfo) },
                            onDismiss = onDismiss
                        )
                    }
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacingXl)
            .testTag("update_checking_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacingMd))
        Text(
            text = stringResource(R.string.update_checking_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Dimens.xxs))
        Text(
            text = stringResource(R.string.update_checking_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacingLg))
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun UpToDateContent(
    currentVersion: String,
    onDismiss: () -> Unit
) {
    val successColor = if (MaterialTheme.colorScheme.surface.isLight()) SuccessLight else SuccessDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacingLg)
            .testTag("update_up_to_date_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(successColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = successColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        Text(
            text = stringResource(R.string.update_up_to_date_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.xs))

        Text(
            text = stringResource(R.string.update_up_to_date_subtitle, currentVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingXl))

        Button(
            onClick = onDismiss,
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeightPrimary)
                .testTag("update_up_to_date_btn")
        ) {
            Text(
                text = stringResource(R.string.update_btn_ok),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    info: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("update_available_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        Text(
            text = stringResource(R.string.update_available_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimens.xs))

        // Version Comparison Pill
        Row(
            modifier = Modifier
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                .padding(horizontal = Dimens.spacingSm, vertical = Dimens.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currVer = "v" + info.currentVersion.removePrefix("v").removePrefix("V")
            val newVer = "v" + info.newVersion.removePrefix("v").removePrefix("V")
            Text(
                text = currVer,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "  ➔  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = newVer,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (info.apkSize > 0) {
                Text(
                    text = " • ${formatBytes(info.apkSize)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        // Release notes card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.cardPaddingStandard)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.update_changelog_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Dimens.xxs))
                Text(
                    text = info.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingLg))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = PillShape,
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightPrimary)
            ) {
                Text(
                    text = stringResource(R.string.update_btn_later),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = onUpdate,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1.5f)
                    .height(Dimens.buttonHeightPrimary)
                    .testTag("update_download_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.update_btn_download_now),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    info: UpdateInfo,
    progress: Float,
    downloadedBytes: Long,
    totalBytes: Long,
    speed: Long,
    onCancel: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "DownloadProgressArc"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rocketBobbing")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("update_downloading_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.update_downloading_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimens.xxs))
        Text(
            text = "WattWise v${info.newVersion}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerPt = this.center

                // Track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerPt,
                    style = Stroke(width = strokeWidth)
                )

                // Animated Sweep Arc
                val sweepBrush = Brush.sweepGradient(
                    listOf(primaryColor, secondaryColor, primaryColor)
                )
                drawArc(
                    brush = sweepBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = bobOffset.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                val percentInt = (animatedProgress * 100).toInt()
                Text(
                    text = "$percentInt%",
                    style = NumberLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        // Stats Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.spacingSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${formatBytes(downloadedBytes)} / ${if (totalBytes > 0) formatBytes(totalBytes) else "..."}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (speed > 0) formatSpeed(speed) else stringResource(R.string.update_status_downloading),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.xs))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.height(Dimens.buttonHeightCompact)
        ) {
            Text(
                text = stringResource(R.string.update_btn_cancel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ReadyToInstallContent(
    info: UpdateInfo,
    installer: AppUpdateInstaller,
    context: Context,
    onInstall: () -> Unit
) {
    val successColor = if (MaterialTheme.colorScheme.surface.isLight()) SuccessLight else SuccessDark
    var canInstall by remember { mutableStateOf(installer.canInstallApks()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canInstall = installer.canInstallApks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val allowed = installer.canInstallApks()
        canInstall = allowed
        if (allowed) {
            onInstall()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("update_ready_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(successColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = successColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        Text(
            text = stringResource(R.string.update_ready_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimens.xxs))

        val cleanVer = info.newVersion.removePrefix("v").removePrefix("V")
        Text(
            text = stringResource(R.string.update_ready_subtitle, cleanVer),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLg))

        if (!canInstall) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPaddingStandard),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(Dimens.xs))
                    Text(
                        text = stringResource(R.string.update_permission_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacingMd))
        }

        Button(
            onClick = {
                val allowed = installer.canInstallApks()
                canInstall = allowed
                if (allowed) {
                    onInstall()
                } else {
                    val intent = installer.requestInstallPermission()
                    try {
                        permissionLauncher.launch(intent)
                    } catch (_: Exception) {
                        if (context !is android.app.Activity) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            },
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = successColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeightPrimary)
                .testTag("update_install_btn")
        ) {
            Text(
                text = if (canInstall) {
                    stringResource(R.string.update_btn_install_now)
                } else {
                    stringResource(R.string.update_btn_grant_permission)
                },
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("update_error_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        Text(
            text = stringResource(R.string.update_error_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimens.xxs))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = PillShape,
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightPrimary)
            ) {
                Text(
                    text = stringResource(R.string.update_btn_cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (canRetry) {
                Button(
                    onClick = onRetry,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonHeightPrimary)
                        .testTag("update_retry_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimens.xs))
                    Text(
                        text = stringResource(R.string.update_btn_retry),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024 * 1024)
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.1f MB", mb)
    } else {
        val kb = bytes.toDouble() / 1024
        String.format(Locale.US, "%.0f KB", kb)
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return ""
    val mbPerSec = bytesPerSec.toDouble() / (1024 * 1024)
    return if (mbPerSec >= 0.1) {
        String.format(Locale.US, "%.1f MB/s", mbPerSec)
    } else {
        val kbPerSec = bytesPerSec.toDouble() / 1024
        String.format(Locale.US, "%.0f KB/s", kbPerSec)
    }
}
