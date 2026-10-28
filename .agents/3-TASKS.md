# Task List: Aplikasi Waktu Sholat (Gratis, Offline, Widget Android)

Berdasarkan `1-PRD.md` dan `2-TECH-SPEC.md`.

## Ringkasan

| Modul | Task | Estimasi |
|-------|------|----------|
| 1. Setup Project | T-01 sampai T-06 (6) | ± 7 jam |
| 2. Lokasi & Jadwal Sholat | T-07 sampai T-18 (12) | ± 39,5 jam |
| 3. Notifikasi & Alarm Adzan | T-19 sampai T-31 (13) | ± 35,5 jam |
| 4. Widget Android | T-32 sampai T-39 (8) | ± 20,5 jam |
| 5. Pengaturan, Onboarding & Kustomisasi | T-40 sampai T-49 (10) | ± 31 jam |
| 6. Pengujian, Keamanan & Rilis | T-50 sampai T-65 (16) | ± 50,5 jam |
| **Total** | **65 task** | **± 184 jam** |

Estimasi kasar. Masa pengamatan alarm (T-54) dan antrean peninjauan F-Droid (T-65) adalah waktu kalender dan tidak termasuk jam kerja di atas.

**Urutan pengerjaan:** ikuti urutan modul 1 sampai 6. Yang harus tuntas lebih dulu adalah T-08 dan T-09 (mesin perhitungan beserta uji akurasinya), karena alarm, widget, dan seluruh layar bergantung pada hasilnya.

---

## MODUL 1: Setup Project

### T-01: Buat proyek Android baru
- **Deskripsi:** Buat proyek Kotlin dengan Jetpack Compose, `minSdk 26`, dan satu `MainActivity`. Siapkan struktur paket `data/`, `domain/`, `alarm/`, `widget/`, `ui/` sesuai Tech Spec Bagian 1.
- **Modul:** Setup Project
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** -
- **Tanggal:** 2026-10-27
- **Estimasi:** 1 jam
- **File yang diubah:** `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/java/id/.../waktusholat/MainActivity.kt`

### T-02: Konfigurasi version catalog dan dependensi
- **Deskripsi:** Kunci versi di `libs.versions.toml`: Compose BOM, Material 3, Navigation Compose, Lifecycle/ViewModel, Room + KSP, DataStore, JUnit. Pastikan tidak ada Google Play Services, Firebase, atau SDK analitik/iklan (syarat F-Droid).
- **Modul:** Setup Project
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-01
- **Tanggal:** 2026-10-27
- **Estimasi:** 1 jam
- **File yang diubah:** `gradle/libs.versions.toml`, `app/build.gradle.kts`

### T-03: Konfigurasi AndroidManifest dasar
- **Deskripsi:** Atur `allowBackup="false"` dan `dataExtractionRules` yang mengecualikan semua data. Pastikan tidak ada izin `INTERNET`. Tetapkan `exported="false"` sebagai default untuk komponen internal.
- **Modul:** Setup Project
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-01
- **Tanggal:** 2026-10-27
- **Estimasi:** 0,5 jam
- **File yang diubah:** `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`

### T-04: Inisialisasi repositori Git dan lisensi
- **Deskripsi:** Buat repositori GitHub, `.gitignore` Android (termasuk keystore), `README.md` singkat, dan pilih lisensi (GPL-3.0 atau Apache-2.0/MIT). Keputusan lisensi harus selesai di sini karena F-Droid mensyaratkannya.
- **Modul:** Setup Project
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-01
- **Tanggal:** 2026-10-27
- **Estimasi:** 1 jam
- **File yang diubah:** `.gitignore`, `README.md`, `LICENSE`

### T-05: Setup CI GitHub Actions dasar
- **Deskripsi:** Workflow yang berjalan pada push dan pull request: build `debug`, jalankan unit test, jalankan Android Lint. (Workflow rilis ditangani di Modul 6.)
- **Modul:** Setup Project
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-02, T-04
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `.github/workflows/ci.yml`

### T-06: Tema Compose dan kerangka navigasi
- **Deskripsi:** Buat tema Material 3 (terang, gelap, ikuti sistem) dan `NavHost` dengan bottom bar tiga tujuan (Harian, Bulanan, Pengaturan). Isi layar masih placeholder. Rute lain sesuai Tech Spec Bagian 3 ditambahkan di modul masing-masing.
- **Modul:** Setup Project
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-02
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `ui/navigation/AppNavHost.kt`, `MainActivity.kt`

**Subtotal Modul 1:** 6 task, sekitar 7 jam.

---

## MODUL 2: Lokasi & Jadwal Sholat

### T-07: Definisikan model domain
- **Deskripsi:** Buat `Prayer` (enum: Imsak, Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya), `PrayerTimes`, `City`, `CalculationMethod`, dan data class `Settings` dengan nilai default sesuai Tech Spec Bagian 2. Kode ini murni Kotlin, tanpa dependensi Android.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-01
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `domain/model/Prayer.kt`, `domain/model/PrayerTimes.kt`, `domain/model/City.kt`, `domain/CalculationMethod.kt`, `domain/model/Settings.kt`

