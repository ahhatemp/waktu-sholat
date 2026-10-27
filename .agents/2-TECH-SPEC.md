# Tech Spec: Aplikasi Waktu Sholat (Gratis, Offline, Widget Android)

Berdasarkan `1-PRD.md`.

---

## BAGIAN 1: Tech Stack & Arsitektur

### Tech Stack

| Layer | Technology | Versi |
|-------|------------|-------|
| Platform | Android native | minSdk 26 (Android 8.0), targetSdk mengikuti syarat terbaru |
| Language | Kotlin | 2.x (stabil terbaru) |
| UI Aplikasi | Jetpack Compose + Material 3 | Compose BOM terbaru |
| UI Widget | AppWidgetProvider + RemoteViews | API bawaan Android |
| State | ViewModel + StateFlow | AndroidX Lifecycle |
| Navigasi | Navigation Compose | AndroidX |
| Database | Room (SQLite) | 2.x |
| Pengaturan | DataStore (Preferences) | AndroidX |
| Waktu sholat | Library Adhan (Kotlin/Java) atau implementasi sendiri | dipilih saat implementasi |
| Lokasi | `android.location.LocationManager` | API bawaan |
| Alarm | `AlarmManager` (exact alarm) | API bawaan |
| Background | BroadcastReceiver, tanpa foreground service | API bawaan |
| DI | Manual (tanpa Hilt/Dagger) | - |
| Build | Gradle (Kotlin DSL) + version catalog | AGP terbaru stabil |
| Testing | JUnit + Room in-memory test | - |
| Caching / Backend / Auth | Tidak ada | - |

> Versi pasti dikunci di `gradle/libs.versions.toml` saat proyek dibuat, supaya mengikuti rilis stabil terbaru pada hari itu.

### Arsitektur Sistem

```
UI (Compose)  <-->  ViewModel  <-->  Repository
                                        |
              +-------------------------+-------------------------+
              |                         |                         |
     PrayerTimeEngine            Room (kota)              DataStore (pengaturan)
     (hitung di perangkat)

Komponen sistem Android:
  AlarmManager --> AlarmReceiver --> Notifikasi + suara adzan
  AppWidgetManager <-- WidgetProvider (RemoteViews + Chronometer)
  BootReceiver / TimeChangeReceiver --> jadwalkan ulang alarm & widget
  LocationManager --> deteksi lokasi (sekali)
```

Semua berjalan di perangkat, tanpa server dan tanpa jaringan.

### Struktur Folder

```
app/src/main/
├── java/id/.../waktusholat/
│   ├── data/
│   │   ├── db/            # Room: AppDatabase, CityDao, CityEntity
│   │   ├── prefs/         # DataStore: SettingsRepository
│   │   └── repository/    # PrayerRepository, LocationRepository
│   ├── domain/
│   │   ├── PrayerTimeEngine.kt   # logika hitung, bebas dari Android
│   │   ├── CalculationMethod.kt
│   │   └── model/                # PrayerTimes, Prayer, City
│   ├── alarm/
│   │   ├── AlarmScheduler.kt
│   │   ├── AlarmReceiver.kt
│   │   ├── BootReceiver.kt
│   │   └── TimeChangeReceiver.kt
│   ├── widget/
│   │   ├── NextPrayerWidget.kt    # kecil
│   │   ├── DailyScheduleWidget.kt # besar
│   │   └── WidgetUpdater.kt
│   ├── ui/
│   │   ├── home/  monthly/  settings/  onboarding/  city/
│   │   ├── theme/
│   │   └── navigation/
│   └── MainActivity.kt
├── res/
│   ├── layout/    # layout RemoteViews widget
│   ├── raw/       # suara adzan
│   └── xml/       # appwidget-provider info
└── assets/
    └── cities.db  # database kota Indonesia (prapopulasi Room)
```

`domain/PrayerTimeEngine.kt` sengaja dipisah dari kode Android supaya bisa diuji dengan unit test biasa dan dibandingkan dengan data Kemenag RI (sesuai NFR Maintainability).

### Justifikasi

