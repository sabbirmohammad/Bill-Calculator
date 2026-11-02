package com.bangla.electricitybill.core.update

import com.bangla.electricitybill.core.update.model.AppUpdateInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubUpdateService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    private data class GithubReleaseDto(
        @SerializedName("tag_name") val tagName: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("body") val body: String?,
        @SerializedName("html_url") val htmlUrl: String?,
        @SerializedName("published_at") val publishedAt: String?,
        @SerializedName("assets") val assets: List<GithubAssetDto>?
    )

    private data class GithubAssetDto(
        @SerializedName("name") val name: String?,
        @SerializedName("size") val size: Long?,
        @SerializedName("browser_download_url") val browserDownloadUrl: String?
    )

    suspend fun checkLatestRelease(
        currentVersionName: String,
        owner: String = UpdateConfig.GITHUB_OWNER,
        repo: String = UpdateConfig.GITHUB_REPO
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = UpdateConfig.getLatestReleaseUrl(owner, repo)
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("GitHub API responded with code: ${response.code}")
                )
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(IOException("Empty response body from GitHub API"))

            val releaseDto = gson.fromJson(bodyString, GithubReleaseDto::class.java)
                ?: return@withContext Result.failure(IOException("Failed to parse release information"))

            val remoteTag = releaseDto.tagName?.trim() ?: ""
            val cleanRemoteVersion = remoteTag.removePrefix("v").removePrefix("V")
            val cleanCurrentVersion = currentVersionName.trim().removePrefix("v").removePrefix("V")

            val isNewer = isVersionNewer(remote = cleanRemoteVersion, current = cleanCurrentVersion)

            // Find APK asset
            val apkAsset = releaseDto.assets?.firstOrNull { asset ->
                asset.name?.endsWith(".apk", ignoreCase = true) == true
            }

            val downloadUrl = apkAsset?.browserDownloadUrl ?: (releaseDto.htmlUrl ?: "")
            val fileName = apkAsset?.name ?: "app-release.apk"
            val fileSize = apkAsset?.size ?: 0L

            val updateInfo = AppUpdateInfo(
                versionName = cleanRemoteVersion.ifEmpty { remoteTag },
                title = releaseDto.name ?: "Version $remoteTag",
                releaseNotes = releaseDto.body ?: "বাগ ফিক্স এবং পারফরম্যান্স উন্নয়ন।",
                apkDownloadUrl = downloadUrl,
                apkFileName = fileName,
                apkSizeBytes = fileSize,
                publishedAt = releaseDto.publishedAt ?: "",
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                htmlUrl = releaseDto.htmlUrl ?: UpdateConfig.getReleasesPageUrl(owner, repo)
            )

            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Semantic version comparison.
     * Returns true if [remote] > [current].
     */
    fun isVersionNewer(remote: String, current: String): Boolean {
        if (remote.isBlank() || current.isBlank()) return false
        if (remote.equals(current, ignoreCase = true)) return false

        val remoteParts = remote.split(".", "-")
            .mapNotNull { it.trim().toIntOrNull() }
        val currentParts = current.split(".", "-")
            .mapNotNull { it.trim().toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }

        return false
    }
}
