import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Aplikasi GUI Kasir Minimarket Kampus: Kopma Mart POS
 * Memenuhi seluruh kriteria spesifikasi Studi Kasus:
 * 1. Input Barcode dengan listener tombol ENTER (otomatis tambah item tanpa klik mouse)
 * 2. Panel Total Bayar dengan Font Size 28
 * 3. Validasi stok gudang sebelum dimasukkan ke keranjang
 * 4. Data Persistence: master_stok_barang.txt & rekap_transaksi_kasir.txt
 * 5. WOW Factor Expo: Live Monitor Stok berkurang seketika saat transaksi
 */
public class KopmaMartApp extends JFrame {

    // Komponen Logika & Data
    private DataManager dataManager;
    private KeranjangBelanja keranjang;
    private int counterNota = 1;

    // Komponen GUI - Kiri (Transaksi)
    private JTextField txtBarcode;
    private JTable tblKeranjang;
    private DefaultTableModel modelKeranjang;
    private JButton btnHapusItem;
    private JButton btnBatalTransaksi;

    // Komponen GUI - Kanan (Pembayaran)
    private JLabel lblTotalBayar;
    private JTextField txtNominalTunai;
    private JLabel lblKembalian;
    private JButton btnBayar;

    // Komponen Live Monitor Stok (Master Gudang)
    private JTable tblMasterStok;
    private DefaultTableModel modelMasterStok;
    private JLabel lblTotalItemMaster;

    // Format Rupiah
    private DecimalFormat rupiahFormat;

    public KopmaMartApp() {
        super("Kopma Mart (Koperasi Mahasiswa) POS - Kasir Cepat Minimarket Kampus");
        inisialisasiSistem();
        inisialisasiKomponenUI();
        refreshTabelMasterStok();
    }

    private void inisialisasiSistem() {
        dataManager = new DataManager();
        keranjang = new KeranjangBelanja();

        // Format angka Rupiah
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        rupiahFormat = new DecimalFormat("Rp #,##0", symbols);

        // Cari nomor nota terakhir jika ada file rekap
        hitungNomorNotaAwal();
    }

