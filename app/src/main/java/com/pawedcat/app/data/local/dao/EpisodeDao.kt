package com.pawedcat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pawedcat.app.data.local.entity.DownloadStatus
import com.pawedcat.app.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY pubDate DESC")
    suspend fun getEpisodesForPodcast(podcastId: Long): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE downloadStatus = 'DOWNLOADED' ORDER BY downloadedAt DESC")
    fun getDownloadedEpisodesFlow(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE downloadStatus = 'DOWNLOADED' ORDER BY downloadedAt DESC")
    suspend fun getDownloadedEpisodes(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    suspend fun getEpisodeById(id: Long): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    fun getEpisodeByIdFlow(id: Long): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId AND guid = :guid LIMIT 1")
    suspend fun getEpisodeByGuid(podcastId: Long, guid: String): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisode(episode: EpisodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>): List<Long>

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Query("UPDATE episodes SET playbackPositionMs = :positionMs, isPlayed = :isPlayed WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long, isPlayed: Boolean)

    @Query("""
        UPDATE episodes 
        SET downloadStatus = :status, 
            localFilePath = :filePath, 
            downloadProgress = :progress, 
            downloadedAt = :downloadedAt 
        WHERE id = :id
    """)
    suspend fun updateDownloadStatus(
        id: Long,
        status: DownloadStatus,
        filePath: String?,
        progress: Int,
        downloadedAt: Long?
    )

    @Query("""
        UPDATE episodes 
        SET downloadProgress = :progress 
        WHERE id = :id
    """)
    suspend fun updateDownloadProgress(id: Long, progress: Int)

    @Query("""
        UPDATE episodes 
        SET downloadStatus = 'NOT_DOWNLOADED', 
            localFilePath = NULL, 
            downloadProgress = 0, 
            downloadedAt = NULL 
        WHERE id = :id
    """)
    suspend fun clearDownloadStatus(id: Long)

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)
}
