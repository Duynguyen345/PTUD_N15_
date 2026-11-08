package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.NhanSuBUS;
import vn.nhom15.nhakhoa.entity.*;

public final class GD_NhanVien extends ManHinhCoSo {
    private final NhanSuBUS nghiepVu;
    private final BangDuLieu<NhanVien> bang;
    private final JTextField ma = new JTextField();
    private final JTextField ten = new JTextField();
    private final JTextField soDienThoai = new JTextField();
    private final JTextField ngaySinh = new JTextField();
    private final JTextField cccd = new JTextField();
    private final JComboBox<ChucVu> chucVu = new JComboBox<>(ChucVu.values());
    private final JComboBox<String> gioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ", "Chưa cung cấp" });
    private final JCheckBox dangLamViec = new JCheckBox("Đang làm việc", true);
    private final JTextField oTimKiem = new JTextField(24);

    public GD_NhanVien(NhanSuBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Nhân viên", "Quản lý hồ sơ, chức vụ và trạng thái làm việc", boXuLy);
        this.nghiepVu = nghiepVu;
        ma.setEditable(false);
        bang = new BangDuLieu<>(
                new String[] { "Mã NV", "Họ tên", "Điện thoại", "Ngày sinh", "Chức vụ", "Làm việc" },
                n -> new Object[] { n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(),
                        ngay(n.getNgaySinh()), n.getChucVu(), n.isConLamViec() ? "Đang làm" : "Đã nghỉ" });
        bang.ganTimKiem(oTimKiem,
                n -> new String[] { n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(), n.getCccd() });
        oTimKiem.setToolTipText("Tìm theo mã nhân viên, họ tên, số điện thoại hoặc CCCD.");
        JPanel thanhCongCu = taoThanhThaoTac(taoNut("Tìm kiếm", this::timKiem),
                taoNut("Xóa tìm kiếm", this::lamMoi));
        thanhCongCu.add(new JLabel("Từ khóa:"));
        thanhCongCu.add(oTimKiem);
        oTimKiem.addActionListener(suKien -> timKiem());
        noiDung.add(thanhCongCu, BorderLayout.NORTH);
        noiDung.add(bang, BorderLayout.CENTER);
        JPanel vungPhai = new JPanel(new BorderLayout());
        vungPhai.add(taoBieuMau(
                new String[] { "Mã tự động", "Họ tên *", "Số điện thoại *", "Ngày sinh *", "Giới tính",
                        "CCCD", "Chức vụ", "Trạng thái" },
                ma, ten, soDienThoai, ngaySinh, gioiTinh, cccd, chucVu, dangLamViec), BorderLayout.NORTH);
        vungPhai.add(taoThanhThaoTac(taoNut("Thêm", () -> luu(true)), taoNut("Cập nhật", () -> luu(false)),
                taoNut("Xóa", this::xoa), taoNut("Làm mới", this::xoaBieuMau)), BorderLayout.SOUTH);
        noiDung.add(vungPhai, BorderLayout.EAST);
        bang.khiChonDong(n -> {
            ma.setText(n.getMaNhanVien());
            ten.setText(n.getHoTen());
            soDienThoai.setText(n.getSoDienThoai());
            ngaySinh.setText(ngay(n.getNgaySinh()));
            cccd.setText(n.getCccd());
            chucVu.setSelectedItem(n.getChucVu());
            gioiTinh.setSelectedIndex(n.getGioiTinh() == null ? 2 : n.getGioiTinh() ? 0 : 1);
            dangLamViec.setSelected(n.isConLamViec());
        });
        ngaySinh.setToolTipText("dd/MM/yyyy, nhân viên từ đủ 18 tuổi");
    }

    private void luu(boolean themMoi) {
        String khoa = themMoi ? null : bang.layDongDangChon().getMaNhanVien();
        NhanVien giaTri = new NhanVien(khoa, ten.getText(), soDienThoai.getText(), ngay(ngaySinh.getText()),
                gioiTinh.getSelectedIndex() == 2 ? null : gioiTinh.getSelectedIndex() == 0, cccd.getText(),
                (ChucVu) chucVu.getSelectedItem(), dangLamViec.isSelected());
        boXuLy.thucHien(this, () -> {
            nghiepVu.luuNhanVien(giaTri, themMoi);
            return true;
        }, ok -> {
            lamMoi();
            trangThai.setText("Đã lưu nhân viên. Nhập từ khóa để tìm lại.");
        });
    }

    private void xoa() {
        NhanVien layDongDangChon = bang.layDongDangChon();
        if (!xacNhan("Xóa nhân viên " + layDongDangChon.getHoTen()
                + "? Nhân viên có dữ liệu liên quan sẽ không được xóa."))
            return;
        boXuLy.thucHien(this, () -> {
            nghiepVu.xoaNhanVien(layDongDangChon.getMaNhanVien());
            return true;
        }, ok -> {
            lamMoi();
            trangThai.setText("Đã xóa nhân viên. Nhập từ khóa để tìm lại.");
        });
    }

    private void xoaBieuMau() {
        ma.setText("");
        ten.setText("");
        soDienThoai.setText("");
        ngaySinh.setText("");
        cccd.setText("");
        chucVu.setSelectedIndex(0);
        gioiTinh.setSelectedIndex(0);
        dangLamViec.setSelected(true);
        bang.boChonDong();
    }

    private void timKiem() {
        bang.capNhatDuLieu(java.util.List.of());
        xoaBieuMau();
        if (oTimKiem.getText().isBlank()) {
            trangThai.setText("Nhập từ khóa trước khi tìm kiếm.");
            return;
        }
        boXuLy.thucHien(this, nghiepVu::layDanhSachNhanVien, cacDong -> {
            bang.capNhatDuLieu(cacDong);
            trangThai.setText(bang.soDongHienThi() + " kết quả. Chọn một dòng để cập nhật.");
        });
    }

    @Override
    public void lamMoi() {
        oTimKiem.setText("");
        bang.capNhatDuLieu(java.util.List.of());
        xoaBieuMau();
        trangThai.setText("Chưa tìm kiếm. Nhập từ khóa rồi bấm Tìm kiếm hoặc Enter.");
    }
}
