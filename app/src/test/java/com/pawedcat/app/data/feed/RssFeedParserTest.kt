package com.pawedcat.app.data.feed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
class RssFeedParserTest {

    private val parser = RssFeedParser()

    @Test
    fun parse_standardRssFeed_extractsMetadataAndEpisodes() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
                <channel>
                    <title>Dan Carlin's Hardcore History</title>
                    <description>In "Hardcore History" Dan Carlin takes his unorthodox way of thinking...</description>
                    <link>https://www.dancarlin.com</link>
                    <itunes:author>Dan Carlin</itunes:author>
                    <item>
                        <title>Show 69 - Twilight of the Aesir</title>
                        <guid>https://traffic.libsyn.com/dancarlin/hh69.mp3</guid>
                        <pubDate>Mon, 23 Jan 2023 12:00:00 -0000</pubDate>
                        <enclosure url="https://traffic.libsyn.com/dancarlin/hh69.mp3" length="350000000" type="audio/mpeg"/>
                        <itunes:duration>04:30:00</itunes:duration>
                        <description><![CDATA[<p>The Viking Era comes to an explosive close.</p>]]></description>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parsed = parser.parse(ByteArrayInputStream(xml.toByteArray()), "https://dancarlin.com/feed.xml")

        assertEquals("Dan Carlin's Hardcore History", parsed.title)
        assertEquals("Dan Carlin", parsed.author)
        assertEquals("https://www.dancarlin.com", parsed.websiteUrl)
        assertEquals(1, parsed.episodes.size)

        val ep = parsed.episodes[0]
        assertEquals("Show 69 - Twilight of the Aesir", ep.title)
        assertEquals("https://traffic.libsyn.com/dancarlin/hh69.mp3", ep.enclosureUrl)
        assertEquals(350000000L, ep.enclosureLength)
        assertEquals(16200000L, ep.durationMs) // 4h 30m = 16,200,000 ms
        assertTrue(ep.description.contains("Viking Era"))
    }

    @Test
    fun parse_octopusStyleFeedWithBareLinkText_doesNotCrash() {
        // Reproduces the Octopus feed bug that crashed with XmlPullParserException
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>Octopus Tech Talks</title>
                    <link>https://octopus.example.com/podcast</link>
                    <item>
                        <title>Episode 1 - Microservices</title>
                        <link>https://octopus.example.com/ep1</link>
                        <enclosure url="https://octopus.example.com/audio/ep1.mp3" length="50000000" type="audio/mpeg">https://octopus.example.com/ep1</enclosure>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parsed = parser.parse(ByteArrayInputStream(xml.toByteArray()), "https://octopus.example.com/feed.xml")

        assertEquals("Octopus Tech Talks", parsed.title)
        assertEquals(1, parsed.episodes.size)
        assertEquals("https://octopus.example.com/audio/ep1.mp3", parsed.episodes[0].enclosureUrl)
    }
}
