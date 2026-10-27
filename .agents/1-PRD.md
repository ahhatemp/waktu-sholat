# PRD: Aplikasi Waktu Sholat (Gratis, Offline, Widget Android)

---

## BAGIAN 1: Visi & Tujuan Produk

### Visi Produk
Menjadi aplikasi waktu sholat yang bisa dipercaya semua muslim di Indonesia: ringan dan gratis sepenuhnya, tanpa iklan, tanpa paywall. Aplikasi tetap bekerja tanpa internet, dan widget di layar utama Android selalu menampilkan waktu sholat berikutnya tanpa perlu membuka aplikasi. Fitur dasar yang dibutuhkan ibadah sehari-hari tidak seharusnya dikunci di balik langganan.

### Tujuan Utama
1. **Waktu sholat akurat tanpa internet** - Jadwal 5 waktu (plus imsak dan terbit) tampil benar setelah setup awal, dan selisih dengan acuan Kemenag RI tidak lebih dari 1-2 menit.
2. **Widget gratis dan andal** - Widget Android menampilkan waktu sholat berikutnya beserta hitung mundur, tetap terbarui sepanjang hari, dan tersedia untuk semua pengguna tanpa premium.
3. **Notifikasi adzan tepat waktu** - Pengingat berbunyi tepat waktu, bahkan saat HP dalam mode hemat baterai, dengan tingkat keberhasilan notifikasi di atas 95%.
4. **Ringan dan hemat sumber daya** - Ukuran instalasi kecil (target < 15 MB), RAM rendah, dan konsumsi baterai di bawah 1% per hari, sehingga nyaman dipakai di HP spek rendah.
5. **Nol iklan, nol paywall** - Tidak ada iklan atau pelacak pihak ketiga dan tidak ada fitur berbayar, yang bisa dibuktikan dari isi aplikasi dan kebijakan privasinya.

### Value Proposition
- **Gratis sepenuhnya:** widget dan semua fitur inti terbuka untuk semua orang, tanpa premium.
- **Bersih dari iklan:** tidak ada iklan, popup, atau pelacak yang mengganggu saat beribadah.
- **Offline-first dan ringan:** jadwal dihitung langsung di perangkat, jadi tidak bergantung internet dan tetap lancar di HP lama.

---

## BAGIAN 2: User Persona

### Persona 1: Rizky
- **Usia/Pekerjaan:** 22 tahun, mahasiswa perantau
- **Level Teknis:** Menengah
- **Tujuan:** Tidak ketinggalan waktu sholat di sela kuliah dan kegiatan kampus, tanpa harus membuka aplikasi berulang kali.
- **Pain Points:**
  - Kuota terbatas, sehingga aplikasi yang butuh internet menyulitkan.
  - HP spek menengah ke bawah jadi lambat dan cepat panas karena aplikasi yang berat.
  - Aplikasi lain menampilkan iklan, dan widget-nya baru bisa dipakai kalau berlangganan premium.
- **Motivasi:** Ingin aplikasi ringan dan gratis yang widget-nya langsung terlihat di layar utama, dan tetap jalan di kos yang sinyalnya jelek.

### Persona 2: Bu Siti
- **Usia/Pekerjaan:** 45 tahun, karyawan swasta dan ibu rumah tangga
- **Level Teknis:** Pemula
- **Tujuan:** Mendapat pengingat adzan yang tepat waktu dan mudah dipahami, tanpa pengaturan yang rumit.
- **Pain Points:**
  - Bingung dengan menu dan pengaturan yang terlalu banyak.
  - Iklan dan popup mengganggu, apalagi saat sedang beribadah.
  - Pernah melewatkan notifikasi karena aplikasi dimatikan otomatis oleh sistem HP.
- **Motivasi:** Ingin aplikasi sederhana yang cukup dipasang sekali: lokasi terdeteksi, jadwal muncul, dan adzan berbunyi sendiri setiap hari.

---

## BAGIAN 3: User Stories

Format: `Sebagai [peran], saya ingin [tindakan], agar [manfaat].`

