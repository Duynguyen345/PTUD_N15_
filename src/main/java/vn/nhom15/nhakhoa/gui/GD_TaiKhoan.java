package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.NhanSuBUS;
import vn.nhom15.nhakhoa.entity.*;

public final class GD_TaiKhoan extends ManHinhCoSo {
    private final NhanSuBUS nghiepVu;
    private final BangDuLieu<TaiKhoan> bang;
    private final JComboBox<NhanVien> nhanVien = new JComboBox<>();
    private final JTextField maTaiKhoan = new JTextField("Hệ thống tự cấp khi tạo");
    private final JPasswordField matKhau = new JPasswordField();
    private final JTextField oTimKiem = new JTextField(20);
    private final JTextField oTimNhanVien = new JTextField();

    private record DuLieuTaiKhoan(List<NhanVien> danhSachNhanVien, List<TaiKhoan> danhSachTaiKhoan) {
    }

    public GD_TaiKhoan(NhanSuBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Tài khoản & phân quyền", "Mỗi nhân viên có một tài khoản. Quyền truy cập lấy từ chức vụ.",
                boXuLy);
        this.nghiepVu = nghiepVu;
        maTaiKhoan.setEditable(false);
        bang = new BangDuLieu<>(new String[] { "Mã tài khoản", "Mã nhân viên", "Trạng thái" },
                t -> new Object[] { t.getMaTaiKhoan(), t.getMaNhanVien(), t.getTrangThai() });
        bang.khiChonDong(t -> maTaiKhoan.setText(t.getMaTaiKhoan()));
        bang.ganTimKiem(oTimKiem);
        JPanel thanhCongCu = taoThanhThaoTac(taoNut("Tìm kiếm", this::timKiem),
                taoNut("Xóa tìm kiếm", this::lamMoi), taoNut("Khóa / Mở tài khoản", this::doiTrangThai));
        thanhCongCu.add(new JLabel("Mã TK/NV:"));
        thanhCongCu.add(oTimKiem);
        oTimKiem.addActionListener(suKien -> timKiem());
        noiDung.add(thanhCongCu, BorderLayout.NORTH);
        noiDung.add(bang, BorderLayout.CENTER);
        JPanel vungPhai = new JPanel(new BorderLayout(0, 16));
        vungPhai.add(
                taoBieuMau(new String[] { "Tìm mã/tên NV", "", "Nhân viên", "Mã tự sinh", "Mật khẩu mới" },
                        oTimNhanVien, taoNut("Tìm nhân viên", this::timNhanVien), nhanVien, maTaiKhoan,
                        matKhau),
                BorderLayout.NORTH);
        vungPhai.add(taoThanhThaoTac(taoNut("Tạo tài khoản", this::taoTaiKhoan)), BorderLayout.SOUTH);
        noiDung.add(vungPhai, BorderLayout.EAST);
        oTimNhanVien.addActionListener(suKien -> timNhanVien());
        oTimNhanVien.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void xoaLuaChon() {
                nhanVien.removeAllItems();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                xoaLuaChon();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                xoaLuaChon();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                xoaLuaChon();
            }
        });
    }

    private DuLieuTaiKhoan taiDuLieu() {
        return new DuLieuTaiKhoan(nghiepVu.layDanhSachNhanVien(), nghiepVu.danhSachTaiKhoan());
    }

    private void timKiem() {
        bang.capNhatDuLieu(List.of());
        if (oTimKiem.getText().isBlank()) {
            trangThai.setText("Nhập mã tài khoản hoặc mã nhân viên trước khi tìm kiếm.");
            return;
        }
        boXuLy.thucHien(this, nghiepVu::danhSachTaiKhoan, cacDong -> {
            bang.capNhatDuLieu(cacDong);
            trangThai.setText(bang.soDongHienThi() + " kết quả tìm kiếm.");
        });
    }

    private void timNhanVien() {
        nhanVien.removeAllItems();
        String tuKhoa = oTimNhanVien.getText().trim().toLowerCase(java.util.Locale.ROOT);
        if (tuKhoa.isEmpty()) {
            trangThai.setText("Nhập mã hoặc tên nhân viên cần tạo tài khoản.");
            return;
        }
        boXuLy.thucHien(this, this::taiDuLieu, DuLieuTaiKhoan -> {
            DuLieuTaiKhoan.danhSachNhanVien().stream().filter(NhanVien::isConLamViec)
                    .filter(n -> n.getMaNhanVien().toLowerCase(java.util.Locale.ROOT).contains(tuKhoa)
                            || n.getHoTen().toLowerCase(java.util.Locale.ROOT).contains(tuKhoa))
                    .filter(n -> DuLieuTaiKhoan.danhSachTaiKhoan().stream()
                            .noneMatch(t -> t.getMaNhanVien().equals(n.getMaNhanVien())))
                    .forEach(nhanVien::addItem);
            trangThai.setText(
                    nhanVien.getItemCount() + " nhân viên phù hợp, đang làm việc và chưa có tài khoản.");
        });
    }

    private void taoTaiKhoan() {
        NhanVien layDongDangChon = (NhanVien) nhanVien.getSelectedItem();
        if (layDongDangChon == null)
            throw new IllegalArgumentException("Không có nhân viên đang làm việc chưa có tài khoản.");
        char[] matKhauNhap = matKhau.getPassword();
        matKhau.setText("");
        boXuLy.thucHien(this, () -> nghiepVu.taoTaiKhoan(layDongDangChon.getMaNhanVien(), matKhauNhap),
                ma -> {
                    maTaiKhoan.setText(ma);
                    JOptionPane.showMessageDialog(this,
                            "Đã tạo tài khoản: " + ma + "\nDùng mã này và mật khẩu để đăng nhập.",
                            "Mã tài khoản được cấp", JOptionPane.INFORMATION_MESSAGE);
                    lamMoi();
                });
    }

    private void doiTrangThai() {
        TaiKhoan layDongDangChon = bang.layDongDangChon();
        if (!xacNhan("Đổi trạng thái tài khoản " + layDongDangChon.getMaTaiKhoan() + "?"))
            return;
        boXuLy.thucHien(this, () -> {
            nghiepVu.doiTrangThaiTaiKhoan(layDongDangChon);
            return true;
        }, ok -> {
            lamMoi();
            trangThai.setText("Đã cập nhật trạng thái. Nhập từ khóa để tìm lại.");
        });
    }

    @Override
    public void lamMoi() {
        oTimKiem.setText("");
        oTimNhanVien.setText("");
        bang.capNhatDuLieu(List.of());
        nhanVien.removeAllItems();
        maTaiKhoan.setText("Hệ thống tự cấp khi tạo");
        matKhau.setText("");
        trangThai.setText("Chưa tìm kiếm. Nhập mã rồi bấm Tìm kiếm hoặc Enter.");
    }
}