- **Kotlin native:** widget, exact alarm, dan penanganan reboot adalah inti produk ini, dan semuanya API bawaan Android. Native juga memudahkan menjaga ukuran < 15 MB dan RAM rendah.
- **RemoteViews untuk widget, bukan Glance:** `Chronometer` pada RemoteViews bisa menghitung mundur oleh sistem tanpa memicu pembaruan per menit (FR-13), dan ukurannya lebih kecil. Compose tetap dipakai untuk seluruh layar di dalam aplikasi.
- **Room + DataStore:** Room menyimpan database kota yang dibundel (FR-02), sedangkan DataStore menyimpan pengaturan seperti metode, koreksi menit, dan toggle notifikasi.
- **`LocationManager` bawaan, bukan Google Play Services:** F-Droid hanya menerima aplikasi yang seluruh dependensinya open source. Fused Location, Firebase, dan SDK Google lain akan menyebabkan penolakan. Jadi kita memakai API lokasi bawaan Android, dan memilih library Adhan yang berlisensi open source.
- **Tanpa foreground service:** alarm dan widget digerakkan event sistem, sehingga baterai tetap irit (< 1% per hari) dan tidak ada notifikasi permanen yang mengganggu.
- **DI manual:** untuk aplikasi sekecil ini, Hilt hanya menambah ukuran dan waktu build tanpa banyak manfaat.

---

## BAGIAN 2: Database Design

### Ringkasan Database

| Item | Detail |
|------|--------|
| Database | SQLite lewat Room (khusus data kota) |
| Penyimpanan pengaturan | DataStore (Preferences) |
| ORM/Driver | Room 2.x dengan KSP |
| Pendekatan | Relational ringan (satu tabel utama), plus key-value untuk pengaturan |
| Prapopulasi | `createFromAsset("cities.db")`, berkas dibundel di `assets/` |
| Tools Migrasi | Room schema export + migrasi manual per versi |

Jadwal sholat **tidak disimpan** di database. Perhitungan sebulan penuh < 200 ms (lihat NFR), jadi jadwal dihitung ulang saat dibutuhkan. Dengan begitu tidak ada data basi yang harus disinkronkan setiap kali pengguna mengubah metode, koreksi, atau lokasi.

### Entity Overview

**Room**

| Entity | Key Fields | Relasi |
|--------|-----------|--------|
| `City` | id, name, name_search, province, latitude, longitude, timezone_id | Berdiri sendiri (read-only di aplikasi) |

Catatan field:
- `name_search`: nama versi huruf kecil tanpa awalan "Kab."/"Kota", untuk pencarian cepat.
- `timezone_id`: ID IANA (`Asia/Jakarta`, `Asia/Makassar`, `Asia/Jayapura`), sehingga WIB/WITA/WIT otomatis (FR-03).

**DataStore (Preferences)**

| Key | Tipe | Default | Dipakai di |
|-----|------|---------|-----------|
| `location_lat`, `location_lng` | Double | - | FR-01, FR-02 |
| `location_name` | String | - | FR-01, FR-02 |
| `location_timezone` | String | zona perangkat | FR-03 |
| `location_source` | Enum (gps / manual) | - | FR-01, FR-02 |
| `calc_method` | Enum | Kemenag RI | FR-15 |
| `asr_madhab` | Enum | Syafi'i | FR-15 |
| `offset_<prayer>` | Int (menit, -10 sampai +10) | 0 | FR-16 |
| `ihtiyath_enabled` | Boolean | false | FR-16 |
| `notify_<prayer>` | Boolean | true, kecuali Imsak dan Terbit false | FR-07 |
| `pre_reminder_minutes` | Int (0 = mati) | 0 | FR-08 |
| `sound_<prayer>` | Enum (adzan penuh / singkat / nada biasa / senyap) | adzan penuh | FR-10 |
| `theme_mode` | Enum (terang / gelap / sistem) | sistem | FR-14, FR-18 |
| `onboarding_done` | Boolean | false | FR-17 |

`<prayer>` berarti satu key per waktu: imsak, subuh, terbit, dzuhur, ashar, maghrib, isya.

### Index Strategy

- **`City.name_search`**: index biasa, untuk pencarian dengan awalan (`LIKE 'bandung%'`) pada FR-02. Dengan sekitar 500 baris (kabupaten/kota se-Indonesia) ini sudah lebih dari cukup, jadi FTS tidak diperlukan.
- **`City.province`**: tidak perlu index, hanya dipakai sebagai teks tambahan di daftar hasil.

### Data Flow

