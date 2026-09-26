import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class TransaksiPenjualan
 * Sesuai spesifikasi OOP Studi Kasus:
 * - atribut: noNota, waktu, totalBayar, nominalTunai, kembalian
 */
public class TransaksiPenjualan {
    private String noNota;
    private LocalDateTime waktu;
    private double totalBayar;
    private double nominalTunai;
    private double kembalian;
    private List<ItemBelanja> daftarItem;

    public TransaksiPenjualan(String noNota, LocalDateTime waktu, double totalBayar, double nominalTunai, double kembalian, List<ItemBelanja> items) {
        this.noNota = noNota;
        this.waktu = waktu;
        this.totalBayar = totalBayar;
        this.nominalTunai = nominalTunai;
        this.kembalian = kembalian;
        // Salin item belanja agar menjadi snapshot transaksi
        this.daftarItem = new ArrayList<>();
        if (items != null) {
            for (ItemBelanja item : items) {
                this.daftarItem.add(new ItemBelanja(item.getBarang(), item.getQty()));
            }
        }
    }

    // Getters and Setters
    public String getNoNota() {
        return noNota;
    }

    public void setNoNota(String noNota) {
        this.noNota = noNota;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public void setTotalBayar(double totalBayar) {
        this.totalBayar = totalBayar;
    }

    public double getNominalTunai() {
        return nominalTunai;
    }

    public void setNominalTunai(double nominalTunai) {
        this.nominalTunai = nominalTunai;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }

    public List<ItemBelanja> getDaftarItem() {
        return daftarItem;
    }

    /**
     * Format cetak struk nota belanja kasir minimarket ala printer thermal
     */
    public String cetakStruk() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("               KOPMA MART                \n");
        sb.append("         Koperasi Mahasiswa Kampus       \n");
        sb.append("         Layanan Kasir Cepat POS         \n");
        sb.append("=========================================\n");
        sb.append(String.format("No. Nota : %-29s\n", noNota));
        sb.append(String.format("Waktu    : %-29s\n", waktu.format(dtf)));
        sb.append(String.format("Kasir    : %-29s\n", "Kasir Mahasiswa (Shift-1)"));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-20s %3s %7s %8s\n", "Item", "Qty", "Harga", "Subtotal"));
        sb.append("-----------------------------------------\n");

        for (ItemBelanja item : daftarItem) {
            String nama = item.getBarang().getNamaBarang();
            if (nama.length() > 20) {
                nama = nama.substring(0, 18) + "..";
            }
            sb.append(String.format("%-20s %3d %7.0f %8.0f\n",
                    nama,
                    item.getQty(),
                    item.getBarang().getHargaJual(),
                    item.getSubtotal()));
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL HARGA  : Rp %21s\n", String.format("%,.0f", totalBayar)));
        sb.append(String.format("TUNAI        : Rp %21s\n", String.format("%,.0f", nominalTunai)));
        sb.append(String.format("KEMBALIAN    : Rp %21s\n", String.format("%,.0f", kembalian)));
        sb.append("=========================================\n");
        sb.append("    Terima Kasih Telah Berbelanja di     \n");
        sb.append("          KOPMA MART MAHASISWA           \n");
        sb.append("    Barang yang dibeli tidak dapat ditukar\n");
        sb.append("=========================================\n");

        return sb.toString();
    }

    /**
     * Format baris untuk disimpan ke rekap_transaksi_kasir.txt
     */
    public String toRekapString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(noNota).append("] ");
        sb.append(waktu.format(dtf)).append(" | ");
        sb.append("Total: Rp ").append(String.format("%,.0f", totalBayar)).append(" | ");
        sb.append("Tunai: Rp ").append(String.format("%,.0f", nominalTunai)).append(" | ");
        sb.append("Kembali: Rp ").append(String.format("%,.0f", kembalian)).append(" | ");
        sb.append("Items: [");
        for (int i = 0; i < daftarItem.size(); i++) {
            ItemBelanja ib = daftarItem.get(i);
            sb.append(ib.getBarang().getNamaBarang()).append(" (x").append(ib.getQty()).append(")");
            if (i < daftarItem.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
