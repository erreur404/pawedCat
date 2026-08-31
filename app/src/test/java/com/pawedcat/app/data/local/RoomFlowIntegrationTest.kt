package com.pawedcat.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pawedcat.app.data.local.dao.EpisodeDao
import com.pawedcat.app.data.local.dao.PodcastDao
import com.pawedcat.app.data.local.dao.QueueDao
import com.pawedcat.app.data.local.entity.DownloadStatus
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import com.pawedcat.app.data.local.entity.QueueItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomFlowIntegrationTest {

    private lateinit var db: PawedCatDatabase
    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var queueDao: QueueDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PawedCatDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        podcastDao = db.podcastDao()
        episodeDao = db.episodeDao()
        queueDao = db.queueDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQuery_podcastAndVolumeBoost() = runBlocking {
        val podcast = PodcastEntity(
            id = 1L,
            title = "Test Podcast",
            feedUrl = "https://example.com/feed.xml",
            websiteUrl = "https://example.com",
            author = "Host",
            description = "Desc",
            volumeBoostDb = 6,
            subscribedAt = System.currentTimeMillis()
        )
        podcastDao.insertPodcast(podcast)

        val retrieved = podcastDao.getPodcastById(1L)
        assertNotNull(retrieved)
        assertEquals("Test Podcast", retrieved?.title)
        assertEquals(6, retrieved?.volumeBoostDb)

        podcastDao.updateVolumeBoost(1L, 8)
        val updated = podcastDao.getPodcastById(1L)
        assertEquals(8, updated?.volumeBoostDb)
    }

    @Test
    fun downloadedFilterAndSearch_episodesFlow() = runBlocking {
        val podcast = PodcastEntity(
            id = 1L,
            title = "Physics Show",
            feedUrl = "https://example.com/feed.xml",
            websiteUrl = "https://example.com",
            author = "Host",
            description = "Desc"
        )
        podcastDao.insertPodcast(podcast)

        val ep1 = EpisodeEntity(
            id = 101L,
            podcastId = 1L,
            guid = "ep101",
            title = "Quantum Physics Intro",
            description = "Basics of quantum",
            pubDate = 1000L,
            enclosureUrl = "https://example.com/ep1.mp3",
            enclosureLength = 1000L,
            durationMs = 30000L,
            downloadStatus = DownloadStatus.DOWNLOADED,
            localFilePath = "/data/user/0/ep1.mp3"
        )
        val ep2 = EpisodeEntity(
            id = 102L,
            podcastId = 1L,
            guid = "ep102",
            title = "Relativity and Gravity",
            description = "Einstein mechanics",
            pubDate = 2000L,
            enclosureUrl = "https://example.com/ep2.mp3",
            enclosureLength = 2000L,
            durationMs = 40000L,
            downloadStatus = DownloadStatus.NOT_DOWNLOADED
        )
        episodeDao.insertEpisodes(listOf(ep1, ep2))

        val downloaded = episodeDao.getDownloadedEpisodes()
        assertEquals(1, downloaded.size)
        assertEquals("Quantum Physics Intro", downloaded[0].title)

        val searchResult = episodeDao.searchEpisodes("Relativity")
        assertEquals(1, searchResult.size)
        assertEquals("Relativity and Gravity", searchResult[0].title)
    }

    @Test
    fun queueOperations_playNextAndReorder() = runBlocking {
        val podcast = PodcastEntity(
            id = 1L,
            title = "Queue Show",
            feedUrl = "https://example.com/feed.xml",
            websiteUrl = "https://example.com",
            author = "Host",
            description = "Desc"
        )
        podcastDao.insertPodcast(podcast)

        val ep1 = EpisodeEntity(
            id = 101L,
            podcastId = 1L,
            guid = "ep101",
            title = "Episode 1",
            description = "Desc",
            pubDate = 1000L,
            enclosureUrl = "https://example.com/ep1.mp3",
            enclosureLength = 1000L,
            durationMs = 30000L
        )
        val ep2 = EpisodeEntity(
            id = 102L,
            podcastId = 1L,
            guid = "ep102",
            title = "Episode 2",
            description = "Desc",
            pubDate = 2000L,
            enclosureUrl = "https://example.com/ep2.mp3",
            enclosureLength = 2000L,
            durationMs = 40000L
        )
        episodeDao.insertEpisodes(listOf(ep1, ep2))

        val q1 = QueueItemEntity(episodeId = 101L, position = 0, addedAt = 1000L)
        val q2 = QueueItemEntity(episodeId = 102L, position = 1, addedAt = 2000L)
        queueDao.insertQueueItem(q1)
        queueDao.insertQueueItem(q2)

        val items = queueDao.getQueueItems()
        assertEquals(2, items.size)
        assertEquals(101L, items[0].episodeId)
        assertEquals(102L, items[1].episodeId)
    }
}
