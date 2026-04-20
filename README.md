# Android Video Downloader 📥

Android-App zum Extrahieren und Herunterladen von Videos. Clean Architecture mit Jetpack Compose.

**Status:** MVP / Work-in-Progress — Video-Extraktion (HTML-Meta-Tags, YouTube-Thumbnails) implementiert, Download-Infrastruktur (Room, WorkManager, Notifications) aufgebaut.

## Architektur

```
presentation/      → Jetpack Compose UI, ViewModels
  ├── MainScreen / MainViewModel    (URL-Eingabe, Extraktion, Download-Liste)
  ├── SettingsScreen / SettingsViewModel
  └── theme/         (Material3 Dark Theme)

domain/            → Use Cases & Models
  ├── model/       (VideoInfo, DownloadTask, Quality, UserPreferences)
  ├── usecase/     (ExtractVideo, GetDownloads, StartDownload)
  └── repository/  (DownloadRepository, PreferencesRepository — Interfaces)

data/             → Implementierungen
  ├── remote/     (VideoExtractor — HTML-Parsing + YouTube-Special-Case)
  ├── local/      (AppDatabase, DownloadDao — Room)
  ├── worker/     (DownloadWorker — WorkManager mit Foreground-Notifications)
  └── repository/ (DownloadRepositoryImpl, PreferencesRepositoryImpl)
```

## Was funktioniert ✅

- URL-Eingabe mit Video-Extraktion aus HTML-Meta-Tags
- YouTube-Spezialfall (Video-ID → Thumbnail in 3 Qualitäten)
- Download-Historie (Room DB)
- Einstellungen (Download-Verzeichnis, Benachrichtigungen)
- Background-Download-Infrastruktur (WorkManager + Foreground Service)

## Was noch fehlt ⏳

- Echte Multi-Site Support (derzeit nur HTML-Meta-Tags + YouTube)
- yt-dlp Integration für YouTube/TikTok/Instagram/Twitter
- Echte Qualitätsauswahl vor Download
- Download-Fortschritt in der UI
- File-Provider für APK Build

## Tech-Stack

- **Language:** Kotlin 1.9+
- **UI:** Jetpack Compose + Material3
- **Architecture:** Clean Architecture (MVVM)
- **DI:** Hilt (Dagger)
- **Database:** Room
- **Networking:** OkHttpClient (HTML-Parsing)
- **Background Tasks:** WorkManager mit Foreground Notifications
- **Min SDK:** 24 (Android 7.0) / **Target SDK:** 34 (Android 14)

## Build

```bash
./gradlew assembleDebug
```

Build status: [![Build](https://github.com/hendr15k/android-video-downloader/actions/workflows/build.yml/badge.svg)](https://github.com/hendr15k/android-video-downloader/actions/workflows/build.yml)

## Lizenz

MIT
