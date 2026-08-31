package com.pawedcat.app.ui.util

import android.content.Context
import android.content.Intent

object ShareUtils {

    /**
     * Shares a podcast subscription feed with clean RSS link and PawedCat discovery signature.
     */
    fun sharePodcast(context: Context, podcastTitle: String, feedUrl: String) {
        val text = """
            Podcast: "$podcastTitle"
            Feed: $feedUrl

            (Open in PawedCat: https://play.google.com/store/apps/details?id=com.pawedcat.app)
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, podcastTitle)
        }
        context.startActivity(Intent.createChooser(intent, "Share podcast"))
    }

    /**
     * Shares an episode with title and direct enclosure audio URL for zero-friction listening in any app or browser.
     */
    fun shareEpisode(context: Context, podcastTitle: String, episodeTitle: String, enclosureUrl: String) {
        val header = if (podcastTitle.isNotBlank()) "$podcastTitle — \"$episodeTitle\"" else "\"$episodeTitle\""
        val text = "$header\n$enclosureUrl"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, episodeTitle)
        }
        context.startActivity(Intent.createChooser(intent, "Share episode"))
    }
}
