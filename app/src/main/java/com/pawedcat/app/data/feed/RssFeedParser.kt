package com.pawedcat.app.data.feed

import android.util.Xml
import com.pawedcat.app.data.feed.model.ParsedEpisode
import com.pawedcat.app.data.feed.model.ParsedFeed
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RssFeedParser {

    private val dateFormats = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
        SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    )

    fun parse(inputStream: InputStream, fallbackFeedUrl: String): ParsedFeed {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()

        return readRoot(parser, fallbackFeedUrl)
    }

    private fun readRoot(parser: XmlPullParser, fallbackFeedUrl: String): ParsedFeed {
        var feedTitle = ""
        var feedDescription = ""
        var feedAuthor = ""
        var feedWebsite = ""
        val episodes = mutableListOf<ParsedEpisode>()

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            val name = parser.name?.lowercase() ?: ""
            when (name) {
                "channel" -> {
                    // RSS channel
                }
                "title" -> {
                    if (feedTitle.isEmpty()) {
                        feedTitle = readText(parser)
                    } else {
                        skip(parser)
                    }
                }
                "description" -> {
                    if (feedDescription.isEmpty()) {
                        feedDescription = readText(parser)
                    } else {
                        skip(parser)
                    }
                }
                "itunes:author", "author" -> {
                    if (feedAuthor.isEmpty()) {
                        feedAuthor = readText(parser)
                    } else {
                        skip(parser)
                    }
                }
                "link" -> {
                    if (feedWebsite.isEmpty()) {
                        val href = parser.getAttributeValue(null, "href")
                        if (!href.isNullOrBlank()) {
                            feedWebsite = href
                            parser.nextTag()
                        } else {
                            feedWebsite = readText(parser)
                        }
                    } else {
                        skip(parser)
                    }
                }
                "item", "entry" -> {
                    val episode = readEpisode(parser)
                    if (episode != null && episode.enclosureUrl.isNotBlank()) {
                        episodes.add(episode)
                    }
                }
                else -> {
                    // Continue scanning
                }
            }
        }

        if (feedTitle.isBlank()) feedTitle = "Untitled Podcast"

        return ParsedFeed(
            title = feedTitle.trim(),
            description = feedDescription.trim(),
            author = feedAuthor.trim(),
            websiteUrl = feedWebsite.trim(),
            feedUrl = fallbackFeedUrl,
            episodes = episodes
        )
    }

    private fun readEpisode(parser: XmlPullParser): ParsedEpisode? {
        var guid = ""
        var title = ""
        var description = ""
        var pubDateMs = 0L
        var enclosureUrl = ""
        var enclosureLength = 0L
        var enclosureType = "audio/mpeg"
        var durationMs = 0L

        val tagName = parser.name

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            val eventType = parser.eventType
            val name = parser.name?.lowercase() ?: ""

            if (eventType == XmlPullParser.START_TAG) {
                when (name) {
                    "guid", "id" -> {
                        guid = readText(parser)
                    }
                    "title" -> {
                        title = readText(parser)
                    }
                    "description", "itunes:summary", "summary" -> {
                        if (description.isEmpty()) {
                            description = readText(parser)
                        } else {
                            skip(parser)
                        }
                    }
                    "pubdate", "published", "updated" -> {
                        val dateStr = readText(parser)
                        pubDateMs = parseDate(dateStr)
                    }
                    "enclosure" -> {
                        val url = parser.getAttributeValue(null, "url")
                        val length = parser.getAttributeValue(null, "length")?.toLongOrNull() ?: 0L
                        val type = parser.getAttributeValue(null, "type") ?: "audio/mpeg"
                        if (!url.isNullOrBlank()) {
                            enclosureUrl = url
                            enclosureLength = length
                            enclosureType = type
                        }
                        parser.nextTag()
                    }
                    "link" -> {
                        val rel = parser.getAttributeValue(null, "rel")
                        val href = parser.getAttributeValue(null, "href")
                        if (rel == "enclosure" && !href.isNullOrBlank()) {
                            enclosureUrl = href
                            enclosureLength = parser.getAttributeValue(null, "length")?.toLongOrNull() ?: 0L
                            enclosureType = parser.getAttributeValue(null, "type") ?: "audio/mpeg"
                        }
                        parser.nextTag()
                    }
                    "itunes:duration", "duration" -> {
                        val durationStr = readText(parser)
                        durationMs = parseDuration(durationStr)
                    }
                    else -> skip(parser)
                }
            } else if (eventType == XmlPullParser.END_TAG && name == tagName) {
                break
            }
        }

        if (enclosureUrl.isBlank()) return null
        if (guid.isBlank()) guid = enclosureUrl
        if (title.isBlank()) title = "Untitled Episode"
        if (pubDateMs == 0L) pubDateMs = System.currentTimeMillis()

        return ParsedEpisode(
            guid = guid.trim(),
            title = title.trim(),
            description = description.trim(),
            pubDateMs = pubDateMs,
            enclosureUrl = enclosureUrl.trim(),
            enclosureLength = enclosureLength,
            enclosureType = enclosureType.trim(),
            durationMs = durationMs
        )
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun parseDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val cleanStr = dateStr.trim()
        for (format in dateFormats) {
            try {
                val date = format.parse(cleanStr)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun parseDuration(durationStr: String): Long {
        if (durationStr.isBlank()) return 0L
        val parts = durationStr.trim().split(":")
        try {
            return when (parts.size) {
                1 -> (parts[0].toLongOrNull() ?: 0L) * 1000L
                2 -> {
                    val minutes = parts[0].toLongOrNull() ?: 0L
                    val seconds = parts[1].toLongOrNull() ?: 0L
                    (minutes * 60 + seconds) * 1000L
                }
                3 -> {
                    val hours = parts[0].toLongOrNull() ?: 0L
                    val minutes = parts[1].toLongOrNull() ?: 0L
                    val seconds = parts[2].toLongOrNull() ?: 0L
                    (hours * 3600 + minutes * 60 + seconds) * 1000L
                }
                else -> 0L
            }
        } catch (_: Exception) {
            return 0L
        }
    }
}
