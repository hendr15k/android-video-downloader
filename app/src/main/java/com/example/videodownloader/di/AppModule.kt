package com.example.videodownloader.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.videodownloader.data.local.AppDatabase
import com.example.videodownloader.data.local.DownloadDao
import com.example.videodownloader.data.remote.VideoExtractor
import com.example.videodownloader.data.repository.DownloadRepositoryImpl
import com.example.videodownloader.domain.repository.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "video_downloader_db"
        ).build()
    }

    @Provides
    fun provideDownloadDao(db: AppDatabase): DownloadDao {
        return db.downloadDao()
    }

    @Provides
    @Singleton
    fun provideVideoExtractor(): VideoExtractor {
        return VideoExtractor()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
        videoExtractor: VideoExtractor,
        workManager: WorkManager
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao, videoExtractor, workManager)
    }
}
