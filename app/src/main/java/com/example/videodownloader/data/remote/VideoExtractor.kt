package com.example.videodownloader.data.remote

import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.regex.Pattern

class VideoExtractor {
    private val client = OkHttpClient()

    suspend fun extract(url: String): VideoInfo = withContext(Dispatchers.IO) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return@withContext extractYouTube(url)
        } else if (url.endsWith(".m3u8")) {
            return@withContext extractM3u8(url)
        } else if (url.endsWith(".mp4") || url.endsWith(".webm")) {
            return@withContext extractDirect(url)
        } else {
            return@withContext extractGeneric(url)
        }
    }

    private fun extractYouTube(url: String): VideoInfo {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch YouTube page")
            val html = response.body?.string() ?: throw Exception("Empty response body")

            val pattern = Pattern.compile("ytInitialPlayerResponse\\s*=\\s*(\\{.*?\\});(?:var meta|</script>)")
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val jsonString = matcher.group(1)
                val json = JSONObject(jsonString)

                val videoDetails = json.getJSONObject("videoDetails")
                val title = videoDetails.getString("title") ?: "Unknown Title"

                var thumbnailUrl = ""
                val thumbnails = videoDetails.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                if (thumbnails != null && thumbnails.length() > 0) {
                    thumbnailUrl = thumbnails.getJSONObject(thumbnails.length() - 1).getString("url")
                }

                val streamingData = json.optJSONObject("streamingData")
                val formatsArray = streamingData?.optJSONArray("formats")
                val adaptiveFormatsArray = streamingData?.optJSONArray("adaptiveFormats")

                val qualities = mutableListOf<Quality>()

                if (formatsArray != null) {
                    for (i in 0 until formatsArray.length()) {
                        val format = formatsArray.getJSONObject(i)
                        val formatUrl = format.optString("url", "")
                        if (formatUrl.isNotEmpty()) {
                            val qualityLabel = format.optString("qualityLabel", format.optString("quality", "unknown"))
                            val mimeType = format.optString("mimeType", "")
                            val ext = if (mimeType.contains("mp4")) "mp4" else if (mimeType.contains("webm")) "webm" else "unknown"
                            qualities.add(Quality(qualityLabel, ext, formatUrl))
                        }
                    }
                }

                if (adaptiveFormatsArray != null) {
                    for (i in 0 until adaptiveFormatsArray.length()) {
                        val format = adaptiveFormatsArray.getJSONObject(i)
                        val formatUrl = format.optString("url", "")
                        val mimeType = format.optString("mimeType", "")
                        if (formatUrl.isNotEmpty() && mimeType.contains("video")) {
                            val qualityLabel = format.optString("qualityLabel", format.optString("quality", "unknown"))
                            val ext = if (mimeType.contains("mp4")) "mp4" else if (mimeType.contains("webm")) "webm" else "unknown"
                            // Avoid duplicates
                            if (qualities.none { it.resolution == qualityLabel && it.format == ext }) {
                                qualities.add(Quality(qualityLabel, ext, formatUrl))
                            }
                        }
                    }
                }

                if (qualities.isEmpty()) {
                    throw Exception("No streamable formats found for this YouTube video")
                }

                return VideoInfo(title, thumbnailUrl, qualities)
            } else {
                throw Exception("Could not find ytInitialPlayerResponse in YouTube HTML")
            }
        }
    }

    private fun extractM3u8(url: String): VideoInfo {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch M3U8 playlist")
            val content = response.body?.string() ?: throw Exception("Empty response body")

            val lines = content.split("\n")
            val qualities = mutableListOf<Quality>()
            var currentResolution = "Auto"

            val baseUrl = if (url.contains("/")) url.substringBeforeLast("/") + "/" else ""

            for (i in lines.indices) {
                val line = lines[i].trim()
                if (line.startsWith("#EXT-X-STREAM-INF:")) {
                    // Extract resolution if available
                    val resMatcher = Pattern.compile("RESOLUTION=(\\d+x\\d+)").matcher(line)
                    if (resMatcher.find()) {
                        currentResolution = resMatcher.group(1) ?: "Auto"
                    } else {
                        val bwMatcher = Pattern.compile("BANDWIDTH=(\\d+)").matcher(line)
                        if (bwMatcher.find()) {
                            currentResolution = "${bwMatcher.group(1)}bps"
                        }
                    }
                } else if (!line.startsWith("#") && line.isNotEmpty()) {
                    val streamUrl = if (line.startsWith("http")) line else baseUrl + line
                    qualities.add(Quality(currentResolution, "m3u8", streamUrl))
                    currentResolution = "Auto" // Reset for the next stream
                }
            }

            if (qualities.isEmpty()) {
                qualities.add(Quality("Auto", "m3u8", url))
            }

            val title = url.substringAfterLast("/").substringBeforeLast(".")
            return VideoInfo(
                title = if (title.isNotEmpty()) title else "M3U8 Stream",
                thumbnailUrl = "",
                qualities = qualities
            )
        }
    }

    private fun extractDirect(url: String): VideoInfo {
        val ext = url.substringAfterLast(".")
        val title = url.substringAfterLast("/").substringBeforeLast(".")
        return VideoInfo(
            title = if (title.isNotEmpty()) title else "Direct Video",
            thumbnailUrl = "",
            qualities = listOf(Quality("Original", ext, url))
        )
    }

    private fun extractGeneric(url: String): VideoInfo {
        // A generic extractor using jsoup (via okhttp + regex since jsoup isn't imported)
        // to find <title> and OpenGraph tags like og:title, og:video, og:image
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch webpage")
            val html = response.body?.string() ?: throw Exception("Empty response body")

            var title = "Unknown Title"
            val titlePattern = Pattern.compile("<title>(.*?)</title>")
            val titleMatcher = titlePattern.matcher(html)
            if (titleMatcher.find()) {
                title = titleMatcher.group(1) ?: "Unknown Title"
            }

            var videoUrl = url
            val ogVideoPattern = Pattern.compile("<meta\\s+property=\"og:video\"\\s+content=\"(.*?)\"")
            val ogVideoMatcher = ogVideoPattern.matcher(html)
            if (ogVideoMatcher.find()) {
                videoUrl = ogVideoMatcher.group(1) ?: url
            } else {
                // look for video src
                val videoSrcPattern = Pattern.compile("<video[^>]+src=\"(.*?)\"")
                val videoSrcMatcher = videoSrcPattern.matcher(html)
                if (videoSrcMatcher.find()) {
                    videoUrl = videoSrcMatcher.group(1) ?: url
                    if (videoUrl.startsWith("/")) {
                        val base = url.substring(0, url.indexOf("/", url.indexOf("://") + 3).takeIf { it != -1 } ?: url.length)
                        videoUrl = base + videoUrl
                    }
                }
            }

            var thumbUrl = ""
            val ogImagePattern = Pattern.compile("<meta\\s+property=\"og:image\"\\s+content=\"(.*?)\"")
            val ogImageMatcher = ogImagePattern.matcher(html)
            if (ogImageMatcher.find()) {
                thumbUrl = ogImageMatcher.group(1) ?: ""
            }

            return VideoInfo(
                title = title,
                thumbnailUrl = thumbUrl,
                qualities = listOf(Quality("Original", "mp4", videoUrl))
            )
        }
    }
}
