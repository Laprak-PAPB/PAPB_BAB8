# Praktikum 8 - Location Services

## TUJUAN PRAKTIKUM
Setelah menyelesaikan praktikum ini, mahasiswa diharapkan mampu:
1. Mengimplementasikan Fused Location Provider dari Google Play Services.
2. Menampilkan koordinat lokasi terkini (Latitude dan Longitude).
3. Mengubah koordinat menjadi alamat yang dapat dibaca manusia (Reverse Geocoding).

---

## TUGAS DAN REFLEKSI

### 1. Screenshot Aplikasi dengan Internet dan GPS Aktif

*[Silakan jalankan aplikasi dan tambahkan screenshot di sini]*

Dalam kondisi koneksi internet aktif dan GPS aktif, aplikasi akan:
- Meminta izin akses lokasi kepada pengguna
- Mengambil koordinat lokasi terkini (Latitude dan Longitude)
- Melakukan reverse geocoding untuk mengubah koordinat menjadi alamat yang dapat dibaca
- Menampilkan lokasi lengkap dengan alamat jalan

---

### 2. Apa yang Terjadi Jika Aplikasi Dijalankan Tanpa Koneksi Internet?

**Jawaban:**

Jika aplikasi dijalankan tanpa koneksi internet, akan terjadi hal berikut:

1. **Pengambilan Koordinat Lokasi (GPS) Tetap Berfungsi**
   - Aplikasi masih dapat mengambil koordinat Latitude dan Longitude karena GPS tidak memerlukan internet
   - GPS menggunakan sinyal satelit langsung untuk menentukan posisi perangkat
   - Data koordinat akan ditampilkan dengan normal

2. **Reverse Geocoding Gagal**
   - Proses konversi koordinat menjadi alamat (reverse geocoding) akan gagal
   - Geocoder API memerlukan koneksi internet untuk mengakses database alamat Google
   - Aplikasi akan menampilkan pesan error: **"Layanan Geocoder tidak tersedia/Koneksi Internet putus."**
   - Atau: **"Alamat tidak ditemukan (Internet mungkin bermasalah)"**

3. **UI yang Ditampilkan**
   - ✅ Latitude dan Longitude tetap muncul
   - ❌ Alamat tidak dapat ditampilkan (menampilkan pesan error)

**Kesimpulan:** Tanpa internet, pengguna tetap bisa melihat koordinat lokasi mereka, tetapi tidak dapat melihat nama jalan atau alamat lengkapnya.

---

### 3. Mengapa Fungsi `getAddressFromCoordinates` Dideklarasikan sebagai `suspend` dan Dijalankan Menggunakan `withContext(Dispatchers.IO)`?

**Jawaban:**

Fungsi `getAddressFromCoordinates` dideklarasikan sebagai `suspend` dan menggunakan `withContext(Dispatchers.IO)` karena beberapa alasan penting:

#### **A. Operasi yang Bersifat Blocking dan Lambat**
- **Geocoder.getFromLocation()** adalah operasi yang memerlukan waktu lama (long-running operation)
- Proses ini melakukan:
  - Koneksi ke server Google untuk mengambil data alamat
  - Parsing data JSON dari server
  - Network I/O operations yang tidak bisa diprediksi kecepatannya
- Jika dijalankan di Main Thread (UI Thread), aplikasi akan **freeze/hang** dan memberikan pengalaman pengguna yang buruk

#### **B. Mengapa Menggunakan `suspend`?**
- Kata kunci `suspend` membuat fungsi dapat dipanggil dari coroutine
- Memungkinkan operasi berjalan secara **asynchronous** tanpa memblokir thread utama
- Fungsi dapat di-pause dan di-resume tanpa memblokir eksekusi kode lainnya
- Kompatibel dengan `viewModelScope.launch` yang digunakan di ViewModel

#### **C. Mengapa Menggunakan `withContext(Dispatchers.IO)`?**
- **Dispatchers.IO** adalah thread pool yang dioptimalkan untuk operasi Input/Output
- Thread pool ini memiliki banyak thread yang dapat menangani operasi I/O secara bersamaan
- Operasi seperti:
  - Network requests
  - Database queries
  - File operations
  - **Geocoding** (yang memerlukan network)
  
  Semuanya harus dijalankan di Dispatchers.IO, bukan di Main Thread

#### **D. Manfaat Penggunaan Pendekatan Ini**
1. **Responsiveness:** UI tetap responsif dan tidak freeze
2. **Thread Safety:** Operasi I/O berjalan di thread yang tepat
3. **Best Practice:** Sesuai dengan prinsip Android Modern Development
4. **Performance:** Mengoptimalkan penggunaan thread pool
5. **Error Handling:** Lebih mudah menangani exception dengan coroutine

#### **E. Alur Eksekusi**
```kotlin
viewModelScope.launch {                    // Berjalan di Main Thread
    val address = getAddressFromCoordinates(...) // Suspend, pindah ke IO Thread
    _addressResult.value = address         // Kembali ke Main Thread untuk update UI
}
```

**Kesimpulan:** Penggunaan `suspend` dan `withContext(Dispatchers.IO)` adalah **best practice** untuk operasi network/I/O yang memastikan aplikasi tetap responsif dan performan, sambil menjaga thread safety dan memberikan pengalaman pengguna yang baik.

---

## IMPLEMENTASI

### Dependencies yang Ditambahkan
```kotlin
implementation("com.google.android.gms:play-services-location:21.3.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
```

### Permissions yang Ditambahkan
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Komponen Utama
1. **LocationViewModel.kt** - Mengelola state dan business logic
2. **MainActivity.kt** - UI dengan Jetpack Compose
3. **LocationState** - Sealed class untuk manajemen state
4. **LocationData** - Data class untuk menyimpan informasi lokasi

---

## CARA MENJALANKAN

1. Buka proyek di Android Studio
2. Sync Gradle dependencies
3. Pastikan GPS dan Internet aktif di device/emulator
4. Run aplikasi
5. Izinkan akses lokasi saat diminta
6. Aplikasi akan menampilkan koordinat dan alamat Anda

---

**Nama:** [Nama Mahasiswa]  
**NIM:** [NIM Mahasiswa]  
**Tanggal:** 11 November 2025