### T-08: Implementasi `PrayerTimeEngine`
- **Deskripsi:** Hitung 7 waktu per tanggal dan koordinat: pakai library Adhan (pastikan lisensinya kompatibel dengan T-04) atau implementasi sendiri. Terapkan metode Kemenag RI sebagai default, mazhab Ashar, ihtiyath, koreksi menit per waktu (-10 sampai +10, BR-03), dan konversi ke zona waktu lokasi (BR-07). Parameter Kemenag diverifikasi ke publikasi resmi (BR-01).
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-07
- **Tanggal:** 2026-10-27
- **Estimasi:** 6 jam
- **File yang diubah:** `domain/PrayerTimeEngine.kt`, `app/build.gradle.kts` (jika memakai library)

### T-09: Unit test akurasi `PrayerTimeEngine`
- **Deskripsi:** Bandingkan keluaran engine dengan jadwal resmi Kemenag untuk beberapa kota (barat, tengah, timur Indonesia) pada beberapa bulan berbeda. Target selisih ≤ 1-2 menit. Tambahkan juga kasus ihtiyath, koreksi menit, dan pergantian hari.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-08
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `app/src/test/.../PrayerTimeEngineTest.kt`, `app/src/test/resources/kemenag_reference.csv`

### T-10: Siapkan data kota Indonesia (`cities.db`)
- **Deskripsi:** Kumpulkan data kabupaten/kota (sekitar 500 baris) dari sumber publik dengan lisensi jelas: nama, `name_search` (huruf kecil tanpa awalan Kab./Kota), provinsi, lintang, bujur, dan `timezone_id` (Asia/Jakarta, Makassar, Jayapura). Catat sumber dan lisensinya di repositori.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-04
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `app/src/main/assets/cities.db`, `docs/DATA_SOURCES.md`

### T-11: Setup Room untuk tabel `City`
- **Deskripsi:** Buat `CityEntity`, `CityDao` (pencarian awalan lewat `name_search`, ambil semua, cari terdekat), dan `AppDatabase` dengan `createFromAsset("cities.db")`. Tambahkan index pada `name_search` dan aktifkan schema export.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-02, T-10
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `data/db/CityEntity.kt`, `data/db/CityDao.kt`, `data/db/AppDatabase.kt`, `app/build.gradle.kts`

### T-12: `SettingsRepository` dengan DataStore (bagian dasar)
- **Deskripsi:** Implementasikan key lokasi (`location_lat`, `location_lng`, `location_name`, `location_timezone`, `location_source`) dan key perhitungan (`calc_method`, `asr_madhab`, `offset_<prayer>`, `ihtiyath_enabled`) dengan default dari Tech Spec. Ekspos `settings: Flow<Settings>` dan fungsi `update { }`. Key notifikasi, suara, tema, dan ekspor/impor menyusul di Modul 3 dan 5.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-02, T-07
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `data/prefs/SettingsRepository.kt`

### T-13: `LocationRepository` (GPS sekali + cari kota)
- **Deskripsi:** `detectOnce()` memakai `LocationManager` bawaan dengan izin `ACCESS_COARSE_LOCATION`, timeout 15 detik, lalu mencocokkan koordinat ke kota terdekat di Room untuk mendapat nama dan `timezone_id`. Tangani GPS mati, izin ditolak, dan timeout lewat `Result`. `searchCity(query)` memakai `CityDao`. Tidak boleh memakai Google Play Services.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-11, T-12
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `data/repository/LocationRepository.kt`, `AndroidManifest.xml`

### T-14: `PrayerRepository`
- **Deskripsi:** Sediakan `getDay(date)`, `getMonth(year, month)`, dan `getNext(now)`. Baca pengaturan dari `SettingsRepository`, panggil `PrayerTimeEngine`, tanpa menyimpan hasil. `getNext()` mengembalikan Subuh esok hari jika semua waktu hari ini sudah lewat. Ini satu-satunya sumber jadwal untuk UI, alarm, dan widget.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-08, T-12
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `data/repository/PrayerRepository.kt`

### T-15: Helper tanggal Hijriah
- **Deskripsi:** Hitung tanggal Hijriah offline memakai kalender Hijriah bawaan `java.time`. Format teks (misalnya "12 Rabiul Akhir 1448 H") dalam bahasa Indonesia. Catat di layar Tentang bahwa hasilnya bisa berbeda satu hari dari ketetapan Kemenag (BR-08).
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** Low
- **Status:** Done
- **Dependensi:** T-07
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `domain/HijriDateFormatter.kt`

### T-16: Layar Jadwal Harian (`home`)
- **Deskripsi:** `HomeViewModel` + layar Compose: daftar 7 waktu, penanda waktu berjalan dan berikutnya, hitung mundur real-time, tanggal Masehi + Hijriah, dan nama lokasi. Tampilkan banner jika lokasi belum diatur. Banner izin alarm dan notifikasi ditambahkan di Modul 3.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-06, T-14, T-15
- **Tanggal:** 2026-10-27
- **Estimasi:** 5 jam
- **File yang diubah:** `ui/home/HomeScreen.kt`, `ui/home/HomeViewModel.kt`

