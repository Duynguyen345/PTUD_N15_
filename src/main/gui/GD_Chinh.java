package gui;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;
import bus.*;
import entity.ChucVu;
import entity.Quyen;

/** Cửa sổ duy nhất; đăng xuất hủy toàn bộ màn hình của phiên trước. */
public final class GD_Chinh extends JFrame {
    private final DangNhapBUS dangNhapBUS;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final NhanVienBUS nhanVienBUS;
    private final TaiKhoanBUS taiKhoanBUS;
    private final HangHoaBUS hangHoaBUS;
    private final DichVuBUS dichVuBUS;
    private final BaoCaoBUS baoCaoBUS;
    private final BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
    private DieuPhoiBUS dieuPhoiBUS;
    private TraCuuHoSoBUS traCuuBUS;
    private LeTanBUS leTanBUS;
    private GD_BangGoiSo bangGoiSo;

    public GD_Chinh(DangNhapBUS dangNhapBUS, QuanLyPhienDangNhap quanLyPhien, NhanVienBUS nhanVienBUS,
            TaiKhoanBUS taiKhoanBUS, DichVuBUS dichVuBUS, HangHoaBUS hangHoaBUS, BaoCaoBUS baoCaoBUS,
            DieuPhoiBUS dieuPhoiBUS, TraCuuHoSoBUS traCuuBUS, LeTanBUS leTanBUS) {
        this(dangNhapBUS, quanLyPhien, nhanVienBUS, taiKhoanBUS, dichVuBUS, hangHoaBUS, baoCaoBUS);
        this.dieuPhoiBUS = dieuPhoiBUS;
        this.traCuuBUS = traCuuBUS;
        this.leTanBUS = leTanBUS;
    }

    private JPanel vungNoiDung;
    private JLabel tenTrang;

