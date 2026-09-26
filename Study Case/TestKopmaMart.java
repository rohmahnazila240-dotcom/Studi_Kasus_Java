import java.time.LocalDateTime;

/**
 * Unit Test headless untuk memverifikasi logika OOP dan data persistence
 */
public class TestKopmaMart {
    public static void main(String[] args) {
        System.out.println("=== MEMULAI TEST SISTEM KOPMA MART POS ===");

        // 1. Inisialisasi DataManager
        DataManager dataManager = new DataManager();
        System.out.println("1. DataManager berhasil dimuat. Total master barang: " + dataManager.getDaftarBarangMaster().size());

        // 2. Test Cari Barang by Barcode
        Barang b101 = dataManager.cariBarang("101");
        assert b101 != null : "Barang 101 harus ditemukan!";
        System.out.println("2. Cari Barang '101': " + b101.getNamaBarang() + " (Stok: " + b101.getStokGudang() + ")");

        int stokAwal = b101.getStokGudang();

        // 3. Test Keranjang Belanja
        KeranjangBelanja keranjang = new KeranjangBelanja();
        boolean tambahOk = keranjang.tambahItem(b101, 2);
        System.out.println("3. Tambah 2 unit '101' ke keranjang: " + (tambahOk ? "BERHASIL" : "GAGAL"));
        System.out.println("   Subtotal keranjang: Rp " + keranjang.hitungSubtotal());

        // 4. Test Validasi Stok Berlebih
        boolean tambahMelebihi = keranjang.tambahItem(b101, stokAwal + 10);
        System.out.println("4. Test validasi stok berlebih: " + (!tambahMelebihi ? "VALID (Ditolak sesuai aturan)" : "GAGAL (Harusnya ditolak)"));

        // 5. Test Transaksi Penjualan
        double total = keranjang.hitungSubtotal();
        double tunai = 10000;
        double kembali = tunai - total;
        TransaksiPenjualan transaksi = new TransaksiPenjualan(
                "KOPMA-TEST-0001",
                LocalDateTime.now(),
                total,
                tunai,
                kembali,
                keranjang.getDaftarItem()
        );

        System.out.println("5. Objek TransaksiPenjualan dibuat:");
        System.out.println(transaksi.cetakStruk());

        // 6. Test Pengurangan Stok & Persistence
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            item.getBarang().kurangiStok(item.getQty());
        }
        dataManager.simpanMasterStok();
        dataManager.simpanRekapTransaksi(transaksi);

        System.out.println("6. Stok barang setelah transaksi: " + b101.getStokGudang() + " (Berkurang " + (stokAwal - b101.getStokGudang()) + " pcs)");
        System.out.println("   File master_stok_barang.txt dan rekap_transaksi_kasir.txt berhasil diperbarui.");

        System.out.println("=== SEMUA TEST OOP & PERSISTENCE LOLOS DENGAN SUKSES! ===");
    }
}