### Modul 1: Lokasi & Jadwal Sholat
1. Sebagai pengguna baru, saya ingin lokasi saya terdeteksi otomatis lewat GPS, agar jadwal sholat sesuai tempat saya.
2. Sebagai pengguna tanpa GPS atau izin lokasi, saya ingin memilih kota secara manual, agar tetap mendapat jadwal yang benar.
3. Sebagai pengguna, saya ingin melihat 5 waktu sholat beserta imsak dan terbit hari ini, agar bisa merencanakan aktivitas.
4. Sebagai pengguna, saya ingin melihat jadwal sholat sebulan penuh, agar bisa merencanakan jauh hari.
5. Sebagai pengguna perantau, saya ingin aplikasi bekerja penuh tanpa internet, agar tidak bergantung pada kuota dan sinyal.

### Modul 2: Notifikasi Adzan
6. Sebagai pengguna, saya ingin notifikasi adzan berbunyi tepat waktu, agar tidak ketinggalan sholat.
7. Sebagai pengguna, saya ingin mengaktifkan atau menonaktifkan notifikasi untuk tiap waktu sholat, agar sesuai kebutuhan saya.
8. Sebagai pengguna, saya ingin pengingat beberapa menit sebelum adzan, agar punya waktu bersiap.
9. Sebagai pengguna, saya ingin notifikasi tetap berbunyi walau HP dalam mode hemat baterai, agar tidak terlewat.

### Modul 3: Widget Android
10. Sebagai pengguna, saya ingin widget yang menampilkan waktu sholat berikutnya beserta hitung mundur, agar tidak perlu membuka aplikasi.
11. Sebagai pengguna, saya ingin memilih ukuran widget (kecil atau besar yang menampilkan semua waktu), agar sesuai dengan layar utama saya.
12. Sebagai pengguna, saya ingin widget mengikuti tema terang atau gelap, agar enak dilihat.

### Modul 4: Pengaturan & Kustomisasi
13. Sebagai pengguna, saya ingin memilih metode perhitungan (misalnya Kemenag RI), agar jadwal sesuai dengan yang saya percayai.
14. Sebagai pengguna, saya ingin menyesuaikan koreksi menit per waktu sholat, agar cocok dengan jadwal masjid setempat.
15. Sebagai pengguna, saya ingin mengganti suara adzan atau memilih nada notifikasi biasa, agar sesuai dengan kenyamanan saya.

*(Total 15 user stories)*

---

## BAGIAN 4: Functional Requirements

### Modul 1: Lokasi & Jadwal Sholat

**FR-01: Deteksi Lokasi Otomatis**
- **Input:** Izin lokasi dari pengguna
- **Proses:** Ambil koordinat GPS sekali saat setup awal, lalu simpan di perangkat
- **Output:** Koordinat (lintang, bujur) dan nama kota tersimpan
- **Aturan Bisnis:** Tidak melacak lokasi terus-menerus. Lokasi diperbarui hanya jika pengguna meminta atau berpindah kota.

**FR-02: Pilih Kota Manual**
- **Input:** Nama kota atau kabupaten yang diketik pengguna
- **Proses:** Cari di database kota lokal (offline) dan ambil koordinatnya
- **Output:** Lokasi terpilih tersimpan
- **Aturan Bisnis:** Tersedia sebagai alternatif jika izin GPS ditolak. Database kota sudah disertakan di dalam aplikasi.

**FR-03: Perhitungan Waktu Sholat Offline**
- **Input:** Koordinat, tanggal, zona waktu, metode perhitungan
- **Proses:** Hitung waktu astronomis langsung di perangkat tanpa panggilan API
- **Output:** Waktu Imsak, Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya
- **Aturan Bisnis:** Metode bawaan Kemenag RI. Zona waktu otomatis (WIB/WITA/WIT).

**FR-04: Tampilan Jadwal Harian**
- **Input:** Tanggal hari ini
- **Proses:** Tampilkan semua waktu sholat dan tandai waktu yang sedang berjalan serta berikutnya
- **Output:** Layar utama dengan daftar waktu dan hitung mundur
- **Aturan Bisnis:** Hitung mundur diperbarui real-time. Tanggal Hijriah ditampilkan di samping tanggal Masehi.

