package com.example.videodownloader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val quality: String,
    val progress: Int,
    val status: String,
    val filePath: String?
) {
    fun toDomainModel() = DownloadTask(
        id = id,
        url = url,
        title = title,
        quality = quality,
        progress = progress,
        status = DownloadStatus.valueOf(status),
        filePath = filePath
    )
}

fun DownloadTask.toEntity() = DownloadEntity(
    id = id,
    url = url,
    title = title,
    quality = quality,
    progress = progress,
    status = status.name,
    filePath = filePath
)
