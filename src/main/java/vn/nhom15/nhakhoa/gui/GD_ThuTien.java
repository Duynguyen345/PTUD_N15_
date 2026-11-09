package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import vn.nhom15.nhakhoa.bus.LeTanBUS;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.DinhDangLeTan;

/**
 * Thu tiền (lễ tân kiêm thu ngân) — gộp 2 tab đúng như màn gốc:
 * Tab 1: 1 · Phí khám (đầu lượt)
 * Tab 2: 2 · Dịch vụ cuối lượt (chốt lượt)
 */
public final class GD_ThuTien extends ManHinhCoSo {
    private final LeTanBUS nghiepVu;
    private final Consumer<BenhNhan> khiChuyenLapPhieu;
    private final JTabbedPane tabs = new JTabbedPane();

    // ===== TAB 1: Phí khám (đầu lượt) =====
    private final JTextField txtTimBN = new JTextField(32);
    private final JLabel lbThongTinBN = new JLabel("(chưa chọn)");
    private final JLabel lbMucPhi = new JLabel("(chọn bệnh nhân để xem phí)");
    private final JComboBox<String> cbbHinhThucPhi = new JComboBox<>(new String[] { "TIEN_MAT", "CHUYEN_KHOAN" });
    private final JTextField txtTienKhachDua = new JTextField(28);
    private final JLabel lbTienThuaPhi = new JLabel(" ");
    private final JButton btnThuPhi;
    private final JButton btnXemHoaDon;
    private final JButton btnLapPhieuNgay;
    private final JLabel lbHoaDonPhi = new JLabel(" ");
    private BenhNhan benhNhan;
    private BigDecimal donGiaPhiKham;
    private HoaDon hoaDonPhiDaThu;

