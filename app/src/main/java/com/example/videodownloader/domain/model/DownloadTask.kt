package com.example.videodownloader.domain.model

data class DownloadTask(
    val id: Long = 0,
    val url: String,
    val title: String,
    val quality: String,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val filePath: String? = null
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED
}
