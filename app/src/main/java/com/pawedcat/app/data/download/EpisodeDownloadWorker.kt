package com.pawedcat.app.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.DownloadStatus
import java.io.File

class EpisodeDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_EPISODE_ID = "episode_id"
    }

    override suspend fun doWork(): Result {
        val episodeId = inputData.getLong(KEY_EPISODE_ID, -1L)
        if (episodeId == -1L) {
            return Result.failure()
        }

        val serviceLocator = ServiceLocator.getInstance(applicationContext)
        val episodeRepo = serviceLocator.episodeRepository
        val episode = episodeRepo.getEpisodeById(episodeId) ?: return Result.failure()

        // Check if already downloaded
        episode.localFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                episodeRepo.updateDownloadStatus(
                    id = episodeId,
                    status = DownloadStatus.DOWNLOADED,
                    filePath = path,
                    progress = 100,
                    downloadedAt = episode.downloadedAt ?: System.currentTimeMillis()
                )
                return Result.success()
            }
        }

        // Set status to DOWNLOADING
        episodeRepo.updateDownloadStatus(
            id = episodeId,
            status = DownloadStatus.DOWNLOADING,
            filePath = null,
            progress = 0,
            downloadedAt = null
        )

        val downloader = AtomicFileDownloader(applicationContext)
        val result = downloader.downloadAudio(
            episodeId = episodeId,
            enclosureUrl = episode.enclosureUrl,
            onProgress = { progress ->
                episodeRepo.updateDownloadProgress(episodeId, progress)
            }
        )

        return when (result) {
            is DownloadResult.Success -> {
                episodeRepo.updateDownloadStatus(
                    id = episodeId,
                    status = DownloadStatus.DOWNLOADED,
                    filePath = result.filePath,
                    progress = 100,
                    downloadedAt = System.currentTimeMillis()
                )
                Result.success()
            }
            is DownloadResult.Error -> {
                episodeRepo.updateDownloadStatus(
                    id = episodeId,
                    status = DownloadStatus.FAILED,
                    filePath = null,
                    progress = 0,
                    downloadedAt = null
                )
                Result.failure()
            }
            is DownloadResult.Cancelled -> {
                episodeRepo.updateDownloadStatus(
                    id = episodeId,
                    status = DownloadStatus.NOT_DOWNLOADED,
                    filePath = null,
                    progress = 0,
                    downloadedAt = null
                )
                Result.failure()
            }
        }
    }
}
