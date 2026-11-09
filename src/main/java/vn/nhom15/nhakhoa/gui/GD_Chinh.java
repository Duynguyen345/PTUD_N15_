package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.entity.ChucVu;

/** Cửa sổ duy nhất; đăng xuất hủy toàn bộ màn hình của phiên trước. */
public final class GD_Chinh extends JFrame {
    private static final Color MAU_CHU_MENU = new Color(23, 58, 94);
    private static final Color MAU_NEN_CHON = new Color(22, 65, 104);
    private static final Color MAU_VIEN_NUT = new Color(216, 226, 236);

    private final DangNhapBUS dangNhapBUS;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final NhanSuBUS nhanSuBUS;
    private final DanhMucBUS danhMucBUS;
    private final BaoCaoBUS baoCaoBUS;
    private final LeTanBUS leTanBUS;
    private final boolean duLieuMau;
    private final BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
    private final List<MucMenu> danhSachMenu = new ArrayList<>();
    private JPanel vungNoiDung;
    private JLabel tenTrang;

    private record MucMenu(String nhan, JButton nut, Icon iconThuong, Icon iconChon) {
    }

    public GD_Chinh(DangNhapBUS dangNhapBUS, QuanLyPhienDangNhap quanLyPhien, NhanSuBUS nhanSuBUS,
            DanhMucBUS danhMucBUS, BaoCaoBUS baoCaoBUS, boolean duLieuMau) {
        this(dangNhapBUS, quanLyPhien, nhanSuBUS, danhMucBUS, baoCaoBUS, null, duLieuMau);
    }

