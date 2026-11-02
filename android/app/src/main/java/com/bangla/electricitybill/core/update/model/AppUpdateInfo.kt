package com.bangla.electricitybill.core.update.model

/**
 * Data class representing GitHub Release information and APK update metadata.
 */
data class AppUpdateInfo(
    val versionName: String,
    val title: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val publishedAt: String,
    val isUpdateAvailable: Boolean,
    val htmlUrl: String
)

sealed interface DownloadProgress {
    data object Idle : DownloadProgress
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percent: Int
    ) : DownloadProgress
    data class Completed(val apkFile: java.io.File) : DownloadProgress
    data class Failed(val error: String) : DownloadProgress
}
