# Sumber Data Pihak Ketiga

Seluruh data dibundel ke dalam aplikasi; aplikasi tidak mengunduh data saat runtime.

## Database kota (`app/src/main/assets/cities.db`)

- **Cakupan:** 514 kabupaten/kota di Indonesia (level ADM2), lengkap dengan lintang, bujur, dan zona waktu IANA.
- **Koordinat dan zona waktu:** [GeoNames](https://www.geonames.org/) — dump `ID.zip`, kelas fitur `ADM2`, diakses 27 Oktober 2026. Lisensi **Creative Commons Attribution 4.0 (CC BY 4.0)**.
- **Nama resmi dan provinsi:** [idn-area-data](https://github.com/fityannugroho/idn-area-data) oleh Fityan Nugroho, berkas `data/regencies.csv` dan `data/provinces.csv`, diakses 27 Oktober 2026. Data berlisensi **Open Database License 1.0 (ODbL)**.
- **Penyesuaian:** 13 kota dengan nama lama/ejaan berbeda dipetakan manual (misalnya Kabupaten Toba Samosir → Kabupaten Toba, Kota Baubau → Kota Bau Bau). Zona `Asia/Pontianak` disatukan ke `Asia/Jakarta` karena keduanya UTC+7 dan aplikasi hanya memakai tiga zona Indonesia (WIB/WITA/WIT).
- **Atribusi yang ditampilkan:** tercantum di layar Tentang & Privasi.

## Suara adzan (`app/src/main/res/raw/`)

- **Sumber:** [File:Beautiful adhan.ogg](https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg) di Wikimedia Commons, karya **Adam-synagda**, diakses 28 Oktober 2026. Lisensi **CC0 1.0 (Public Domain Dedication)** — boleh dipakai, diubah, dan didistribusikan tanpa syarat atribusi.
- **Penyesuaian:** `adzan_full.ogg` di-encode ulang ke mono 48 kbps (592 KB). `adzan_short.ogg` adalah potongan 25 detik pertama dengan fade-in/out, mono 48 kbps (105 KB). Keduanya format OGG Vorbis agar ukuran APK tetap kecil.
- **Kesesuaian lisensi:** CC0 kompatibel penuh dengan GPL-3.0 dan syarat F-Droid.

## Data acuan jadwal Kemenag (khusus pengujian)

- **Sumber:** jadwal Kemenag RI (SIHAT) melalui [api.myquran.com](https://api.myquran.com/), diakses 27 Oktober 2026. Dipakai hanya sebagai pembanding di `app/src/test/resources/kemenag_reference.csv`.
- **Cakupan:** Medan, Yogyakarta, Makassar, dan Jayapura untuk 4 tanggal di Januari dan Juli 2026, mencakup WIB, WITA, dan WIT.
- **Toleransi pengujian:** selisih maksimum 2 menit dari jadwal acuan.

## Catatan lisensi

Aplikasi ini berlisensi GPL-3.0. Data kota tetap tunduk pada lisensinya masing-masing (CC BY 4.0 dan ODbL 1.0); atribusi disertakan di aplikasi dan dokumen ini. Tidak ada data dari Google Play Services, Firebase, atau layanan tertutup lainnya.
