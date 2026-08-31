package com.pawedcat.app.data.repository

import com.pawedcat.app.data.local.dao.EpisodeDao
import com.pawedcat.app.data.local.entity.DownloadStatus
import com.pawedcat.app.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EpisodeRepository {
    fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<EpisodeEntity>>
    suspend fun getEpisodesForPodcast(podcastId: Long): List<EpisodeEntity>
    fun getDownloadedEpisodesFlow(): Flow<List<EpisodeEntity>>
    suspend fun getDownloadedEpisodes(): List<EpisodeEntity>
    suspend fun getEpisodeById(id: Long): EpisodeEntity?
    fun getEpisodeByIdFlow(id: Long): Flow<EpisodeEntity?>
    suspend fun getEpisodeByGuid(podcastId: Long, guid: String): EpisodeEntity?
    suspend fun insertEpisode(episode: EpisodeEntity): Long
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>): List<Long>
    suspend fun updateEpisode(episode: EpisodeEntity)
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long, isPlayed: Boolean)
    suspend fun updateDownloadStatus(
        id: Long,
        status: DownloadStatus,
        filePath: String? = null,
        progress: Int = 0,
        downloadedAt: Long? = null
    )
    suspend fun updateDownloadProgress(id: Long, progress: Int)
    suspend fun deleteEpisodeAudioFile(episodeId: Long)
}

class EpisodeRepositoryImpl(
    private val episodeDao: EpisodeDao
) : EpisodeRepository {

    override fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<EpisodeEntity>> =
        episodeDao.getEpisodesForPodcastFlow(podcastId)

    override suspend fun getEpisodesForPodcast(podcastId: Long): List<EpisodeEntity> =
        episodeDao.getEpisodesForPodcast(podcastId)

    override fun getDownloadedEpisodesFlow(): Flow<List<EpisodeEntity>> =
        episodeDao.getDownloadedEpisodesFlow()

    override suspend fun getDownloadedEpisodes(): List<EpisodeEntity> =
        episodeDao.getDownloadedEpisodes()

    override suspend fun getEpisodeById(id: Long): EpisodeEntity? =
        episodeDao.getEpisodeById(id)

    override fun getEpisodeByIdFlow(id: Long): Flow<EpisodeEntity?> =
        episodeDao.getEpisodeByIdFlow(id)

    override suspend fun getEpisodeByGuid(podcastId: Long, guid: String): EpisodeEntity? =
        episodeDao.getEpisodeByGuid(podcastId, guid)

    override suspend fun insertEpisode(episode: EpisodeEntity): Long =
        episodeDao.insertEpisode(episode)

    override suspend fun insertEpisodes(episodes: List<EpisodeEntity>): List<Long> =
        episodeDao.insertEpisodes(episodes)

    override suspend fun updateEpisode(episode: EpisodeEntity) =
        episodeDao.updateEpisode(episode)

    override suspend fun updatePlaybackPosition(id: Long, positionMs: Long, isPlayed: Boolean) =
        episodeDao.updatePlaybackPosition(id, positionMs, isPlayed)

    override suspend fun updateDownloadStatus(
        id: Long,
        status: DownloadStatus,
        filePath: String?,
        progress: Int,
        downloadedAt: Long?
    ) = episodeDao.updateDownloadStatus(id, status, filePath, progress, downloadedAt)

    override suspend fun updateDownloadProgress(id: Long, progress: Int) =
        episodeDao.updateDownloadProgress(id, progress)

    override suspend fun deleteEpisodeAudioFile(episodeId: Long) {
        val episode = episodeDao.getEpisodeById(episodeId) ?: return
        episode.localFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore file deletion errors
            }
        }
        episodeDao.clearDownloadStatus(episodeId)
    }
}
