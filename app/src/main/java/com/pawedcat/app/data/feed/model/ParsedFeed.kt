package com.pawedcat.app.data.feed.model

data class ParsedFeed(
    val title: String,
    val description: String,
    val author: String,
    val websiteUrl: String,
    val feedUrl: String,
    val episodes: List<ParsedEpisode>
)

data class ParsedEpisode(
    val guid: String,
    val title: String,
    val description: String,
    val pubDateMs: Long,
    val enclosureUrl: String,
    val enclosureLength: Long,
    val enclosureType: String,
    val durationMs: Long
)

data class PodcastSearchResult(
    val title: String,
    val author: String,
    val feedUrl: String,
    val websiteUrl: String = ""
)