**FR-05: Jadwal Bulanan**
- **Input:** Bulan dan tahun yang dipilih
- **Proses:** Hitung jadwal seluruh hari dalam bulan tersebut
- **Output:** Tabel jadwal sebulan penuh yang bisa digulir
- **Aturan Bisnis:** Bisa diakses tanpa internet. Navigasi bulan sebelumnya dan berikutnya.

### Modul 2: Notifikasi Adzan

**FR-06: Penjadwalan Alarm Adzan**
- **Input:** Jadwal sholat harian dan pengaturan notifikasi
- **Proses:** Jadwalkan alarm tepat (exact alarm) di sistem Android untuk tiap waktu sholat yang aktif
- **Output:** Notifikasi dan suara adzan berbunyi tepat waktu
- **Aturan Bisnis:** Alarm dijadwalkan ulang otomatis tiap hari dan setelah HP restart.

**FR-07: Pengaturan Notifikasi per Waktu**
- **Input:** Toggle aktif atau nonaktif untuk tiap waktu sholat
- **Proses:** Simpan preferensi dan sesuaikan alarm yang terjadwal
- **Output:** Hanya waktu yang diaktifkan yang membunyikan notifikasi
- **Aturan Bisnis:** Semua waktu wajib aktif secara default, kecuali Imsak dan Terbit.

**FR-08: Pengingat Sebelum Adzan**
- **Input:** Durasi pengingat (misalnya 5, 10, atau 15 menit)
- **Proses:** Jadwalkan notifikasi tambahan sebelum waktu sholat
- **Output:** Notifikasi pengingat "Sebentar lagi masuk waktu [sholat]"
- **Aturan Bisnis:** Opsional dan nonaktif secara default.

**FR-09: Bypass Pembatasan Baterai**
- **Input:** Persetujuan pengguna pada panduan izin
- **Proses:** Minta izin exact alarm dan pengecualian optimasi baterai, serta tampilkan panduan khusus untuk merek HP yang agresif mematikan aplikasi
- **Output:** Notifikasi tetap berbunyi dalam mode hemat baterai
- **Aturan Bisnis:** Panduan muncul saat onboarding dan bisa dibuka lagi dari pengaturan.

**FR-10: Pilihan Suara Adzan**
- **Input:** Pilihan suara (adzan penuh, adzan singkat, nada biasa, senyap/getar)
- **Proses:** Simpan pilihan dan terapkan ke kanal notifikasi
- **Output:** Notifikasi berbunyi sesuai pilihan
- **Aturan Bisnis:** Pilihan bisa berbeda untuk Subuh dan waktu lainnya. File suara sudah termasuk di aplikasi (offline).

### Modul 3: Widget Android

**FR-11: Widget Waktu Berikutnya (Kecil)**
- **Input:** Jadwal harian dan waktu saat ini
- **Proses:** Tampilkan nama dan jam sholat berikutnya beserta hitung mundur
- **Output:** Widget ukuran kecil di layar utama
- **Aturan Bisnis:** Gratis untuk semua pengguna. Otomatis berganti ke sholat berikutnya saat waktunya lewat.

**FR-12: Widget Jadwal Lengkap (Besar)**
- **Input:** Jadwal harian
- **Proses:** Tampilkan semua waktu sholat hari ini, dengan waktu berikutnya disorot
- **Output:** Widget ukuran besar di layar utama
- **Aturan Bisnis:** Gratis dan bisa diubah ukurannya.

**FR-13: Pembaruan Widget Hemat Daya**
- **Input:** Event pergantian waktu sholat dan pergantian hari
- **Proses:** Perbarui widget hanya saat diperlukan (pergantian waktu, tengah malam, perubahan zona waktu), tanpa polling terus-menerus
- **Output:** Widget selalu akurat dengan konsumsi baterai minimal
- **Aturan Bisnis:** Hitung mundur memakai komponen jam sistem agar tidak memicu pembaruan per menit.

