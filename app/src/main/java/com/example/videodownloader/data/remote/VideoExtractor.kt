package com.example.videodownloader.data.remote

import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class VideoExtractor {
    private val client = OkHttpClient()

    suspend fun extract(url: String): VideoInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch URL: ${response.code}")
            }

            val html = response.body?.string() ?: throw Exception("Empty response body")

            val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
            val ogImageRegex = "<meta property=\"og:image\" content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val ogVideoRegex = "<meta property=\"og:video:url\" content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val sourceRegex = "<source src=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)

            val titleMatch = titleRegex.find(html)
            val title = titleMatch?.groups?.get(1)?.value ?: "Unknown Title"

            val imageMatch = ogImageRegex.find(html)
            val thumbnailUrl = imageMatch?.groups?.get(1)?.value ?: "https://via.placeholder.com/640x360.png?text=No+Thumbnail"

            var videoUrl = ogVideoRegex.find(html)?.groups?.get(1)?.value
            if (videoUrl == null) {
                videoUrl = sourceRegex.find(html)?.groups?.get(1)?.value
            }

            if (videoUrl == null) {
                throw Exception("Could not extract video URL from $url")
            }

            // To ensure the URL is absolute
            if (videoUrl.startsWith("/")) {
                val uri = java.net.URI(url)
                videoUrl = "${uri.scheme}://${uri.host}$videoUrl"
            }

            VideoInfo(
                title = title.replace("&amp;", "&").replace("&#39;", "'").replace("&quot;", "\""),
                thumbnailUrl = thumbnailUrl,
                qualities = listOf(
                    Quality("Original", "mp4", videoUrl)
                )
            )
        }
    }
}
