package com.pawedcat.app.data.feed

import com.pawedcat.app.data.local.entity.PodcastEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
class OpmlRoundTripTest {

    @Test
    fun exportAndImport_preservesAllFeedsAndTitles() {
        val originalPodcasts = listOf(
            PodcastEntity(
                id = 1L,
                title = "Hardcore History",
                feedUrl = "https://dancarlin.com/feed.xml",
                websiteUrl = "https://dancarlin.com",
                author = "Dan Carlin",
                description = "History podcast",
                subscribedAt = 1000L
            ),
            PodcastEntity(
                id = 2L,
                title = "Lex Fridman Podcast",
                feedUrl = "https://lexfridman.com/feed/podcast/",
                websiteUrl = "https://lexfridman.com",
                author = "Lex Fridman",
                description = "Conversations about AI and science",
                subscribedAt = 2000L
            )
        )

        // 1. Export to OPML stream
        val outputStream = ByteArrayOutputStream()
        OpmlExporter().export(originalPodcasts, outputStream)
        val opmlXml = outputStream.toString(Charsets.UTF_8.name())

        // 2. Re-import using OpmlParser
        val parsedFeeds = OpmlParser().parse(ByteArrayInputStream(opmlXml.toByteArray()))

        assertEquals(2, parsedFeeds.size)
        assertEquals("Hardcore History", parsedFeeds[0].title)
        assertEquals("https://dancarlin.com/feed.xml", parsedFeeds[0].feedUrl)
        assertEquals("Lex Fridman Podcast", parsedFeeds[1].title)
        assertEquals("https://lexfridman.com/feed/podcast/", parsedFeeds[1].feedUrl)
    }
}