**FR-14: Tema Widget**
- **Input:** Pengaturan tema sistem atau pilihan pengguna
- **Proses:** Terapkan warna terang atau gelap pada widget
- **Output:** Widget tampil sesuai tema
- **Aturan Bisnis:** Default mengikuti tema sistem.

### Modul 4: Pengaturan & Kustomisasi

**FR-15: Pilihan Metode Perhitungan**
- **Input:** Metode dari daftar (Kemenag RI, MWL, Umm al-Qura, dan lainnya) dan mazhab Ashar
- **Proses:** Hitung ulang jadwal dengan parameter metode terpilih
- **Output:** Jadwal diperbarui sesuai metode
- **Aturan Bisnis:** Default Kemenag RI untuk lokasi di Indonesia. Alarm dan widget ikut dijadwalkan ulang.

**FR-16: Koreksi Menit Manual**
- **Input:** Nilai koreksi per waktu sholat (-10 sampai +10 menit)
- **Proses:** Tambahkan atau kurangi menit pada hasil perhitungan
- **Output:** Jadwal dan alarm menyesuaikan koreksi
- **Aturan Bisnis:** Koreksi default 0. Ihtiyath (pembulatan ke atas) 1-2 menit bisa diaktifkan.

**FR-17: Onboarding Awal**
- **Input:** Interaksi pengguna pada langkah-langkah awal
- **Proses:** Pandu pengguna melalui izin lokasi, izin notifikasi, izin alarm, dan pengecualian baterai
- **Output:** Aplikasi siap dipakai dalam kurang dari 1 menit
- **Aturan Bisnis:** Maksimal 4 layar. Setiap izin bisa dilewati dan diatur ulang nanti.

**FR-18: Tema Aplikasi & Bahasa**
- **Input:** Pilihan tema (terang, gelap, ikuti sistem)
- **Proses:** Terapkan tema ke seluruh aplikasi
- **Output:** Tampilan aplikasi sesuai pilihan
- **Aturan Bisnis:** Bahasa utama Indonesia.

**FR-19: Cadangan & Pemulihan Pengaturan**
- **Input:** Aksi ekspor atau impor pengaturan oleh pengguna
- **Proses:** Simpan pengaturan ke berkas lokal dan baca kembali
- **Output:** Pengaturan terpulihkan saat ganti HP
- **Aturan Bisnis:** Hanya berkas lokal, tanpa akun dan tanpa server.

*(Total 19 FR)*

---

## BAGIAN 5: Non-Functional Requirements

### Performa
- Aplikasi terbuka dan menampilkan jadwal dalam < 1 detik (cold start < 2 detik di HP spek rendah)
- Perhitungan jadwal satu bulan selesai < 200 ms di perangkat
- Ukuran instalasi < 15 MB
- Pemakaian RAM saat aktif < 100 MB
- Konsumsi baterai < 1% per hari (alarm dan widget memakai mekanisme sistem, tanpa layanan latar belakang yang terus berjalan)

### Akurasi & Keandalan
- Selisih waktu sholat dengan acuan Kemenag RI tidak lebih dari 1-2 menit
- Tingkat keberhasilan notifikasi adzan > 95%, termasuk saat mode hemat baterai
- Alarm dijadwalkan ulang otomatis setelah HP restart, perubahan zona waktu, atau perubahan tanggal/jam manual
- Aplikasi berfungsi 100% tanpa internet setelah instalasi, termasuk database kota, suara adzan, dan perhitungan jadwal
- Tidak crash pada kondisi izin ditolak (GPS, notifikasi, atau alarm), dan aplikasi menampilkan alternatif yang jelas

### Keamanan & Privasi
- Tidak ada iklan, SDK iklan, atau pelacak pihak ketiga
- Tidak ada akun, login, atau server: semua data (lokasi, pengaturan) hanya tersimpan di perangkat
- Lokasi hanya diambil saat dibutuhkan dan tidak dikirim ke mana pun
- Hanya meminta izin yang benar-benar diperlukan (lokasi, notifikasi, exact alarm, pengecualian baterai)
- Kebijakan privasi singkat dan mudah dipahami tersedia di dalam aplikasi

