/**
 * Class Barang
 * Merepresentasikan data produk di minimarket Kopma Mart.
 * Sesuai spesifikasi OOP Studi Kasus:
 * - barcodeId (String)
 * - namaBarang (String)
 * - kategori (String)
 * - hargaJual (double)
 * - stokGudang (int)
 */
public class Barang {
    private String barcodeId;
    private String namaBarang;
    private String kategori;
    private double hargaJual;
    private int stokGudang;

    public Barang(String barcodeId, String namaBarang, String kategori, double hargaJual, int stokGudang) {
        this.barcodeId = barcodeId;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.hargaJual = hargaJual;
        this.stokGudang = stokGudang;
    }

    // Getters and Setters
    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getStokGudang() {
        return stokGudang;
    }

    public void setStokGudang(int stokGudang) {
        this.stokGudang = stokGudang;
    }

    /**
     * Mengurangi stok gudang saat barang terjual
     * @param jumlah jumlah yang dikurangi
     * @return true jika stok mencukupi dan berhasil dikurangi
     */
    public boolean kurangiStok(int jumlah) {
        if (this.stokGudang >= jumlah) {
            this.stokGudang -= jumlah;
            return true;
        }
        return false;
    }

    /**
     * Menambah stok gudang (untuk restock atau pembatalan)
     */
    public void tambahStok(int jumlah) {
        this.stokGudang += jumlah;
    }

    /**
     * Konversi ke baris format teks master_stok_barang.txt
     * Format: barcodeId|namaBarang|kategori|hargaJual|stokGudang
     */
    public String toFileString() {
        return barcodeId + "|" + namaBarang + "|" + kategori + "|" + (long)hargaJual + "|" + stokGudang;
    }

    /**
     * Parsing baris dari file master_stok_barang.txt
     */
    public static Barang fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            String barcodeId = parts[0].trim();
            String namaBarang = parts[1].trim();
            String kategori = parts[2].trim();
            double hargaJual = Double.parseDouble(parts[3].trim());
            int stokGudang = Integer.parseInt(parts[4].trim());
            return new Barang(barcodeId, namaBarang, kategori, hargaJual, stokGudang);
        }
        return null;
    }

    @Override
    public String toString() {
        return "[" + barcodeId + "] " + namaBarang + " (" + kategori + ") - Rp " + String.format("%,.0f", hargaJual) + " | Stok: " + stokGudang;
    }
}
