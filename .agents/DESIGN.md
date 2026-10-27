# DESIGN.md: Design System Aplikasi Waktu Sholat

Sistem desain untuk aplikasi Android (Jetpack Compose) dan widget (RemoteViews). Mengikuti arsitektur token tiga lapis: **Primitive → Semantic → Component**. Dokumen ini melengkapi `1-PRD.md`, `2-TECH-SPEC.md`, dan `3-TASKS.md`.

---

## 1. Prinsip Desain

1. **Tenang dan tidak mengganggu.** Aplikasi dipakai saat beribadah. Tanpa iklan, tanpa popup, tanpa warna mencolok.
2. **Satu info utama per layar.** Pengguna hampir selalu hanya ingin tahu: sholat berikutnya apa dan berapa lama lagi.
3. **Terbaca sekilas.** Angka waktu besar, kontras tinggi, rata kolom (tabular) agar tidak bergeser saat berganti.
4. **Ringan.** Memakai font sistem, ikon vektor, tanpa gambar bitmap besar. Selaras dengan target APK < 15 MB.
5. **Konsisten antara aplikasi dan widget.** Warna widget berasal dari token yang sama dengan aplikasi.
6. **Mudah untuk pemula.** Sedikit pilihan di tiap layar, label jelas dalam bahasa Indonesia, area sentuh besar.

### Keputusan Desain (bisa kamu ubah)

| Keputusan | Pilihan | Alasan |
|-----------|---------|--------|
| Warna utama | Hijau tua (teal-hijau) | Kesan tenang, lazim untuk aplikasi ibadah, kontras baik di terang dan gelap |
| Warna aksen | Emas redup | Dipakai hemat untuk penanda "sholat berikutnya" |
| Dynamic color (Material You) | **Tidak dipakai di v1** | Menjaga warna aplikasi dan widget tetap sama di semua HP. Bisa jadi opsi di v2 |
| Font | Font sistem (Roboto/bawaan HP) | Tanpa berkas font tambahan, ukuran APK kecil |
| Teks Arab | Tidak ada di v1 | Tidak ada fitur Al-Qur'an atau doa (Out of Scope PRD) |

---

## 2. Arsitektur Token

```
┌──────────────────────────────────────────┐
│ Component  → PrayerRow, NextPrayerCard,  │  Per-komponen
│              Widget, Banner, ...         │
├──────────────────────────────────────────┤
│ Semantic   → primary, surface, onSurface │  Berdasarkan makna,
│              error, nextPrayer, ...      │  ganti nilai untuk gelap
├──────────────────────────────────────────┤
│ Primitive  → green-700, neutral-900,     │  Nilai mentah
│              space-4, radius-md          │
└──────────────────────────────────────────┘
```

| Lapis | Isi | Kapan berubah |
|-------|-----|---------------|
| Primitive | Warna mentah, skala spasi, ukuran huruf, radius | Jarang. Ini fondasi |
| Semantic | Makna ("warna utama", "latar kartu") | Saat ganti tema terang/gelap |
| Component | Nilai khusus tiap komponen | Saat satu komponen perlu penyesuaian |

**Aturan utama:** komponen tidak boleh memakai nilai mentah (hex atau angka dp). Komponen merujuk token semantic atau component, dan token itu merujuk primitive.

### Konvensi Penamaan

| Platform | Primitive | Semantic | Component |
|----------|-----------|----------|-----------|
| Compose (Kotlin) | `Green700` | `MaterialTheme.colorScheme.primary` | `PrayerRowTokens.highlightBg` |
| XML (widget) | `color_green_700` | `color_primary` | `widget_bg`, `widget_text_primary` |

### Pemetaan ke Berkas Proyek

```
ui/theme/
├── Primitives.kt     # warna mentah, spasi, ukuran
├── Color.kt          # LightColors, DarkColors (semantic -> colorScheme)
├── Type.kt           # Typography
├── Shape.kt          # Shapes
├── Dimens.kt         # Spacing, ukuran minimum
├── ComponentTokens.kt# token per komponen
└── Theme.kt          # WaktuSholatTheme (terang/gelap/sistem)

res/
├── values/colors.xml          # primitive + semantic (terang) + widget
├── values-night/colors.xml    # semantic + widget (gelap)
├── values/dimens.xml          # spasi dan ukuran widget
└── values-v31/dimens.xml      # radius widget mengikuti sistem (Android 12+)
```

