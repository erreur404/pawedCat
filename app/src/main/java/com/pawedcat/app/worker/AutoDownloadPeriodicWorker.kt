package com.pawedcat.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pawedcat.app.ServiceLocator
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class AutoDownloadPeriodicWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "pawedcat_auto_download_sync"

        suspend fun schedule(context: Context) {
            val serviceLocator = ServiceLocator.getInstance(context)
            val wifiOnly = serviceLocator.settingsRepository.downloadOnWifiOnlyFlow.first()
            val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<AutoDownloadPeriodicWorker>(
                3, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val serviceLocator = ServiceLocator.getInstance(applicationContext)
        val podcastRepo = serviceLocator.podcastRepository
        val feedManager = serviceLocator.feedManager

        val podcasts = podcastRepo.getAllPodcasts()
        for (podcast in podcasts) {
            try {
                feedManager.refreshPodcast(podcast.id)
            } catch (_: Exception) {}
        }

        return Result.success()
    }
}
