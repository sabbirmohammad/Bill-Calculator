package com.powerunit.core.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateDownloader @Inject constructor() {

    suspend fun downloadApk(
        downloadUrl: String,
        targetFile: File,
        expectedSize: Long = 0L,
        expectedSha256: String? = null,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedBytesPerSec: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            targetFile.parentFile?.mkdirs()
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val connection = openRedirectedConnection(downloadUrl)
            val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: expectedSize.takeIf { it > 0 } ?: -1L

            try {
                connection.inputStream.use { input ->
                    copyStreamWithProgress(input, targetFile, totalBytes, onProgress)
                }
            } finally {
                connection.disconnect()
            }

            check(targetFile.exists() && targetFile.length() > 0L) {
                targetFile.delete()
                "Downloaded APK file is empty or missing"
            }

            if (expectedSize > 0L && targetFile.length() != expectedSize) {
                targetFile.delete()
                error("APK size mismatch: expected $expectedSize, got ${targetFile.length()}")
            }

            if (!expectedSha256.isNullOrBlank()) {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                targetFile.inputStream().use { input ->
                    val buf = ByteArray(8192)
                    var read: Int
                    while (input.read(buf).also { read = it } != -1) {
                        digest.update(buf, 0, read)
                    }
                }
                val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
                if (!computedHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                    targetFile.delete()
                    error("SHA-256 checksum verification failed (expected: $expectedSha256, computed: $computedHash)")
                }
            }

            targetFile.setReadable(true, false)

            targetFile
        }
    }

    private fun openRedirectedConnection(url: String): HttpURLConnection {
        var currentUrl = url
        var connection: HttpURLConnection? = null
        var redirects = 0
        val maxRedirects = 5

        while (redirects < maxRedirects) {
            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "WattWise-App-Updater")
                setRequestProperty("Accept", "*/*")
            }

            val status = conn.responseCode
            if (isRedirectStatus(status)) {
                val newLocation = conn.getHeaderField("Location")
                    ?: error("Redirect location missing from HTTP $status")
                conn.disconnect()
                currentUrl = newLocation
                redirects++
            } else if (status in 200..299) {
                connection = conn
                break
            } else {
                conn.disconnect()
                error("Failed to download update: HTTP $status")
            }
        }

        return connection ?: error("Connection could not be established after redirects")
    }

    private fun isRedirectStatus(status: Int): Boolean {
        return status in listOf(
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_SEE_OTHER,
            307,
            308
        )
    }

    private suspend fun copyStreamWithProgress(
        inputStream: InputStream,
        targetFile: File,
        totalBytes: Long,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedBytesPerSec: Long) -> Unit
    ) {
        var downloadedBytes = 0L
        var lastReportTime = System.currentTimeMillis()
        var bytesSinceLastReport = 0L
        var lastReportedPercent = -1

        FileOutputStream(targetFile).use { outputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (!currentCoroutineContext().isActive) {
                    targetFile.delete()
                    throw kotlinx.coroutines.CancellationException("Download cancelled")
                }

                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                bytesSinceLastReport += bytesRead

                val now = System.currentTimeMillis()
                val timeDelta = now - lastReportTime
                val percent = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else -1
                if (timeDelta >= 150 && (percent != lastReportedPercent || totalBytes <= 0)) {
                    val currentSpeed = (bytesSinceLastReport * 1000L) / timeDelta
                    onProgress(downloadedBytes, totalBytes, currentSpeed)
                    lastReportTime = now
                    lastReportedPercent = percent
                    bytesSinceLastReport = 0L
                }
            }
            outputStream.flush()
        }

        onProgress(downloadedBytes, downloadedBytes, 0L)
    }
}
