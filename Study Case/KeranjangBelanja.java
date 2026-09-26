import java.util.ArrayList;
import java.util.List;

/**
 * Class KeranjangBelanja
 * Sesuai spesifikasi OOP Studi Kasus:
 * - atribut: daftarItem
 * - method: tambahItem(), hapusItem(), hitungSubtotal()
 */
public class KeranjangBelanja {
    private List<ItemBelanja> daftarItem;

    public KeranjangBelanja() {
        this.daftarItem = new ArrayList<>();
    }

    public List<ItemBelanja> getDaftarItem() {
        return daftarItem;
    }

    /**
     * Menambahkan item ke keranjang.
     * Jika barcode sudah ada di keranjang, kuantitas akan bertambah.
     * Melakukan validasi terhadap batas stok gudang yang tersedia.
     * 
     * @param barang objek Barang yang dibeli
     * @param qty kuantitas yang ditambahkan
     * @return true jika berhasil ditambahkan, false jika melebihi stok
     */
    public boolean tambahItem(Barang barang, int qty) {
        if (barang == null || qty <= 0) return false;

        // Cari apakah item sudah ada di keranjang
        for (ItemBelanja item : daftarItem) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barang.getBarcodeId())) {
                int totalPermintaan = item.getQty() + qty;
                if (totalPermintaan > barang.getStokGudang()) {
                    return false; // Melebihi stok gudang
                }
                item.tambahQty(qty);
                return true;
            }
        }

        // Jika belum ada di keranjang, validasi stok
        if (qty > barang.getStokGudang()) {
            return false;
        }

        daftarItem.add(new ItemBelanja(barang, qty));
        return true;
    }

    /**
     * Overload tambahItem() untuk penambahan 1 unit barang (misal saat scan barcode)
     */
    public boolean tambahItem(Barang barang) {
        return tambahItem(barang, 1);
    }

    /**
     * Menghapus item berdasarkan index tabel
     */
    public boolean hapusItem(int index) {
        if (index >= 0 && index < daftarItem.size()) {
            daftarItem.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Menghapus item berdasarkan barcodeId
     */
    public boolean hapusItem(String barcodeId) {
        if (barcodeId == null) return false;
        for (int i = 0; i < daftarItem.size(); i++) {
            if (daftarItem.get(i).getBarang().getBarcodeId().equalsIgnoreCase(barcodeId)) {
                daftarItem.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Menghitung subtotal / total keseluruhan belanjaan dalam keranjang
     */
    public double hitungSubtotal() {
        double total = 0.0;
        for (ItemBelanja item : daftarItem) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Mengembalikan total jumlah fisik item yang dibeli
     */
    public int getTotalQty() {
        int totalQty = 0;
        for (ItemBelanja item : daftarItem) {
            totalQty += item.getQty();
        }
        return totalQty;
    }

    /**
     * Mengosongkan keranjang belanja
     */
    public void kosongkan() {
        daftarItem.clear();
    }

    /**
     * Mengecek apakah keranjang kosong
     */
    public boolean isEmpty() {
        return daftarItem.isEmpty();
    }
}