### T-17: Layar Jadwal Bulanan (`monthly`)
- **Deskripsi:** Tabel jadwal satu bulan yang bisa digulir, dengan tombol bulan sebelumnya dan berikutnya. Hari ini disorot. Perhitungan lewat `getMonth()`, dan harus selesai < 200 ms.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-06, T-14
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `ui/monthly/MonthlyScreen.kt`, `ui/monthly/MonthlyViewModel.kt`

### T-18: Layar Cari Kota (`city_search`)
- **Deskripsi:** Kolom pencarian dengan hasil dari `searchCity()` (nama, provinsi), plus tombol "Gunakan lokasi saya" yang memanggil `detectOnce()`. Memilih kota menyimpan lokasi ke DataStore. Pemicu penjadwalan ulang alarm dan widget ditambahkan di Modul 3 dan 4.
- **Modul:** Lokasi & Jadwal Sholat
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-06, T-13
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `ui/city/CitySearchScreen.kt`, `ui/city/CitySearchViewModel.kt`

**Subtotal Modul 2:** 12 task, sekitar 39,5 jam.

---

## MODUL 3: Notifikasi & Alarm Adzan

### T-19: Perluas `SettingsRepository` untuk notifikasi dan suara
- **Deskripsi:** Tambahkan key `notify_<prayer>` (default aktif, kecuali Imsak dan Terbit), `pre_reminder_minutes` (default 0, pilihan 5/10/15), dan `sound_<prayer>` (adzan penuh, adzan singkat, nada biasa, senyap; default adzan penuh) beserta field-nya di `Settings`.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-12
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `data/prefs/SettingsRepository.kt`, `domain/model/Settings.kt`

### T-20: Siapkan berkas suara adzan
- **Deskripsi:** Sediakan dua rekaman, `adzan_full` dan `adzan_short`, dengan lisensi yang jelas dan boleh didistribusikan gratis (syarat F-Droid). Pilih format yang kecil agar ukuran APK tetap < 15 MB, lalu catat sumber dan lisensinya.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-04
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `app/src/main/res/raw/adzan_full.*`, `app/src/main/res/raw/adzan_short.*`, `docs/DATA_SOURCES.md`

### T-21: `NotificationHelper` dan kanal notifikasi
- **Deskripsi:** Buat empat kanal: `adzan_full`, `adzan_short`, `reminder`, `silent`, dengan suara sesuai Tech Spec Bagian 3. Implementasikan `ensureChannels()` dan `show(prayer, sound)` yang memilih kanal sesuai pilihan suara. Tap notifikasi membuka `MainActivity` (PendingIntent `FLAG_IMMUTABLE`, intent eksplisit).
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-03, T-20
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `alarm/NotificationHelper.kt`, `res/values/strings.xml`

### T-22: Deklarasi izin dan receiver di manifest
- **Deskripsi:** Tambahkan izin `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `VIBRATE`, dan `ACCESS_COARSE_LOCATION` (jika belum ada dari T-13). Daftarkan semua receiver dengan `exported="false"`, kecuali yang menerima broadcast sistem, beserta intent-filter-nya. Pastikan tetap tidak ada izin `INTERNET`.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-03
- **Tanggal:** 2026-10-27
- **Estimasi:** 1 jam
- **File yang diubah:** `app/src/main/AndroidManifest.xml`

### T-23: `AlarmScheduler`
- **Deskripsi:** Implementasikan `scheduleAll()`, `cancelAll()`, dan `scheduleNext()` sesuai alur C di Tech Spec Bagian 4: batalkan alarm lama, pasang alarm untuk setiap waktu aktif yang belum lewat (hari ini dan, bila perlu, besok), alarm pengingat `waktu - N menit`, dan alarm harian 00:05. Pakai `setExactAndAllowWhileIdle` dengan `requestCode` tetap. Jika izin exact alarm belum diberikan, turun ke `setAndAllowWhileIdle`.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-14, T-19, T-22
- **Tanggal:** 2026-10-27
- **Estimasi:** 6 jam
- **File yang diubah:** `alarm/AlarmScheduler.kt`

### T-24: `AlarmReceiver`
- **Deskripsi:** Terima broadcast alarm (jenis sholat dan penanda adzan/pengingat), tampilkan notifikasi lewat `NotificationHelper` sesuai preferensi suara, lalu panggil `scheduleNext()`. Proses harus ringan, tanpa layanan latar belakang. Pemanggilan `WidgetUpdater` ditambahkan di Modul 4.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-21, T-23
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `alarm/AlarmReceiver.kt`

### T-25: `MidnightReceiver`
- **Deskripsi:** Terima alarm harian 00:05 waktu lokasi, lalu hitung jadwal hari baru dan panggil `scheduleAll()`. Pemanggilan `WidgetUpdater` ditambahkan di Modul 4.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-23
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `alarm/MidnightReceiver.kt`

### T-26: Receiver pemulihan (boot, waktu, izin)
- **Deskripsi:** `BootReceiver` (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`), `TimeChangeReceiver` (`TIMEZONE_CHANGED`, `TIME_SET`), dan `ExactAlarmPermissionReceiver` (`ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`). Semuanya menghitung ulang jadwal dan memanggil `scheduleAll()` sesuai tabel E di Tech Spec Bagian 4. Setelah restart hanya sisa waktu hari ini yang dijadwalkan.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-23
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `alarm/BootReceiver.kt`, `alarm/TimeChangeReceiver.kt`, `alarm/ExactAlarmPermissionReceiver.kt`

