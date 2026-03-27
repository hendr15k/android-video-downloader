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
import com.example.videodownloader.data.repository.PreferencesRepositoryImpl
import com.example.videodownloader.domain.repository.PreferencesRepository
import com.example.videodownloader.data.repository.dataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(dataStore: DataStore<Preferences>, @ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepositoryImpl(dataStore, context)
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