    public GD_Chinh(DangNhapBUS dangNhapBUS, QuanLyPhienDangNhap quanLyPhien, NhanSuBUS nhanSuBUS,
            DanhMucBUS danhMucBUS, BaoCaoBUS baoCaoBUS, LeTanBUS leTanBUS, boolean duLieuMau) {
        super("Nha khoa Sài Gòn — Quản lý");
        this.dangNhapBUS = dangNhapBUS;
        this.quanLyPhien = quanLyPhien;
        this.nhanSuBUS = nhanSuBUS;
        this.danhMucBUS = danhMucBUS;
        this.baoCaoBUS = baoCaoBUS;
        this.leTanBUS = leTanBUS;
        this.duLieuMau = duLieuMau;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1380, 820);
        setLocationRelativeTo(null);
        hienThiDangNhap();
    }

    private void hienThiDangNhap() {
        quanLyPhien.dangXuat();
        GD_DangNhap dangNhap = new GD_DangNhap(dangNhapBUS, boXuLy, duLieuMau, this::hienThiKhongGianLamViec);
        setContentPane(dangNhap);
        revalidate();
        repaint();
        dangNhap.lamMoi();
    }

    private void hienThiKhongGianLamViec() {
        danhSachMenu.clear();
        JPanel goc = new JPanel(new BorderLayout());

        // ---------- HEADER TRÊN CÙNG (Logo + NHA KHOA SÀI GÒN / DENTAL CLINIC + User chip) ----------
        JPanel thanhDau = new JPanel(new BorderLayout());
        thanhDau.setBackground(Color.WHITE);
        thanhDau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 242)),
                BorderFactory.createEmptyBorder(10, 22, 10, 22)));

        JPanel cumLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cumLogo.setOpaque(false);
        Icon logo = taiIcon("logo_crop.png", 44);
        if (logo != null)
            cumLogo.add(new JLabel(logo));
        JPanel chuThuongHieu = new JPanel(new GridLayout(2, 1, 0, 1));
        chuThuongHieu.setOpaque(false);
        JLabel lbTenPK = new JLabel("NHA KHOA SÀI GÒN");
        lbTenPK.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbTenPK.setForeground(MAU_CHU_MENU);
        JLabel lbPhuPK = new JLabel("DENTAL CLINIC");
        lbPhuPK.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbPhuPK.setForeground(new Color(122, 140, 163));
        chuThuongHieu.add(lbTenPK);
        chuThuongHieu.add(lbPhuPK);
        cumLogo.add(chuThuongHieu);
        thanhDau.add(cumLogo, BorderLayout.WEST);

        tenTrang = new JLabel("Không gian làm việc");
        tenTrang.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tenTrang.setForeground(MAU_CHU_MENU);
        thanhDau.add(new KhungThongTinNguoiDung(quanLyPhien.phienHienTai()), BorderLayout.EAST);
        goc.add(thanhDau, BorderLayout.NORTH);

        // ---------- SIDEBAR BÊN TRÁI (nền sáng + chữ CHỨC NĂNG + các nút có icon từ /pic) ----------
        JPanel thanhBen = new JPanel();
        thanhBen.setLayout(new BoxLayout(thanhBen, BoxLayout.Y_AXIS));
        thanhBen.setBackground(new Color(245, 248, 252));
        thanhBen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 230, 240)),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        thanhBen.setPreferredSize(new Dimension(280, 0));

        JLabel nhanChucNang = new JLabel("CHỨC NĂNG");
        nhanChucNang.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nhanChucNang.setForeground(new Color(122, 140, 163));
        nhanChucNang.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 0));
        nhanChucNang.setAlignmentX(Component.LEFT_ALIGNMENT);
        thanhBen.add(nhanChucNang);

        vungNoiDung = new JPanel(new BorderLayout());
        goc.add(thanhBen, BorderLayout.WEST);
        goc.add(vungNoiDung, BorderLayout.CENTER);

        if (quanLyPhien.phienHienTai().getChucVu() == ChucVu.QUAN_LY) {
            themNutDieuHuong(thanhBen, "Tổng quan", "icon_house.png",
                    () -> new GD_TongQuan(nhanSuBUS, danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Nhân viên", "icon_users.png",
                    () -> new GD_NhanVien(nhanSuBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Tài khoản", "icon_lock.png",
                    () -> new GD_TaiKhoan(nhanSuBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Dịch vụ", "icon_tooth.png",
                    () -> new GD_DichVu(danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Hàng hóa", "icon_folder.png",
                    () -> new GD_HangHoa(danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Báo cáo", "icon_chart.png",
                    () -> new GD_BaoCao(baoCaoBUS, boXuLy));
        } else if (laLeTan()) {
            themNutDieuHuong(thanhBen, "Tổng quan", "icon_house.png",
                    () -> new GD_TongQuanLeTan(leTanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Hồ sơ bệnh nhân", "icon_folder.png",
                    () -> new GD_HoSoBenhNhan(leTanBUS, boXuLy, this::moThuTienChoBenhNhan));
            themNutDieuHuong(thanhBen, "Tra cứu hồ sơ bệnh nhân", "icon_search.png",
                    () -> new GD_TraCuuBenhNhan(leTanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Thu tiền (phí + dịch vụ)", "icon_dollar.png",
                    () -> new GD_ThuTien(leTanBUS, boXuLy, null, this::moLapPhieuChoBenhNhan));
            themNutDieuHuong(thanhBen, "Lập phiếu khám", "icon_clipboard.png",
                    () -> new GD_LapPhieuKham(leTanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Quản lý ca thu ngân", "icon_receipt.png",
                    () -> new GD_ChotCa(leTanBUS, boXuLy));
        }

        thanhBen.add(Box.createVerticalGlue());
        JButton nutDangXuat = taoNutMenu("Đăng xuất", taiIcon("icon_logout.png", 20));
        nutDangXuat.addActionListener(suKien -> {
            if (!boXuLy.dangXuLy() && JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi hệ thống?",
                    "Đăng xuất", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                hienThiDangNhap();
        });
        thanhBen.add(nutDangXuat);

        setContentPane(goc);
        revalidate();
        repaint();
        if (quanLyPhien.phienHienTai().getChucVu() == ChucVu.QUAN_LY)
            moManHinh("Tổng quan", new GD_TongQuan(nhanSuBUS, danhMucBUS, boXuLy));
        else if (laLeTan())
            moManHinh("Tổng quan", new GD_TongQuanLeTan(leTanBUS, boXuLy));
        else {
            tenTrang.setText("Phiên " + quanLyPhien.phienHienTai().getChucVu());
            JTextArea thongBaoQuyen = new JTextArea("Đăng nhập thành công với vai trò "
                    + quanLyPhien.phienHienTai().getChucVu()
                    + ".\n\nCác chức năng quản trị trong ứng dụng chỉ dành cho Quản lý.\nBạn không có quyền mở các màn quản lý.");
            thongBaoQuyen.setEditable(false);
            thongBaoQuyen.setLineWrap(true);
            thongBaoQuyen.setWrapStyleWord(true);
            thongBaoQuyen.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            thongBaoQuyen.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            vungNoiDung.add(thongBaoQuyen);
            revalidate();
        }
    }

    private boolean laLeTan() {
        return leTanBUS != null && quanLyPhien.phienHienTai().getChucVu() == ChucVu.LE_TAN;
    }

    private void moThuTienChoBenhNhan(vn.nhom15.nhakhoa.entity.BenhNhan bn) {
        moManHinh("Thu tiền (phí + dịch vụ)",
                new GD_ThuTien(leTanBUS, boXuLy, bn, this::moLapPhieuChoBenhNhan));
    }

    private void moLapPhieuChoBenhNhan(vn.nhom15.nhakhoa.entity.BenhNhan bn) {
        moManHinh("Lập phiếu khám", new GD_LapPhieuKham(leTanBUS, boXuLy, bn));
    }

    private void themNutDieuHuong(JPanel thanhBen, String nhan, String tenIcon,
            Supplier<ManHinhCoSo> manHinh) {
        Icon thuong = taiIcon(tenIcon, 22);
        Icon chon = taiIcon(tenIcon.replace(".png", "_w.png"), 22);
        if (chon == null)
            chon = thuong;
        JButton nut = taoNutMenu(nhan, thuong);
        nut.addActionListener(suKien -> {
            if (!boXuLy.dangXuLy())
                moManHinh(nhan, manHinh.get());
        });
        danhSachMenu.add(new MucMenu(nhan, nut, thuong, chon));
        thanhBen.add(nut);
        thanhBen.add(Box.createVerticalStrut(6));
    }

    private JButton taoNutMenu(String nhan, Icon bieuTuong) {
        JButton nut = new JButton(nhan, bieuTuong);
        nut.setHorizontalAlignment(SwingConstants.LEFT);
        nut.setIconTextGap(12);
        nut.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nut.setFocusPainted(false);
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_VIEN_NUT, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        nut.setMaximumSize(new Dimension(256, 46));
        nut.setPreferredSize(new Dimension(256, 46));
        nut.setAlignmentX(Component.LEFT_ALIGNMENT);
        nut.setBackground(Color.WHITE);
        nut.setForeground(MAU_CHU_MENU);
        return nut;
    }

    private void danhDauMenuDangChon(String tieuDe) {
        for (MucMenu muc : danhSachMenu) {
            boolean chon = muc.nhan().equals(tieuDe);
            muc.nut().setBackground(chon ? MAU_NEN_CHON : Color.WHITE);
            muc.nut().setForeground(chon ? Color.WHITE : MAU_CHU_MENU);
            muc.nut().setIcon(chon ? muc.iconChon() : muc.iconThuong());
        }
    }

    private static Icon taiIcon(String tenFile, int kichThuoc) {
        try (var luong = GD_Chinh.class.getResourceAsStream("/pic/" + tenFile)) {
            if (luong == null)
                return null;
            BufferedImage goc = ImageIO.read(luong);
            if (goc == null)
                return null;
            BufferedImage thuNho = new BufferedImage(kichThuoc, kichThuoc, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = thuNho.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(goc, 0, 0, kichThuoc, kichThuoc, null);
            } finally {
                g2.dispose();
            }
            return new ImageIcon(thuNho);
        } catch (Exception boQua) {
            return null;
        }
    }

    private void moManHinh(String tieuDe, ManHinhCoSo khung) {
        tenTrang.setText(tieuDe);
        danhDauMenuDangChon(tieuDe);
        vungNoiDung.removeAll();
        vungNoiDung.add(khung);
        vungNoiDung.revalidate();
        vungNoiDung.repaint();
        if (!(khung instanceof GD_BaoCao))
            khung.lamMoi();
    }
}