    public GD_Chinh(DangNhapBUS dangNhapBUS, QuanLyPhienDangNhap quanLyPhien, NhanVienBUS nhanVienBUS, TaiKhoanBUS taiKhoanBUS,
            DichVuBUS dichVuBUS, HangHoaBUS hangHoaBUS, BaoCaoBUS baoCaoBUS) {
        super("Nha khoa Sài Gòn — Quản lý");
        this.dangNhapBUS = dangNhapBUS;
        this.quanLyPhien = quanLyPhien;
        this.nhanVienBUS = nhanVienBUS;
        this.taiKhoanBUS = taiKhoanBUS;
        this.hangHoaBUS = hangHoaBUS;
        this.dichVuBUS = dichVuBUS;
        this.baoCaoBUS = baoCaoBUS;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1380, 820);
        setLocationRelativeTo(null);
        hienThiDangNhap();
    }

    private void hienThiDangNhap() {
        if (bangGoiSo != null) { bangGoiSo.dispose(); bangGoiSo = null; }
        quanLyPhien.dangXuat();
        GD_DangNhap dangNhap = new GD_DangNhap(dangNhapBUS, boXuLy, this::hienThiKhongGianLamViec);
        setContentPane(dangNhap);
        revalidate();
        repaint();
        dangNhap.lamMoi();
    }

    private void hienThiKhongGianLamViec() {
        JPanel goc = new JPanel(new BorderLayout());
        JPanel thanhBen = new JPanel();
        thanhBen.setLayout(new BoxLayout(thanhBen, BoxLayout.Y_AXIS));
        thanhBen.setBackground(new Color(19, 43, 65));
        thanhBen.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));
        thanhBen.setPreferredSize(new Dimension(220, 0));
        JLabel thuongHieu = new JLabel("NHA KHOA SÀI GÒN");
        thuongHieu.setForeground(Color.WHITE);
        thuongHieu.setFont(new Font("Segoe UI", Font.BOLD, 17));
        thanhBen.add(thuongHieu);
        thanhBen.add(Box.createVerticalStrut(26));
        vungNoiDung = new JPanel(new BorderLayout());
        JPanel vungGiua = new JPanel(new BorderLayout());
        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        thanhTren.setBackground(Color.WHITE);
        tenTrang = new JLabel("Không gian quản lý");
        thanhTren.add(tenTrang, BorderLayout.WEST);
        thanhTren.add(new KhungThongTinNguoiDung(quanLyPhien.phienHienTai()), BorderLayout.EAST);
        vungGiua.add(thanhTren, BorderLayout.NORTH);
        vungGiua.add(vungNoiDung, BorderLayout.CENTER);
        goc.add(thanhBen, BorderLayout.WEST);
        goc.add(vungGiua, BorderLayout.CENTER);
        if (quanLyPhien.phienHienTai().getChucVu() == ChucVu.QUAN_LY) {
            themNutDieuHuong(thanhBen, "Tổng quan", () -> new GD_TongQuan(nhanVienBUS, dichVuBUS, hangHoaBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Nhân viên", () -> new GD_NhanVien(nhanVienBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Tài khoản", () -> new GD_TaiKhoan(taiKhoanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Dịch vụ", () -> new GD_DichVu(dichVuBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Hàng hóa", () -> new GD_HangHoa(hangHoaBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Báo cáo", () -> new GD_BaoCao(baoCaoBUS, boXuLy));
        }
        if (dieuPhoiBUS != null && coQuyen(Quyen.XEM_HANG_CHO))
            themNutDieuHuong(thanhBen, "Hàng chờ / Gọi số", this::taoHangCho);
        if (dieuPhoiBUS != null && coQuyen(Quyen.BAT_TAT_PHONG))
            themNutDieuHuong(thanhBen, "Phòng nhận bệnh", () -> new GD_PhongKham(dieuPhoiBUS, boXuLy));
        if (traCuuBUS != null && coQuyen(Quyen.TRA_CUU_HO_SO))
            themNutDieuHuong(thanhBen, "Tra cứu hồ sơ", () -> new GD_TraCuuHoSo(traCuuBUS, boXuLy));
        if (leTanBUS != null && quanLyPhien.phienHienTai().getChucVu() == ChucVu.LE_TAN) {
            themNutDieuHuong(thanhBen, "Tổng quan lễ tân", () -> new GD_TongQuanLeTan(leTanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Hồ sơ bệnh nhân", () -> new GD_HoSoBenhNhan(leTanBUS, boXuLy, this::moThuTienChoBenhNhan));
            themNutDieuHuong(thanhBen, "Lập phiếu khám", () -> new GD_LapPhieuKham(leTanBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Thu tiền", () -> new GD_ThuTien(leTanBUS, boXuLy, null, this::moLapPhieuChoBenhNhan));
            themNutDieuHuong(thanhBen, "Ca thu ngân", () -> new GD_ChotCa(leTanBUS, boXuLy));
        }
        thanhBen.add(Box.createVerticalGlue());
        JButton nutDangXuat = new JButton("Đăng xuất");
        nutDangXuat.setMaximumSize(new Dimension(190, 42));
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
            moManHinh("Tổng quan", new GD_TongQuan(nhanVienBUS, dichVuBUS, hangHoaBUS, boXuLy));
        else if (leTanBUS != null && quanLyPhien.phienHienTai().getChucVu() == ChucVu.LE_TAN)
            moManHinh("Tổng quan lễ tân", new GD_TongQuanLeTan(leTanBUS, boXuLy));
        else if (dieuPhoiBUS != null && coQuyen(Quyen.XEM_HANG_CHO))
            moManHinh("Hàng chờ / Gọi số", taoHangCho());
        else if (traCuuBUS != null && coQuyen(Quyen.TRA_CUU_HO_SO))
            moManHinh("Tra cứu hồ sơ", new GD_TraCuuHoSo(traCuuBUS, boXuLy));
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

    private void moThuTienChoBenhNhan(entity.BenhNhan bn) {
        moManHinh("Thu tiền", new GD_ThuTien(leTanBUS, boXuLy, bn, this::moLapPhieuChoBenhNhan));
    }

    private void moLapPhieuChoBenhNhan(entity.BenhNhan bn) {
        moManHinh("Lập phiếu khám", new GD_LapPhieuKham(leTanBUS, boXuLy, bn));
    }

    private boolean coQuyen(Quyen quyen) {
        return quanLyPhien.phienHienTai() != null
                && PhanQuyenBUS.layQuyen(quanLyPhien.phienHienTai().getChucVu()).contains(quyen);
    }

    private GD_HangCho taoHangCho() {
        return new GD_HangCho(dieuPhoiBUS, coQuyen(Quyen.DIEU_PHOI_HANG_CHO), boXuLy, () -> {
            if (bangGoiSo == null || !bangGoiSo.isDisplayable()) bangGoiSo = new GD_BangGoiSo(dieuPhoiBUS);
            bangGoiSo.setVisible(true);
            bangGoiSo.toFront();
        });
    }

    @Override
    public void dispose() {
        if (bangGoiSo != null) bangGoiSo.dispose();
        super.dispose();
    }

    private void themNutDieuHuong(JPanel thanhBen, String nhan, Supplier<ManHinhCoSo> manHinh) {
        JButton taoNut = new JButton(nhan);
        taoNut.setHorizontalAlignment(SwingConstants.LEFT);
        taoNut.setMaximumSize(new Dimension(190, 44));
        taoNut.setPreferredSize(new Dimension(190, 44));
        taoNut.addActionListener(suKien -> {
            if (!boXuLy.dangXuLy())
                moManHinh(nhan, manHinh.get());
        });
        thanhBen.add(taoNut);
        thanhBen.add(Box.createVerticalStrut(9));
    }

    private void moManHinh(String tieuDe, ManHinhCoSo khung) {
        tenTrang.setText(tieuDe);
        vungNoiDung.removeAll();
        vungNoiDung.add(khung);
        vungNoiDung.revalidate();
        vungNoiDung.repaint();
        if (!(khung instanceof GD_BaoCao))
            khung.lamMoi();
    }
}