### T-27: Penjadwalan ulang saat pengaturan berubah dan saat aplikasi dibuka
- **Deskripsi:** Amati `settings` Flow, dan setiap perubahan lokasi, metode, koreksi, toggle notifikasi, atau suara memicu `scheduleAll()`. Saat aplikasi dibuka, jalankan sinkronisasi ringan sebagai jaring pengaman: pastikan alarm hari ini ada, jika tidak jadwalkan ulang. Ini juga melengkapi alur setelah memilih kota di T-18.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-23, T-18
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `MainActivity.kt`, `alarm/AlarmSyncCoordinator.kt`

### T-28: Banner peringatan izin di layar utama
- **Deskripsi:** Tampilkan banner permanen di `home` jika izin exact alarm belum diberikan (alarm kurang presisi) atau izin notifikasi ditolak (tanpa adzan). Tombolnya membuka layar izin sistem yang sesuai. Banner hilang otomatis begitu izin diberikan.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-16, T-22
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `ui/home/HomeScreen.kt`, `ui/home/PermissionBanner.kt`

### T-29: Layar Pengaturan Notifikasi & Suara
- **Deskripsi:** Layar `settings/notifications`: toggle per waktu sholat (FR-07), pilihan pengingat sebelum adzan 5/10/15 menit atau mati (FR-08), dan pilihan suara per waktu sholat (FR-10) dengan tombol pratinjau suara. Setiap perubahan tersimpan ke DataStore dan memicu penjadwalan ulang lewat T-27.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** High
- **Status:** Done
- **Dependensi:** T-19, T-21, T-27
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `ui/settings/NotificationSettingsScreen.kt`, `ui/settings/NotificationSettingsViewModel.kt`, `ui/navigation/AppNavHost.kt`

### T-30: Panduan baterai per merek HP
- **Deskripsi:** Layar `settings/battery_guide` berisi langkah-langkah per merek yang agresif menutup aplikasi (misalnya Xiaomi, Oppo, Vivo, Samsung, Realme), plus tombol untuk meminta pengecualian optimasi baterai. Layar bisa dibuka ulang kapan saja dari pengaturan dan dari onboarding (FR-09).
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-06, T-22
- **Tanggal:** 2026-10-27
- **Estimasi:** 3,5 jam
- **File yang diubah:** `ui/settings/BatteryGuideScreen.kt`, `res/values/strings.xml`

### T-31: Unit test logika penjadwalan
- **Deskripsi:** Pisahkan logika penentuan alarm (alarm mana yang dipasang, `requestCode`, alarm pengingat, kasus setelah Isya, perubahan zona waktu) dari pemanggilan `AlarmManager` agar bisa diuji. Test mencakup toggle nonaktif, pengingat aktif, dan tidak ada alarm duplikat.
- **Modul:** Notifikasi & Alarm Adzan
- **Prioritas:** Mid
- **Status:** Done
- **Dependensi:** T-23
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `app/src/test/.../AlarmPlannerTest.kt`, `alarm/AlarmPlanner.kt`

**Subtotal Modul 3:** 13 task, sekitar 35,5 jam.

---

## MODUL 4: Widget Android

### T-32: `WidgetStateMapper` dan `WidgetUpdater`
- **Deskripsi:** Buat fungsi murni yang mengubah `PrayerTimes` + waktu sekarang menjadi `WidgetState` (nama dan jam sholat berikutnya, daftar semua waktu hari ini, penanda waktu berikutnya, dasar hitung mundur). Setelah Isya, state menunjuk Subuh esok hari. `WidgetUpdater.updateAll()` mengambil data dari `PrayerRepository`, lalu merender kedua widget yang terpasang lewat `AppWidgetManager`.
- **Modul:** Widget Android
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-14
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `widget/WidgetState.kt`, `widget/WidgetStateMapper.kt`, `widget/WidgetUpdater.kt`

### T-33: Layout dan konfigurasi widget kecil
- **Deskripsi:** Buat layout RemoteViews untuk widget kecil (nama sholat berikutnya, jam, `Chronometer` untuk hitung mundur), berkas `appwidget-provider` (ukuran minimum, `resizeMode`, pratinjau), dan gambar pratinjau untuk pemilih widget. Hanya memakai view yang didukung RemoteViews.
- **Modul:** Widget Android
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-01
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `res/layout/widget_next_prayer.xml`, `res/xml/widget_next_prayer_info.xml`, `res/drawable/widget_preview_next.*`

