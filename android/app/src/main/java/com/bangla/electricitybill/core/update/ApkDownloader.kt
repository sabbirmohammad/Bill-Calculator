package com.bangla.electricitybill.core.update

import android.content.Context
import android.os.Environment
import com.bangla.electricitybill.core.update.model.DownloadProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {

    fun downloadApk(url: String, fileName: String): Flow<DownloadProgress> = flow {
        emit(DownloadProgress.Idle)

        val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.cacheDir, "apk_updates").apply { mkdirs() }

        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        // Clean up any old apk files
        targetDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".apk")) {
                file.delete()
            }
        }

        val destinationFile = File(targetDir, fileName.ifBlank { "update.apk" })

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Bangladesh-Electricity-Bill-Calculator-App")
            .build()

        var response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            emit(DownloadProgress.Failed("ডাউনলোড ব্যর্থ হয়েছে। কোড: ${response.code}"))
            return@flow
        }

        val body = response.body
        if (body == null) {
            emit(DownloadProgress.Failed("ডাউনলোড ডাটা পাওয়া যায়নি।"))
            return@flow
        }

        val totalLength = body.contentLength()
        val inputStream = body.byteStream()
        val outputStream = FileOutputStream(destinationFile)

        try {
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            var totalBytesRead = 0L
            var lastReportedPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val percent = if (totalLength > 0) {
                    ((totalBytesRead * 100) / totalLength).toInt()
                } else {
                    -1
                }

                if (percent != lastReportedPercent) {
                    lastReportedPercent = percent
                    emit(
                        DownloadProgress.Downloading(
                            bytesDownloaded = totalBytesRead,
                            totalBytes = totalLength,
                            percent = if (percent in 0..100) percent else 0
                        )
                    )
                }
            }

            outputStream.flush()
            emit(DownloadProgress.Completed(destinationFile))
        } catch (e: Exception) {
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            emit(DownloadProgress.Failed(e.localizedMessage ?: "ডাউনলোড বাধাগ্রস্ত হয়েছে।"))
        } finally {
            try {
                outputStream.close()
            } catch (_: IOException) {}
            try {
                inputStream.close()
            } catch (_: IOException) {}
            response.close()
        }
    }.flowOn(Dispatchers.IO)
}
