package com.example.videodownloader.data.remote

import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import kotlinx.coroutines.delay

class VideoExtractor {
    suspend fun extract(url: String): VideoInfo {
        // Simulate network delay
        delay(1500)

        // Mocking an extraction since real extraction requires complex libraries or scraping
        return VideoInfo(
            title = "Mock Video Title for $url",
            thumbnailUrl = "https://via.placeholder.com/640x360.png?text=Video+Thumbnail",
            qualities = listOf(
                Quality("1080p", "mp4", "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4"),
                Quality("720p", "mp4", "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4"),
                Quality("480p", "mp4", "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/480/Big_Buck_Bunny_480_10s_1MB.mp4")
            )
        )
    }
}
