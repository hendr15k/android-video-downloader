package com.hendrik.videodownloader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VideoDownloaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