---

## 3. Primitive Tokens

### 3.1 Warna

**Hijau (utama)**

| Token | Hex |
|-------|-----|
| green-50 | `#E8F5F0` |
| green-100 | `#C5E8DB` |
| green-200 | `#9DD8C2` |
| green-300 | `#6FC4A6` |
| green-400 | `#3FAE8B` |
| green-500 | `#1F9573` |
| green-600 | `#137A5E` |
| green-700 | `#0F6149` |
| green-800 | `#0B4A38` |
| green-900 | `#073327` |
| green-950 | `#04201A` |

**Emas (aksen)**

| Token | Hex |
|-------|-----|
| gold-100 | `#FBEFD0` |
| gold-300 | `#F2CF7A` |
| gold-500 | `#D9A21E` |
| gold-800 | `#7A5600` |
| gold-950 | `#4A3300` |

**Netral (sedikit kehijauan agar serasi)**

| Token | Hex |
|-------|-----|
| neutral-0 | `#FFFFFF` |
| neutral-50 | `#F7F8F7` |
| neutral-100 | `#EEF1EF` |
| neutral-200 | `#DDE2DF` |
| neutral-300 | `#C3CAC6` |
| neutral-400 | `#9AA39E` |
| neutral-500 | `#6F7873` |
| neutral-600 | `#535B57` |
| neutral-700 | `#3B423F` |
| neutral-800 | `#262B29` |
| neutral-900 | `#171B19` |
| neutral-950 | `#0E1110` |

**Status**

| Token | Hex |
|-------|-----|
| red-300 | `#F29C93` |
| red-600 | `#C0392B` |

### 3.2 Spasi (basis 4 dp)

| Token | Nilai |
|-------|-------|
| space-1 | 4 dp |
| space-2 | 8 dp |
| space-3 | 12 dp |
| space-4 | 16 dp |
| space-6 | 24 dp |
| space-8 | 32 dp |
| space-12 | 48 dp |

### 3.3 Tipografi (font sistem)

| Token | Ukuran | Berat | Catatan |
|-------|--------|-------|---------|
| text-display | 44 sp | Medium | Hitung mundur di layar utama, angka tabular |
| text-headline | 28 sp | Medium | Jam sholat berikutnya |
| text-title-lg | 22 sp | Medium | Judul layar |
| text-title-md | 16 sp | Medium | Judul bagian, nama sholat |
| text-body-lg | 16 sp | Regular | Teks utama, isi baris |
| text-body-md | 14 sp | Regular | Keterangan |
| text-label | 14 sp | Medium | Tombol, tab |
| text-caption | 12 sp | Regular | Batas terkecil. Tidak ada teks di bawah 12 sp |

Semua angka waktu memakai angka tabular (`fontFeatureSettings = "tnum"`) agar lebar angka sama dan tampilan tidak bergoyang saat berganti.

### 3.4 Radius, Elevasi, Durasi

| Token | Nilai |
|-------|-------|
| radius-sm | 8 dp |
| radius-md | 12 dp |
| radius-lg | 16 dp |
| radius-full | 50% (pil/bulat) |
| elevation-0 | 0 dp (default, hanya batas tipis) |
| elevation-1 | 1 dp (kartu) |
| elevation-2 | 3 dp (bottom bar, dialog) |
| duration-fast | 100 ms |
| duration-normal | 200 ms |
| duration-slow | 300 ms |

Gerak minimal. Hormati pengaturan "Hapus animasi" dari sistem: jika aktif, semua transisi menjadi 0 ms.

---

## 4. Semantic Tokens

### 4.1 Warna Semantic (terang dan gelap)

