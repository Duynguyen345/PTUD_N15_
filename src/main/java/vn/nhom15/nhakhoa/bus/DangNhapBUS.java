package vn.nhom15.nhakhoa.bus;

import java.util.Arrays;
import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

public final class DangNhapBUS {
    private final PhongKhamDAO phongKhamDAO;
    private final BoBamMatKhau boBamMatKhau;
    private final QuanLyPhienDangNhap quanLyPhien;

    public DangNhapBUS(PhongKhamDAO phongKhamDAO, BoBamMatKhau boBamMatKhau,
            QuanLyPhienDangNhap quanLyPhien) {
        this.phongKhamDAO = phongKhamDAO;
        this.boBamMatKhau = boBamMatKhau;
        this.quanLyPhien = quanLyPhien;
    }

    public PhienDangNhap dangNhap(String maTaiKhoan, char[] matKhau) {
        try {
            if (maTaiKhoan == null || maTaiKhoan.isBlank() || matKhau.length == 0)
                throw new IllegalArgumentException("Nhập đầy đủ mã tài khoản và mật khẩu.");
            if (!maTaiKhoan.trim().matches("TK\\d{10}"))
                throw new IllegalArgumentException(
                        "Mã tài khoản phải là mã TK gồm 12 ký tự do hệ thống cấp.");
            TaiKhoan taiKhoan = phongKhamDAO.timTaiKhoan(maTaiKhoan.trim());
            if (taiKhoan == null || !boBamMatKhau.kiemTraMatKhau(matKhau, taiKhoan.getMatKhau()))
                throw new IllegalArgumentException("Mã tài khoản hoặc mật khẩu không đúng.");
            if (taiKhoan.getTrangThai() == TrangThaiTaiKhoan.BI_KHOA)
                throw new IllegalArgumentException("Tài khoản đã bị khóa.");
            NhanVien nhanVien = phongKhamDAO.danhSachNhanVien().stream()
                    .filter(n -> n.getMaNhanVien().equals(taiKhoan.getMaNhanVien())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));
            if (!nhanVien.isConLamViec())
                throw new IllegalArgumentException("Nhân viên đã ngừng làm việc.");
            PhienDangNhap phien = new PhienDangNhap(nhanVien.getMaNhanVien(), nhanVien.getHoTen(),
                    nhanVien.getChucVu());
            quanLyPhien.batDauPhien(taiKhoan.getMaTaiKhoan(), phien);
            return phien;
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    public void kiemTraKetNoi() {
        phongKhamDAO.kiemTraKetNoi();
    }
}
