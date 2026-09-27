# Studi_Kasus_Java
# 🛒 Kopma Mart (Koperasi Mahasiswa) POS – Kasir Cepat Minimarket Kampus

Aplikasi Point of Sale (POS) berbasis **Java SE & Swing GUI** yang dirancang khusus untuk memfasilitasi transaksi kasir minimarket kampus yang cepat, otomatisasi pengurangan stok gudang, dan pencarian instan berbasis barcode.

---

## 📌 Fitur & Pemenuhan Spesifikasi Studi Kasus

1. **Rancangan Berorientasi Objek (OOP):**
   - **`Barang`**: Menyimpan atribut `barcodeId`, `namaBarang`, `kategori`, `hargaJual`, dan `stokGudang`, serta method `kurangiStok()` dan `tambahStok()`.
   - **`ItemBelanja`**: Representasi item belanjaan (produk, kuantitas, dan kalkulasi subtotal).
   - **`KeranjangBelanja`**: Mengelola `daftarItem` belanja, memiliki method `tambahItem()`, `hapusItem()`, `hitungSubtotal()`, dan validasi stok.
   - **`TransaksiPenjualan`**: Menyimpan data nota berupa `noNota`, `waktu`, `totalBayar`, `nominalTunai`, `kembalian`, serta generator cetak struk kasir dan string rekap.
   - **`DataManager`**: Mengelola pembacaan dan penyimpanan file persisten (`master_stok_barang.txt` dan `rekap_transaksi_kasir.txt`).

2. **Antarmuka GUI (Modern Java Swing):**
   - **Input Barcode Cepat**: Kolom barcode dilengkapi dengan *Action Listener* tombol **ENTER**. Kasir cukup mengetik barcode lalu menekan Enter tanpa perlu menyentuh mouse.
   - **Panel Ringkasan Total Bayar**: Menggunakan huruf berukuran besar (**Font Size 28**) dengan kontras tinggi sehingga sangat mudah dibaca oleh kasir maupun pembeli.
   - **Validasi Stok Otomatis**: Memunculkan dialog peringatan jika pembelian suatu produk melebihi batas stok gudang yang tersedia.
   - **Kalkulasi Kembalian Live**: Menghitung uang kembalian secara otomatis saat kasir mengetik nominal tunai atau menekan tombol pecahan cepat.
   - **Dialog Cetak Struk (Thermal Receipt)**: Menampilkan struk belanja berformat kasir minimarket lengkap setelah pembayaran berhasil.

3. **Data Persistence:**
   - **`master_stok_barang.txt`**: File teks penyimpan data produk & stok gudang. Stok otomatis berkurang setiap kali transaksi sukses diselesaikan.
   - **`rekap_transaksi_kasir.txt`**: File histori yang mencatat seluruh transaksi kasir yang telah berhasil diproses.

4. **WOW Factor Demo Expo:**
   - Barcode Scanner seketika memasukkan item ke keranjang dan memperbarui total bayar.
   - Terdapat tab **"Live Monitor Stok Gudang"** yang menampilkan stok barang secara *real-time*, sehingga audiens/penguji dapat melihat angka stok berkurang seketika setelah pembayaran.
   - Tersedia tombol cepat *Barcode Demo Chips* (misal: 101 Indomie, 102 Aqua, 103 Ultra Milk) untuk demonstrasi instan saat expo.

---

## 🚀 Cara Menjalankan Program

### Cara 1: Menggunakan File Batch (Paling Praktis)
Cukup klik dua kali file **`run.bat`** di File Explorer.

### Cara 2: Melalui Terminal / Command Prompt
Buka terminal di direktori ini, lalu jalankan:

```bash
# 1. Kompilasi seluruh file Java
javac -encoding UTF-8 *.java

# 2. Jalankan aplikasi
java KopmaMartApp
```

*(Atau jalankan `java Main`)*

---

## ⚡ Daftar Barcode Cepat untuk Demo Expo

| Barcode | Nama Produk | Kategori | Harga Satuan | Stok Awal |
| :---: | :--- | :---: | :---: | :---: |
| **`101`** | Indomie Goreng Spesial | Makanan | Rp 3.500 | 50 pcs |
| **`102`** | Aqua Botol 600ml | Minuman | Rp 4.000 | 40 pcs |
| **`103`** | Ultra Milk Cokelat 250ml | Minuman | Rp 6.500 | 30 pcs |
| **`104`** | Chitato Sapi Panggang 68g | Makanan | Rp 11.500 | 20 pcs |
| **`105`** | Roti Aoka Panggang Cokelat | Makanan | Rp 3.000 | 25 pcs |
| **`106`** | Buku Tulis Sinar Dunia 38lbr | ATK | Rp 4.500 | 35 pcs |
| **`107`** | Pulpen Standard AE7 Hitam | ATK | Rp 3.000 | 45 pcs |
| **`108`** | Kopi Good Day Cappuccino Botol | Minuman | Rp 7.000 | 25 pcs |
| **`109`** | Paseo Facial Tissue 50s | Perlengkapan | Rp 6.000 | 15 pcs |
| **`110`** | Teh Pucuk Harum 350ml | Minuman | Rp 4.000 | 40 pcs |

---

## ⌨️ Shortcut Keyboard

- **`ENTER`** pada kolom Barcode: Otomatis mencari & menambahkan barang ke keranjang.
- **`F8`**: Langsung memindahkan kursor ke kolom Nominal Tunai.
- **`F9`** atau **`ENTER`** pada kolom Tunai: Memproses pembayaran dan mencetak nota.
- **`ESC`**: Mengembalikan fokus ke kolom input Barcode.
