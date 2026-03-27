package com.example.videodownloader.domain.model

data class UserPreferences(
    val downloadLocation: String = "",
    val defaultQuality: String = "720p",
    val darkTheme: Boolean = false
)
