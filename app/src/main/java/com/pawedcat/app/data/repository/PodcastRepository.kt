package com.pawedcat.app.data.repository

import com.pawedcat.app.data.local.dao.AutoDownloadRuleDao
import com.pawedcat.app.data.local.dao.PodcastDao
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import kotlinx.coroutines.flow.Flow

interface PodcastRepository {
    fun getAllPodcastsFlow(): Flow<List<PodcastEntity>>
    suspend fun getAllPodcasts(): List<PodcastEntity>
    suspend fun getPodcastById(id: Long): PodcastEntity?
    fun getPodcastByIdFlow(id: Long): Flow<PodcastEntity?>
    suspend fun getPodcastByFeedUrl(feedUrl: String): PodcastEntity?
    suspend fun insertPodcast(podcast: PodcastEntity): Long
    suspend fun updatePodcast(podcast: PodcastEntity)
    suspend fun deletePodcast(podcastId: Long)

    fun getAutoDownloadRuleFlow(podcastId: Long): Flow<AutoDownloadRuleEntity?>
    suspend fun getAutoDownloadRule(podcastId: Long): AutoDownloadRuleEntity?
    suspend fun getActiveRules(): List<AutoDownloadRuleEntity>
    suspend fun saveAutoDownloadRule(rule: AutoDownloadRuleEntity)
    suspend fun deleteAutoDownloadRule(podcastId: Long)
    suspend fun updateVolumeBoost(podcastId: Long, volumeBoostDb: Int)
}

class PodcastRepositoryImpl(
    private val podcastDao: PodcastDao,
    private val autoDownloadRuleDao: AutoDownloadRuleDao
) : PodcastRepository {

    override fun getAllPodcastsFlow(): Flow<List<PodcastEntity>> = podcastDao.getAllPodcastsFlow()

    override suspend fun getAllPodcasts(): List<PodcastEntity> = podcastDao.getAllPodcasts()

    override suspend fun getPodcastById(id: Long): PodcastEntity? = podcastDao.getPodcastById(id)

    override fun getPodcastByIdFlow(id: Long): Flow<PodcastEntity?> = podcastDao.getPodcastByIdFlow(id)

    override suspend fun getPodcastByFeedUrl(feedUrl: String): PodcastEntity? = podcastDao.getPodcastByFeedUrl(feedUrl)

    override suspend fun insertPodcast(podcast: PodcastEntity): Long = podcastDao.insertPodcast(podcast)

    override suspend fun updatePodcast(podcast: PodcastEntity) = podcastDao.updatePodcast(podcast)

    override suspend fun deletePodcast(podcastId: Long) {
        autoDownloadRuleDao.deleteRuleForPodcast(podcastId)
        podcastDao.deletePodcastById(podcastId)
    }

    override fun getAutoDownloadRuleFlow(podcastId: Long): Flow<AutoDownloadRuleEntity?> =
        autoDownloadRuleDao.getRuleForPodcastFlow(podcastId)

    override suspend fun getAutoDownloadRule(podcastId: Long): AutoDownloadRuleEntity? =
        autoDownloadRuleDao.getRuleForPodcast(podcastId)

    override suspend fun getActiveRules(): List<AutoDownloadRuleEntity> =
        autoDownloadRuleDao.getActiveRules()

    override suspend fun saveAutoDownloadRule(rule: AutoDownloadRuleEntity) =
        autoDownloadRuleDao.insertOrUpdateRule(rule)

    override suspend fun deleteAutoDownloadRule(podcastId: Long) =
        autoDownloadRuleDao.deleteRuleForPodcast(podcastId)

    override suspend fun updateVolumeBoost(podcastId: Long, volumeBoostDb: Int) =
        podcastDao.updateVolumeBoost(podcastId, volumeBoostDb)
}
