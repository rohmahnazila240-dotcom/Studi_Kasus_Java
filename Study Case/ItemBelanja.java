/**
 * Class ItemBelanja
 * Merepresentasikan satu jenis item yang dimasukkan ke dalam keranjang kasir
 * beserta kuantitas (qty) dan subtotal harganya.
 */
public class ItemBelanja {
    private Barang barang;
    private int qty;

    public ItemBelanja(Barang barang, int qty) {
        this.barang = barang;
        this.qty = qty;
    }

    public Barang getBarang() {
        return barang;
    }

    public void setBarang(Barang barang) {
        this.barang = barang;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void tambahQty(int jumlah) {
        this.qty += jumlah;
    }

    /**
     * Menghitung subtotal untuk item ini (harga jual * qty)
     */
    public double getSubtotal() {
        return barang.getHargaJual() * qty;
    }

    @Override
    public String toString() {
        return barang.getNamaBarang() + " x" + qty + " = Rp " + String.format("%,.0f", getSubtotal());
    }
}
