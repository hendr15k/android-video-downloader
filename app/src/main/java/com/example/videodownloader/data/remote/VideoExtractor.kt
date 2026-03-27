package com.example.videodownloader.data.remote

import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoExtractor {
    suspend fun extract(url: String): VideoInfo = withContext(Dispatchers.IO) {
        val streamInfo = YoutubeDL.getInstance().getInfo(url)

        val title = streamInfo.title ?: "Unknown Title"
        val thumbnail = streamInfo.thumbnail ?: ""

        val formats = streamInfo.formats?.mapNotNull { format ->
            val formatUrl = format.url ?: return@mapNotNull null

            val resolution = if (format.height > 0) {
                "${format.height}p"
            } else if (!format.formatNote.isNullOrEmpty()) {
                format.formatNote!!
            } else {
                "Unknown"
            }

            val extension = format.ext ?: "mp4"

            Quality(
                resolution = resolution,
                format = extension,
                downloadUrl = formatUrl
            )
        } ?: emptyList()

        VideoInfo(
            title = title,
            thumbnailUrl = thumbnail,
            qualities = formats
        )
    }
}
