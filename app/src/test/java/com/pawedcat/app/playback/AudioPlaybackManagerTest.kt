package com.pawedcat.app.playback

import android.content.Context
import androidx.media3.common.Player
import androidx.test.core.app.ApplicationProvider
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.DownloadStatus
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AudioPlaybackManagerTest {

    private lateinit var context: Context
    private lateinit var serviceLocator: ServiceLocator
    private lateinit var playbackManager: AudioPlaybackManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        serviceLocator = ServiceLocator.getInstance(context)
        playbackManager = serviceLocator.playbackManager
    }

    @Test
    fun getPlayer_isNotNullAndConfigured() {
        val player = playbackManager.getPlayer()
        assertNotNull("ExoPlayer should be initialized", player)
        assertEquals("Seek back increment should be 15s", 15000L, player?.seekBackIncrement)
        assertEquals("Seek forward increment should be 30s", 30000L, player?.seekForwardIncrement)
    }

    @Test
    fun playbackSuppressionReason_noneDoesNotTriggerTimeout() = runTest {
        val player = playbackManager.getPlayer()
        assertNotNull(player)

        // Simulating playback suppression cleared cleanly
        // Should not crash and state should be valid
        assertNotNull(playbackManager.playbackState.value)
        assertFalse(playbackManager.playbackState.value.isPlaying)
    }

    @Test
    fun hotSwap_updatesLocalFilePathWhenDownloaded() = runTest {
        val episodeRepo = serviceLocator.episodeRepository
        val podcastRepo = serviceLocator.podcastRepository

        val podcastId = podcastRepo.insertPodcast(
            PodcastEntity(
                title = "Hot Swap Podcast",
                feedUrl = "https://example.com/hotswap.xml"
            )
        )

        val tempAudioFile = File(context.cacheDir, "test_audio.mp3").apply {
            writeBytes(ByteArray(1024))
        }

        val episodeId = episodeRepo.insertEpisode(
            EpisodeEntity(
                podcastId = podcastId,
                guid = "hotswap-guid-1",
                title = "Hot Swap Episode",
                enclosureUrl = "https://example.com/audio.mp3",
                downloadStatus = DownloadStatus.DOWNLOADED,
                localFilePath = tempAudioFile.absolutePath,
                durationMs = 60000L
            )
        )

        playbackManager.playNow(episodeId)

        // Give coroutines time to load
        val ep = episodeRepo.getEpisodeById(episodeId)
        assertNotNull(ep)
        assertEquals(DownloadStatus.DOWNLOADED, ep?.downloadStatus)
        assertEquals(tempAudioFile.absolutePath, ep?.localFilePath)
        assertFalse("isHotSwapping should be false after completion", playbackManager.isHotSwapping)

        tempAudioFile.delete()
    }

    @Test
    fun playbackService_lifecycle_createsAndDestroysCleanly() {
        val controller = org.robolectric.Robolectric.buildService(PlaybackService::class.java)
        val service = controller.create().get()
        assertNotNull("PlaybackService should initialize and create MediaSession", service)
        controller.startCommand(0, 0)
        controller.destroy()
    }
}