### T-34: `NextPrayerWidget` (widget kecil)
- **Deskripsi:** Provider `AppWidgetProvider` untuk widget kecil. Isi nama dan jam sholat berikutnya, lalu atur `Chronometer` ke mode hitung mundur dengan `base = elapsedRealtime() + (waktuTarget - sekarang)`, sehingga sistem yang menggerakkan angkanya tanpa pembaruan per menit (FR-13). Tap widget membuka `MainActivity` (PendingIntent `FLAG_IMMUTABLE`). Daftarkan di manifest.
- **Modul:** Widget Android
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-32, T-33, T-03
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `widget/NextPrayerWidget.kt`, `AndroidManifest.xml`

### T-35: `DailyScheduleWidget` (widget besar)
- **Deskripsi:** Layout dan provider widget besar yang menampilkan semua waktu hari ini dengan waktu berikutnya disorot, bisa diubah ukurannya. Pastikan teks tetap terbaca pada ukuran minimum dan tidak terpotong. Daftarkan di manifest beserta berkas `appwidget-provider` dan pratinjaunya.
- **Modul:** Widget Android
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-32, T-03
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `widget/DailyScheduleWidget.kt`, `res/layout/widget_daily_schedule.xml`, `res/xml/widget_daily_schedule_info.xml`, `res/drawable/widget_preview_daily.*`, `AndroidManifest.xml`

### T-36: Tema terang dan gelap untuk widget
- **Deskripsi:** Siapkan warna latar dan teks widget untuk tema terang dan gelap (`values` dan `values-night`) sehingga widget mengikuti tema sistem secara default (FR-14). Pilihan tema manual dari pengguna disambungkan di Modul 5 saat key `theme_mode` dan layar Tampilan dibuat. Periksa kontras teks di kedua tema.
- **Modul:** Widget Android
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-34, T-35
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `res/values/colors.xml`, `res/values-night/colors.xml`, `res/drawable/widget_background.xml`, `res/layout/widget_*.xml`

### T-37: Hubungkan pemicu pembaruan widget
- **Deskripsi:** Panggil `WidgetUpdater.updateAll()` dari titik-titik yang sudah dibuat di Modul 3: `AlarmReceiver` (T-24), `MidnightReceiver` (T-25), `BootReceiver` dan `TimeChangeReceiver` (T-26), serta penjadwalan ulang saat pengaturan berubah (T-27). Tidak ada polling berkala. Tambahkan juga `onEnabled` dan `onUpdate` pada provider agar widget yang baru dipasang langsung terisi.
- **Modul:** Widget Android
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-24, T-25, T-26, T-27, T-34, T-35
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `alarm/AlarmReceiver.kt`, `alarm/MidnightReceiver.kt`, `alarm/BootReceiver.kt`, `alarm/TimeChangeReceiver.kt`, `alarm/AlarmSyncCoordinator.kt`, `widget/NextPrayerWidget.kt`, `widget/DailyScheduleWidget.kt`

### T-38: Kondisi kosong dan error pada widget
- **Deskripsi:** Jika lokasi belum diatur, kedua widget menampilkan teks ajakan "Atur lokasi" dan tap-nya membuka aplikasi. Jika perhitungan gagal, tampilkan pesan singkat tanpa crash. Pastikan hitung mundur tidak menampilkan angka negatif yang membingungkan ketika waktu target terlewat sebelum pembaruan berikutnya tiba.
- **Modul:** Widget Android
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-34, T-35
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `widget/WidgetUpdater.kt`, `widget/NextPrayerWidget.kt`, `widget/DailyScheduleWidget.kt`, `res/values/strings.xml`

### T-39: Unit test `WidgetStateMapper`
- **Deskripsi:** Uji kasus: sebelum Subuh, di antara dua waktu, tepat pada waktu sholat, setelah Isya (menunjuk Subuh esok hari), pergantian hari, dan perhitungan dasar hitung mundur. Fungsi murni ini mudah diuji tanpa perangkat.
- **Modul:** Widget Android
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-32
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `app/src/test/.../WidgetStateMapperTest.kt`

**Subtotal Modul 4:** 8 task, sekitar 20,5 jam.

---

## MODUL 5: Pengaturan, Onboarding & Kustomisasi

### T-40: Perluas `SettingsRepository` untuk tema dan onboarding
- **Deskripsi:** Tambahkan key `theme_mode` (terang/gelap/sistem, default sistem) dan `onboarding_done` (default false) beserta field-nya di `Settings`. Dengan ini seluruh key di Tech Spec Bagian 2 sudah lengkap.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-12, T-19
- **Tanggal:** 2026-10-27
- **Estimasi:** 1,5 jam
- **File yang diubah:** `data/prefs/SettingsRepository.kt`, `domain/model/Settings.kt`

