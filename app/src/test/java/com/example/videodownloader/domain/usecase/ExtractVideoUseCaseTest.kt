package com.example.videodownloader.domain.usecase

import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo
import com.example.videodownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractVideoUseCaseTest {

    class FakeDownloadRepository : DownloadRepository {
        var dummyVideoInfo: VideoInfo? = null
        var shouldThrowException: Exception? = null

        override suspend fun extractVideoInfo(url: String): VideoInfo {
            shouldThrowException?.let { throw it }
            return dummyVideoInfo ?: throw NotImplementedError("Dummy info not set")
        }

        override suspend fun startDownload(task: DownloadTask): Long {
            throw NotImplementedError()
        }

        override fun getDownloads(): Flow<List<DownloadTask>> {
            return emptyFlow()
        }

        override suspend fun updateDownloadStatus(
            id: Long,
            status: DownloadStatus,
            progress: Int,
            filePath: String?
        ) {
            throw NotImplementedError()
        }

        override suspend fun getDownloadTaskById(id: Long): DownloadTask? {
            throw NotImplementedError()
        }
    }

    @Test
    fun `invoke returns video info from repository on success`() = runBlocking {
        // Arrange
        val fakeRepository = FakeDownloadRepository()
        val mockVideoInfo = VideoInfo(
            title = "Test Video",
            thumbnailUrl = "http://example.com/thumb.jpg",
            qualities = listOf(
                Quality(resolution = "720p", format = "mp4", downloadUrl = "http://example.com/video_720.mp4"),
                Quality(resolution = "360p", format = "mp4", downloadUrl = "http://example.com/video_360.mp4")
            )
        )
        fakeRepository.dummyVideoInfo = mockVideoInfo
        val extractVideoUseCase = ExtractVideoUseCase(fakeRepository)
        val url = "http://example.com/video"

        // Act
        val result = extractVideoUseCase(url)

        // Assert
        assertEquals(mockVideoInfo, result)
        assertEquals("Test Video", result.title)
    }

    @Test(expected = Exception::class)
    fun `invoke throws exception when repository throws exception`(): Unit = runBlocking {
        // Arrange
        val fakeRepository = FakeDownloadRepository()
        fakeRepository.shouldThrowException = Exception("Network error")
        val extractVideoUseCase = ExtractVideoUseCase(fakeRepository)
        val url = "http://example.com/video"

        // Act
        extractVideoUseCase(url) // Should throw Exception
    }
}
