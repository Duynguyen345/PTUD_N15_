package bus;

import java.util.Arrays;
import dao.DangNhapDAO;
import entity.*;
import util.BoBamMatKhau;

public final class DangNhapBUS {
    private final DangNhapDAO dangNhapDAO;
    private final BoBamMatKhau boBamMatKhau;
    private final QuanLyPhienDangNhap quanLyPhien;

    public DangNhapBUS(DangNhapDAO dangNhapDAO, BoBamMatKhau boBamMatKhau,
            QuanLyPhienDangNhap quanLyPhien) {
        this.dangNhapDAO = dangNhapDAO;
        this.boBamMatKhau = boBamMatKhau;
        this.quanLyPhien = quanLyPhien;
    }

    public PhienDangNhap dangNhap(String maTaiKhoan, char[] matKhau) {
        quanLyPhien.dangXuat();
        try {
            if (maTaiKhoan == null || maTaiKhoan.isBlank() || matKhau == null || matKhau.length == 0)
                throw new IllegalArgumentException("Nhập đầy đủ mã tài khoản và mật khẩu.");
            if (!maTaiKhoan.trim().matches("TK\\d{10}"))
                throw new IllegalArgumentException(
                        "Mã tài khoản phải là mã TK gồm 12 ký tự do hệ thống cấp.");
            TaiKhoan taiKhoan = dangNhapDAO.timTaiKhoan(maTaiKhoan.trim());
            if (taiKhoan == null || !boBamMatKhau.kiemTraMatKhau(matKhau, taiKhoan.getMatKhau()))
                throw new IllegalArgumentException("Mã tài khoản hoặc mật khẩu không đúng.");
            if (taiKhoan.getTrangThai() == TrangThaiTaiKhoan.BI_KHOA)
                throw new IllegalArgumentException("Tài khoản đã bị khóa.");
            NhanVien nhanVien = dangNhapDAO.timNhanVien(taiKhoan.getMaNhanVien());
            if (nhanVien == null)
                throw new IllegalArgumentException("Không tìm thấy nhân viên.");
            if (!nhanVien.isConLamViec())
                throw new IllegalArgumentException("Nhân viên đã ngừng làm việc.");
            PhienDangNhap phien = new PhienDangNhap(nhanVien.getMaNhanVien(), nhanVien.getHoTen(),
                    nhanVien.getChucVu());
            quanLyPhien.batDauPhien(taiKhoan.getMaTaiKhoan(), phien);
            return phien;
        } finally {
            if (matKhau != null)
                Arrays.fill(matKhau, '\0');
        }
    }

    public void kiemTraKetNoi() {
        dangNhapDAO.kiemTraKetNoi();
    }
}
