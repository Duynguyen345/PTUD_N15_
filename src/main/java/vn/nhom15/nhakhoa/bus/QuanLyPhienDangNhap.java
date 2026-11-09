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

    synchronized void batDauPhien(String maTaiKhoan, PhienDangNhap phien) {
        this.maTaiKhoan = maTaiKhoan;
        phienHienTai = phien;
    }

    public synchronized void dangXuat() {
        phienHienTai = null;
        maTaiKhoan = null;
    }

    public PhienDangNhap phienHienTai() {
        return phienHienTai;
    }

    public void kiemTraQuyenQuanLy() {
        kiemTraQuyen(Quyen.QUAN_TRI);
    }

    /** Kiểm tra lại tài khoản và vai trò hiện hành, kể cả sau khi đã đăng nhập. */
    public synchronized void kiemTraQuyen(Quyen quyen) {
        PhienDangNhap phien = phienHienTai;
        String dangNhap = maTaiKhoan;
        if (phien == null || dangNhap == null)
            throw new SecurityException("Vui lòng đăng nhập trước khi sử dụng chức năng.");
        TaiKhoan taiKhoan = phongKhamDAO.timTaiKhoan(dangNhap);
        NhanVien nhanVien = phongKhamDAO.danhSachNhanVien().stream()
                .filter(n -> n.getMaNhanVien().equals(phien.getMaNhanVien())).findFirst().orElse(null);
        if (taiKhoan == null || taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG || nhanVien == null
                || !taiKhoan.getMaNhanVien().equals(phien.getMaNhanVien())
                || !nhanVien.isConLamViec() || nhanVien.getChucVu() != phien.getChucVu()) {
            dangXuat();
            throw new SecurityException("Quyền truy cập đã thay đổi. Vui lòng đăng xuất.");
        }
        if (!PhanQuyenBUS.layQuyen(nhanVien.getChucVu()).contains(quyen))
            throw new SecurityException("Vai trò " + nhanVien.getChucVu() + " không được thực hiện chức năng này.");
    }
}
