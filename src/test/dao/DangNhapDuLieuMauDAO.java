package dao;

import java.util.*;
import entity.*;

/** DAO dữ liệu mẫu cho luồng DangNhap. */
public final class DangNhapDuLieuMauDAO extends DangNhapDAO {
    private final DuLieuMauDungChung duLieu;

    DangNhapDuLieuMauDAO(DuLieuMauDungChung duLieu) {
        super(() -> { throw new java.sql.SQLException("DEMO không sử dụng JDBC."); });
        this.duLieu = duLieu;
    }

    @Override
    public void kiemTraKetNoi() {
        synchronized (duLieu) {
        }
    }

    @Override
    public NhanVien timNhanVien(String maNhanVien) {
        synchronized (duLieu) {
            NhanVien nhanVien = duLieu.danhSachNhanVien.get(maNhanVien);
            return nhanVien == null ? null : duLieu.saoChep(nhanVien);
        }
    }

    @Override
    public TaiKhoan timTaiKhoan(String maTaiKhoan) {
        synchronized (duLieu) {
            return duLieu.danhSachTaiKhoan.values().stream().filter(t -> t.getMaTaiKhoan().equalsIgnoreCase(maTaiKhoan))
                    .findFirst().map(duLieu::saoChep).orElse(null);
        }
    }
}
