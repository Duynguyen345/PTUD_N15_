package vn.nhom15.nhakhoa.bus;

import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.*;

/** Session thuộc về một ứng dụng, không dùng biến toàn cục. */
public final class QuanLyPhienDangNhap {
    private final PhongKhamDAO phongKhamDAO;
    private volatile PhienDangNhap phienHienTai;
    private volatile String maTaiKhoan;

    public QuanLyPhienDangNhap(PhongKhamDAO phongKhamDAO) {
        this.phongKhamDAO = phongKhamDAO;
    }

    void batDauPhien(String maTaiKhoan, PhienDangNhap phien) {
        this.maTaiKhoan = maTaiKhoan;
        phienHienTai = phien;
    }

    public void dangXuat() {
        phienHienTai = null;
        maTaiKhoan = null;
    }

    public PhienDangNhap phienHienTai() {
        return phienHienTai;
    }

    public void kiemTraQuyenQuanLy() {
        kiemTraQuyen(ChucVu.QUAN_LY);
    }

    public PhienDangNhap kiemTraQuyen(ChucVu vaiTro) {
        PhienDangNhap phien = phienHienTai;
        String dangNhap = maTaiKhoan;
        if (phien == null || phien.getChucVu() != vaiTro || dangNhap == null)
            throw new SecurityException("Bạn không có quyền thực hiện chức năng này.");
        TaiKhoan taiKhoan = phongKhamDAO.timTaiKhoan(dangNhap);
        NhanVien nhanVien = phongKhamDAO.danhSachNhanVien().stream()
                .filter(n -> n.getMaNhanVien().equals(phien.getMaNhanVien())).findFirst().orElse(null);
        if (taiKhoan == null || taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG || nhanVien == null
                || !nhanVien.isConLamViec() || nhanVien.getChucVu() != vaiTro)
            throw new SecurityException("Quyền truy cập đã thay đổi. Vui lòng đăng xuất.");
        return phien;
    }
}
