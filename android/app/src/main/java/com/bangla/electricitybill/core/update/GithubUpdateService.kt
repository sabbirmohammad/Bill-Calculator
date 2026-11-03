package com.bangla.electricitybill.core.update

import com.bangla.electricitybill.core.update.model.AppUpdateInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

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

    /**
     * Checks for the latest release with multi-tiered resilience:
     * 1. Primary: GitHub Releases Atom feed (rate-limit free, immune to 403 errors).
     * 2. Fallback 1: GitHub REST API (used if available and unthrottled).
     * 3. Fallback 2: GitHub Web redirect (rate-limit free HEAD request to /releases/latest).
     */
    suspend fun checkLatestRelease(
        currentVersionName: String,
        owner: String = UpdateConfig.GITHUB_OWNER,
        repo: String = UpdateConfig.GITHUB_REPO
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        // Tier 1: Try Atom Feed (Reliable, fast, completely free of GitHub API 60 req/hr rate limits)
        tryAtomFeed(currentVersionName, owner, repo).fold(
            onSuccess = { return@withContext Result.success(it) },
            onFailure = { atomError ->
                // Tier 2: Try REST API
                tryRestApi(currentVersionName, owner, repo).fold(
                    onSuccess = { return@withContext Result.success(it) },
                    onFailure = { restError ->
                        // Tier 3: Try Web Redirect
                        tryWebRedirect(currentVersionName, owner, repo).fold(
                            onSuccess = { return@withContext Result.success(it) },
                            onFailure = { redirectError ->
                                Result.failure(
                                    IOException(
                                        "আপডেট সার্ভারের সাথে সংযোগ করা সম্ভব হয়নি। (${atomError.message ?: redirectError.message})"
                                    )
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    private fun tryAtomFeed(
        currentVersionName: String,
        owner: String,
        repo: String
    ): Result<AppUpdateInfo> {
        return try {
            val url = UpdateConfig.getReleasesAtomUrl(owner, repo)
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/atom+xml, application/xml, text/xml, */*")
                .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
                .build()

            val body = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IOException("Atom feed HTTP code: ${response.code}"))
                }
                response.body?.string()
            } ?: return Result.failure(IOException("Empty Atom feed body"))

            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                try {
                    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                    setFeature("http://xml.org/sax/features/external-general-entities", false)
                    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                } catch (_: Exception) {}
            }
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(body.byteInputStream())
            val entryNodes = doc.getElementsByTagName("entry")

            if (entryNodes.length == 0) {
                return Result.failure(IllegalStateException("No release entries in feed"))
            }

            val firstEntry = entryNodes.item(0) as Element
            var titleText = ""
            var updatedText = ""
            var htmlUrl = ""
            var releaseTag = ""
            var contentHtml = ""

            val childNodes = firstEntry.childNodes
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val el = node as Element
                    when (el.localName ?: el.tagName) {
                        "title" -> titleText = el.textContent.trim()
                        "updated" -> updatedText = el.textContent.trim()
                        "content" -> contentHtml = el.textContent.trim()
                        "link" -> {
                            val rel = el.getAttribute("rel")
                            if (rel == "alternate" || rel.isEmpty()) {
                                htmlUrl = el.getAttribute("href")
                            }
                        }
                        "id" -> {
                            val idText = el.textContent.trim()
                            if (releaseTag.isEmpty() && idText.contains("/")) {
                                releaseTag = idText.substringAfterLast("/")
                            }
                        }
                    }
                }
            }

            if (htmlUrl.isNotBlank() && htmlUrl.contains("/tag/")) {
                releaseTag = htmlUrl.substringAfterLast("/tag/").substringBefore("/").substringBefore("?")
            }

            if (releaseTag.isBlank()) {
                return Result.failure(IllegalStateException("Failed to parse release tag from Atom feed"))
            }

            val cleanRemoteVersion = releaseTag.removePrefix("v").removePrefix("V")
            val cleanCurrentVersion = currentVersionName.trim().removePrefix("v").removePrefix("V")
            val isNewer = isVersionNewer(remote = cleanRemoteVersion, current = cleanCurrentVersion)

            val downloadUrl = UpdateConfig.getDirectApkDownloadUrl(releaseTag, "app-release.apk", owner, repo)
            val cleanNotes = formatHtmlToMarkdown(contentHtml).ifBlank {
                "নতুন সংস্করণ $releaseTag প্রকাশিত হয়েছে।"
            }

            // Quick non-blocking attempt to probe Content-Length of the APK asset
            val probedSize = probeFileSize(downloadUrl)

            val info = AppUpdateInfo(
                versionName = cleanRemoteVersion.ifEmpty { releaseTag },
                title = titleText.ifBlank { "Version $releaseTag" },
                releaseNotes = cleanNotes,
                apkDownloadUrl = downloadUrl,
                apkFileName = "app-release.apk",
                apkSizeBytes = probedSize,
                publishedAt = updatedText,
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                htmlUrl = htmlUrl.ifBlank { UpdateConfig.getReleasesPageUrl(owner, repo) }
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tryRestApi(
        currentVersionName: String,
        owner: String,
        repo: String
    ): Result<AppUpdateInfo> {
        return try {
            val url = UpdateConfig.getLatestReleaseUrl(owner, repo)
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
                .build()

            val bodyString = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IOException("GitHub REST API responded with code: ${response.code}"))
                }
                response.body?.string()
            } ?: return Result.failure(IOException("Empty response body from GitHub REST API"))

            val releaseDto = gson.fromJson(bodyString, GithubReleaseDto::class.java)
                ?: return Result.failure(IOException("Failed to parse release JSON"))

            val remoteTag = releaseDto.tagName?.trim() ?: ""
            val cleanRemoteVersion = remoteTag.removePrefix("v").removePrefix("V")
            val cleanCurrentVersion = currentVersionName.trim().removePrefix("v").removePrefix("V")
            val isNewer = isVersionNewer(remote = cleanRemoteVersion, current = cleanCurrentVersion)

            val apkAsset = releaseDto.assets?.firstOrNull { asset ->
                asset.name?.endsWith(".apk", ignoreCase = true) == true
            }

            val downloadUrl = apkAsset?.browserDownloadUrl ?: UpdateConfig.getDirectApkDownloadUrl(remoteTag, "app-release.apk", owner, repo)
            val fileName = apkAsset?.name ?: "app-release.apk"
            val fileSize = apkAsset?.size ?: 0L

            val info = AppUpdateInfo(
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

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tryWebRedirect(
        currentVersionName: String,
        owner: String,
        repo: String
    ): Result<AppUpdateInfo> {
        return try {
            val clientNoRedirect = okHttpClient.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(UpdateConfig.getLatestReleaseWebUrl(owner, repo))
                .head()
                .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
                .build()

            val location = clientNoRedirect.newCall(request).execute().use { response ->
                response.header("Location") ?: ""
            }

            if (location.isBlank()) {
                return Result.failure(IOException("Could not resolve latest release redirect URL"))
            }

            val remoteTag = location.substringAfterLast("/tag/").substringBefore("/").substringBefore("?")
            val cleanRemoteVersion = remoteTag.removePrefix("v").removePrefix("V")
            val cleanCurrentVersion = currentVersionName.trim().removePrefix("v").removePrefix("V")
            val isNewer = isVersionNewer(remote = cleanRemoteVersion, current = cleanCurrentVersion)

            val downloadUrl = UpdateConfig.getDirectApkDownloadUrl(remoteTag, "app-release.apk", owner, repo)

            val info = AppUpdateInfo(
                versionName = cleanRemoteVersion.ifEmpty { remoteTag },
                title = "সংস্করণ $remoteTag",
                releaseNotes = "নতুন সংস্করণ $remoteTag প্রকাশিত হয়েছে। অ্যাপটি আপডেট করে নতুন ফিচারসমূহ উপভোগ করুন।",
                apkDownloadUrl = downloadUrl,
                apkFileName = "app-release.apk",
                apkSizeBytes = 0L,
                publishedAt = "",
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                htmlUrl = location
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun probeFileSize(downloadUrl: String): Long {
        return try {
            val quickClient = okHttpClient.newBuilder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(downloadUrl)
                .head()
                .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
                .build()
            val length = quickClient.newCall(request).execute().use { response ->
                response.body?.contentLength() ?: -1L
            }
            if (length > 0) length else 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun formatHtmlToMarkdown(html: String): String {
        if (html.isBlank()) return ""
        return html
            .replace(Regex("(?i)<li[^>]*>"), "\n• ")
            .replace(Regex("(?i)</li[^>]*>"), "")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)</h[1-6]>"), "\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
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

