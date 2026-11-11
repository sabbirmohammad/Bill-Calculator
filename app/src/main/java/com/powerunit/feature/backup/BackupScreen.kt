package com.powerunit.feature.backup

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerunit.R
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var backupPassphrase by remember { mutableStateOf("") }
    var restorePassphrase by remember { mutableStateOf("") }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("backup_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimens.iconSizeStandard)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
        ) {
            item {
                Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
                CreateBackupCard(
                    passphrase = backupPassphrase,
                    onPassphraseChange = { backupPassphrase = it },
                    isLoading = uiState.isLoading,
                    hasBackupFile = uiState.backupFile != null,
                    onCreateClick = { viewModel.createBackup(backupPassphrase) },
                    onShareClick = {
                        uiState.backupFile?.let { file ->
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/octet-stream"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                        }
                    }
                )
            }

            item {
                RestoreBackupCard(
                    passphrase = restorePassphrase,
                    onPassphraseChange = { restorePassphrase = it },
                    isLoading = uiState.isLoading,
                    onRestoreLatest = {
                        uiState.backupFile?.let { file ->
                            viewModel.restoreBackup(file, restorePassphrase)
                        }
                    }
                )
            }

            item {
                SecurityNoticeCard()
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.scrollBottomPadding))
            }
        }
    }
}

@Composable
private fun CreateBackupCard(
    passphrase: String,
    onPassphraseChange: (String) -> Unit,
    isLoading: Boolean,
    hasBackupFile: Boolean,
    onCreateClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("create_backup_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.backup_create_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xs))
            Text(
                text = stringResource(R.string.backup_create_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedTextField(
                value = passphrase,
                onValueChange = onPassphraseChange,
                label = { Text(stringResource(R.string.backup_passphrase_label)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("backup_passphrase_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Button(
                onClick = onCreateClick,
                enabled = passphrase.length >= 4 && !isLoading,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightCompact).testTag("create_backup_btn"),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(Dimens.iconSizeSmall), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.backup_btn_create), style = MaterialTheme.typography.labelLarge)
                }
            }

            if (hasBackupFile) {
                Spacer(modifier = Modifier.height(Dimens.xs))
                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightCompact).testTag("share_backup_btn"),
                    shape = ButtonShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimens.xs))
                    Text(stringResource(R.string.backup_btn_share), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun RestoreBackupCard(
    passphrase: String,
    onPassphraseChange: (String) -> Unit,
    isLoading: Boolean,
    onRestoreLatest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("restore_backup_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(Dimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = stringResource(R.string.backup_restore_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xs))
            Text(
                text = stringResource(R.string.backup_restore_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedTextField(
                value = passphrase,
                onValueChange = onPassphraseChange,
                label = { Text(stringResource(R.string.backup_restore_passphrase_label)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("restore_passphrase_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            OutlinedButton(
                onClick = onRestoreLatest,
                enabled = passphrase.length >= 4 && !isLoading,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightCompact).testTag("restore_backup_btn"),
                shape = ButtonShape
            ) {
                Text(stringResource(R.string.backup_btn_restore), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SecurityNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeStandard)
            )
            Spacer(modifier = Modifier.width(Dimens.iconToTextGap))
            Text(
                text = stringResource(R.string.backup_security_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