| Token | Terang | Gelap | Dipakai untuk |
|-------|--------|-------|---------------|
| `background` | neutral-50 | neutral-950 | Latar layar |
| `surface` | neutral-0 | neutral-900 | Kartu, bottom bar |
| `surfaceVariant` | neutral-100 | neutral-800 | Baris yang disorot netral, kolom pencarian |
| `onSurface` | neutral-900 | neutral-50 | Teks utama |
| `onSurfaceVariant` | neutral-600 | neutral-300 | Teks sekunder, keterangan |
| `primary` | green-700 | green-300 | Aksi utama, tab aktif, ikon aktif |
| `onPrimary` | neutral-0 | green-900 | Teks di atas `primary` |
| `primaryContainer` | green-100 | green-800 | Latar penanda sholat berikutnya |
| `onPrimaryContainer` | green-900 | green-100 | Teks di atas `primaryContainer` |
| `accent` | gold-800 | gold-300 | Aksen kecil: label "Berikutnya", ikon bintang/penanda |
| `accentContainer` | gold-100 | gold-950 | Latar label aksen |
| `onAccentContainer` | gold-950 | gold-100 | Teks di label aksen |
| `outline` | neutral-500 | neutral-400 | Batas kolom input, pegangan toggle (komponen interaktif) |
| `outlineVariant` | neutral-200 | neutral-700 | Pemisah dekoratif antarbaris |
| `error` | red-600 | red-300 | Pesan galat, peringatan keras |
| `warningContainer` | gold-100 | gold-950 | Latar banner izin (peringatan, bukan galat) |
| `onWarningContainer` | gold-950 | gold-100 | Teks di banner izin |

### 4.2 Verifikasi Kontras (WCAG)

Rasio dihitung dari nilai hex di atas. Target: teks biasa ≥ 4,5:1, teks besar dan komponen UI ≥ 3:1.

| Pasangan | Terang | Gelap | Hasil |
|----------|--------|-------|-------|
| onSurface di background | 16,34:1 | 17,83:1 | Lulus |
| onSurface di surface | 17,40:1 | 16,34:1 | Lulus |
| onSurfaceVariant di surface | 7,00:1 | 10,42:1 | Lulus |
| onSurfaceVariant di background | 6,57:1 | n/a | Lulus |
| onPrimary di primary | 7,43:1 | 6,70:1 | Lulus |
| primary di background | 6,98:1 | 9,15:1 | Lulus |
| onPrimaryContainer di primaryContainer | 10,54:1 | 7,77:1 | Lulus |
| accent di surface | 6,65:1 (gold-800) | 11,58:1 (gold-300) | Lulus |
| onAccentContainer di accentContainer | 10,40:1 | 10,40:1 | Lulus |
| error di surface | 5,44:1 | 8,24:1 | Lulus |
| outline di surface | 4,56:1 | 6,71:1 | Lulus (≥ 3:1 untuk komponen UI) |
| onSurface di surfaceVariant | 15,29:1 | 13,51:1 | Lulus |
| onSurfaceVariant di surfaceVariant | 6,15:1 | 8,62:1 | Lulus |

Catatan penting:
- **Emas terang (gold-500) tidak boleh dipakai untuk teks** di latar putih. Rasionya tidak memenuhi syarat. Teks emas di tema terang memakai gold-800.
- `outlineVariant` hanya untuk pemisah dekoratif, bukan untuk batas komponen yang perlu dikenali (kontrasnya rendah secara sengaja).
- Informasi tidak boleh hanya disampaikan lewat warna. Sholat berikutnya ditandai warna **dan** label teks "Berikutnya".

### 4.3 Spasi dan Ukuran Semantic

| Token | Nilai | Dipakai untuk |
|-------|-------|---------------|
| `spacing-screen` | space-4 (16 dp) | Padding tepi layar |
| `spacing-section` | space-6 (24 dp) | Jarak antarbagian |
| `spacing-component` | space-3 (12 dp) | Jarak antarkomponen |
| `spacing-inline` | space-2 (8 dp) | Jarak ikon dan teks |
| `touch-target-min` | 48 dp | Tinggi/lebar minimum elemen yang bisa disentuh |

---

## 5. Component Tokens dan Spesifikasi

### 5.1 Daftar Token Komponen