    // ===== TAB 2: Dịch vụ cuối lượt (chốt lượt) =====
    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new Object[] { "Mã phiếu", "Bệnh nhân", "Ngày khám" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tablePhieu = new JTable(modelPhieu);
    private List<PhieuKham> dsPhieuCho = List.of();
    private final DefaultTableModel modelDv = new DefaultTableModel(
            new Object[] { "Dịch vụ", "Đơn giá" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tableDv = new JTable(modelDv);
    private final JLabel lbThongTin = new JLabel("(chưa chọn lượt khám)");
    private final JLabel lbTong = new JLabel("0 ₫");
    private final JComboBox<String> cbbHinhThucDv = new JComboBox<>(new String[] { "TIEN_MAT", "CHUYEN_KHOAN" });
    private final JTextField txtTienDuaDv = new JTextField(20);
    private final JLabel lbTienThuaDv = new JLabel(" ");
    private PhieuKham phieuChon;
    private BigDecimal tongDichVu = BigDecimal.ZERO;

    public GD_ThuTien(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        this(nghiepVu, boXuLy, null, null);
    }

    public GD_ThuTien(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy, BenhNhan bnBanDau,
            Consumer<BenhNhan> khiChuyenLapPhieu) {
        super("Thu tiền (lễ tân kiêm thu ngân)", "", boXuLy);
        this.nghiepVu = nghiepVu;
        this.khiChuyenLapPhieu = khiChuyenLapPhieu;
        this.benhNhan = bnBanDau;

        // ---------- TAB 1 ----------
        JPanel tabPhi = new JPanel(new BorderLayout());
        tabPhi.setBackground(Color.WHITE);
        tabPhi.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel formPhi = new JPanel(new GridBagLayout());
        formPhi.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 12);
        g.anchor = GridBagConstraints.WEST;

        txtTimBN.setToolTipText("BN... hoặc 090... hoặc 12 số CCCD");
        txtTimBN.addActionListener(e -> timBenhNhan());
        g.gridx = 0;
        g.gridy = 0;
        formPhi.add(new JLabel("Khách cũ (SĐT/CCCD/mã BN)"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        formPhi.add(txtTimBN, g);
        g.gridx = 2;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        formPhi.add(taoNut("Tìm", this::timBenhNhan), g);

        lbThongTinBN.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbMucPhi.setFont(new Font("Segoe UI", Font.BOLD, 14));

        g.gridx = 0;
        g.gridy = 1;
        formPhi.add(new JLabel("Bệnh nhân"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        formPhi.add(lbThongTinBN, g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 1;
        formPhi.add(new JLabel("Mức phí khám"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        formPhi.add(lbMucPhi, g);

        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 1;
        formPhi.add(new JLabel("Hình thức"), g);
        g.gridx = 1;
        cbbHinhThucPhi.setPreferredSize(new Dimension(180, 32));
        formPhi.add(cbbHinhThucPhi, g);

        g.gridx = 0;
        g.gridy = 4;
        formPhi.add(new JLabel("Tiền khách đưa"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        formPhi.add(txtTienKhachDua, g);

        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 1;
        g.fill = GridBagConstraints.NONE;
        formPhi.add(new JLabel("Tiền thừa"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        lbTienThuaPhi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPhi.add(lbTienThuaPhi, g);

        btnThuPhi = taoNut("Thu phí khám", this::thuPhi);
        btnThuPhi.setBackground(new Color(19, 62, 102));
        btnThuPhi.setForeground(Color.WHITE);
        btnXemHoaDon = taoNut("Xem/In hóa đơn", this::xemHoaDonPhi);
        btnXemHoaDon.setEnabled(false);
        btnLapPhieuNgay = taoNut("Lập phiếu khám ngay →", this::sangLapPhieu);
        btnLapPhieuNgay.setVisible(false);

        JPanel hangNutPhi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hangNutPhi.setOpaque(false);
        hangNutPhi.add(btnThuPhi);
        hangNutPhi.add(btnXemHoaDon);
        hangNutPhi.add(btnLapPhieuNgay);
        hangNutPhi.add(lbHoaDonPhi);

        g.gridx = 0;
        g.gridy = 6;
        g.gridwidth = 3;
        formPhi.add(hangNutPhi, g);

        tabPhi.add(formPhi, BorderLayout.NORTH);

        cbbHinhThucPhi.addActionListener(e -> {
            if ("CHUYEN_KHOAN".equals(cbbHinhThucPhi.getSelectedItem()) && donGiaPhiKham != null)
                txtTienKhachDua.setText(donGiaPhiKham.toPlainString());
        });
        ganSuKienThayDoi(txtTienKhachDua, this::capNhatTienThuaPhi);

        // ---------- TAB 2 ----------
        JPanel tabDv = new JPanel(new BorderLayout(14, 10));
        tabDv.setBackground(Color.WHITE);
        tabDv.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel trai = new JPanel(new BorderLayout(0, 6));
        trai.setOpaque(false);
        trai.setPreferredSize(new Dimension(420, 0));
        JPanel dauTrai = new JPanel(new BorderLayout());
        dauTrai.setOpaque(false);
        JLabel nhanCho = new JLabel("Lượt khám đã HOAN_TAT — chờ thu");
        nhanCho.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dauTrai.add(nhanCho, BorderLayout.WEST);
        dauTrai.add(taoNut("Tải lại", this::lamMoi), BorderLayout.EAST);
        trai.add(dauTrai, BorderLayout.NORTH);
        tablePhieu.setRowHeight(30);
        tablePhieu.setFillsViewportHeight(true);
        tablePhieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhieu.getTableHeader().setReorderingAllowed(false);
        trai.add(new JScrollPane(tablePhieu), BorderLayout.CENTER);
        tabDv.add(trai, BorderLayout.WEST);

        JPanel phai = new JPanel(new BorderLayout(0, 8));
        phai.setOpaque(false);
        JLabel nhanCt = new JLabel("CHI TIẾT LƯỢT KHÁM");
        nhanCt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        phai.add(nhanCt, BorderLayout.NORTH);
        tableDv.setRowHeight(30);
        tableDv.setFillsViewportHeight(true);
        tableDv.getTableHeader().setReorderingAllowed(false);
        phai.add(new JScrollPane(tableDv), BorderLayout.CENTER);

        JPanel formDv = new JPanel(new GridBagLayout());
        formDv.setOpaque(false);
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(5, 4, 5, 10);
        g2.anchor = GridBagConstraints.WEST;
        lbThongTin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbTong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbTong.setForeground(new Color(19, 62, 102));

        g2.gridx = 0;
        g2.gridy = 0;
        g2.gridwidth = 2;
        formDv.add(lbThongTin, g2);

        g2.gridy = 1;
        g2.gridwidth = 1;
        formDv.add(new JLabel("TỔNG:"), g2);
        g2.gridx = 1;
        formDv.add(lbTong, g2);

        g2.gridx = 0;
        g2.gridy = 2;
        formDv.add(new JLabel("Hình thức"), g2);
        g2.gridx = 1;
        formDv.add(cbbHinhThucDv, g2);

        g2.gridx = 0;
        g2.gridy = 3;
        formDv.add(new JLabel("Tiền khách đưa"), g2);
        g2.gridx = 1;
        g2.fill = GridBagConstraints.HORIZONTAL;
        g2.weightx = 1;
        formDv.add(txtTienDuaDv, g2);

        g2.gridx = 0;
        g2.gridy = 4;
        g2.fill = GridBagConstraints.NONE;
        g2.weightx = 0;
        formDv.add(new JLabel("Tiền thừa"), g2);
        g2.gridx = 1;
        lbTienThuaDv.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formDv.add(lbTienThuaDv, g2);

        g2.gridx = 0;
        g2.gridy = 5;
        g2.gridwidth = 2;
        JButton btnThanhToan = taoNut("Thanh toán & in hóa đơn", this::thanhToanDichVu);
        btnThanhToan.setBackground(new Color(19, 62, 102));
        btnThanhToan.setForeground(Color.WHITE);
        formDv.add(btnThanhToan, g2);

        phai.add(formDv, BorderLayout.SOUTH);
        tabDv.add(phai, BorderLayout.CENTER);

        tablePhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = tablePhieu.getSelectedRow();
                chonPhieu(r >= 0 && r < dsPhieuCho.size() ? dsPhieuCho.get(r) : null);
            }
        });
        cbbHinhThucDv.addActionListener(e -> {
            if ("CHUYEN_KHOAN".equals(cbbHinhThucDv.getSelectedItem()) && phieuChon != null)
                txtTienDuaDv.setText(tongDichVu.toPlainString());
        });
        ganSuKienThayDoi(txtTienDuaDv, this::capNhatTienThuaDv);

        tabs.addTab("1 · Phí khám (đầu lượt)", tabPhi);
        tabs.addTab("2 · Dịch vụ cuối lượt (chốt lượt)", tabDv);
        noiDung.add(tabs, BorderLayout.CENTER);
    }

    private static void ganSuKienThayDoi(JTextField o, Runnable xuLy) {
        o.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                xuLy.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                xuLy.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                xuLy.run();
            }
        });
    }

    private void timBenhNhan() {
        String tu = txtTimBN.getText().trim();
        if (tu.isEmpty()) {
            trangThai.setText("Nhập SĐT / CCCD / mã BN trước khi bấm Tìm.");
            return;
        }
        boXuLy.thucHien(this, () -> nghiepVu.timBenhNhan(tu), ds -> {
            if (ds.isEmpty()) {
                benhNhan = null;
                lbThongTinBN.setText("(không tìm thấy)");
                lbMucPhi.setText("(chọn bệnh nhân để xem phí)");
                btnXemHoaDon.setEnabled(false);
                btnLapPhieuNgay.setVisible(false);
                JOptionPane.showMessageDialog(this, "Không tìm thấy hồ sơ bệnh nhân phù hợp.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BenhNhan chon = ds.getFirst();
            if (ds.size() > 1) {
                BenhNhan luaChon = (BenhNhan) JOptionPane.showInputDialog(this,
                        "Chọn bệnh nhân:", "Nhiều kết quả phù hợp",
                        JOptionPane.PLAIN_MESSAGE, null, ds.toArray(), ds.getFirst());
                if (luaChon != null)
                    chon = luaChon;
            }
            ganBenhNhan(chon);
        });
    }

    private void ganBenhNhan(BenhNhan bn) {
        benhNhan = bn;
        txtTimBN.setText(bn.getSoDienThoai());
        lbThongTinBN.setText(bn.getMaBenhNhan() + " - " + bn.getHoTen()
                + (bn.getCccd() != null ? " · CCCD " + bn.getCccd() : ""));
        lbMucPhi.setText(donGiaPhiKham == null ? "—" : DinhDangLeTan.tien(donGiaPhiKham));
        txtTienKhachDua.setText("");
        lbTienThuaPhi.setText(" ");
        boXuLy.thucHien(this, () -> nghiepVu.phiChuaDung(bn.getMaBenhNhan()), hd -> {
            hoaDonPhiDaThu = hd;
            if (hd != null) {
                lbHoaDonPhi.setText("Đã có phí khám " + hd.getMaHoaDon() + " chưa dùng.");
                btnXemHoaDon.setEnabled(true);
                btnLapPhieuNgay.setVisible(true);
            } else {
                lbHoaDonPhi.setText(" ");
                btnXemHoaDon.setEnabled(false);
                btnLapPhieuNgay.setVisible(false);
            }
        });
    }

    private void capNhatTienThuaPhi() {
        if (donGiaPhiKham == null)
            return;
        try {
            BigDecimal dua = new BigDecimal(txtTienKhachDua.getText().trim());
            lbTienThuaPhi.setText(DinhDangLeTan.tien(dua.subtract(donGiaPhiKham)));
        } catch (Exception ex) {
            lbTienThuaPhi.setText(" ");
        }
    }

    private void thuPhi() {
        if (benhNhan == null)
            throw new IllegalArgumentException("Tìm và chọn bệnh nhân trước khi thu phí.");
        String ht = (String) cbbHinhThucPhi.getSelectedItem();
        String ma = benhNhan.getMaBenhNhan();
        String tien = txtTienKhachDua.getText();
        boXuLy.thucHien(this, () -> nghiepVu.thuPhi(ma, ht, tien), hd -> {
            hoaDonPhiDaThu = hd;
            lbHoaDonPhi.setText("Đã thu: " + hd.getMaHoaDon() + " - " + DinhDangLeTan.tien(hd.getTongTien()));
            btnXemHoaDon.setEnabled(true);
            btnLapPhieuNgay.setVisible(true);
            trangThai.setText("Đã thu phí khám " + hd.getMaHoaDon() + ". Bấm 'Lập phiếu khám ngay →' để xếp hàng chờ.");
            JOptionPane.showMessageDialog(this,
                    "Thu phí thành công " + hd.getMaHoaDon() + ".\nBấm 'Lập phiếu khám ngay →' để xếp phòng khám.",
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void xemHoaDonPhi() {
        if (hoaDonPhiDaThu == null)
            return;
        InChungTuLeTan.xemVaIn(this, "Hóa đơn phí khám " + hoaDonPhiDaThu.getMaHoaDon(),
                noiDungHoaDon(hoaDonPhiDaThu));
    }

    private void sangLapPhieu() {
        if (benhNhan != null && khiChuyenLapPhieu != null)
            khiChuyenLapPhieu.accept(benhNhan);
    }

    private void chonPhieu(PhieuKham p) {
        phieuChon = p;
        modelDv.setRowCount(0);
        tongDichVu = BigDecimal.ZERO;
        lbTong.setText("0 ₫");
        txtTienDuaDv.setText("");
        lbTienThuaDv.setText(" ");
        if (p == null) {
            lbThongTin.setText("(chưa chọn lượt khám)");
            return;
        }
        lbThongTin.setText(p.getMaPhieuKham() + " - " + p.getTenBenhNhan() + " - " + ngay(p.getNgayKham()));
        boXuLy.thucHien(this, () -> nghiepVu.dichVuTheoPhieu(p.getMaPhieuKham()), ds -> {
            for (ChiDinhDichVu dv : ds) {
                modelDv.addRow(new Object[] { dv.getTenDichVu(), DinhDangLeTan.tien(dv.getDonGia()) });
                tongDichVu = tongDichVu.add(dv.getDonGia());
            }
            lbTong.setText(DinhDangLeTan.tien(tongDichVu));
        });
    }

    private void capNhatTienThuaDv() {
        try {
            BigDecimal dua = new BigDecimal(txtTienDuaDv.getText().trim());
            lbTienThuaDv.setText(DinhDangLeTan.tien(dua.subtract(tongDichVu)));
        } catch (Exception ex) {
            lbTienThuaDv.setText(" ");
        }
    }

    private void thanhToanDichVu() {
        if (phieuChon == null)
            throw new IllegalArgumentException("Chọn lượt khám đã hoàn tất ở bảng bên trái.");
        if (!xacNhan("Thanh toán " + DinhDangLeTan.tien(tongDichVu) + " cho lượt khám "
                + phieuChon.getMaPhieuKham() + "?"))
            return;
        String ma = phieuChon.getMaPhieuKham();
        String ht = (String) cbbHinhThucDv.getSelectedItem();
        String tien = txtTienDuaDv.getText();
        boXuLy.thucHien(this, () -> nghiepVu.thanhToanDichVu(ma, ht, tien), hd -> {
            InChungTuLeTan.xemVaIn(this, "Hóa đơn dịch vụ " + hd.getMaHoaDon(), noiDungHoaDon(hd));
            lamMoi();
            trangThai.setText("Đã thanh toán hóa đơn " + hd.getMaHoaDon() + ".");
        });
    }

    static String noiDungHoaDon(HoaDon hd) {
        boolean laPhiKham = hd.getMaPhieuKham() == null;
        return """
                NHA KHOA SÀI GÒN — DentistClinic
                HÓA ĐƠN %s
                — — — — — — — — — — — — — — — — — — —
                Mã HĐ:        %s
                Bệnh nhân:    %s · %s
                Ngày thu:     %s
                Hình thức:    %s
                Tổng tiền:    %s
                Khách đưa:    %s
                Tiền thừa:    %s
                Thu ngân:     %s
                """.formatted(laPhiKham ? "PHÍ KHÁM BAN ĐẦU" : "THANH TOÁN DỊCH VỤ",
                hd.getMaHoaDon(), hd.getMaBenhNhan(), hd.getTenBenhNhan(), DinhDangLeTan.gio(hd.getNgayThu()),
                hd.getHinhThucThanhToan(), DinhDangLeTan.tien(hd.getTongTien()),
                DinhDangLeTan.tien(hd.getTienKhachDua()), DinhDangLeTan.tien(hd.getTienThua()),
                hd.getMaNhanVien());
    }

    private record DuLieuThuTien(BigDecimal mucPhi, List<PhieuKham> phieuCho, HoaDon phiChuaDung) {
    }

    @Override
    public void lamMoi() {
        BenhNhan bnHienTai = benhNhan;
        boXuLy.thucHien(this, () -> new DuLieuThuTien(
                nghiepVu.mucPhiKham(),
                nghiepVu.phieuChoThanhToan(),
                bnHienTai == null ? null : nghiepVu.phiChuaDung(bnHienTai.getMaBenhNhan())), d -> {
            donGiaPhiKham = d.mucPhi();
            dsPhieuCho = List.copyOf(d.phieuCho());
            modelPhieu.setRowCount(0);
            for (PhieuKham p : dsPhieuCho)
                modelPhieu.addRow(new Object[] { p.getMaPhieuKham(), p.getTenBenhNhan(), ngay(p.getNgayKham()) });
            if (!dsPhieuCho.isEmpty()) {
                tablePhieu.setRowSelectionInterval(0, 0);
            } else {
                chonPhieu(null);
                lbThongTin.setText("(không có lượt khám nào chờ thanh toán)");
            }
            if (bnHienTai != null) {
                txtTimBN.setText(bnHienTai.getSoDienThoai());
                lbThongTinBN.setText(bnHienTai.getMaBenhNhan() + " - " + bnHienTai.getHoTen());
                lbMucPhi.setText(DinhDangLeTan.tien(donGiaPhiKham));
                hoaDonPhiDaThu = d.phiChuaDung();
                btnXemHoaDon.setEnabled(hoaDonPhiDaThu != null);
                btnLapPhieuNgay.setVisible(hoaDonPhiDaThu != null);
                lbHoaDonPhi.setText(hoaDonPhiDaThu == null ? " "
                        : "Phí khám đã được thu (" + hoaDonPhiDaThu.getMaHoaDon() + ") — có thể lập phiếu.");
            } else {
                lbMucPhi.setText("(chọn bệnh nhân để xem phí)");
            }
            trangThai.setText(" ");
        });
    }
}

