package com.example.videodownloader.domain.model

data class VideoInfo(
    val title: String,
    val thumbnailUrl: String,
    val qualities: List<Quality>
)