1. **Setup awal:** pengguna memilih lokasi lewat GPS (FR-01) atau pencarian kota di tabel `City` (FR-02). Hasilnya (koordinat, nama, zona waktu) ditulis ke DataStore.
2. **Setiap perhitungan:** `PrayerRepository` membaca lokasi dan pengaturan dari DataStore, lalu memberikannya ke `PrayerTimeEngine`, yang menghasilkan waktu sholat.
3. **Konsumen hasil:** layar harian, layar bulanan, `AlarmScheduler`, dan widget semuanya memanggil `PrayerRepository` yang sama, sehingga angka di semua tempat selalu konsisten.
4. **Saat pengaturan berubah:** DataStore memancarkan nilai baru lewat Flow. UI ikut berubah, dan `AlarmScheduler` serta `WidgetUpdater` dipicu untuk menjadwalkan ulang (FR-15, FR-16).
5. **Cadangan (FR-19):** seluruh isi DataStore diekspor ke satu berkas JSON lokal dan diimpor kembali dengan validasi. Tabel `City` tidak ikut dicadangkan karena sudah ada di aplikasi.

### Catatan Data Kota
- Sumber data harus berlisensi publik yang jelas (sudah tercatat sebagai dependensi di PRD). Tahap implementasi perlu memastikan lisensinya sebelum rilis ke F-Droid.
- Cakupan v1 adalah level kabupaten/kota. Level kecamatan bisa ditambahkan di v2 jika selisih jarak dianggap berpengaruh, karena pergeseran beberapa kilometer hanya menggeser waktu sholat sekitar satu menit atau kurang.

---

## BAGIAN 3: Interface Design

Karena aplikasi ini tanpa server, "interface"-nya bukan endpoint API. Bentuknya adalah layar aplikasi, komponen Android (receiver dan widget), kontrak antarmodul di dalam kode, dan izin sistem. Autentikasi tidak relevan (N/A).

### 1. Layar & Navigasi (Navigation Compose)

| Route | Layar | Deskripsi | FR |
|-------|-------|-----------|----|
| `onboarding` | Onboarding (maks. 4 layar) | Pilih lokasi, izin notifikasi, izin alarm, pengecualian baterai. Setiap langkah bisa dilewati | FR-17, FR-01, FR-09 |
| `home` | Jadwal Harian | Daftar waktu sholat, penanda sholat berjalan/berikutnya, hitung mundur, tanggal Hijriah | FR-04 |
| `monthly` | Jadwal Bulanan | Tabel sebulan yang bisa digulir, navigasi bulan sebelumnya/berikutnya | FR-05 |
| `city_search` | Cari Kota | Kolom pencarian dengan hasil dari tabel `City` | FR-02 |
| `settings` | Pengaturan (menu utama) | Pintu masuk ke sub-layar di bawah | - |
| `settings/notifications` | Notifikasi & Suara | Toggle per waktu, pengingat sebelum adzan, pilihan suara | FR-07, FR-08, FR-10 |
| `settings/calculation` | Metode & Koreksi | Metode perhitungan, mazhab Ashar, koreksi menit, ihtiyath | FR-15, FR-16 |
| `settings/appearance` | Tampilan | Tema terang/gelap/sistem | FR-18, FR-14 |
| `settings/backup` | Cadangan | Ekspor dan impor pengaturan (berkas JSON) | FR-19 |
| `settings/battery_guide` | Panduan Baterai | Panduan per merek HP, bisa dibuka ulang kapan saja | FR-09 |
| `settings/about` | Tentang & Privasi | Kebijakan privasi singkat, lisensi, versi aplikasi | NFR Privasi |

Alur awal: jika `onboarding_done = false`, aplikasi dibuka ke `onboarding`, selain itu langsung ke `home`. Navigasi utama memakai bottom bar dengan tiga tujuan: Harian, Bulanan, Pengaturan.

### 2. Komponen Android (AndroidManifest)

