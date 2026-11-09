package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;

/** DAO dữ liệu mẫu cho luồng TaiKhoan. */
public final class TaiKhoanDuLieuMauDAO extends TaiKhoanDAO {
    private final DuLieuMauDungChung duLieu;

    TaiKhoanDuLieuMauDAO(DuLieuMauDungChung duLieu) {
        super(() -> { throw new java.sql.SQLException("DEMO không sử dụng JDBC."); });
        this.duLieu = duLieu;
    }

    @Override
    public List<NhanVien> danhSachNhanVien() {
        synchronized (duLieu) {
            return duLieu.danhSachNhanVien.values().stream().map(duLieu::saoChep).toList();
        }
    }

    @Override
    public List<TaiKhoan> danhSachTaiKhoan() {
        synchronized (duLieu) {
            return duLieu.danhSachTaiKhoan.values().stream().map(duLieu::saoChep).toList();
        }
    }

    @Override
    public TaiKhoan timTaiKhoan(String maTaiKhoan) {
        synchronized (duLieu) {
            return duLieu.danhSachTaiKhoan.values().stream().filter(t -> t.getMaTaiKhoan().equalsIgnoreCase(maTaiKhoan))
                    .findFirst().map(duLieu::saoChep).orElse(null);
        }
    }

    @Override
    public String taoTaiKhoan(String maNhanVien, String matKhauDaBam) {
        synchronized (duLieu) {
            if (duLieu.danhSachTaiKhoan.values().stream().anyMatch(t -> t.getMaNhanVien().equals(maNhanVien)))
                throw new IllegalArgumentException("Nhân viên đã có tài khoản.");
            if (!duLieu.danhSachNhanVien.containsKey(maNhanVien) || !duLieu.danhSachNhanVien.get(maNhanVien).isConLamViec())
                throw new IllegalArgumentException("Nhân viên không còn làm việc.");
            String ma = duLieu.taoMaTaiKhoan(LocalDate.now());
            duLieu.danhSachTaiKhoan.put(ma, new TaiKhoan(ma, matKhauDaBam, TrangThaiTaiKhoan.HOAT_DONG, maNhanVien));
            return ma;
        }
    }

    @Override
    public void capNhatTrangThaiTaiKhoan(String maTaiKhoan, TrangThaiTaiKhoan trangThai) {
        synchronized (duLieu) {
            TaiKhoan taiKhoan = duLieu.danhSachTaiKhoan.get(maTaiKhoan);
            if (taiKhoan == null)
                throw new IllegalArgumentException("Tài khoản không tồn tại.");
            taiKhoan.setTrangThai(trangThai);
        }
    }
}