| Komponen | Token | Merujuk ke |
|----------|-------|------------|
| **PrayerRow** | `prayerRow.bg` | surface |
| | `prayerRow.bg.next` | primaryContainer |
| | `prayerRow.fg` | onSurface |
| | `prayerRow.fg.next` | onPrimaryContainer |
| | `prayerRow.fg.past` | onSurfaceVariant |
| | `prayerRow.height` | 56 dp (≥ touch-target-min) |
| | `prayerRow.padding` | space-4 |
| | `prayerRow.radius` | radius-md |
| **NextPrayerCard** | `nextCard.bg` | primary |
| | `nextCard.fg` | onPrimary |
| | `nextCard.radius` | radius-lg |
| | `nextCard.padding` | space-6 |
| **NextBadge** (label "Berikutnya") | `badge.bg` | accentContainer |
| | `badge.fg` | onAccentContainer |
| | `badge.radius` | radius-full |
| **PermissionBanner** | `banner.bg` | warningContainer |
| | `banner.fg` | onWarningContainer |
| | `banner.radius` | radius-md |
| **BottomBar** | `bottomBar.bg` | surface |
| | `bottomBar.icon.active` | primary |
| | `bottomBar.icon.inactive` | onSurfaceVariant |
| | `bottomBar.indicator` | primaryContainer |
| **SettingsItem** | `settingsItem.height.min` | 56 dp |
| | `settingsItem.divider` | outlineVariant |
| **SearchField** | `search.bg` | surfaceVariant |
| | `search.border.focus` | primary |
| | `search.radius` | radius-full |
| **Widget** | `widget.bg` | surface |
| | `widget.text.primary` | onSurface |
| | `widget.text.secondary` | onSurfaceVariant |
| | `widget.highlight.bg` | primaryContainer |
| | `widget.highlight.fg` | onPrimaryContainer |

### 5.2 Spesifikasi Komponen

#### NextPrayerCard (layar utama, bagian atas)
Kartu besar berisi: label "Berikutnya" (NextBadge), nama sholat (`text-title-lg`), jam (`text-headline`), hitung mundur (`text-display`, tabular), serta nama lokasi dan tanggal Masehi + Hijriah di bawahnya (`text-body-md`).

| Properti | Default | Disentuh | Dinonaktifkan |
|----------|---------|----------|---------------|
| Latar | primary | primary | - |
| Teks | onPrimary | onPrimary | - |
| Bentuk | radius-lg | sama | - |

Kartu bersifat informasi, bukan tombol, jadi tidak punya status hover/pressed.

#### PrayerRow (daftar 7 waktu)

| Properti | Sudah lewat | Sedang/berikutnya | Akan datang |
|----------|-------------|-------------------|-------------|
| Latar | surface | primaryContainer | surface |
| Teks | onSurfaceVariant | onPrimaryContainer (tebal) | onSurface |
| Penanda tambahan | - | NextBadge "Berikutnya" | - |
| Ikon lonceng | sesuai pengaturan | sesuai pengaturan | sesuai pengaturan |

Ikon lonceng menunjukkan notifikasi aktif atau mati untuk waktu itu. Bentuk ikon berbeda (lonceng vs lonceng dicoret), bukan hanya warna.

#### Tombol

| Varian | Latar | Teks | Dipakai untuk |
|--------|-------|------|---------------|
| Primary (filled) | primary | onPrimary | Aksi utama (Lanjut, Gunakan lokasi saya) |
| Secondary (tonal) | primaryContainer | onPrimaryContainer | Aksi pendamping (Pilih kota manual) |
| Text | transparan | primary | Aksi ringan (Lewati) |

| Status | Perubahan |
|--------|-----------|
| Default | Sesuai tabel |
| Pressed | Lapisan state 12% dari warna teks |
| Focused | Cincin fokus 2 dp warna `primary` |
| Disabled | Latar onSurface 12%, teks onSurface 38% |

Tinggi minimum 48 dp, padding horizontal space-6, bentuk radius-full.

#### Toggle (Switch)
Track aktif `primary`, thumb `onPrimary`. Track nonaktif `surfaceVariant` dengan batas `outline`, thumb `outline`. Area sentuh minimum 48 dp, seluruh baris pengaturan ikut menjadi area sentuh.

