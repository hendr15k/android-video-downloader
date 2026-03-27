package com.example.videodownloader.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.repository.DownloadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: DownloadRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong("DOWNLOAD_ID", -1)
        if (downloadId == -1L) return Result.failure()

        val task = repository.getDownloadTaskById(downloadId) ?: return Result.failure()

        try {
            repository.updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING, 0, null)

            // Simulate downloading by actually fetching a small file and updating progress
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val url = URL(task.url)
                val connection = url.openConnection()
                connection.connect()
                val fileLength = connection.contentLength

                val file = File(context.getExternalFilesDir(null), "${task.title}_${task.quality}.mp4")
                val input = connection.getInputStream()
                val output = FileOutputStream(file)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                var lastProgress = 0

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        if (progress > lastProgress) {
                            repository.updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING, progress, null)
                            lastProgress = progress
                        }
                    }
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()

                repository.updateDownloadStatus(downloadId, DownloadStatus.COMPLETED, 100, file.absolutePath)
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error downloading file", e)
            repository.updateDownloadStatus(downloadId, DownloadStatus.FAILED, task.progress, null)
            return Result.failure()
        }
    }
}
