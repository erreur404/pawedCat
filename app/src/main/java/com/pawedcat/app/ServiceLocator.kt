package com.pawedcat.app

import android.content.Context
import com.pawedcat.app.data.local.PawedCatDatabase
import com.pawedcat.app.data.repository.EpisodeRepository
import com.pawedcat.app.data.repository.EpisodeRepositoryImpl
import com.pawedcat.app.data.repository.PodcastRepository
import com.pawedcat.app.data.repository.PodcastRepositoryImpl
import com.pawedcat.app.data.repository.QueueRepository
import com.pawedcat.app.data.repository.QueueRepositoryImpl
import com.pawedcat.app.data.repository.SettingsRepository
import com.pawedcat.app.data.repository.SettingsRepositoryImpl

class ServiceLocator private constructor(context: Context) {
    private val database: PawedCatDatabase = PawedCatDatabase.getInstance(context)

    val podcastRepository: PodcastRepository by lazy {
        PodcastRepositoryImpl(database.podcastDao(), database.autoDownloadRuleDao())
    }

    val episodeRepository: EpisodeRepository by lazy {
        EpisodeRepositoryImpl(database.episodeDao())
    }

    val queueRepository: QueueRepository by lazy {
        QueueRepositoryImpl(database.queueDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    companion object {
        @Volatile
        private var INSTANCE: ServiceLocator? = null

        fun getInstance(context: Context): ServiceLocator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServiceLocator(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
