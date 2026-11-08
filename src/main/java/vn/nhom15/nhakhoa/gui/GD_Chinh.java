package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.entity.ChucVu;

/** Cửa sổ duy nhất; đăng xuất hủy toàn bộ màn hình của phiên trước. */
public final class GD_Chinh extends JFrame {
    private final DangNhapBUS dangNhapBUS;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final NhanSuBUS nhanSuBUS;
    private final DanhMucBUS danhMucBUS;
    private final BaoCaoBUS baoCaoBUS;
    private final boolean duLieuMau;
    private final BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
    private JPanel vungNoiDung;
    private JLabel tenTrang;

    public GD_Chinh(DangNhapBUS dangNhapBUS, QuanLyPhienDangNhap quanLyPhien, NhanSuBUS nhanSuBUS,
            DanhMucBUS danhMucBUS, BaoCaoBUS baoCaoBUS, boolean duLieuMau) {
        super("Nha khoa Sài Gòn — Quản lý");
        this.dangNhapBUS = dangNhapBUS;
        this.quanLyPhien = quanLyPhien;
        this.nhanSuBUS = nhanSuBUS;
        this.danhMucBUS = danhMucBUS;
        this.baoCaoBUS = baoCaoBUS;
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
            themNutDieuHuong(thanhBen, "Tổng quan", () -> new GD_TongQuan(nhanSuBUS, danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Nhân viên", () -> new GD_NhanVien(nhanSuBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Tài khoản", () -> new GD_TaiKhoan(nhanSuBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Dịch vụ", () -> new GD_DichVu(danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Hàng hóa", () -> new GD_HangHoa(danhMucBUS, boXuLy));
            themNutDieuHuong(thanhBen, "Báo cáo", () -> new GD_BaoCao(baoCaoBUS, boXuLy));
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
            moManHinh("Tổng quan", new GD_TongQuan(nhanSuBUS, danhMucBUS, boXuLy));
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
