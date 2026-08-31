package com.pawedcat.app.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.DownloadStatus
import kotlinx.coroutines.flow.first

class PodcastDownloadManager(
    private val context: Context
) {
    private val workManager: WorkManager = WorkManager.getInstance(context)
    private val serviceLocator: ServiceLocator = ServiceLocator.getInstance(context)

    companion object {
        fun workTag(episodeId: Long) = "download_episode_$episodeId"
    }

    suspend fun enqueueDownload(episodeId: Long) {
        val episodeRepo = serviceLocator.episodeRepository
        val settingsRepo = serviceLocator.settingsRepository

        val wifiOnly = settingsRepo.downloadOnWifiOnlyFlow.first()
        val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val inputData = Data.Builder()
            .putLong(EpisodeDownloadWorker.KEY_EPISODE_ID, episodeId)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<EpisodeDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workTag(episodeId))
            .build()

        // Set status in DB to QUEUED
        episodeRepo.updateDownloadStatus(
            id = episodeId,
            status = DownloadStatus.QUEUED,
            filePath = null,
            progress = 0,
            downloadedAt = null
        )

        workManager.enqueueUniqueWork(
            workTag(episodeId),
            ExistingWorkPolicy.KEEP,
            downloadWork
        )
    }

    suspend fun cancelDownload(episodeId: Long) {
        workManager.cancelUniqueWork(workTag(episodeId))
        val episodeRepo = serviceLocator.episodeRepository
        episodeRepo.deleteEpisodeAudioFile(episodeId)
    }

    suspend fun deleteDownload(episodeId: Long) {
        workManager.cancelUniqueWork(workTag(episodeId))
        val episodeRepo = serviceLocator.episodeRepository
        episodeRepo.deleteEpisodeAudioFile(episodeId)
    }
}
