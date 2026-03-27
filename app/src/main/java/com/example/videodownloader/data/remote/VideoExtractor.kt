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
                throw Exception("Could not access the website. Please check your internet connection and the URL.")
            }

            val html = response.body?.string() ?: throw Exception("The website returned an empty response.")

            // Try to extract video info from HTML meta tags
            val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
            val ogImageRegex = "<meta property=\"og:image\" content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val ogVideoRegex = "<meta property=\"og:video:url\" content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val ogVideoSecureRegex = "<meta property=\"og:video:secure_url\" content=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val sourceRegex = "<source[^>]+src=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val videoTagRegex = "<video[^>]+src=\"(.*?)\"".toRegex(RegexOption.IGNORE_CASE)
            val m3u8Regex = "(https?://[^\"]+\\.m3u8[^\"]*)".toRegex(RegexOption.IGNORE_CASE)

            val titleMatch = titleRegex.find(html)
            val title = titleMatch?.groups?.get(1)?.value?.replace("&amp;", "&")?.replace("&#39;", "'")?.replace("&quot;", "\"") ?: "Unknown Video"

            val imageMatch = ogImageRegex.find(html)
            val thumbnailUrl = imageMatch?.groups?.get(1)?.value ?: "https://via.placeholder.com/640x360.png?text=No+Thumbnail"

            // Try to find video URL
            var videoUrl: String? = null

            // Check og:video:secure_url first (highest priority)
            videoUrl = ogVideoSecureRegex.find(html)?.groups?.get(1)?.value

            // Then og:video:url
            if (videoUrl == null) {
                videoUrl = ogVideoRegex.find(html)?.groups?.get(1)?.value
            }

            // Then <source> tags
            if (videoUrl == null) {
                videoUrl = sourceRegex.find(html)?.groups?.get(1)?.value
            }

            // Then <video> src
            if (videoUrl == null) {
                videoUrl = videoTagRegex.find(html)?.groups?.get(1)?.value
            }

            // Make URLs absolute if needed
            if (videoUrl != null && videoUrl.startsWith("//")) {
                videoUrl = "https:$videoUrl"
            } else if (videoUrl != null && videoUrl.startsWith("/")) {
                val uri = java.net.URI(url)
                videoUrl = "${uri.scheme}://${uri.host}$videoUrl"
            }

            // If we found a video URL, return it
            if (videoUrl != null) {
                return@withContext VideoInfo(
                    title = title,
                    thumbnailUrl = thumbnailUrl,
                    qualities = listOf(
                        Quality("Original", "mp4", videoUrl)
                    )
                )
            }

            // If it's a YouTube video, try to extract video ID
            val youtubeIdRegex = "(?:youtube\\.com/(?:watch\\?v=|embed/)|youtu\\.be/)([a-zA-Z0-9_-]{11})".toRegex()
            val youtubeIdMatch = youtubeIdRegex.find(url)
            if (youtubeIdMatch != null) {
                val videoId = youtubeIdMatch.groups[1]?.value
                // Try to get thumbnail from YouTube
                val ytThumbnail = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

                return@withContext VideoInfo(
                    title = "YouTube Video",
                    thumbnailUrl = ytThumbnail,
                    qualities = listOf(
                        Quality("1080p", "mp4", "https://www.youtube.com/watch?v=$videoId"),
                        Quality("720p", "mp4", "https://www.youtube.com/watch?v=$videoId"),
                        Quality("480p", "mp4", "https://www.youtube.com/watch?v=$videoId")
                    )
                )
            }

            // If no video found, throw an exception
            throw Exception("No supported video found on this page. Try opening the page in a browser to see if the video is accessible.")
        }
    }
}
