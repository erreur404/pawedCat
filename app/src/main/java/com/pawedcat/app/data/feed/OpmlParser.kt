package com.pawedcat.app.data.feed

import android.util.Xml
import com.pawedcat.app.data.feed.model.PodcastSearchResult
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class OpmlParser {

    fun parse(inputStream: InputStream): List<PodcastSearchResult> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val results = mutableListOf<PodcastSearchResult>()

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            val name = parser.name?.lowercase() ?: ""
            if (name == "outline") {
                val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
                    ?: parser.getAttributeValue(null, "xmlurl")
                val title = parser.getAttributeValue(null, "title")
                    ?: parser.getAttributeValue(null, "text")
                    ?: "Untitled Show"
                val htmlUrl = parser.getAttributeValue(null, "htmlUrl")
                    ?: parser.getAttributeValue(null, "htmlurl")
                    ?: ""

                if (!xmlUrl.isNullOrBlank()) {
                    results.add(
                        PodcastSearchResult(
                            title = title.trim(),
                            author = "",
                            feedUrl = xmlUrl.trim(),
                            websiteUrl = htmlUrl.trim()
                        )
                    )
                }
            }
        }

        return results
    }
}