### T-41: Layar menu Pengaturan (`settings`)
- **Deskripsi:** Layar utama pengaturan berisi pintu masuk ke sub-layar: Notifikasi & Suara, Metode & Koreksi, Tampilan, Cadangan, Panduan Baterai, Tentang & Privasi. Tambahkan juga baris untuk mengubah lokasi (membuka `city_search`). Daftarkan semua rute sub-layar di `AppNavHost`.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-06, T-18, T-29, T-30
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `ui/settings/SettingsScreen.kt`, `ui/navigation/AppNavHost.kt`

### T-42: Layar Metode & Koreksi (`settings/calculation`)
- **Deskripsi:** Pilihan metode perhitungan (Kemenag RI, MWL, Umm al-Qura, dan lainnya) dan mazhab Ashar (FR-15), koreksi menit per waktu sholat dengan batas -10 sampai +10 (FR-16, BR-03), serta saklar ihtiyath. Setiap perubahan tersimpan ke DataStore dan otomatis memicu penjadwalan ulang alarm dan pembaruan widget lewat T-27 dan T-37. Tampilkan pratinjau jadwal hari ini agar efek perubahan langsung terlihat.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-08, T-12, T-27, T-37, T-41
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `ui/settings/CalculationSettingsScreen.kt`, `ui/settings/CalculationSettingsViewModel.kt`

### T-43: Layar Tampilan dan penerapan tema (`settings/appearance`)
- **Deskripsi:** Pilihan tema terang, gelap, atau ikuti sistem (FR-18). Terapkan ke seluruh tema Compose, dan sambungkan ke widget: `WidgetUpdater` memilih warna widget sesuai `theme_mode` (melengkapi T-36, yang baru mengikuti tema sistem) lalu memperbarui widget saat pilihan berubah. Bahasa tetap Indonesia, tanpa pemilih bahasa di v1.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-36, T-37, T-40, T-41
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `ui/settings/AppearanceSettingsScreen.kt`, `ui/theme/Theme.kt`, `widget/WidgetUpdater.kt`, `MainActivity.kt`

### T-44: Ekspor dan impor pengaturan (logika)
- **Deskripsi:** Implementasikan `export()` dan `import(json)` di `SettingsRepository`: serialisasi seluruh isi DataStore ke JSON dengan field `version`. Impor memvalidasi versi, tipe, dan rentang nilai (misalnya koreksi menit -10 sampai +10, enum yang dikenal), menolak berkas tidak valid seluruhnya tanpa mengubah pengaturan lama, lalu memicu penjadwalan ulang alarm dan pembaruan widget bila valid. Tabel kota tidak ikut dicadangkan.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-40, T-27, T-37
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `data/prefs/SettingsRepository.kt`, `data/prefs/SettingsBackup.kt`

### T-45: Layar Cadangan (`settings/backup`)
- **Deskripsi:** Dua tombol, Ekspor dan Impor, memakai Storage Access Framework (`CreateDocument` dan `OpenDocument`) sehingga tidak butuh izin penyimpanan. Tampilkan hasil yang jelas: berhasil, atau alasan penolakan jika berkas tidak valid.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-41, T-44
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `ui/settings/BackupScreen.kt`, `ui/settings/BackupViewModel.kt`

### T-46: Onboarding (`onboarding`)
- **Deskripsi:** Maksimal 4 layar dan selesai < 1 menit: (1) sambutan singkat, (2) lokasi: GPS atau pilih kota, (3) izin notifikasi dan exact alarm, (4) pengecualian baterai dengan tautan ke panduan di T-30. Setiap izin bisa dilewati dan diatur ulang nanti. Setelah selesai, set `onboarding_done = true`, jalankan penjadwalan alarm dan pembaruan widget, lalu buka `home`. Penentuan layar awal: jika `onboarding_done = false`, aplikasi dibuka ke `onboarding`.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-13, T-18, T-22, T-27, T-30, T-40
- **Tanggal:** 2026-10-27
- **Estimasi:** 6 jam
- **File yang diubah:** `ui/onboarding/OnboardingScreen.kt`, `ui/onboarding/OnboardingViewModel.kt`, `ui/navigation/AppNavHost.kt`

### T-47: Layar Tentang & Privasi (`settings/about`)
- **Deskripsi:** Tampilkan versi aplikasi, kebijakan privasi singkat (tanpa internet, tanpa akun, data hanya di perangkat), lisensi aplikasi dan lisensi pihak ketiga (library perhitungan, data kota, suara adzan), serta catatan bahwa tanggal Hijriah dihitung offline dan bisa berbeda satu hari dari ketetapan Kemenag (BR-08).
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-04, T-10, T-20, T-41
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `ui/settings/AboutScreen.kt`, `res/values/strings.xml`, `docs/PRIVACY.md`

