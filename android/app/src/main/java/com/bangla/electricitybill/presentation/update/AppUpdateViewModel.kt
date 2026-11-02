package com.bangla.electricitybill.presentation.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.BuildConfig
import com.bangla.electricitybill.core.update.ApkDownloader
import com.bangla.electricitybill.core.update.ApkInstaller
import com.bangla.electricitybill.core.update.GithubUpdateService
import com.bangla.electricitybill.core.update.model.AppUpdateInfo
import com.bangla.electricitybill.core.update.model.DownloadProgress
import com.bangla.electricitybill.data.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Downloading(
        val info: AppUpdateInfo,
        val percent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateUiState
    data class ReadyToInstall(
        val info: AppUpdateInfo,
        val apkFile: File
    ) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val githubUpdateService: GithubUpdateService,
    private val apkDownloader: ApkDownloader,
    private val apkInstaller: ApkInstaller,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    /**
     * Checks GitHub for a newer version release.
     * @param isManual True when initiated by user from Settings, False for automatic startup check.
     */
    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Checking

            val result = githubUpdateService.checkLatestRelease(
                currentVersionName = BuildConfig.VERSION_NAME
            )

            result.onSuccess { updateInfo ->
                if (updateInfo.isUpdateAvailable) {
                    if (!isManual) {
                        // Check if previously dismissed within 24 hours
                        val (dismissedVer, dismissedTime) = preferencesManager.dismissedUpdateInfo.first()
                        val oneDayMillis = 24 * 60 * 60 * 1000L
                        val isWithin24Hours = (System.currentTimeMillis() - dismissedTime) < oneDayMillis
                        if (dismissedVer == updateInfo.versionName && isWithin24Hours) {
                            _uiState.value = UpdateUiState.Idle
                            return@onSuccess
                        }
                    }
                    _uiState.value = UpdateUiState.UpdateAvailable(updateInfo)
                } else {
                    _uiState.value = if (isManual) UpdateUiState.UpToDate else UpdateUiState.Idle
                }
            }.onFailure { error ->
                _uiState.value = if (isManual) {
                    UpdateUiState.Error(
                        error.localizedMessage ?: "আপডেট সার্ভারের সাথে সংযোগ করা সম্ভব হয়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
                    )
                } else {
                    UpdateUiState.Idle
                }
            }
        }
    }

    /**
     * Downloads the APK file from GitHub release assets with progress emission.
     */
    fun startDownload(info: AppUpdateInfo) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _uiState.value = UpdateUiState.Downloading(
                info = info,
                percent = 0,
                bytesDownloaded = 0L,
                totalBytes = info.apkSizeBytes
            )

            apkDownloader.downloadApk(info.apkDownloadUrl, info.apkFileName).collect { progress ->
                when (progress) {
                    is DownloadProgress.Idle -> {
                        _uiState.value = UpdateUiState.Downloading(info, 0, 0L, info.apkSizeBytes)
                    }
                    is DownloadProgress.Downloading -> {
                        _uiState.value = UpdateUiState.Downloading(
                            info = info,
                            percent = progress.percent,
                            bytesDownloaded = progress.bytesDownloaded,
                            totalBytes = progress.totalBytes
                        )
                    }
                    is DownloadProgress.Completed -> {
                        _uiState.value = UpdateUiState.ReadyToInstall(
                            info = info,
                            apkFile = progress.apkFile
                        )
                    }
                    is DownloadProgress.Failed -> {
                        _uiState.value = UpdateUiState.Error(progress.error)
                    }
                }
            }
        }
    }

    /**
     * Cancels the current download and returns to UpdateAvailable state or Idle.
     */
    fun cancelDownload(info: AppUpdateInfo) {
        downloadJob?.cancel()
        _uiState.value = UpdateUiState.UpdateAvailable(info)
    }

    /**
     * Triggers Android package installer for the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        val result = apkInstaller.installApk(context, apkFile)
        result.onFailure { error ->
            _uiState.value = UpdateUiState.Error(
                error.localizedMessage ?: "অ্যাপ্লিকেশন ইনস্টল শুরু করতে সমস্যা হয়েছে।"
            )
        }
    }

    /**
     * Remind user later: saves timestamp and dismisses dialog.
     */
    fun remindLater(versionName: String) {
        viewModelScope.launch {
            preferencesManager.recordDismissedUpdate(versionName)
            _uiState.value = UpdateUiState.Idle
        }
    }

    /**
     * Dismiss current dialog / reset state.
     */
    fun dismiss() {
        downloadJob?.cancel()
        _uiState.value = UpdateUiState.Idle
    }
}