#### PermissionBanner
Di atas daftar sholat. Berisi ikon, satu kalimat penjelasan, dan satu tombol teks (Text button) menuju layar izin. Latar `warningContainer`, bukan merah, karena ini peringatan yang bisa diperbaiki, bukan galat. Hilang otomatis begitu izin diberikan.

#### BottomBar (3 tujuan: Harian, Bulanan, Pengaturan)
Ikon + label selalu terlihat (tidak hanya ikon). Tujuan aktif memakai indikator `primaryContainer` dan ikon `primary`.

#### SearchField (Cari Kota)
Latar `surfaceVariant`, bentuk pil. Saat fokus muncul batas 2 dp `primary`. Hasil pencarian: baris dengan nama kota (`text-body-lg`) dan provinsi (`text-body-md`, `onSurfaceVariant`), tinggi minimum 56 dp.

#### Onboarding
Satu pesan per layar: judul (`text-title-lg`), satu paragraf pendek (`text-body-lg`), ilustrasi ikon vektor sederhana, indikator langkah (4 titik), dan tombol Primary di bawah. Tombol "Lewati" (Text button) untuk langkah izin.

---

## 6. Widget (RemoteViews)

RemoteViews hanya mendukung sebagian view, jadi widget tidak memakai Compose. Token diterjemahkan ke XML.

### 6.1 Token Widget di XML

```xml
<!-- res/values/colors.xml (terang) -->
<color name="widget_bg">@color/color_surface</color>
<color name="widget_text_primary">@color/color_on_surface</color>
<color name="widget_text_secondary">@color/color_on_surface_variant</color>
<color name="widget_highlight_bg">@color/color_primary_container</color>
<color name="widget_highlight_fg">@color/color_on_primary_container</color>

<!-- res/values-night/colors.xml (gelap): semantic gelap, nama widget_* sama -->
```

Widget memakai nama `widget_*` yang sama di `values` dan `values-night`, sehingga otomatis mengikuti tema sistem. Untuk pilihan tema manual (T-43), `WidgetUpdater` memilih set warna terang atau gelap secara eksplisit saat merender.

### 6.2 Widget Kecil (NextPrayerWidget)

| Elemen | Token | Ukuran |
|--------|-------|--------|
| Latar | widget.bg | radius mengikuti sistem |
| Label "Berikutnya" | widget.text.secondary | text-caption (12 sp) |
| Nama sholat | widget.text.primary | text-title-md (16 sp) |
| Jam | widget.text.primary | text-headline (28 sp) |
| Hitung mundur (Chronometer) | widget.text.secondary | text-body-lg (16 sp) |
| Padding | space-3 (12 dp) | - |

Ukuran minimum 2×1 sel. Seluruh widget bisa disentuh untuk membuka aplikasi.

### 6.3 Widget Besar (DailyScheduleWidget)

| Elemen | Token | Ukuran |
|--------|-------|--------|
| Latar | widget.bg | radius mengikuti sistem |
| Judul (lokasi + tanggal) | widget.text.secondary | text-caption (12 sp) |
| Baris sholat | widget.text.primary | text-body-lg (16 sp) |
| Baris berikutnya | widget.highlight.bg / widget.highlight.fg | radius-sm, teks tebal |
| Tinggi baris | 32 dp minimum | - |

Ukuran minimum 4×2 sel dan bisa diubah ukurannya. Pada ukuran terkecil, Imsak dan Terbit boleh disembunyikan agar 5 waktu wajib tetap terbaca tanpa terpotong.

### 6.4 Radius Widget
- Android 12+ (API 31): pakai radius sistem agar sudut widget seragam dengan widget lain (`values-v31/dimens.xml`, merujuk dimen sistem untuk radius latar widget).
- Di bawah Android 12: `radius-lg` (16 dp).
- Verifikasi nama dimen sistem pada saat implementasi T-33.

### 6.5 Kondisi Khusus Widget
- **Lokasi belum diatur:** teks "Atur lokasi" (`widget.text.primary`) dan tap membuka aplikasi.
- **Gagal menghitung:** pesan singkat "Jadwal tidak tersedia", tanpa angka.
- **Hitung mundur habis sebelum pembaruan:** tampilkan "Sekarang", bukan angka negatif.