    private void hitungNomorNotaAwal() {
        File fileRekap = new File("rekap_transaksi_kasir.txt");
        if (fileRekap.exists()) {
            try (java.util.Scanner sc = new java.util.Scanner(fileRekap)) {
                int count = 0;
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (!line.isEmpty()) count++;
                }
                counterNota = count + 1;
            } catch (Exception ignored) {}
        }
    }

    private void inisialisasiKomponenUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        // Gunakan layout utama BorderLayout
        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBackground(new Color(241, 245, 249)); // Slate-100
        rootPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        // 1. Header Panel
        rootPanel.add(buatHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Panel (Split Transaksi & Pembayaran)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(700);
        mainSplit.setResizeWeight(0.65);
        mainSplit.setBorder(null);
        mainSplit.setOpaque(false);

        mainSplit.setLeftComponent(buatPanelKiriTransaksi());
        mainSplit.setRightComponent(buatPanelKananPembayaran());

        // 3. Tabbed Panel (Keranjang & Live Monitor Stok Gudang)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Kasir POS Utama
        tabbedPane.addTab("🛒  Kasir POS (Transaksi Cepat)", mainSplit);

        // Tab 2: Live Monitor Stok Gudang
        tabbedPane.addTab("📦  Live Monitor Stok Gudang (Master Data)", buatPanelMonitorGudang());

        // Tab 3: Rekap Riwayat Transaksi
        tabbedPane.addTab("📋  Rekap Riwayat Transaksi", buatPanelRekapTransaksi());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Shortcut keyboard global
        setupGlobalKeyboardShortcuts();
    }

    // ==========================================
    // 1. PANEL HEADER
    // ==========================================
    private JPanel buatHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 5));
        header.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Brand & Title
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("🏪  KOPMA MART POS – MINIMARKET KAMPUS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(248, 250, 252));

        JLabel lblSubtitle = new JLabel("Sistem Kasir Barcode Cepat & Sinkronisasi Stok Otomatis");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblTitle);
        brandPanel.add(lblSubtitle);

        // Status & Real-time Clock
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        statusPanel.setOpaque(false);

        JLabel lblKasir = new JLabel("Kasir: Mahasiswa (Shift-1) | Status: ONLINE  🟢", SwingConstants.RIGHT);
        lblKasir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKasir.setForeground(new Color(52, 211, 153)); // Emerald green

        JLabel lblClock = new JLabel("", SwingConstants.RIGHT);
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClock.setForeground(new Color(203, 213, 225));

        // Live Clock Timer
        Timer timer = new Timer(1000, e -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss");
            lblClock.setText(LocalDateTime.now().format(dtf));
        });
        timer.start();

        statusPanel.add(lblKasir);
        statusPanel.add(lblClock);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(statusPanel, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // 2. PANEL KIRI: TRANSAKSI & KERANJANG
    // ==========================================
    private JPanel buatPanelKiriTransaksi() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 5));

        // Input Barcode Section
        JPanel scanContainer = new JPanel(new BorderLayout(5, 5));
        scanContainer.setBackground(Color.WHITE);
        scanContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblScanPrompt = new JLabel("🔍 SCAN BARCODE PRODUK (Ketik Kode lalu Tekan ENTER):");
        lblScanPrompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblScanPrompt.setForeground(new Color(30, 41, 59));

        txtBarcode = new JTextField();
        txtBarcode.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtBarcode.setPreferredSize(new Dimension(300, 40));
        txtBarcode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
                new EmptyBorder(4, 10, 4, 10)
        ));

        // WOW FACTOR: ENTER KEY LISTENER
        txtBarcode.addActionListener(e -> prosesScanBarcode());

        JButton btnScan = new JButton("Enter / Tambah ↵");
        btnScan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnScan.setBackground(new Color(37, 99, 235));
        btnScan.setForeground(Color.WHITE);
        btnScan.setFocusPainted(false);
        btnScan.addActionListener(e -> prosesScanBarcode());

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.add(txtBarcode, BorderLayout.CENTER);
        inputRow.add(btnScan, BorderLayout.EAST);

        // Demo Cheat Sheet Barcode Buttons
        JPanel cheatSheetPanel = buatPanelCheatSheetBarcode();

        scanContainer.add(lblScanPrompt, BorderLayout.NORTH);
        scanContainer.add(inputRow, BorderLayout.CENTER);
        scanContainer.add(cheatSheetPanel, BorderLayout.SOUTH);

        // Tabel Keranjang Belanja
        String[] kolom = {"No", "Barcode", "Nama Barang", "Kategori", "Harga Satuan", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKeranjang = new JTable(modelKeranjang);
        tblKeranjang.setRowHeight(32);
        tblKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKeranjang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblKeranjang.getTableHeader().setBackground(new Color(241, 245, 249));
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom formatting kolom
        tblKeranjang.getColumnModel().getColumn(0).setPreferredWidth(35); // No
        tblKeranjang.getColumnModel().getColumn(1).setPreferredWidth(80); // Barcode
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(210); // Nama
        tblKeranjang.getColumnModel().getColumn(3).setPreferredWidth(85); // Kategori
        tblKeranjang.getColumnModel().getColumn(4).setPreferredWidth(95); // Harga
        tblKeranjang.getColumnModel().getColumn(5).setPreferredWidth(45); // Qty
        tblKeranjang.getColumnModel().getColumn(6).setPreferredWidth(105); // Subtotal

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblKeranjang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblKeranjang.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblKeranjang.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane scrollTabel = new JScrollPane(tblKeranjang);
        scrollTabel.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        // Tombol Aksi Keranjang
        JPanel aksiPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        aksiPanel.setOpaque(false);

        btnHapusItem = new JButton("🗑  Hapus Item Terpilih");
        btnHapusItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapusItem.setBackground(new Color(239, 68, 68));
        btnHapusItem.setForeground(Color.WHITE);
        btnHapusItem.setFocusPainted(false);
        btnHapusItem.addActionListener(e -> hapusItemTerpilih());

        btnBatalTransaksi = new JButton("❌  Kosongkan Keranjang");
        btnBatalTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBatalTransaksi.setBackground(new Color(226, 232, 240));
        btnBatalTransaksi.setForeground(new Color(51, 65, 85));
        btnBatalTransaksi.setFocusPainted(false);
        btnBatalTransaksi.addActionListener(e -> batalkanKeranjang());

        aksiPanel.add(btnBatalTransaksi);
        aksiPanel.add(btnHapusItem);

        panel.add(scanContainer, BorderLayout.NORTH);
        panel.add(scrollTabel, BorderLayout.CENTER);
        panel.add(aksiPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Quick Barcode Chips untuk kemudahan saat Demo Expo
     */
    private JPanel buatPanelCheatSheetBarcode() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        p.setOpaque(false);

        JLabel lblHint = new JLabel("⚡ Barcode Cepat (Demo Expo): ");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(100, 116, 139));
        p.add(lblHint);

        String[][] sampleItems = {
                {"101", "Indomie (101)"},
                {"102", "Aqua (102)"},
                {"103", "Ultra Milk (103)"},
                {"104", "Chitato (104)"},
                {"107", "Pulpen (107)"}
        };

        for (String[] item : sampleItems) {
            JButton btnChip = new JButton(item[1]);
            btnChip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnChip.setMargin(new Insets(2, 6, 2, 6));
            btnChip.setBackground(new Color(241, 245, 249));
            btnChip.setFocusPainted(false);
            btnChip.addActionListener(e -> {
                txtBarcode.setText(item[0]);
                prosesScanBarcode();
            });
            p.add(btnChip);
        }

        return p;
    }

    // ==========================================
    // 3. PANEL KANAN: RINGKASAN PEMBAYARAN
    // ==========================================
    private JPanel buatPanelKananPembayaran() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 0));

        // Box 1: TOTAL BAYAR (Font Size 28 Sesuai Spesifikasi!)
        JPanel totalBox = new JPanel(new BorderLayout(5, 5));
        totalBox.setBackground(new Color(15, 23, 42)); // Slate Navy
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 41, 59), 2),
                new EmptyBorder(15, 18, 15, 18)
        ));
        totalBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));

        JLabel lblTitleTotal = new JLabel("TOTAL BAYAR");
        lblTitleTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleTotal.setForeground(new Color(148, 163, 184));

        // Font Size 28 spesifikasi wajib!
        lblTotalBayar = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalBayar.setForeground(new Color(52, 211, 153)); // Vibrant Emerald

        totalBox.add(lblTitleTotal, BorderLayout.NORTH);
        totalBox.add(lblTotalBayar, BorderLayout.CENTER);

        // Box 2: PEMBAYARAN TUNAI & KEMBALIAN
        JPanel bayarBox = new JPanel();
        bayarBox.setLayout(new BoxLayout(bayarBox, BoxLayout.Y_AXIS));
        bayarBox.setBackground(Color.WHITE);
        bayarBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JLabel lblTunaiPrompt = new JLabel("NOMINAL TUNAI (F8):");
        lblTunaiPrompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTunaiPrompt.setForeground(new Color(30, 41, 59));
        lblTunaiPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNominalTunai = new JTextField();
        txtNominalTunai.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtNominalTunai.setPreferredSize(new Dimension(280, 42));
        txtNominalTunai.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        txtNominalTunai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtNominalTunai.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hitung kembalian live saat user mengetik
        txtNominalTunai.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { hitungKembalianLive(); }
            public void removeUpdate(DocumentEvent e) { hitungKembalianLive(); }
            public void changedUpdate(DocumentEvent e) { hitungKembalianLive(); }
        });

        // Enter pada field tunai langsung memicu pembayaran
        txtNominalTunai.addActionListener(e -> selesaikanTransaksi());

        // Quick Nominal Buttons
        JPanel quickCashPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        quickCashPanel.setOpaque(false);
        quickCashPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        quickCashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        quickCashPanel.add(buatTombolUangPas());
        quickCashPanel.add(buatTombolNominal("Rp 10.000", 10000));
        quickCashPanel.add(buatTombolNominal("Rp 20.000", 20000));
        quickCashPanel.add(buatTombolNominal("Rp 50.000", 50000));
        quickCashPanel.add(buatTombolNominal("Rp 100.000", 100000));
        quickCashPanel.add(buatTombolNominal("Rp 200.000", 200000));

        // Box Kembalian
        JPanel kembalianPanel = new JPanel(new BorderLayout(5, 5));
        kembalianPanel.setBackground(new Color(248, 250, 252));
        kembalianPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        kembalianPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
        kembalianPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitleKembali = new JLabel("KEMBALIAN:");
        lblTitleKembali.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitleKembali.setForeground(new Color(100, 116, 139));

        lblKembalian = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblKembalian.setForeground(new Color(16, 185, 129));

        kembalianPanel.add(lblTitleKembali, BorderLayout.NORTH);
        kembalianPanel.add(lblKembalian, BorderLayout.CENTER);

        bayarBox.add(lblTunaiPrompt);
        bayarBox.add(Box.createVerticalStrut(6));
        bayarBox.add(txtNominalTunai);
        bayarBox.add(Box.createVerticalStrut(10));
        bayarBox.add(quickCashPanel);
        bayarBox.add(Box.createVerticalStrut(12));
        bayarBox.add(kembalianPanel);

        // Box 3: TOMBOL BAYAR BESAR
        btnBayar = new JButton("💳  SELESAIKAN TRANSAKSI (F9)");
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBayar.setBackground(new Color(16, 185, 129)); // Green
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setPreferredSize(new Dimension(280, 52));
        btnBayar.setMaximumSize(new Dimension(Short.MAX_VALUE, 52));
        btnBayar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBayar.setFocusPainted(false);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.addActionListener(e -> selesaikanTransaksi());

        // Box 4: Petunjuk Shortcut Keyboard
        JPanel shortcutBox = new JPanel(new GridLayout(3, 1, 2, 2));
        shortcutBox.setOpaque(false);
        shortcutBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sc1 = new JLabel("⌨️  [ENTER di Barcode] : Tambah Produk");
        JLabel sc2 = new JLabel("⌨️  [F8] : Fokus ke Nominal Tunai");
        JLabel sc3 = new JLabel("⌨️  [F9] : Proses Pembayaran & Cetak Nota");
        Font scFont = new Font("Segoe UI", Font.ITALIC, 11);
        Color scColor = new Color(100, 116, 139);
        sc1.setFont(scFont); sc1.setForeground(scColor);
        sc2.setFont(scFont); sc2.setForeground(scColor);
        sc3.setFont(scFont); sc3.setForeground(scColor);
        shortcutBox.add(sc1);
        shortcutBox.add(sc2);
        shortcutBox.add(sc3);

        panel.add(totalBox);
        panel.add(Box.createVerticalStrut(12));
        panel.add(bayarBox);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btnBayar);
        panel.add(Box.createVerticalStrut(12));
        panel.add(shortcutBox);

        return panel;
    }

    private JButton buatTombolUangPas() {
        JButton btn = new JButton("Uang Pas");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            long total = (long) keranjang.hitungSubtotal();
            txtNominalTunai.setText(String.valueOf(total));
            hitungKembalianLive();
        });
        return btn;
    }

    private JButton buatTombolNominal(String label, long nominal) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            txtNominalTunai.setText(String.valueOf(nominal));
            hitungKembalianLive();
        });
        return btn;
    }

    // ==========================================
    // 4. TAB MONITOR LIVE STOK GUDANG (EXPO WOW FACTOR)
    // ==========================================
    private JPanel buatPanelMonitorGudang() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblInfo = new JLabel("📦  Data Master Stok Gudang (Tersinkronisasi Otomatis dengan master_stok_barang.txt)");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfo.setForeground(new Color(30, 41, 59));

        lblTotalItemMaster = new JLabel("Total Jenis Produk: 0", SwingConstants.RIGHT);
        lblTotalItemMaster.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalItemMaster.setForeground(new Color(71, 85, 105));

        topBar.add(lblInfo, BorderLayout.WEST);
        topBar.add(lblTotalItemMaster, BorderLayout.EAST);

        String[] cols = {"Barcode ID", "Nama Barang", "Kategori", "Harga Jual", "Stok Gudang", "Status Stok"};
        modelMasterStok = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblMasterStok = new JTable(modelMasterStok);
        tblMasterStok.setRowHeight(30);
        tblMasterStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMasterStok.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblMasterStok.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        tblMasterStok.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblMasterStok.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        tblMasterStok.getColumnModel().getColumn(4).setCellRenderer(centerRender);
        tblMasterStok.getColumnModel().getColumn(5).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        tblMasterStok.getColumnModel().getColumn(3).setCellRenderer(rightRender);

        JScrollPane scroll = new JScrollPane(tblMasterStok);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        // Tombol Tambah Barang Baru / Restock
        JPanel btmBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btmBar.setOpaque(false);

        JButton btnRestock = new JButton("➕  Tambah / Restock Produk");
        btnRestock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRestock.setBackground(new Color(37, 99, 235));
        btnRestock.setForeground(Color.WHITE);
        btnRestock.setFocusPainted(false);
        btnRestock.addActionListener(e -> dialogTambahBarang());

        JButton btnRefresh = new JButton("🔄  Muat Ulang dari File");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            dataManager.muatDataMaster();
            refreshTabelMasterStok();
            JOptionPane.showMessageDialog(this, "Data master stok berhasil dimuat ulang dari file!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        btmBar.add(btnRefresh);
        btmBar.add(btnRestock);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btmBar, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // 5. TAB REKAP RIWAYAT TRANSAKSI
    // ==========================================
    private JPanel buatPanelRekapTransaksi() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("📋  Catatan Riwayat Penjualan (Tersimpan di rekap_transaksi_kasir.txt)");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextArea txtRekap = new JTextArea();
        txtRekap.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtRekap.setEditable(false);
        txtRekap.setBackground(new Color(248, 250, 252));
        txtRekap.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtRekap);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        JButton btnMuatRekap = new JButton("🔄  Perbarui Catatan Rekap");
        btnMuatRekap.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMuatRekap.addActionListener(e -> {
            File f = new File("rekap_transaksi_kasir.txt");
            if (!f.exists()) {
                txtRekap.setText("Belum ada transaksi yang tercatat.");
                return;
            }
            try (java.util.Scanner sc = new java.util.Scanner(f, java.nio.charset.StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine()).append("\n");
                }
                txtRekap.setText(sb.toString());
            } catch (Exception ex) {
                txtRekap.setText("Gagal membaca rekap: " + ex.getMessage());
            }
        });

        // Load pertama kali
        btnMuatRekap.doClick();

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(btnMuatRekap);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btm, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // LOGIKA TRANSAKSI KASIR
    // ==========================================

    /**
     * Fitur Utama: Listener Enter pada input barcode.
     * Otomatis menambah item ke tabel tanpa perlu klik mouse!
     */
    private void prosesScanBarcode() {
        String barcodeInput = txtBarcode.getText().trim();
        if (barcodeInput.isEmpty()) {
            return;
        }

        Barang barang = dataManager.cariBarang(barcodeInput);

        if (barang == null) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this,
                    "⚠️  Kode Barcode '" + barcodeInput + "' tidak ditemukan dalam sistem!",
                    "Barang Tidak Ditemukan",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.selectAll();
            txtBarcode.requestFocus();
            return;
        }

        // VALIDASI STOK: Cek stok di gudang dan jumlah di keranjang
        int qtyDiKeranjang = 0;
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barang.getBarcodeId())) {
                qtyDiKeranjang = item.getQty();
                break;
            }
        }

        if (qtyDiKeranjang + 1 > barang.getStokGudang()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this,
                    "⚠️  PERINGATAN VALIDASI STOK:\n\n" +
                            "Barang: " + barang.getNamaBarang() + "\n" +
                            "Sisa Stok Gudang: " + barang.getStokGudang() + " pcs\n" +
                            "Sudah di Keranjang: " + qtyDiKeranjang + " pcs\n\n" +
                            "Pembelian tidak dapat melebihi stok yang tersedia!",
                    "Stok Tidak Mencukupi",
                    JOptionPane.ERROR_MESSAGE);
            txtBarcode.selectAll();
            txtBarcode.requestFocus();
            return;
        }

        // Tambahkan ke keranjang
        boolean sukses = keranjang.tambahItem(barang, 1);
        if (sukses) {
            Toolkit.getDefaultToolkit().beep(); // Sound feedback scan sukses
            refreshTabelKeranjang();
            txtBarcode.setText("");
            txtBarcode.requestFocus();
        }
    }

    private void refreshTabelKeranjang() {
        modelKeranjang.setRowCount(0);
        int no = 1;
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            modelKeranjang.addRow(new Object[]{
                    no++,
                    item.getBarang().getBarcodeId(),
                    item.getBarang().getNamaBarang(),
                    item.getBarang().getKategori(),
                    rupiahFormat.format(item.getBarang().getHargaJual()),
                    item.getQty(),
                    rupiahFormat.format(item.getSubtotal())
            });
        }

        // Perbarui Label Total Bayar Besar (Font 28)
        double total = keranjang.hitungSubtotal();
        lblTotalBayar.setText(rupiahFormat.format(total));

        // Hitung ulang kembalian jika nominal tunai sudah diisi
        hitungKembalianLive();
    }

    private void hitungKembalianLive() {
        double total = keranjang.hitungSubtotal();
        String strTunai = txtNominalTunai.getText().trim().replaceAll("[^0-9]", "");

        if (strTunai.isEmpty() || total == 0) {
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(new Color(100, 116, 139));
            return;
        }

        try {
            double tunai = Double.parseDouble(strTunai);
            double kembalian = tunai - total;

            if (kembalian >= 0) {
                lblKembalian.setText(rupiahFormat.format(kembalian));
                lblKembalian.setForeground(new Color(16, 185, 129)); // Hijau
            } else {
                lblKembalian.setText("Kurang " + rupiahFormat.format(Math.abs(kembalian)));
                lblKembalian.setForeground(new Color(239, 68, 68)); // Merah
            }
        } catch (NumberFormatException ignored) {
            lblKembalian.setText("Rp 0");
        }
    }

    private void hapusItemTerpilih() {
        int selectedRow = tblKeranjang.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih baris barang di tabel keranjang yang ingin dihapus terlebih dahulu.",
                    "Pemberitahuan",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        keranjang.hapusItem(selectedRow);
        refreshTabelKeranjang();
        txtBarcode.requestFocus();
    }

    private void batalkanKeranjang() {
        if (keranjang.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin mengosongkan keranjang belanja?",
                "Konfirmasi Batal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            keranjang.kosongkan();
            txtNominalTunai.setText("");
            refreshTabelKeranjang();
            txtBarcode.requestFocus();
        }
    }

    /**
     * Memproses Selesai Transaksi:
     * 1. Validasi keranjang & pembayaran tunai
     * 2. Kurangi stok barang di master data
     * 3. Simpan ke master_stok_barang.txt
     * 4. Simpan ke rekap_transaksi_kasir.txt
     * 5. Refresh live monitor stok gudang (WOW Factor Demo)
     * 6. Munculkan dialog nota struk kasir
     */
    private void selesaikanTransaksi() {
        if (keranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Keranjang belanja masih kosong! Silakan scan barang terlebih dahulu.",
                    "Keranjang Kosong",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.requestFocus();
            return;
        }

        double total = keranjang.hitungSubtotal();
        String strTunai = txtNominalTunai.getText().trim().replaceAll("[^0-9]", "");

        if (strTunai.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Masukkan nominal uang tunai yang diterima dari pembeli!",
                    "Nominal Kosong",
                    JOptionPane.WARNING_MESSAGE);
            txtNominalTunai.requestFocus();
            return;
        }

        double tunai = Double.parseDouble(strTunai);
        if (tunai < total) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this,
                    "Uang tunai tidak mencukupi!\nTotal Belanja: " + rupiahFormat.format(total) +
                            "\nTunai: " + rupiahFormat.format(tunai) +
                            "\nKurang: " + rupiahFormat.format(total - tunai),
                    "Uang Tidak Cukup",
                    JOptionPane.ERROR_MESSAGE);
            txtNominalTunai.requestFocus();
            return;
        }

        double kembalian = tunai - total;

        // 1. Generate No. Nota
        DateTimeFormatter dtfNota = DateTimeFormatter.ofPattern("yyyyMMdd");
        String noNota = "KOPMA-" + LocalDateTime.now().format(dtfNota) + "-" + String.format("%04d", counterNota++);

        // 2. Buat objek TransaksiPenjualan
        TransaksiPenjualan transaksi = new TransaksiPenjualan(
                noNota,
                LocalDateTime.now(),
                total,
                tunai,
                kembalian,
                keranjang.getDaftarItem()
        );

        // 3. PERSISTENCE: Kurangi stok gudang pada master data
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            Barang b = dataManager.cariBarang(item.getBarang().getBarcodeId());
            if (b != null) {
                b.kurangiStok(item.getQty());
            }
        }

        // 4. PERSISTENCE: Tulis kembali ke master_stok_barang.txt
        dataManager.simpanMasterStok();

        // 5. PERSISTENCE: Tulis rekap ke rekap_transaksi_kasir.txt
        dataManager.simpanRekapTransaksi(transaksi);

        // 6. WOW FACTOR: Perbarui live monitor tabel master stok seketika!
        refreshTabelMasterStok();

        // 7. Tampilkan Struk Pembayaran
        tampilkanDialogStruk(transaksi);

        // 8. Bersihkan keranjang belanja untuk transaksi berikutnya
        keranjang.kosongkan();
        txtNominalTunai.setText("");
        refreshTabelKeranjang();
        txtBarcode.requestFocus();
    }

    private void tampilkanDialogStruk(TransaksiPenjualan transaksi) {
        JDialog dialog = new JDialog(this, "Struk Transaksi Pembayaran - " + transaksi.getNoNota(), true);
        dialog.setSize(420, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea txtStruk = new JTextArea();
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStruk.setText(transaksi.cetakStruk());
        txtStruk.setEditable(false);
        txtStruk.setBackground(Color.WHITE);
        txtStruk.setBorder(new EmptyBorder(10, 15, 10, 15));

        JScrollPane scroll = new JScrollPane(txtStruk);
        scroll.setBorder(null);

        JPanel pnlBawah = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnTutup = new JButton("✓ Transaksi Selesai (Tutup)");
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTutup.setBackground(new Color(16, 185, 129));
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dialog.dispose());

        pnlBawah.add(btnTutup);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(pnlBawah, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==========================================
    // REFRESH DATA & DIALOG MASTER STOK
    // ==========================================
    private void refreshTabelMasterStok() {
        modelMasterStok.setRowCount(0);
        List<Barang> list = dataManager.getDaftarBarangMaster();
        for (Barang b : list) {
            String status = b.getStokGudang() <= 5 ? "⚠️ MENIPIS" : "✅ AMAN";
            if (b.getStokGudang() == 0) status = "❌ HABIS";

            modelMasterStok.addRow(new Object[]{
                    b.getBarcodeId(),
                    b.getNamaBarang(),
                    b.getKategori(),
                    rupiahFormat.format(b.getHargaJual()),
                    b.getStokGudang(),
                    status
            });
        }
        lblTotalItemMaster.setText("Total Jenis Produk: " + list.size() + " Item");
    }

    private void dialogTambahBarang() {
        JDialog dlg = new JDialog(this, "Tambah / Restock Produk Kopma Mart", true);
        dlg.setSize(400, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField txtBcode = new JTextField();
        JTextField txtNama = new JTextField();
        JComboBox<String> cbKat = new JComboBox<>(new String[]{"Makanan", "Minuman", "ATK", "Perlengkapan", "Lainnya"});
        JTextField txtHarga = new JTextField();
        JTextField txtStok = new JTextField();

        form.add(new JLabel("Barcode ID:"));
        form.add(txtBcode);
        form.add(new JLabel("Nama Barang:"));
        form.add(txtNama);
        form.add(new JLabel("Kategori:"));
        form.add(cbKat);
        form.add(new JLabel("Harga Jual (Rp):"));
        form.add(txtHarga);
        form.add(new JLabel("Stok Gudang:"));
        form.add(txtStok);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSimpan = new JButton("Simpan ke Master Stok");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(37, 99, 235));
        btnSimpan.setForeground(Color.WHITE);

        btnSimpan.addActionListener(e -> {
            String bcode = txtBcode.getText().trim();
            String nama = txtNama.getText().trim();
            String kat = (String) cbKat.getSelectedItem();
            String strHarga = txtHarga.getText().trim();
            String strStok = txtStok.getText().trim();

            if (bcode.isEmpty() || nama.isEmpty() || strHarga.isEmpty() || strStok.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Semua data produk wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double harga = Double.parseDouble(strHarga);
                int stok = Integer.parseInt(strStok);

                Barang existing = dataManager.cariBarang(bcode);
                if (existing != null) {
                    existing.setNamaBarang(nama);
                    existing.setKategori(kat);
                    existing.setHargaJual(harga);
                    existing.setStokGudang(existing.getStokGudang() + stok);
                } else {
                    Barang baru = new Barang(bcode, nama, kat, harga, stok);
                    dataManager.getDaftarBarangMaster().add(baru);
                    dataManager.cariBarang(bcode); // update map di DataManager
                    dataManager.muatDataMaster(); // reload
                }

                dataManager.simpanMasterStok();
                refreshTabelMasterStok();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Produk berhasil disimpan ke master stok!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Harga dan Stok harus berupa angka!", "Format Salah", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtn.add(btnSimpan);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pnlBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void setupGlobalKeyboardShortcuts() {
        // Shortcut F8: Fokus ke input tunai
        getRootPane().registerKeyboardAction(e -> {
            txtNominalTunai.requestFocus();
            txtNominalTunai.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Shortcut F9: Selesaikan Transaksi
        getRootPane().registerKeyboardAction(e -> {
            selesaikanTransaksi();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Shortcut Escape: Fokus ke scan barcode
        getRootPane().registerKeyboardAction(e -> {
            txtBarcode.requestFocus();
            txtBarcode.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ==========================================
    // MAIN METHOD (ENTRY POINT)
    // ==========================================
    public static void main(String[] args) {
        // Terapkan Look and Feel sistem untuk tampilan modern dan bersih
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        SwingUtilities.invokeLater(() -> {
            KopmaMartApp app = new KopmaMartApp();
            app.setVisible(true);
        });
    }
}