### T-48: Penanganan edge case lokasi
- **Deskripsi:** (a) Saat zona waktu perangkat berubah, tawarkan "Perbarui lokasi". (b) Untuk lokasi di luar Indonesia dan garis lintang sangat tinggi, tampilkan peringatan bahwa jadwal mungkin kurang akurat. (c) Jika database kota gagal dibuka, tampilkan pesan yang jelas dan tetap izinkan GPS dengan nama "Lokasi saya". Tidak boleh ada crash pada ketiganya (BR-10).
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-13, T-16, T-26
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `ui/home/HomeScreen.kt`, `data/repository/LocationRepository.kt`, `data/db/AppDatabase.kt`

### T-49: Unit test pengaturan dan validasi impor
- **Deskripsi:** Uji nilai default `SettingsRepository`, pembatasan koreksi menit, serta impor: berkas valid, versi tidak dikenal, nilai di luar rentang, JSON rusak (pengaturan lama harus tetap utuh), dan ekspor lalu impor menghasilkan pengaturan yang sama.
- **Modul:** Pengaturan, Onboarding & Kustomisasi
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-44
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `app/src/test/.../SettingsRepositoryTest.kt`, `app/src/test/.../SettingsBackupTest.kt`

**Subtotal Modul 5:** 10 task, sekitar 31 jam.

---

## MODUL 6: Pengujian, Keamanan & Rilis

### T-50: Konfigurasi build rilis dan R8
- **Deskripsi:** Aktifkan `minifyEnabled` (R8) dan `shrinkResources` pada build `release`, tambahkan aturan keep yang diperlukan (Room, library perhitungan), dan matikan log debug di rilis. Konfigurasi penandatanganan membaca keystore dari variabel lingkungan, bukan dari berkas di repositori.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-02, T-03
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `app/build.gradle.kts`, `app/proguard-rules.pro`

### T-51: Buat dan amankan keystore penandatanganan
- **Deskripsi:** Buat keystore rilis, simpan cadangannya di dua tempat terpisah di luar repositori, lalu daftarkan alias, password, dan berkas berenkode base64 ke GitHub Secrets. Keystore adalah satu-satunya aset yang tidak boleh hilang, karena tanpanya tidak ada pembaruan yang bisa dipasang di atas versi lama.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-04
- **Tanggal:** 2026-10-27
- **Estimasi:** 1 jam
- **File yang diubah:** - (di luar repositori; konfigurasi GitHub Secrets)

### T-52: Workflow rilis GitHub Actions
- **Deskripsi:** Workflow yang berjalan pada tag versi (misalnya `v1.0.0`): build `release` dengan keystore dari Secrets, jalankan test, tandatangani APK, hitung checksum SHA-256, lalu publikasikan ke GitHub Releases beserta catatan rilis. Tag versi pra-rilis ditandai sebagai pre-release.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-05, T-50, T-51
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `.github/workflows/release.yml`

### T-53: Audit keamanan dan manifest
- **Deskripsi:** Periksa hasil build: tidak ada izin `INTERNET`, semua komponen internal `exported="false"`, semua `PendingIntent` memakai `FLAG_IMMUTABLE` dan intent eksplisit, lokasi tidak muncul di log, `allowBackup="false"` aktif, dan seluruh dependensi open source tanpa Google Play Services, Firebase, atau SDK iklan/analitik. Simpan hasilnya sebagai daftar periksa.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-22, T-37, T-46, T-47
- **Tanggal:** 2026-10-27
- **Estimasi:** 2,5 jam
- **File yang diubah:** `docs/SECURITY_CHECKLIST.md`, `AndroidManifest.xml` (jika ada perbaikan)

### T-54: Uji keandalan alarm di perangkat fisik
- **Deskripsi:** Pasang build di beberapa HP fisik dengan merek berbeda, termasuk yang agresif menutup aplikasi, lalu amati selama minimal beberapa hari. Catat setiap notifikasi yang berbunyi, terlambat, atau tidak berbunyi. Target > 95% tepat waktu. Jika di bawah target pada merek tertentu, pertimbangkan `setAlarmClock` sebagai cadangan (Tech Spec Bagian 4).
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-30, T-37, T-46
- **Tanggal:** 2026-10-27
- **Estimasi:** 8 jam kerja aktif (ditambah masa pengamatan beberapa hari)
- **File yang diubah:** `docs/TEST_REPORT_ALARM.md`

### T-55: Uji Doze dan pemulihan setelah peristiwa sistem
- **Deskripsi:** Simulasikan Doze lewat `adb shell dumpsys deviceidle` dan pastikan alarm tetap berbunyi. Uji restart HP, perubahan zona waktu, perubahan jam manual, dan pemberian izin exact alarm di tengah hari. Pastikan alarm dan widget terjadwal ulang tanpa alarm basi atau duplikat.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-26, T-37
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `docs/TEST_REPORT_RECOVERY.md`

### T-56: Uji izin ditolak dan edge case
- **Deskripsi:** Tolak izin lokasi, notifikasi, dan exact alarm satu per satu, lalu matikan GPS. Pastikan tidak ada crash dan setiap kasus punya jalur alternatif yang jelas (pilih kota manual, banner peringatan). Uji juga berkas impor rusak dan database kota yang gagal dibuka.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-28, T-46, T-48
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `docs/TEST_REPORT_PERMISSIONS.md`