| Komponen | Tipe | Pemicu | Tugas | FR |
|----------|------|--------|-------|----|
| `AlarmReceiver` | BroadcastReceiver | Alarm exact dari `AlarmManager` | Tampilkan notifikasi + suara adzan, lalu jadwalkan sholat berikutnya | FR-06, FR-08 |
| `MidnightReceiver` | BroadcastReceiver | Alarm harian setelah tengah malam | Hitung jadwal hari baru, jadwalkan ulang alarm, perbarui widget | FR-06, FR-13 |
| `BootReceiver` | BroadcastReceiver | `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED` | Jadwalkan ulang semua alarm dan widget | FR-06 |
| `TimeChangeReceiver` | BroadcastReceiver | `TIMEZONE_CHANGED`, `TIME_SET` | Hitung ulang jadwal dan jadwalkan ulang | FR-06, FR-13 |
| `ExactAlarmPermissionReceiver` | BroadcastReceiver | Perubahan izin exact alarm | Jadwalkan ulang setelah izin diberikan | FR-06, FR-09 |
| `NextPrayerWidget` | AppWidgetProvider | Update dari sistem dan `WidgetUpdater` | Widget kecil: sholat berikutnya + hitung mundur | FR-11, FR-13 |
| `DailyScheduleWidget` | AppWidgetProvider | Sama | Widget besar: semua waktu hari ini, resizable | FR-12, FR-13 |
| `MainActivity` | Activity (satu-satunya) | Launcher, tap notifikasi, tap widget | Host Compose dan navigasi | - |

Seluruh `PendingIntent` memakai `FLAG_IMMUTABLE`, dan receiver internal tidak di-export (`exported="false"`), kecuali yang memang menerima broadcast sistem.

### 3. Kontrak Antarmodul (Kotlin)

| Antarmuka | Fungsi utama | Dipakai oleh |
|-----------|--------------|--------------|
| `PrayerTimeEngine` | `calculate(date, location, settings): PrayerTimes` | `PrayerRepository` |
| `PrayerRepository` | `getDay(date)`, `getMonth(year, month)`, `getNext(now)` | UI, alarm, widget |
| `SettingsRepository` | `settings: Flow<Settings>`, `update { ... }`, `export()`, `import(json)` | UI, scheduler |
| `LocationRepository` | `detectOnce(): Result<Location>`, `searchCity(query): List<City>` | Onboarding, pengaturan |
| `AlarmScheduler` | `scheduleAll()`, `cancelAll()`, `scheduleNext()` | Receiver, `SettingsRepository` |
| `WidgetUpdater` | `updateAll()` | Receiver, `AlarmScheduler` |
| `NotificationHelper` | `show(prayer, sound)`, `ensureChannels()` | `AlarmReceiver` |

### 4. Izin Sistem (Manifest + Runtime)

| Izin | Kapan diminta | Jika ditolak | Alasan |
|------|---------------|--------------|--------|
| `ACCESS_COARSE_LOCATION` | Onboarding (FR-01) | Pilih kota manual (FR-02) | Cukup level kota, lebih ramah privasi daripada lokasi presisi |
| `POST_NOTIFICATIONS` (Android 13+) | Onboarding | Jadwal tetap tampil, tapi tanpa adzan | Menampilkan notifikasi |
| `SCHEDULE_EXACT_ALARM` | Onboarding, arahkan ke layar izin sistem | Alarm kurang presisi, tampilkan peringatan | Alarm tepat waktu (Android 12+) |
| `RECEIVE_BOOT_COMPLETED` | Otomatis | - | Jadwalkan ulang setelah restart |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Onboarding (opsional) | Tampilkan panduan merek HP | Mencegah alarm dimatikan sistem |
| `VIBRATE` | Otomatis | - | Mode getar |

Aplikasi tidak memakai izin internet (`INTERNET`) sama sekali. Ini bisa dibuktikan langsung dari manifest dan menjadi bukti kuat untuk klaim "offline dan tanpa pelacak".

### 5. Kanal Notifikasi

| Channel ID | Dipakai untuk | Suara |
|------------|---------------|-------|
| `adzan_full` | Adzan penuh | `res/raw/adzan_full` |
| `adzan_short` | Adzan singkat | `res/raw/adzan_short` |
| `reminder` | Pengingat sebelum adzan dan nada biasa | Nada default sistem |
| `silent` | Mode senyap/getar | Tanpa suara |

Suara notifikasi di Android 8+ ditentukan oleh kanal dan tidak bisa diubah per notifikasi, jadi tiap pilihan suara di FR-10 dipetakan ke satu kanal. Pilihan suara per waktu sholat (misalnya Subuh berbeda) cukup dengan memilih kanal yang berbeda saat `NotificationHelper.show()` dipanggil.

---

## BAGIAN 4: Alur Logika & Business Rules

### 1. Alur Utama

