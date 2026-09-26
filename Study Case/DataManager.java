import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class DataManager
 * Menangani Data Persistence:
 * 1. File master_stok_barang.txt (stok berkurang saat transaksi sukses)
 * 2. File rekap_transaksi_kasir.txt (mencatat histori transaksi kasir)
 */
public class DataManager {
    private static final String FILE_MASTER_STOK = "master_stok_barang.txt";
    private static final String FILE_REKAP_TRANSAKSI = "rekap_transaksi_kasir.txt";

    private List<Barang> daftarBarangMaster;
    private Map<String, Barang> barcodeMap;

    public DataManager() {
        daftarBarangMaster = new ArrayList<>();
        barcodeMap = new HashMap<>();
        muatDataMaster();
    }

    /**
     * Memuat data master barang dari file master_stok_barang.txt.
     * Jika file belum ada, sistem akan membuatkan file contoh barang Kopma Mart.
     */
    public void muatDataMaster() {
        daftarBarangMaster.clear();
        barcodeMap.clear();

        File file = new File(FILE_MASTER_STOK);
        if (!file.exists()) {
            inisialisasiDataAwal();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Barang b = Barang.fromFileString(line);
                if (b != null) {
                    daftarBarangMaster.add(b);
                    barcodeMap.put(b.getBarcodeId().toLowerCase(), b);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca master stok: " + e.getMessage());
        }

        // Jika file ada tapi kosong, isi data awal
        if (daftarBarangMaster.isEmpty()) {
            inisialisasiDataAwal();
        }
    }

    /**
     * Membuat data awal bawaan jika file belum tersedia
     */
    private void inisialisasiDataAwal() {
        daftarBarangMaster.clear();
        barcodeMap.clear();

        // Koleksi barang khas minimarket koperasi mahasiswa (Barcode pendek mudah ditest)
        tambahBarangMaster(new Barang("101", "Indomie Goreng Spesial", "Makanan", 3500, 50));
        tambahBarangMaster(new Barang("102", "Aqua Botol 600ml", "Minuman", 4000, 40));
        tambahBarangMaster(new Barang("103", "Ultra Milk Cokelat 250ml", "Minuman", 6500, 30));
        tambahBarangMaster(new Barang("104", "Chitato Sapi Panggang 68g", "Makanan", 11500, 20));
        tambahBarangMaster(new Barang("105", "Roti Aoka Panggang Cokelat", "Makanan", 3000, 25));
        tambahBarangMaster(new Barang("106", "Buku Tulis Sinar Dunia 38lbr", "ATK", 4500, 35));
        tambahBarangMaster(new Barang("107", "Pulpen Standard AE7 Hitam", "ATK", 3000, 45));
        tambahBarangMaster(new Barang("108", "Kopi Good Day Cappuccino Botol", "Minuman", 7000, 25));
        tambahBarangMaster(new Barang("109", "Paseo Facial Tissue 50s", "Perlengkapan", 6000, 15));
        tambahBarangMaster(new Barang("110", "Teh Pucuk Harum 350ml", "Minuman", 4000, 40));
        tambahBarangMaster(new Barang("8992761136015", "Oreo Vanila Roll 133g", "Makanan", 9500, 15));
        tambahBarangMaster(new Barang("8999999001234", "Pocari Sweat Can 330ml", "Minuman", 7500, 20));

        simpanMasterStok();
    }

    private void tambahBarangMaster(Barang b) {
        daftarBarangMaster.add(b);
        barcodeMap.put(b.getBarcodeId().toLowerCase(), b);
    }

    /**
     * Menyimpan seluruh list master barang kembali ke file master_stok_barang.txt
     */
    public synchronized boolean simpanMasterStok() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FILE_MASTER_STOK), StandardCharsets.UTF_8))) {
            writer.println("# Format: barcodeId|namaBarang|kategori|hargaJual|stokGudang");
            for (Barang b : daftarBarangMaster) {
                writer.println(b.toFileString());
            }
            return true;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan master stok: " + e.getMessage());
            return false;
        }
    }

    /**
     * Menyimpan riwayat transaksi ke file rekap_transaksi_kasir.txt
     */
    public synchronized boolean simpanRekapTransaksi(TransaksiPenjualan transaksi) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FILE_REKAP_TRANSAKSI, true), StandardCharsets.UTF_8))) {
            writer.println(transaksi.toRekapString());
            return true;
        } catch (IOException e) {
            System.err.println("Gagal mencatat rekap transaksi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mencari barang berdasarkan kode barcode
     */
    public Barang cariBarang(String barcodeId) {
        if (barcodeId == null) return null;
        return barcodeMap.get(barcodeId.trim().toLowerCase());
    }

    public List<Barang> getDaftarBarangMaster() {
        return daftarBarangMaster;
    }
}