### T-57: Uji ukuran, performa, dan baterai
- **Deskripsi:** Ukur pada build rilis: ukuran APK < 15 MB, cold start < 2 detik di HP spek rendah, RAM aktif < 100 MB, perhitungan sebulan < 200 ms, dan konsumsi baterai < 1% per hari dari statistik baterai Android. Jika ada target yang gagal, buat catatan perbaikan sebelum beta.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-17, T-37, T-50
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `docs/TEST_REPORT_PERFORMANCE.md`

### T-58: Uji widget lintas launcher dan tema
- **Deskripsi:** Pasang kedua widget di beberapa launcher, ukuran layar, dan ukuran widget berbeda, di tema terang dan gelap. Periksa hitung mundur yang akurat, pergantian otomatis ke sholat berikutnya, tampilan saat lokasi belum diatur, dan teks yang tidak terpotong.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-38, T-43
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `docs/TEST_REPORT_WIDGET.md`

### T-59: Uji aksesibilitas
- **Deskripsi:** Jalankan aplikasi dengan TalkBack dan ukuran font sistem terbesar. Lengkapi `contentDescription` untuk elemen tanpa teks, pastikan area sentuh nyaman, dan pastikan semua layar bisa dipakai tanpa memotong teks penting.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-43, T-46
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `ui/**/*.kt`, `res/values/strings.xml`

### T-60: Dokumentasi proyek
- **Deskripsi:** Lengkapi `README.md` (fitur, tangkapan layar, cara build, lisensi), `CHANGELOG.md`, dan panduan memasang APK dari sumber tidak dikenal beserta cara memeriksa checksum SHA-256.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-04, T-47
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `README.md`, `CHANGELOG.md`, `docs/INSTALL.md`

### T-61: Metadata Fastlane untuk F-Droid
- **Deskripsi:** Siapkan struktur `fastlane/metadata/android/id/` berisi judul, deskripsi singkat dan panjang, ikon, tangkapan layar, dan changelog per versi, yang dibaca F-Droid secara otomatis. Gunakan ikon dan tangkapan layar tanpa elemen berhak cipta pihak lain.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-47, T-60
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `fastlane/metadata/android/id/**`

### T-62: Reproducible build dan kesiapan F-Droid
- **Deskripsi:** Pastikan build deterministik (versi Gradle dan dependensi dikunci, tanpa timestamp atau data acak di keluaran) supaya F-Droid bisa mencocokkan hasil build-nya dengan APK bertanda tangan sendiri, dan pengguna bisa berpindah antara GitHub dan F-Droid tanpa uninstall. Susun berkas metadata untuk repositori `fdroiddata`. Jika reproducible build terlalu berat untuk v1, putuskan dan dokumentasikan bahwa pengguna disarankan memakai satu sumber saja.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** Mid
- **Status:** Todo
- **Dependensi:** T-52, T-61
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `app/build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`, `docs/FDROID.md`

### T-63: Rilis alfa
- **Deskripsi:** Bagikan APK ke diri sendiri dan teman dekat (tag pra-rilis, misalnya `v0.1.0-alpha`). Fokus pada keandalan alarm dan akurasi jadwal. Kumpulkan temuan dan perbaiki yang kritis sebelum beta.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-52, T-53, T-54, T-55, T-56
- **Tanggal:** 2026-10-27
- **Estimasi:** 2 jam
- **File yang diubah:** `CHANGELOG.md`

### T-64: Rilis beta
- **Deskripsi:** Publikasikan APK pra-rilis di GitHub Releases untuk penguji lebih luas, aktifkan GitHub Issues dengan templat laporan (merek HP, versi Android, langkah reproduksi), lalu perbaiki temuan. Pastikan hasil uji ukuran, widget, dan aksesibilitas sudah memenuhi target.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-57, T-58, T-59, T-63
- **Tanggal:** 2026-10-27
- **Estimasi:** 3 jam
- **File yang diubah:** `.github/ISSUE_TEMPLATE/bug_report.md`, `CHANGELOG.md`

### T-65: Rilis v1.0 dan pengajuan ke F-Droid
- **Deskripsi:** Beri tag `v1.0.0`, biarkan workflow menerbitkan APK bertanda tangan beserta checksum ke GitHub Releases, lalu kirim merge request metadata ke `fdroiddata`. Siap merevisi sesuai masukan relawan peninjau F-Droid, karena prosesnya butuh waktu.
- **Modul:** Pengujian, Keamanan & Rilis
- **Prioritas:** High
- **Status:** Todo
- **Dependensi:** T-60, T-61, T-62, T-64
- **Tanggal:** 2026-10-27
- **Estimasi:** 4 jam
- **File yang diubah:** `CHANGELOG.md`, `fastlane/metadata/android/id/changelogs/*`

**Subtotal Modul 6:** 16 task, sekitar 50,5 jam.
