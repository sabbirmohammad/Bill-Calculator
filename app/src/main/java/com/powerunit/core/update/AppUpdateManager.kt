package com.powerunit.core.update

import android.content.Context
import com.powerunit.BuildConfig
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.update.model.UpdateInfo
import com.powerunit.core.update.model.UpdateState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateService: GitHubUpdateService,
    private val downloader: AppUpdateDownloader,
    val installer: AppUpdateInstaller,
    private val preferences: SettingsPreferences
) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var downloadJob: Job? = null

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun checkForUpdates(isManual: Boolean = false) {
        if (_updateState.value is UpdateState.Checking || _updateState.value is UpdateState.Downloading) {
            return
        }

        _updateState.value = UpdateState.Checking

        scope.launch {
            try {
                val autoCheckEnabled = preferences.autoCheckUpdates.first()
                if (!isManual && !autoCheckEnabled) {
                    _updateState.value = UpdateState.Idle
                    return@launch
                }

                val repo = preferences.githubRepo.first().let {
                    if (it.isBlank() || it.contains("electricity-bill", ignoreCase = true) || !it.contains('/')) {
                        SettingsPreferences.DEFAULT_GITHUB_REPO
                    } else {
                        it
                    }
                }
                val currentVersion = BuildConfig.VERSION_NAME

                val result = updateService.fetchLatestRelease(repo, currentVersion)
                result.fold(
                    onSuccess = { info ->
                        if (info != null) {
                            _updateState.value = UpdateState.Available(info)
                        } else {
                            if (isManual) {
                                _updateState.value = UpdateState.UpToDate
                            } else {
                                _updateState.value = UpdateState.Idle
                            }
                        }
                    },
                    onFailure = { error ->
                        if (isManual) {
                            _updateState.value = UpdateState.Error(
                                message = error.message ?: "Failed to check for updates"
                            )
                        } else {
                            _updateState.value = UpdateState.Idle
                        }
                    }
                )
            } catch (e: Exception) {
                if (isManual) {
                    _updateState.value = UpdateState.Error(message = e.localizedMessage ?: "Unknown error")
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    fun startDownload(info: UpdateInfo) {
        if (_updateState.value is UpdateState.Downloading) return

        downloadJob?.cancel()
        downloadJob = scope.launch {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            // Clean up old update files
            updatesDir.listFiles()?.forEach { if (it.isFile) it.delete() }

            val targetFile = File(updatesDir, "WattWise-${info.newVersion}.apk")

            _updateState.value = UpdateState.Downloading(
                info = info,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = info.apkSize
            )

            val result = downloader.downloadApk(
                downloadUrl = info.downloadUrl,
                targetFile = targetFile,
                expectedSize = info.apkSize,
                expectedSha256 = info.expectedSha256
            ) { downloaded, total, speed ->
                val progress = if (total > 0) {
                    (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                _updateState.value = UpdateState.Downloading(
                    info = info,
                    progress = progress,
                    downloadedBytes = downloaded,
                    totalBytes = total,
                    speedBytesPerSec = speed
                )
            }

            result.fold(
                onSuccess = { apkFile ->
                    _updateState.value = UpdateState.ReadyToInstall(apkFile = apkFile, info = info)
                },
                onFailure = { error ->
                    if (error is kotlinx.coroutines.CancellationException) {
                        _updateState.value = UpdateState.Available(info)
                    } else {
                        _updateState.value = UpdateState.Error(
                            message = error.message ?: "Download failed",
                            canRetry = true,
                            failedInfo = info
                        )
                    }
                }
            )
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        val current = _updateState.value
        if (current is UpdateState.Downloading) {
            _updateState.value = UpdateState.Available(current.info)
        } else {
            _updateState.value = UpdateState.Idle
        }
    }

    fun installApk(apkFile: File, callerContext: Context? = null) {
        val result = installer.installApk(apkFile, callerContext)
        result.onFailure { error ->
            _updateState.value = UpdateState.Error(
                message = error.localizedMessage ?: "Failed to launch installer"
            )
        }
    }

    fun retry(failedInfo: UpdateInfo? = null) {
        if (failedInfo != null) {
            startDownload(failedInfo)
        } else {
            checkForUpdates(isManual = true)
        }
    }

    fun dismissUpdate() {
        if (_updateState.value is UpdateState.Downloading) {
            cancelDownload()
        }
        _updateState.value = UpdateState.Idle
    }
}
