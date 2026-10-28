# Waktu Sholat

Aplikasi waktu sholat Android yang gratis, sepenuhnya offline, tanpa iklan, dan tanpa pelacakan.

- Tanpa izin `INTERNET`, tanpa akun, dan tanpa server.
- Jadwal sholat dihitung di perangkat.
- Notifikasi adzan dan widget jadwal sholat.

## Status

Modul 1–3 — kerangka aplikasi, lokasi & jadwal sholat, serta notifikasi & alarm adzan.

## Teknologi

- Kotlin + Jetpack Compose + Material 3
- Navigation Compose
- Room + KSP
- DataStore Preferences
- `AppWidgetProvider` + `RemoteViews` (modul widget)
- `AlarmManager` (modul alarm)

Persyaratan build: JDK 21 atau lebih baru, Android SDK Platform 37, dan Android Build Tools 36.

## Build

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

APK debug dihasilkan di `app/build/outputs/apk/debug/`.

## Lisensi

GPL-3.0. Lihat [LICENSE](LICENSE).
