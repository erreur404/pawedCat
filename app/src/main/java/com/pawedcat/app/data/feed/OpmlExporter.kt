package com.pawedcat.app.data.feed

import android.util.Xml
import com.pawedcat.app.data.local.entity.PodcastEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes a valid OPML 2.0 file for the given list of subscribed [PodcastEntity] objects.
 *
 * Each podcast becomes one <outline> element with:
 *   type="rss"  title  xmlUrl  htmlUrl
 *
 * No third-party dependencies — uses the platform's XmlSerializer directly.
 */
class OpmlExporter {

    fun export(podcasts: List<PodcastEntity>, outputStream: OutputStream) {
        val serializer = Xml.newSerializer()
        serializer.setOutput(outputStream, "UTF-8")
        serializer.startDocument("UTF-8", true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        // <opml version="2.0">
        serializer.startTag(null, "opml")
        serializer.attribute(null, "version", "2.0")

        // <head>
        serializer.startTag(null, "head")

        serializer.startTag(null, "title")
        serializer.text("PawedCat Subscriptions")
        serializer.endTag(null, "title")

        val dateStr = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).format(Date())
        serializer.startTag(null, "dateCreated")
        serializer.text(dateStr)
        serializer.endTag(null, "dateCreated")

        serializer.endTag(null, "head")

        // <body>
        serializer.startTag(null, "body")

        for (podcast in podcasts) {
            serializer.startTag(null, "outline")
            serializer.attribute(null, "type", "rss")
            serializer.attribute(null, "text", podcast.title)
            serializer.attribute(null, "title", podcast.title)
            serializer.attribute(null, "xmlUrl", podcast.feedUrl)
            if (podcast.websiteUrl.isNotBlank()) {
                serializer.attribute(null, "htmlUrl", podcast.websiteUrl)
            }
            serializer.endTag(null, "outline")
        }

        serializer.endTag(null, "body")
        serializer.endTag(null, "opml")
        serializer.endDocument()
        outputStream.flush()
    }
}
