package com.example.videodownloader.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
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

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val CHANNEL_ID = "download_channel"

    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong("DOWNLOAD_ID", -1)
        if (downloadId == -1L) return Result.failure()

        val task = repository.getDownloadTaskById(downloadId) ?: return Result.failure()

        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading ${task.title}")
            .setContentText("Starting download...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForeground(ForegroundInfo(downloadId.toInt(), notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC))
            } else {
                setForeground(ForegroundInfo(downloadId.toInt(), notificationBuilder.build()))
            }

            repository.updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING, 0, null)

            // Simulate downloading by actually fetching a small file and updating progress
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val url = URL(task.url)
                val connection = url.openConnection()
                connection.connect()
                val fileLength = connection.contentLength

                val fileName = "${sanitizeFileName(task.title)}_${task.quality}.mp4"
                val file = File(context.getExternalFilesDir(null), fileName)
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

                            notificationBuilder.setProgress(100, progress, false)
                                .setContentText("Downloaded $progress%")
                            notificationManager.notify(downloadId.toInt(), notificationBuilder.build())

                            lastProgress = progress
                        }
                    }
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()

                repository.updateDownloadStatus(downloadId, DownloadStatus.COMPLETED, 100, file.absolutePath)

                val successNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Download Complete")
                    .setContentText(task.title)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                notificationManager.notify(downloadId.toInt() + 1000, successNotification.build())
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error downloading file", e)
            repository.updateDownloadStatus(downloadId, DownloadStatus.FAILED, task.progress, null)

            val failNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Download Failed")
                .setContentText(task.title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            notificationManager.notify(downloadId.toInt() + 1000, failNotification.build())

            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Download Channel"
            val descriptionText = "Notifications for video downloads"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(80)
    }
}
