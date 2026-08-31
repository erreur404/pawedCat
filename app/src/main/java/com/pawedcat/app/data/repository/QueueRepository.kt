package com.pawedcat.app.data.repository

import com.pawedcat.app.data.local.dao.QueueDao
import com.pawedcat.app.data.local.entity.QueueItemEntity
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    fun getQueueItemsFlow(): Flow<List<QueueItemEntity>>
    suspend fun getQueueItems(): List<QueueItemEntity>
    suspend fun getCurrentQueueItem(): QueueItemEntity?
    suspend fun playNow(episodeId: Long)
    suspend fun playNext(episodeId: Long)
    suspend fun addToQueueEnd(episodeId: Long)
    suspend fun removeAndCompact(episodeId: Long)
    suspend fun clearQueue()
    suspend fun updatePositions(items: List<QueueItemEntity>)
}

class QueueRepositoryImpl(
    private val queueDao: QueueDao
) : QueueRepository {

    override fun getQueueItemsFlow(): Flow<List<QueueItemEntity>> = queueDao.getQueueItemsFlow()

    override suspend fun getQueueItems(): List<QueueItemEntity> = queueDao.getQueueItems()

    override suspend fun getCurrentQueueItem(): QueueItemEntity? = queueDao.getCurrentQueueItem()

    override suspend fun playNow(episodeId: Long) = queueDao.playNow(episodeId)

    override suspend fun playNext(episodeId: Long) = queueDao.playNext(episodeId)

    override suspend fun addToQueueEnd(episodeId: Long) = queueDao.addToQueueEnd(episodeId)

    override suspend fun removeAndCompact(episodeId: Long) = queueDao.removeAndCompact(episodeId)

    override suspend fun clearQueue() = queueDao.clearQueue()

    override suspend fun updatePositions(items: List<QueueItemEntity>) {
        items.forEachIndexed { index, item ->
            queueDao.updatePosition(item.id, index)
        }
    }
}
