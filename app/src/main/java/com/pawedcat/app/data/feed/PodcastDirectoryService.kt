package com.pawedcat.app.data.feed

import com.pawedcat.app.data.feed.model.PodcastSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.regex.Pattern

class PodcastDirectoryService(
    private val okHttpClient: OkHttpClient = defaultClient()
) {
    companion object {
        private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search?media=podcast&entity=podcast&limit=30&term="
        private const val FYYD_SEARCH_URL = "https://api.fyyd.de/0.2/search/podcast?title="
        private const val BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        private const val APP_USER_AGENT = "PawedCat/${com.pawedcat.app.BuildConfig.VERSION_NAME} (Android)"

        private val APPLE_PODCASTS_URL_REGEX = Pattern.compile(
            "https?://(?:podcasts|itunes)\\.apple\\.com/(?:[a-zA-Z]{2}/)?podcast/(?:[^/]+/)?id(\\d+)",
            Pattern.CASE_INSENSITIVE
        )

        private val FEED_URL_IN_JSON_REGEX = Pattern.compile(
            "\"feedUrl\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
        )

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }

    fun isApplePodcastsUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.contains("apple.com") && (trimmed.contains("/podcast/") || trimmed.contains("/id"))
    }

    fun extractApplePodcastId(url: String): String? {
        val matcher = APPLE_PODCASTS_URL_REGEX.matcher(url.trim())
        if (matcher.find()) {
            return matcher.group(1)
        }
        val idMatcher = Pattern.compile("id(\\d+)").matcher(url.trim())
        if (idMatcher.find()) {
            return idMatcher.group(1)
        }
        return null
    }

    suspend fun resolveApplePodcastFeedUrl(urlOrId: String): String? = withContext(Dispatchers.IO) {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return@withContext null

        val targetUrl = if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed
        } else {
            val id = extractApplePodcastId(trimmed) ?: trimmed.filter { it.isDigit() }
            if (id.isNotBlank()) "https://podcasts.apple.com/podcast/id$id" else return@withContext null
        }

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "fr,en-US;q=0.9,en;q=0.8")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string().orEmpty()
                val matcher = FEED_URL_IN_JSON_REGEX.matcher(html)
                if (matcher.find()) {
                    val rawUrl = matcher.group(1) ?: ""
                    val cleanedUrl = unescapeJsonUrl(rawUrl)
                    if (cleanedUrl.startsWith("http://", ignoreCase = true) || cleanedUrl.startsWith("https://", ignoreCase = true)) {
                        return@withContext cleanedUrl
                    }
                }
            }
        } catch (_: Exception) {
            // Fall through to fallback
        }

        // Fallback: iTunes lookup API if ID is available
        val showId = extractApplePodcastId(targetUrl)
        if (!showId.isNullOrBlank()) {
            try {
                val lookupRequest = Request.Builder()
                    .url("https://itunes.apple.com/lookup?id=$showId")
                    .header("User-Agent", APP_USER_AGENT)
                    .build()
                val lookupResp = okHttpClient.newCall(lookupRequest).execute()
                if (lookupResp.isSuccessful) {
                    val lookupBody = lookupResp.body?.string().orEmpty()
                    val json = JSONObject(lookupBody)
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val feed = results.getJSONObject(0).optString("feedUrl", "")
                        if (feed.isNotBlank()) return@withContext feed
                    }
                }
            } catch (_: Exception) {}
        }

        null
    }

    private fun unescapeJsonUrl(url: String): String {
        return url.replace("\\/", "/")
            .replace("\\u0026", "&")
            .replace("&amp;", "&")
            .trim()
    }

    suspend fun searchPodcasts(query: String): List<PodcastSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val itunesResults = searchItunes(query)
        if (itunesResults.isNotEmpty()) {
            return@withContext itunesResults
        }

        // Secondary fallback directory: Fyyd
        searchFyyd(query)
    }

    private suspend fun searchItunes(query: String): List<PodcastSearchResult> = supervisorScope {
        val encodedQuery = try {
            URLEncoder.encode(query.trim(), "UTF-8")
        } catch (_: Exception) {
            query.trim()
        }

        val request = Request.Builder()
            .url(ITUNES_SEARCH_URL + encodedQuery)
            .header("User-Agent", APP_USER_AGENT)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@supervisorScope emptyList()

            val bodyString = response.body?.string() ?: return@supervisorScope emptyList()
            val json = JSONObject(bodyString)
            val resultsArray = json.optJSONArray("results") ?: return@supervisorScope emptyList()

            // Prepare items and resolve missing feed URLs concurrently
            val deferredList = (0 until resultsArray.length()).map { i ->
                async(Dispatchers.IO) {
                    val item = resultsArray.getJSONObject(i)
                    var feedUrl = item.optString("feedUrl", "").trim()
                    val title = item.optString("collectionName", item.optString("trackName", "")).trim()
                    val author = item.optString("artistName", "").trim()
                    val website = item.optString("collectionViewUrl", item.optString("trackViewUrl", "")).trim()
                    val collectionId = item.optLong("collectionId", 0L)

                    if (title.isBlank()) return@async null

                    // If feedUrl is missing in the iTunes search result (e.g. France Inter / Radio France),
                    // resolve it via the Apple Podcasts show page
                    if (feedUrl.isBlank()) {
                        val lookupUrl = website.ifBlank {
                            if (collectionId > 0) "https://podcasts.apple.com/podcast/id$collectionId" else ""
                        }
                        if (lookupUrl.isNotBlank()) {
                            val resolved = resolveApplePodcastFeedUrl(lookupUrl)
                            if (!resolved.isNullOrBlank()) {
                                feedUrl = resolved
                            } else {
                                // Fallback: assign the Apple Podcasts show URL so FeedManager can attempt resolution on subscribe
                                feedUrl = lookupUrl
                            }
                        }
                    }

                    if (feedUrl.isNotBlank() && title.isNotBlank()) {
                        PodcastSearchResult(
                            title = title,
                            author = author,
                            feedUrl = feedUrl,
                            websiteUrl = website
                        )
                    } else {
                        null
                    }
                }
            }

            deferredList.awaitAll().filterNotNull()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun searchFyyd(query: String): List<PodcastSearchResult> = withContext(Dispatchers.IO) {
        val encodedQuery = try {
            URLEncoder.encode(query.trim(), "UTF-8")
        } catch (_: Exception) {
            query.trim()
        }

        val request = Request.Builder()
            .url(FYYD_SEARCH_URL + encodedQuery)
            .header("User-Agent", APP_USER_AGENT)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val bodyString = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(bodyString)
            val dataArray = json.optJSONArray("data") ?: return@withContext emptyList()

            val results = mutableListOf<PodcastSearchResult>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val feedUrl = item.optString("xmlURL", "").trim()
                val title = item.optString("title", "").trim()
                val author = item.optString("author", "").trim()
                val website = item.optString("htmlURL", "").trim()

                if (feedUrl.isNotBlank() && title.isNotBlank()) {
                    results.add(
                        PodcastSearchResult(
                            title = title,
                            author = author,
                            feedUrl = feedUrl,
                            websiteUrl = website
                        )
                    )
                }
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }
}