**A. Setup Lokasi (FR-01, FR-02, FR-17)**
1. Onboarding meminta izin lokasi kasar.
2. Jika diizinkan: `LocationManager` mengambil satu posisi (timeout 15 detik). Koordinat dicocokkan ke kota terdekat di tabel `City` untuk mendapatkan nama dan `timezone_id`.
3. Jika ditolak, timeout, atau GPS mati: tampilkan `city_search`, pengguna memilih kota secara manual.
4. Simpan lokasi ke DataStore, lalu panggil `AlarmScheduler.scheduleAll()` dan `WidgetUpdater.updateAll()`.
5. Lokasi tidak dipantau terus-menerus. Perbarui hanya jika pengguna menekan "Perbarui lokasi" atau memilih kota baru.

**B. Perhitungan Jadwal (FR-03, FR-15, FR-16)**
1. `PrayerRepository` membaca lokasi dan pengaturan dari DataStore.
2. `PrayerTimeEngine.calculate()` menghitung waktu astronomis untuk tanggal dan koordinat tersebut.
3. Terapkan ihtiyath jika aktif, lalu tambahkan koreksi menit per waktu sholat.
4. Ubah ke zona waktu lokasi (`timezone_id`) dan kembalikan `PrayerTimes`.
5. Hasil tidak disimpan, dihitung ulang tiap dibutuhkan (sebulan < 200 ms).

**C. Penjadwalan Alarm (FR-06, FR-07, FR-08)**
1. `scheduleAll()` membatalkan semua alarm lama milik aplikasi.
2. Hitung jadwal hari ini (dan besok jika sisa waktu hari ini sudah lewat semua).
3. Untuk setiap waktu sholat yang aktif dan belum lewat, pasang satu alarm. Jika pengingat sebelum adzan aktif, pasang alarm tambahan di `waktu - N menit`.
4. Pasang satu alarm harian `MidnightReceiver` pada 00:05 waktu lokasi untuk menghitung hari baru.
5. Setiap alarm memakai `requestCode` tetap (misalnya hari ke-offset × 10 + indeks sholat), sehingga bisa dibatalkan dan diganti tanpa duplikat.

**D. Saat Alarm Berbunyi (FR-06, FR-10)**
1. `AlarmReceiver` menerima broadcast berisi jenis sholat dan apakah ini pengingat atau adzan.
2. Baca preferensi suara untuk sholat tersebut, tentukan kanal notifikasi.
3. Tampilkan notifikasi lewat `NotificationHelper`.
4. Perbarui widget agar langsung menunjuk sholat berikutnya (FR-13).
5. Proses harus selesai cepat (receiver tidak boleh bekerja lama). Tidak ada layanan latar belakang.

**E. Pemulihan Setelah Peristiwa Sistem (FR-06, FR-13)**

| Peristiwa | Tindakan |
|-----------|----------|
| HP restart / aplikasi diperbarui | `BootReceiver`: `scheduleAll()` + `updateAll()` |
| Zona waktu atau jam sistem berubah | `TimeChangeReceiver`: hitung ulang, `scheduleAll()` + `updateAll()` |
| Izin exact alarm baru diberikan | `ExactAlarmPermissionReceiver`: `scheduleAll()` |
| Aplikasi dibuka | Sinkronisasi ringan: pastikan alarm hari ini ada, jika tidak jadwalkan ulang |
| Pengaturan berubah (metode, koreksi, toggle, lokasi) | `scheduleAll()` + `updateAll()` |

Titik sinkronisasi saat aplikasi dibuka berfungsi sebagai jaring pengaman, karena beberapa merek HP kadang membuang alarm tanpa pemberitahuan.

**F. Pembaruan Widget (FR-11 sampai FR-14)**
1. `WidgetUpdater` mengambil sholat berikutnya dari `PrayerRepository.getNext(now)`.
2. Widget kecil: set teks nama dan jam sholat, lalu set `Chronometer` dalam mode hitung mundur dengan `base = elapsedRealtime() + (waktuTarget - sekarang)`. Sistem yang menggerakkan angkanya, aplikasi tidak perlu bangun tiap menit.
3. Widget besar: tampilkan semua waktu hari ini dan sorot waktu berikutnya.
4. Pembaruan hanya dipicu oleh: bunyi alarm sholat, pergantian hari, perubahan pengaturan atau tema, dan peristiwa sistem pada tabel E. Tidak ada polling berkala.
5. Warna mengikuti tema sistem (terang/gelap), kecuali pengguna memilih lain.

