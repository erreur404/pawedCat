package com.pawedcat.app.data.feed

import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import com.pawedcat.app.data.local.entity.EpisodeEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoDownloadRuleTest {

    private fun createEpisode(title: String): EpisodeEntity {
        return EpisodeEntity(
            id = 1L,
            podcastId = 1L,
            guid = "guid-$title",
            title = title,
            description = "description",
            pubDate = System.currentTimeMillis(),
            enclosureUrl = "https://example.com/audio.mp3",
            enclosureLength = 1000L,
            durationMs = 60000L
        )
    }

    @Test
    fun matchesRule_positiveIncludeRegex_matchesCorrectly() {
        val rule = AutoDownloadRuleEntity(
            podcastId = 1L,
            isEnabled = true,
            positiveRegex = "(?i).*bonus.*",
            maxRecentCount = 5
        )

        val matchingEp = createEpisode("Special Bonus Episode #4")
        val normalEp = createEpisode("Episode 45 - Regular Show")

        val pattern = Regex(rule.positiveRegex)

        assertTrue(pattern.containsMatchIn(matchingEp.title))
        assertFalse(pattern.containsMatchIn(normalEp.title))
    }

    @Test
    fun matchesRule_defaultWildcardRegex_matchesAllEpisodes() {
        val rule = AutoDownloadRuleEntity(
            podcastId = 1L,
            isEnabled = true,
            positiveRegex = ".*",
            maxRecentCount = 3
        )

        val ep1 = createEpisode("Any Episode Title")
        val pattern = Regex(rule.positiveRegex)

        assertTrue(pattern.containsMatchIn(ep1.title))
    }
}
