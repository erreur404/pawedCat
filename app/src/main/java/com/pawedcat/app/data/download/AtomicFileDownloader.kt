package com.pawedcat.app.data.download

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

class AtomicFileDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient = defaultOkHttpClient()
) {

    companion object {
        private const val BUFFER_SIZE = 32 * 1024 // 32 KB buffer
        private const val MIN_AUDIO_SIZE_BYTES = 10 * 1024L // 10 KB minimum
        private const val USER_AGENT = "PawedCat/1.0 (Android; Lightweight Podcast Client)"

        fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun getStorageDir(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)
            ?: File(context.filesDir, "podcasts")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun downloadAudio(
        episodeId: Long,
        enclosureUrl: String,
        onProgress: suspend (progressPercent: Int) -> Unit = {}
    ): DownloadResult = withContext(Dispatchers.IO) {
        val storageDir = getStorageDir()
        val finalFile = File(storageDir, "episode_$episodeId.mp3")
        val partFile = File(storageDir, "episode_$episodeId.mp3.part")

        // Clean up any stale part file
        if (partFile.exists()) {
            partFile.delete()
        }

        val request = Request.Builder()
            .url(enclosureUrl)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "*/*")
            .build()

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext DownloadResult.Error("HTTP error: ${response.code} ${response.message}")
            }

            val body = response.body
                ?: return@withContext DownloadResult.Error("Empty response body from server")

            val contentType = body.contentType()?.toString()?.lowercase() ?: ""
            if (contentType.contains("text/html")) {
                return@withContext DownloadResult.Error("Server returned HTML page instead of audio file")
            }

            val contentLength = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(partFile)

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            var totalBytesRead = 0L
            var lastReportedProgress = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
                    if (progress != lastReportedProgress && progress % 5 == 0) {
                        lastReportedProgress = progress
                        onProgress(progress)
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null
            inputStream.close()
            inputStream = null

            // Integrity Checks
            if (totalBytesRead < MIN_AUDIO_SIZE_BYTES) {
                partFile.delete()
                return@withContext DownloadResult.Error(
                    "Downloaded file is too small ($totalBytesRead bytes), stream was likely truncated"
                )
            }

            if (contentLength > 0 && totalBytesRead != contentLength) {
                partFile.delete()
                return@withContext DownloadResult.Error(
                    "Content-Length mismatch: expected $contentLength bytes, received $totalBytesRead bytes"
                )
            }

            // Atomic Rename to target file
            val renamed = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Files.move(
                        partFile.toPath(),
                        finalFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    true
                } else {
                    if (finalFile.exists()) finalFile.delete()
                    partFile.renameTo(finalFile)
                }
            } catch (e: Exception) {
                if (finalFile.exists()) finalFile.delete()
                partFile.renameTo(finalFile)
            }

            if (!renamed || !finalFile.exists()) {
                partFile.delete()
                return@withContext DownloadResult.Error("Failed to rename temporary file to destination")
            }

            onProgress(100)
            DownloadResult.Success(
                filePath = finalFile.absolutePath,
                bytesDownloaded = totalBytesRead
            )

        } catch (e: Exception) {
            // Clean up temporary file immediately on error
            try {
                outputStream?.close()
                inputStream?.close()
                if (partFile.exists()) {
                    partFile.delete()
                }
            } catch (_: Exception) {}

            DownloadResult.Error("Download failed: ${e.message}", e)
        }
    }
}
