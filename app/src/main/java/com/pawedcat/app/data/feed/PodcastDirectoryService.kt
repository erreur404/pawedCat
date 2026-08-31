package com.pawedcat.app.data.feed

import com.pawedcat.app.data.feed.model.PodcastSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class PodcastDirectoryService(
    private val okHttpClient: OkHttpClient = defaultClient()
) {
    companion object {
        private const val SEARCH_URL = "https://itunes.apple.com/search?media=podcast&entity=podcast&limit=30&term="

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(true)
                .build()
        }
    }

    suspend fun searchPodcasts(query: String): List<PodcastSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val encodedQuery = try {
            URLEncoder.encode(query.trim(), "UTF-8")
        } catch (_: Exception) {
            query.trim()
        }

        val request = Request.Builder()
            .url(SEARCH_URL + encodedQuery)
            .header("User-Agent", "PawedCat/1.0 (Android)")
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val bodyString = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(bodyString)
            val resultsArray = json.optJSONArray("results") ?: return@withContext emptyList()

            val results = mutableListOf<PodcastSearchResult>()
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val feedUrl = item.optString("feedUrl", "")
                val title = item.optString("collectionName", item.optString("trackName", ""))
                val author = item.optString("artistName", "")
                val website = item.optString("collectionViewUrl", item.optString("trackViewUrl", ""))

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
        } catch (e: Exception) {
            emptyList()
        }
    }
}
