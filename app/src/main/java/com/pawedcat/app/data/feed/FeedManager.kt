package com.pawedcat.app.data.feed

import android.content.Context
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.feed.model.ParsedFeed
import com.pawedcat.app.data.feed.model.PodcastSearchResult
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.regex.Pattern

class FeedManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient = defaultClient()
) {
    private val serviceLocator = ServiceLocator.getInstance(context)
    private val podcastRepo = serviceLocator.podcastRepository
    private val episodeRepo = serviceLocator.episodeRepository
    private val downloadManager = serviceLocator.downloadManager
    private val rssParser = RssFeedParser()
    private val directoryService = PodcastDirectoryService(okHttpClient)
    private val opmlParser = OpmlParser()

    companion object {
        private const val USER_AGENT = "PawedCat/1.0 (Android; Lightweight Podcast Client)"

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }

    suspend fun fetchFeed(feedUrl: String): Result<ParsedFeed> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(feedUrl.trim())
            .header("User-Agent", USER_AGENT)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty feed response"))
            val parsedFeed = rssParser.parse(body.byteStream(), feedUrl.trim())
            Result.success(parsedFeed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subscribeToFeed(feedUrl: String): Result<PodcastEntity> = withContext(Dispatchers.IO) {
        val cleanUrl = feedUrl.trim()
        val existing = podcastRepo.getPodcastByFeedUrl(cleanUrl)
        if (existing != null) {
            return@withContext Result.success(existing)
        }

        val fetchResult = fetchFeed(cleanUrl)
        if (fetchResult.isFailure) {
            return@withContext Result.failure(fetchResult.exceptionOrNull() ?: Exception("Failed to fetch feed"))
        }

        val parsedFeed = fetchResult.getOrThrow()

        val podcastEntity = PodcastEntity(
            feedUrl = cleanUrl,
            title = parsedFeed.title,
            author = parsedFeed.author,
            description = parsedFeed.description,
            websiteUrl = parsedFeed.websiteUrl,
            subscribedAt = System.currentTimeMillis(),
            lastRefreshedAt = System.currentTimeMillis()
        )

        val podcastId = podcastRepo.insertPodcast(podcastEntity)
        val savedPodcast = podcastEntity.copy(id = podcastId)

        // Insert initial auto-download rule (default: positive regex ".*", max 1 recent episode, enabled = false initially until configured)
        podcastRepo.saveAutoDownloadRule(
            AutoDownloadRuleEntity(
                podcastId = podcastId,
                positiveRegex = ".*",
                maxRecentCount = 1,
                isEnabled = false
            )
        )

        // Insert episodes
        val episodeEntities = parsedFeed.episodes.map { ep ->
            EpisodeEntity(
                podcastId = podcastId,
                guid = ep.guid,
                title = ep.title,
                description = ep.description,
                pubDate = ep.pubDateMs,
                enclosureUrl = ep.enclosureUrl,
                enclosureLength = ep.enclosureLength,
                enclosureType = ep.enclosureType,
                durationMs = ep.durationMs
            )
        }
        episodeRepo.insertEpisodes(episodeEntities)

        Result.success(savedPodcast)
    }

    suspend fun refreshPodcast(podcastId: Long): Result<Int> = withContext(Dispatchers.IO) {
        val podcast = podcastRepo.getPodcastById(podcastId)
            ?: return@withContext Result.failure(Exception("Podcast not found"))

        val fetchResult = fetchFeed(podcast.feedUrl)
        if (fetchResult.isFailure) {
            return@withContext Result.failure(fetchResult.exceptionOrNull() ?: Exception("Failed to fetch feed"))
        }

        val parsedFeed = fetchResult.getOrThrow()

        // Update podcast metadata
        podcastRepo.updatePodcast(
            podcast.copy(
                title = parsedFeed.title,
                author = parsedFeed.author,
                description = parsedFeed.description,
                websiteUrl = parsedFeed.websiteUrl,
                lastRefreshedAt = System.currentTimeMillis()
            )
        )

        var newEpisodesCount = 0
        val rule = podcastRepo.getAutoDownloadRule(podcastId)

        // Process episodes
        for (ep in parsedFeed.episodes) {
            val existing = episodeRepo.getEpisodeByGuid(podcastId, ep.guid)
            if (existing == null) {
                val newEpisode = EpisodeEntity(
                    podcastId = podcastId,
                    guid = ep.guid,
                    title = ep.title,
                    description = ep.description,
                    pubDate = ep.pubDateMs,
                    enclosureUrl = ep.enclosureUrl,
                    enclosureLength = ep.enclosureLength,
                    enclosureType = ep.enclosureType,
                    durationMs = ep.durationMs
                )
                val newId = episodeRepo.insertEpisode(newEpisode)
                if (newId > 0) {
                    newEpisodesCount++
                }
            }
        }

        // Check Auto-Download Rule if enabled
        if (rule != null && rule.isEnabled && rule.positiveRegex.isNotBlank()) {
            try {
                val pattern = Pattern.compile(rule.positiveRegex, Pattern.CASE_INSENSITIVE)
                val allEpisodes = episodeRepo.getEpisodesForPodcast(podcastId)
                val matchingEpisodes = allEpisodes.filter { episode ->
                    pattern.matcher(episode.title).find()
                }.take(rule.maxRecentCount)

                for (match in matchingEpisodes) {
                    if (match.downloadStatus == com.pawedcat.app.data.local.entity.DownloadStatus.NOT_DOWNLOADED) {
                        downloadManager.enqueueDownload(match.id)
                    }
                }
            } catch (_: Exception) {}
        }

        Result.success(newEpisodesCount)
    }

    suspend fun searchDirectory(query: String): List<PodcastSearchResult> {
        return directoryService.searchPodcasts(query)
    }

    suspend fun importOpml(inputStream: InputStream): List<PodcastSearchResult> = withContext(Dispatchers.IO) {
        opmlParser.parse(inputStream)
    }
}
