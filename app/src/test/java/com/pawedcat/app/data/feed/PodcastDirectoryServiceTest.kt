package com.pawedcat.app.data.feed

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PodcastDirectoryServiceTest {

    private lateinit var directoryService: PodcastDirectoryService

    @Before
    fun setup() {
        directoryService = PodcastDirectoryService()
    }

    @Test
    fun testIsApplePodcastsUrl() {
        assertTrue(directoryService.isApplePodcastsUrl("https://podcasts.apple.com/fr/podcast/la-terre-au-carr%C3%A9/id294060079"))
        assertTrue(directoryService.isApplePodcastsUrl("https://podcasts.apple.com/us/podcast/the-daily/id1200361736"))
        assertTrue(directoryService.isApplePodcastsUrl("https://itunes.apple.com/podcast/id294060079"))
        assertFalse(directoryService.isApplePodcastsUrl("https://example.com/rss.xml"))
        assertFalse(directoryService.isApplePodcastsUrl("https://radiofrance-podcast.net/podcast09/podcast_5e238698.xml"))
    }

    @Test
    fun testExtractApplePodcastId() {
        assertEquals("294060079", directoryService.extractApplePodcastId("https://podcasts.apple.com/fr/podcast/la-terre-au-carr%C3%A9/id294060079"))
        assertEquals("1200361736", directoryService.extractApplePodcastId("https://podcasts.apple.com/us/podcast/the-daily/id1200361736"))
        assertEquals("294060079", directoryService.extractApplePodcastId("https://itunes.apple.com/podcast/id294060079"))
    }

    @Test
    fun testResolveApplePodcastFeedUrlForFranceInter() = runBlocking {
        val feedUrl = directoryService.resolveApplePodcastFeedUrl("https://podcasts.apple.com/fr/podcast/la-terre-au-carr%C3%A9/id294060079")
        assertNotNull(feedUrl)
        assertTrue(feedUrl!!.startsWith("https://radiofrance-podcast.net/"))
        assertTrue(feedUrl.endsWith(".xml"))
    }

    @Test
    fun testSearchPodcastsFindsLaTerreAuCarre() = runBlocking {
        val results = directoryService.searchPodcasts("la terre au carre")
        assertTrue("Expected search results for 'la terre au carre'", results.isNotEmpty())
        val match = results.find { it.title.contains("terre au carr", ignoreCase = true) }
        assertNotNull("Expected 'La Terre au carré' in results", match)
        assertTrue("Expected non-blank feedUrl", match!!.feedUrl.isNotBlank())
        assertTrue("Expected valid RSS URL ending with .xml or http", match.feedUrl.startsWith("http"))
    }
}
