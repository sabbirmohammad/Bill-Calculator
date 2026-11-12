package com.powerunit.core.update.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String,
    val size: Long = 0L,
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("content_type") val contentType: String? = null
)

data class UpdateInfo(
    val currentVersion: String,
    val newVersion: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSize: Long,
    val publishedAt: String,
    val sha256Url: String? = null,
    val expectedSha256: String? = null
)

enum class UpdatePhase {
    CHECKING,
    UP_TO_DATE,
    AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    ERROR
}

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(
        val info: UpdateInfo,
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long = 0L
    ) : UpdateState
    data class ReadyToInstall(
        val apkFile: File,
        val info: UpdateInfo
    ) : UpdateState
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val failedInfo: UpdateInfo? = null
    ) : UpdateState

    val phase: UpdatePhase?
        get() = when (this) {
            is Idle -> null
            is Checking -> UpdatePhase.CHECKING
            is UpToDate -> UpdatePhase.UP_TO_DATE
            is Available -> UpdatePhase.AVAILABLE
            is Downloading -> UpdatePhase.DOWNLOADING
            is ReadyToInstall -> UpdatePhase.READY_TO_INSTALL
            is Error -> UpdatePhase.ERROR
        }
}
