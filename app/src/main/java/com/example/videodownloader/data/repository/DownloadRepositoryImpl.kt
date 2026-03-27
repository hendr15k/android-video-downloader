package com.example.videodownloader.data.repository

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.videodownloader.data.local.DownloadDao
import com.example.videodownloader.data.local.toEntity
import com.example.videodownloader.data.remote.VideoExtractor
import com.example.videodownloader.data.worker.DownloadWorker
import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao,
    private val videoExtractor: VideoExtractor,
    private val workManager: WorkManager
) : DownloadRepository {

    override suspend fun extractVideoInfo(url: String): VideoInfo {
        return videoExtractor.extract(url)
    }

    override suspend fun startDownload(task: DownloadTask): Long {
        val id = downloadDao.insertDownload(task.toEntity())

        val data = Data.Builder()
            .putLong("DOWNLOAD_ID", id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
        return id
    }

    override fun getDownloads(): Flow<List<DownloadTask>> {
        return downloadDao.getAllDownloads().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun updateDownloadStatus(
        id: Long,
        status: DownloadStatus,
        progress: Int,
        filePath: String?
    ) {
        downloadDao.updateStatusAndProgress(id, status.name, progress, filePath)
    }

    override suspend fun getDownloadTaskById(id: Long): DownloadTask? {
        return downloadDao.getDownloadById(id)?.toDomainModel()
    }
}