**G. Cadangan & Pemulihan (FR-19)**
1. Ekspor: serialisasi seluruh isi DataStore ke JSON dengan field `version`, simpan lewat Storage Access Framework (pengguna memilih lokasi berkas, tanpa izin penyimpanan tambahan).
2. Impor: baca berkas, validasi versi dan rentang nilai (misalnya koreksi menit -10 sampai +10), tolak jika tidak valid.
3. Jika valid, tulis ke DataStore, lalu `scheduleAll()` dan `updateAll()`.

### 2. Business Rules

| # | Aturan | Sumber |
|---|--------|--------|
| BR-01 | Default metode: Kemenag RI untuk lokasi di Indonesia. Parameter acuan: sudut Subuh 20°, Isya 18°, Imsak = 10 menit sebelum Subuh, ihtiyath 2 menit. Angka ini harus diverifikasi ulang terhadap publikasi resmi Kemenag saat implementasi, lalu dikunci lewat unit test. | FR-03, FR-15 |
| BR-02 | Mazhab Ashar default Syafi'i (bayangan = 1× tinggi benda). Hanafi sebagai pilihan. | FR-15 |
| BR-03 | Koreksi menit dibatasi -10 sampai +10 per waktu sholat. Koreksi diterapkan setelah ihtiyath. | FR-16 |
| BR-04 | Notifikasi default aktif untuk Subuh, Dzuhur, Ashar, Maghrib, Isya. Imsak dan Terbit default nonaktif. | FR-07 |
| BR-05 | Pengingat sebelum adzan nonaktif secara default, pilihan 5, 10, atau 15 menit. | FR-08 |
| BR-06 | Satu kanal notifikasi per pilihan suara. Suara per waktu sholat dipetakan ke kanal. | FR-10 |
| BR-07 | Zona waktu mengikuti lokasi yang dipilih, bukan zona perangkat. Jadwal selalu ditampilkan dalam waktu lokasi tersebut. | FR-03 |
| BR-08 | Tanggal Hijriah dihitung offline di perangkat (kalender Umm al-Qura bawaan Java/Android). Bisa berbeda satu hari dari ketetapan Kemenag, dan ini dicatat di layar Tentang. | FR-04 |
| BR-09 | Aplikasi tidak pernah mengirim data keluar perangkat dan tidak memiliki izin internet. | NFR Privasi |
| BR-10 | Setiap izin yang ditolak harus memberi jalur alternatif yang jelas, dan aplikasi tidak boleh crash. | NFR Keandalan |

### 3. Edge Cases & Penanganan

| Situasi | Penanganan |
|---------|-----------|
| Izin exact alarm ditolak | Pakai `setAndAllowWhileIdle` (kurang presisi), tampilkan banner peringatan permanen di layar utama sampai izin diberikan |
| Izin notifikasi ditolak | Jadwal dan widget tetap bekerja, tampilkan banner ajakan mengaktifkan |
| GPS mati atau tidak ada sinyal | Fallback ke pencarian kota manual, tanpa error |
| Pengguna di perbatasan zona waktu, atau berpindah kota | Tawarkan "Perbarui lokasi" saat zona waktu perangkat berubah |
| Sholat berikutnya jatuh besok (setelah Isya) | `getNext()` mengembalikan Subuh esok hari, widget dan hitung mundur menyesuaikan |
| HP mati saat waktu sholat | Alarm yang terlewat tidak dibunyikan terlambat. Setelah restart, `BootReceiver` hanya menjadwalkan sisa waktu hari ini |
| Jam sistem diubah manual | `TimeChangeReceiver` menghitung ulang, tidak ada alarm basi |
| Database kota gagal dibuka | Tampilkan pesan jelas, GPS tetap bisa dipakai dengan nama "Lokasi saya" |
| Berkas impor rusak atau versi tidak dikenal | Tolak seluruhnya, pengaturan lama tidak berubah |
| Lokasi di luar Indonesia | Hitung memakai metode terpilih. Untuk garis lintang sangat tinggi, tampilkan peringatan bahwa jadwal mungkin kurang akurat |
| Mode hemat baterai agresif (merek tertentu) | Panduan baterai per merek (FR-09), plus sinkronisasi saat aplikasi dibuka |

