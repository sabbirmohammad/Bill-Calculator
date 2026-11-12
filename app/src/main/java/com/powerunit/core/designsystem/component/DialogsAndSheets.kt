package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.BottomSheetShape
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * ConfirmDialog / BottomSheet / Snackbar (§5.11):
 * - BottomSheet: 4x36dp drag handle, 20dp padding, 16dp gaps, 28dp top corners
 * - ConfirmDialog: radius 24dp, titleLarge, bodyMedium, 2 actions
 * - Snackbar: floats 16dp above nav bar, radius 16dp, with Undo action
 */

@Composable
fun WattWiseBottomSheetContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BottomSheetShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.level3
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle: 4 x 36 dp
            Box(
                modifier = Modifier
                    .size(width = Dimens.bottomSheetDragHandleWidth, height = Dimens.bottomSheetDragHandleHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.height(Dimens.spacingMd))
            content()
        }
    }
}

@Composable
fun WattWiseConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(confirmText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = ButtonShape
            ) {
                Text(dismissText, style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
}

@Composable
fun WattWiseSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    Snackbar(
        snackbarData = snackbarData,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.onSurface,
        contentColor = MaterialTheme.colorScheme.surface,
        actionColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacingMd)
    )
}

@Preview(name = "BottomSheet - Light", showBackground = true)
@Composable
private fun PreviewBottomSheetLight() {
    WattWiseTheme(darkTheme = false) {
        WattWiseBottomSheetContent {
            Text("Edit Reading", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Adjust meter units or timestamp below.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "ConfirmDialog - Light", showBackground = true)
@Composable
private fun PreviewConfirmDialogLight() {
    WattWiseTheme(darkTheme = false) {
        WattWiseConfirmDialog(
            title = "Delete Reading?",
            message = "This action will recalculate your monthly bill projection.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
