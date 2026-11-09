package bus;

import dao.DangNhapDAO;
import entity.*;

/** Session thuộc về một ứng dụng, không dùng biến toàn cục. */
public final class QuanLyPhienDangNhap {
    private final DangNhapDAO dangNhapDAO;
    private volatile PhienDangNhap phienHienTai;
    private volatile String maTaiKhoan;

    public QuanLyPhienDangNhap(DangNhapDAO dangNhapDAO) {
        this.dangNhapDAO = dangNhapDAO;
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

    public synchronized PhienDangNhap kiemTraQuyen(ChucVu vaiTro) {
        kiemTraQuyen(Quyen.LE_TAN);
        if (phienHienTai.getChucVu() != vaiTro)
            throw new SecurityException("Bạn không có quyền thực hiện chức năng này.");
        return phienHienTai;
    }

    /** Kiểm tra lại tài khoản và vai trò hiện hành, kể cả sau khi đã đăng nhập. */
    public synchronized void kiemTraQuyen(Quyen quyen) {
        PhienDangNhap phien = phienHienTai;
        String dangNhap = maTaiKhoan;
        if (phien == null || dangNhap == null)
            throw new SecurityException("Vui lòng đăng nhập trước khi sử dụng chức năng.");
        TaiKhoan taiKhoan = dangNhapDAO.timTaiKhoan(dangNhap);
        NhanVien nhanVien = dangNhapDAO.timNhanVien(phien.getMaNhanVien());
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
