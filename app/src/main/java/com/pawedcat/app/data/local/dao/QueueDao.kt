package com.pawedcat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import com.pawedcat.app.data.local.entity.QueueItemEntity
import kotlinx.coroutines.flow.Flow

data class QueueEpisodeItem(
    @Embedded val queueItem: QueueItemEntity,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "id"
    )
    val episode: EpisodeEntity,
    @Relation(
        entity = PodcastEntity::class,
        parentColumn = "episodeId",
        entityColumn = "id"
    )
    val podcast: PodcastEntity? = null
)

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    fun getQueueItemsFlow(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    suspend fun getQueueItems(): List<QueueItemEntity>

    @Query("SELECT * FROM queue_items WHERE position = 0 LIMIT 1")
    suspend fun getCurrentQueueItem(): QueueItemEntity?

    @Query("SELECT * FROM queue_items WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getQueueItemByEpisodeId(episodeId: Long): QueueItemEntity?

    @Query("SELECT MAX(position) FROM queue_items")
    suspend fun getMaxPosition(): Int?

    @Query("SELECT COUNT(*) FROM queue_items")
    suspend fun getQueueCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: QueueItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteQueueItemById(id: Long)

    @Query("DELETE FROM queue_items WHERE episodeId = :episodeId")
    suspend fun deleteQueueItemByEpisodeId(episodeId: Long)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Query("UPDATE queue_items SET position = :newPosition WHERE id = :id")
    suspend fun updatePosition(id: Long, newPosition: Int)

    @Query("UPDATE queue_items SET position = position + 1 WHERE position >= :fromPosition")
    suspend fun shiftPositionsDownFrom(fromPosition: Int)

    @Query("UPDATE queue_items SET position = position - 1 WHERE position > :fromPosition")
    suspend fun shiftPositionsUpAfter(fromPosition: Int)

    @Transaction
    suspend fun addToQueueEnd(episodeId: Long) {
        val existing = getQueueItemByEpisodeId(episodeId)
        if (existing != null) return
        val maxPos = getMaxPosition() ?: -1
        insertQueueItem(QueueItemEntity(episodeId = episodeId, position = maxPos + 1))
    }

    @Transaction
    suspend fun playNext(episodeId: Long) {
        val existing = getQueueItemByEpisodeId(episodeId)
        if (existing != null) {
            deleteQueueItemByEpisodeId(episodeId)
            shiftPositionsUpAfter(existing.position)
        }
        val count = getQueueCount()
        if (count == 0) {
            insertQueueItem(QueueItemEntity(episodeId = episodeId, position = 0))
        } else {
            shiftPositionsDownFrom(1)
            insertQueueItem(QueueItemEntity(episodeId = episodeId, position = 1))
        }
    }

    @Transaction
    suspend fun playNow(episodeId: Long) {
        val existing = getQueueItemByEpisodeId(episodeId)
        if (existing != null) {
            deleteQueueItemByEpisodeId(episodeId)
            shiftPositionsUpAfter(existing.position)
        }
        shiftPositionsDownFrom(0)
        insertQueueItem(QueueItemEntity(episodeId = episodeId, position = 0))
    }

    @Transaction
    suspend fun removeAndCompact(episodeId: Long) {
        val item = getQueueItemByEpisodeId(episodeId) ?: return
        deleteQueueItemByEpisodeId(episodeId)
        shiftPositionsUpAfter(item.position)
    }
}