### 4. Keputusan Teknis
- **Kenapa tidak `setAlarmClock`:** metode ini memang kebal terhadap Doze dan tidak butuh izin exact alarm, tetapi memunculkan ikon alarm di status bar dan menampilkan "alarm berikutnya" di layar kunci. Itu mengubah kesan aplikasi, jadi dipakai `setExactAndAllowWhileIdle` dengan fallback di atas. Jika saat pengujian keandalan notifikasi di bawah target 95% pada merek tertentu, `setAlarmClock` adalah cadangan yang bisa dipertimbangkan.
- **Alarm per sholat, bukan rantai:** semua sisa alarm hari itu dipasang sekaligus. Jika satu alarm gagal, yang lain tetap berbunyi.

---

## BAGIAN 5: Keamanan & Deployment

### 1. Keamanan

Karena aplikasi tanpa server, tanpa akun, dan tanpa internet, permukaan serangannya kecil. Fokusnya menjaga data lokal tetap lokal dan komponen aplikasi tidak bisa disalahgunakan aplikasi lain.

| Area | Keputusan |
|------|-----------|
| Jaringan | Tidak ada izin `INTERNET`. Data tidak bisa keluar dari perangkat (BR-09) |
| Autentikasi | Tidak ada (tanpa akun dan login) |
| Data pengguna | Hanya lokasi kota dan pengaturan, tersimpan di DataStore di penyimpanan privat aplikasi |
| Auto-backup Google | `allowBackup="false"` dan aturan `dataExtractionRules` yang mengecualikan semua data, agar lokasi tidak ikut terunggah ke cloud tanpa sepengetahuan pengguna. Cadangan hanya lewat FR-19 yang dikendalikan pengguna |
| Komponen manifest | Semua `exported="false"`, kecuali receiver yang memang menerima broadcast sistem dan provider widget yang diwajibkan sistem |
| PendingIntent | Selalu `FLAG_IMMUTABLE`, intent eksplisit (menyebut kelas tujuan) |
| Validasi impor (FR-19) | Cek versi, tipe, dan rentang nilai. Berkas tidak valid ditolak seluruhnya |
| Lokasi | Hanya `ACCESS_COARSE_LOCATION`, diambil sekali, tidak dicatat di log |
| Logging | Tanpa data lokasi atau pengaturan di log. Log debug dimatikan di build rilis |
| Dependensi | Minimal dan seluruhnya open source. Tanpa Firebase, Google Play Services, atau SDK analitik/iklan |
| Hardening build | R8 (minify + shrink resources) aktif di rilis. Ini juga membantu target ukuran < 15 MB |

### 2. Lisensi (Perlu Keputusan)

F-Droid hanya menerima aplikasi dengan lisensi open source. Satu lisensi harus dipilih sebelum rilis pertama:

- **GPL-3.0:** turunan aplikasi wajib tetap open source. Cocok untuk menjaga prinsip "selalu gratis dan tanpa iklan".
- **Apache-2.0 atau MIT:** lebih longgar, orang lain boleh memakai kode di aplikasi berbayar.

Periksa juga kompatibilitas lisensi library Adhan, data kota, dan berkas suara adzan dengan lisensi yang dipilih. Berkas suara yang tidak jelas lisensinya adalah penyebab penolakan yang paling umum.

### 3. Lingkungan & Konfigurasi

| Item | Detail |
|------|--------|
| Environment | Hanya dua: `debug` (lokal) dan `release` (ditandatangani) |
| Server / database cloud | Tidak ada |
| Variabel rahasia | Hanya keystore penandatanganan APK, disimpan di GitHub Secrets (alias, password, berkas keystore berenkode base64) |
| Konfigurasi runtime | Tidak ada. Semua pengaturan ada di DataStore |

Keystore adalah satu-satunya aset yang tidak boleh hilang. Jika hilang, pembaruan yang bisa dipasang di atas versi lama tidak dapat dirilis. Simpan cadangannya di dua tempat terpisah di luar repositori.

### 4. Pipeline Build & Rilis (GitHub Actions)

