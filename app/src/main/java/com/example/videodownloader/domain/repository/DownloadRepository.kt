package com.example.videodownloader.domain.repository

import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.VideoInfo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun extractVideoInfo(url: String): VideoInfo
    suspend fun startDownload(task: DownloadTask): Long
    fun getDownloads(): Flow<List<DownloadTask>>
    suspend fun updateDownloadStatus(id: Long, status: com.example.videodownloader.domain.model.DownloadStatus, progress: Int, filePath: String?)
    suspend fun getDownloadTaskById(id: Long): DownloadTask?
}
