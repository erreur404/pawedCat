package com.pawedcat.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["podcastId"]),
        Index(value = ["podcastId", "guid"], unique = true),
        Index(value = ["pubDate"]),
        Index(value = ["downloadStatus"])
    ]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val podcastId: Long,
    val guid: String,
    val title: String,
    val description: String = "",
    val pubDate: Long = 0L,
    val enclosureUrl: String,
    val enclosureLength: Long = 0L,
    val enclosureType: String = "audio/mpeg",
    val durationMs: Long = 0L,
    val playbackPositionMs: Long = 0L,
    val isPlayed: Boolean = false,
    val localFilePath: String? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgress: Int = 0,
    val downloadedAt: Long? = null
)