### Kompatibilitas
- Android 8.0 (API 26) ke atas, untuk menjangkau HP lama
- Mendukung berbagai ukuran layar dan resolusi (HP kecil sampai tablet)
- Penanganan khusus untuk perilaku agresif beberapa merek HP (panduan pengaturan baterai/autostart)
- Widget kompatibel dengan launcher Android yang umum dipakai

### Usability & Aksesibilitas
- Antarmuka berbahasa Indonesia
- Dukungan tema terang dan gelap (ikuti sistem)
- Onboarding maksimal 4 layar dan selesai < 1 menit
- Ukuran teks dan tombol nyaman dibaca, mendukung pengaturan ukuran font sistem
- Label konten (content description) untuk pembaca layar seperti TalkBack
- Mudah dipakai oleh pengguna pemula tanpa perlu membaca panduan

### Maintainability
- Logika perhitungan waktu sholat dipisah dari UI agar mudah diuji
- Unit test untuk perhitungan jadwal dengan data pembanding dari Kemenag RI
- Dependensi dijaga seminimal mungkin agar ukuran kecil dan mudah dirawat

---

## BAGIAN 6: Out of Scope & Dependensi

### Out of Scope (Tidak Dikerjakan di V1)
- **Versi iOS:** V1 hanya untuk Android, ditunda ke v2.
- **Kompas arah kiblat:** butuh sensor dan kalibrasi tambahan, ditunda ke v2.
- **Al-Qur'an, doa, dan dzikir:** di luar fokus aplikasi yang ringan, ditunda ke v2.
- **Akun pengguna dan sinkronisasi cloud:** bertentangan dengan prinsip tanpa server, jadi cadangan lokal (FR-19) sudah cukup.
- **Pelacak sholat (checklist harian dan statistik):** ditunda ke v2.
- **Kalender Hijriah lengkap dan pengingat hari besar Islam:** V1 hanya menampilkan tanggal Hijriah di layar utama (FR-04).
- **Pencarian masjid terdekat:** butuh internet dan peta, ditunda.
- **Wear OS dan widget layar kunci:** ditunda ke v2.
- **Monetisasi apa pun:** iklan, premium, dan donasi dalam aplikasi tidak termasuk.

### Dependensi
- **Algoritma perhitungan waktu sholat:** library open source yang sudah teruji, atau implementasi sendiri berdasarkan parameter Kemenag RI, untuk FR-03 dan FR-15. Dipilih di Tech Spec.
- **Android AlarmManager (exact alarm):** untuk alarm adzan yang tepat waktu (FR-06).
- **Android App Widget API:** untuk widget kecil dan besar (FR-11 sampai FR-14).
- **Layanan lokasi Android:** untuk deteksi lokasi sekali saat setup (FR-01).
- **Database kota offline:** data kota/kabupaten Indonesia beserta koordinat dan zona waktu yang dibundel di dalam aplikasi (FR-02). Perlu sumber data publik yang lisensinya jelas.
- **Berkas suara adzan:** rekaman dengan lisensi yang jelas dan boleh didistribusikan gratis (FR-10).
- **Data pembanding Kemenag RI:** jadwal resmi untuk menguji akurasi perhitungan.
- **Distribusi:** akun Google Play Developer (biaya satu kali) dan/atau rilis APK lewat GitHub atau F-Droid.
- **Infrastruktur:** tidak ada server atau database cloud, sehingga biaya operasional nyaris nol.

### Asumsi
- Pengguna memakai HP Android 8.0 ke atas.
- Pengguna bersedia memberikan izin notifikasi dan alarm agar adzan berbunyi tepat waktu.
- Internet hanya dibutuhkan saat mengunduh aplikasi. Setelah itu semuanya berjalan offline.
- Metode Kemenag RI sudah sesuai untuk mayoritas pengguna di Indonesia.
- Aplikasi dikembangkan dan dirawat oleh satu orang (solo developer), sehingga cakupan v1 dijaga tetap kecil.
