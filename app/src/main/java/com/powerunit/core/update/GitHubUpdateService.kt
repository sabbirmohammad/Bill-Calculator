package com.powerunit.core.update

import com.powerunit.core.update.model.GitHubAsset
import com.powerunit.core.update.model.GitHubRelease
import com.powerunit.core.update.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateService @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchLatestRelease(
        repository: String = "sabbirmohammad/Bill-Calculator",
        currentVersion: String
    ): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanRepo = repository.trim().trim('/')
                .takeIf { it.isNotBlank() && it.contains('/') && !it.contains("electricity-bill", ignoreCase = true) }
                ?: "sabbirmohammad/Bill-Calculator"

            var updateInfo: UpdateInfo? = null

            // 1. Try standard GitHub REST API
            try {
                val apiUrl = "https://api.github.com/repos/$cleanRepo/releases/latest"
                val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "WattWise-App-Updater")
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val release = json.decodeFromString<GitHubRelease>(responseBody)

                    val apkAsset = findApkAsset(release.assets)
                    if (apkAsset != null) {
                        val remoteVersion = cleanVersion(release.tagName)
                        val currentClean = cleanVersion(currentVersion)

                        if (isNewerVersion(remoteVersion, currentClean)) {
                            updateInfo = UpdateInfo(
                                currentVersion = currentVersion,
                                newVersion = remoteVersion,
                                releaseName = release.name ?: release.tagName,
                                releaseNotes = release.body?.trim().orEmpty().ifEmpty { "Performance updates and bug fixes." },
                                downloadUrl = apkAsset.downloadUrl,
                                apkSize = apkAsset.size,
                                publishedAt = release.publishedAt.orEmpty()
                            )
                        } else {
                            return@runCatching null // Already on latest version
                        }
                    }
                }
            } catch (_: Exception) {
                // If API fails (e.g. rate limit, connection timeout), smoothly fall through to web redirect
            }

            if (updateInfo != null) {
                return@runCatching updateInfo
            }

            // 2. High-reliability fallback using GitHub web release redirect (Zero rate limit, works everywhere)
            val fallback = fetchFromWebRedirect(cleanRepo, currentVersion)
            if (fallback != null) {
                return@runCatching fallback
            }

            // If neither finds a newer version, current app is up-to-date
            null
        }
    }

    /**
     * Fallback method using GitHub web release redirect (Zero API rate limit).
     * https://github.com/owner/repo/releases/latest redirects (302) to https://github.com/owner/repo/releases/tag/<tag_name>
     */
    private fun fetchFromWebRedirect(
        cleanRepo: String,
        currentVersion: String
    ): UpdateInfo? {
        return try {
            val webUrl = "https://github.com/$cleanRepo/releases/latest"
            val conn = (URL(webUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            }

            val code = conn.responseCode
            if (code == 302 || code == 301) {
                val location = conn.getHeaderField("Location") ?: return null
                val tag = location.substringAfterLast("/releases/tag/", "").ifEmpty {
                    location.substringAfterLast("/")
                }
                if (tag.isNotBlank()) {
                    val remoteVersion = cleanVersion(tag)
                    val currentClean = cleanVersion(currentVersion)

                    if (isNewerVersion(remoteVersion, currentClean)) {
                        val downloadUrl = "https://github.com/$cleanRepo/releases/download/$tag/app-release.apk"
                        return UpdateInfo(
                            currentVersion = currentVersion,
                            newVersion = remoteVersion,
                            releaseName = "Release $tag",
                            releaseNotes = "New version $tag available on GitHub.",
                            downloadUrl = downloadUrl,
                            apkSize = 0L,
                            publishedAt = ""
                        )
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun findApkAsset(assets: List<GitHubAsset>): GitHubAsset? {
        val apks = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) {
            return assets.firstOrNull { it.contentType == "application/vnd.android.package-archive" }
        }
        return apks.firstOrNull { it.name.contains("WattWise", ignoreCase = true) }
            ?: apks.firstOrNull { !it.name.contains("unsigned", ignoreCase = true) }
            ?: apks.first()
    }

    companion object {
        private val VERSION_REGEX = Regex("""\d+(?:\.\d+)*""")

        fun cleanVersion(tag: String): String {
            val trimmed = tag.trim()
            val match = VERSION_REGEX.find(trimmed)
            return match?.value ?: trimmed.removePrefix("v").removePrefix("V").split("-")[0]
        }

        fun isNewerVersion(remote: String, current: String): Boolean {
            val remoteParts = cleanVersion(remote).split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanVersion(current).split(".").mapNotNull { it.toIntOrNull() }

            if (remoteParts.isEmpty()) return false
            if (currentParts.isEmpty()) return true

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
}