---

## 7. Tema Terang, Gelap, dan Sistem

| Pengaturan | Perilaku |
|------------|----------|
| Ikuti sistem (default) | Aplikasi dan widget mengikuti tema HP |
| Terang | Aplikasi dan widget memakai semantic terang |
| Gelap | Aplikasi dan widget memakai semantic gelap |

- Pergantian tema memicu pembaruan widget agar warnanya ikut berubah (T-43).
- Tema gelap memakai permukaan netral gelap, bukan hitam murni, agar teks tidak terasa menyilaukan. Latar `background` neutral-950 sengaja sangat gelap untuk menghemat daya di layar AMOLED.
- Warna tidak dihitung di komponen. Semua berasal dari `colorScheme` (Compose) atau resource `widget_*` (XML).

---

## 8. Aksesibilitas

| Aspek | Aturan |
|-------|--------|
| Kontras | Semua pasangan teks/latar memenuhi tabel 4.2 |
| Area sentuh | Minimum 48 × 48 dp |
| Ukuran teks | Memakai `sp`, mengikuti pengaturan ukuran font sistem. Tata letak tidak boleh memotong teks pada skala font terbesar |
| Pembaca layar | Semua ikon tanpa teks punya `contentDescription`. Hitung mundur dibacakan sebagai kalimat ("Subuh dalam 1 jam 12 menit") |
| Selain warna | Status tidak hanya dari warna: ada label teks ("Berikutnya") dan bentuk ikon berbeda |
| Gerak | Hormati pengaturan "Hapus animasi" |
| Fokus | Cincin fokus terlihat untuk navigasi keyboard/switch |
| Peringatan | Banner izin memakai `liveRegion` agar perubahan dibacakan |

---

## 9. Aturan Kepatuhan Token

1. **Dilarang** memakai hex langsung di Composable atau layout XML. Selalu rujuk token.
2. **Dilarang** memakai angka dp/sp langsung untuk spasi, radius, atau ukuran huruf. Pakai `Dimens`, `Shapes`, atau `Typography`.
3. Komponen baru wajib mendefinisikan token komponennya di `ComponentTokens.kt` (atau `colors.xml` untuk widget) sebelum dipakai.
4. Setiap token semantic baru wajib punya nilai terang **dan** gelap, serta lolos pemeriksaan kontras.
5. Perubahan warna dilakukan di lapis primitive atau semantic, tidak di komponen.
6. Gunakan Android Lint dan pencarian teks (`grep "#[0-9A-Fa-f]{6}"`) pada tinjauan kode untuk menangkap nilai yang ditulis langsung.

---

## 10. Kaitan dengan Daftar Task

| Task | Kaitan dengan dokumen ini |
|------|---------------------------|
| T-06 | Buat `Primitives.kt`, `Color.kt`, `Type.kt`, `Shape.kt`, `Dimens.kt`, `Theme.kt` berdasarkan bagian 3 dan 4 |
| T-16 | NextPrayerCard, PrayerRow, NextBadge (bagian 5) |
| T-28 | PermissionBanner (bagian 5) |
| T-33 sampai T-36 | Widget kecil dan besar, warna `widget_*`, radius (bagian 6) |
| T-43 | Pemilihan tema dan penerapannya ke widget (bagian 7) |
| T-46 | Onboarding (bagian 5) |
| T-59 | Uji aksesibilitas memakai bagian 8 sebagai daftar periksa |

---

## 11. Hal yang Perlu Diputuskan atau Diverifikasi

- **Palet warna** di dokumen ini adalah usulan. Nilainya mudah diganti karena hanya di lapis primitive.
- **Nama aplikasi dan ikon peluncur** belum didefinisikan. Ikon perlu dibuat dengan warna dari palet ini (T-61 membutuhkannya untuk F-Droid).
- **Nama dimen sistem untuk radius widget** (Android 12+) diverifikasi saat implementasi widget.
- **Dynamic color** ditunda ke v2. Jika kelak dipakai, cukup mengganti sumber `colorScheme` di lapis semantic.