1. **Pada setiap push atau pull request:** build `debug`, jalankan unit test (terutama `PrayerTimeEngine` dan Room in-memory), jalankan Android Lint.
2. **Pada tag versi (misalnya `v1.0.0`):** build `release` dengan keystore dari Secrets, jalankan test, tandatangani APK, lalu publikasikan ke **GitHub Releases** beserta checksum SHA-256.
3. **Penomoran versi:** `versionName` mengikuti semantic versioning (1.0.0), `versionCode` naik satu tiap rilis.

### 5. Distribusi: GitHub Releases + F-Droid

**GitHub Releases**
- Pengguna mengunduh APK dari halaman rilis. Sertakan checksum dan petunjuk memasang dari sumber tidak dikenal.
- Catatan rilis (changelog) ditulis per versi.

**F-Droid**
- F-Droid membangun aplikasi dari kode sumber, bukan menerima APK jadi. Berkas metadata dikirim (merge request) ke repositori `fdroiddata`, lalu mereka membangun dari tag rilis.
- Siapkan deskripsi, ikon, tangkapan layar, dan changelog dalam struktur Fastlane di repositori (`fastlane/metadata/android/id/...`), yang dibaca F-Droid secara otomatis.
- Bangun harus bisa diulang tanpa kunci rahasia dan tanpa dependensi proprietary. Karena itu pilihan di Bagian 1 (tanpa Google Play Services) wajib dijaga.
- Proses peninjauan dilakukan relawan, jadi beri jeda waktu dan siap merevisi.

**Satu jebakan yang harus diketahui:** secara bawaan F-Droid menandatangani APK dengan kuncinya sendiri, sedangkan APK GitHub ditandatangani kunci developer. Android menolak memperbarui aplikasi yang tanda tangannya berbeda, sehingga pengguna tidak bisa berpindah antara dua sumber itu tanpa uninstall. Solusinya adalah *reproducible builds*: F-Droid mencocokkan hasil build-nya dengan APK bertanda tangan developer, lalu memakai tanda tangan tersebut. Itu butuh build yang deterministik dan perlu dirancang sejak awal. Jika terlalu berat untuk v1, terima saja bahwa pengguna disarankan memakai satu sumber saja.

### 6. Strategi Pengujian Sebelum Rilis

| Pengujian | Cara | Target |
|-----------|------|--------|
| Akurasi jadwal | Bandingkan hasil `PrayerTimeEngine` dengan jadwal resmi Kemenag untuk beberapa kota (barat, tengah, timur Indonesia) dan beberapa bulan berbeda | Selisih ≤ 1-2 menit |
| Keandalan alarm | Uji di HP fisik beberapa merek, termasuk yang agresif menutup aplikasi, selama minimal beberapa hari | > 95% notifikasi berbunyi tepat waktu |
| Mode Doze | Simulasi lewat `adb shell dumpsys deviceidle` | Alarm tetap berbunyi |
| Pemulihan | Restart HP, ubah zona waktu, ubah jam manual | Alarm dan widget terjadwal ulang |
| Izin ditolak | Tolak lokasi, notifikasi, dan exact alarm satu per satu | Tidak crash, alternatif muncul |
| Ukuran & baterai | Cek ukuran APK rilis dan statistik baterai harian | < 15 MB, < 1% per hari |
| Widget | Berbagai launcher dan ukuran layar, tema terang dan gelap | Tampil benar dan hitung mundur akurat |

### 7. Rencana Rilis Bertahap

1. **Alfa:** dipakai sendiri dan teman dekat, fokus pada keandalan alarm dan akurasi jadwal.
2. **Beta:** APK di GitHub Releases (pra-rilis) untuk penguji lebih luas, kumpulkan laporan lewat GitHub Issues.
3. **v1.0:** rilis stabil di GitHub, lalu ajukan ke F-Droid.

### 8. Biaya & Pemeliharaan
- **Biaya:** nyaris nol. GitHub dan F-Droid gratis, tanpa server dan tanpa biaya bulanan. (Google Play tidak dipakai, sehingga tidak ada biaya developer.)
- **Pemeliharaan rutin:** sesuaikan `targetSdk` dengan perubahan Android tahunan, tinjau ulang aturan alarm dan izin tiap versi Android baru, dan verifikasi ulang parameter Kemenag jika ada perubahan resmi.
- **Catatan jika kelak masuk Play Store:** kebijakan Google membatasi izin alarm tepat waktu untuk kategori aplikasi tertentu dan mengharuskan penjelasan penggunaan, jadi perlu dikaji ulang saat itu.
