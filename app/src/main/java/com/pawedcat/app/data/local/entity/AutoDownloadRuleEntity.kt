package com.pawedcat.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "auto_download_rules",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AutoDownloadRuleEntity(
    @PrimaryKey
    val podcastId: Long,
    val positiveRegex: String = ".*",
    val maxRecentCount: Int = 1,
    val isEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
