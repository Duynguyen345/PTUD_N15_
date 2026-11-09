package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;

/** DAO dữ liệu mẫu cho luồng NhanVien. */
public final class NhanVienDuLieuMauDAO extends NhanVienDAO {
    private final DuLieuMauDungChung duLieu;

    NhanVienDuLieuMauDAO(DuLieuMauDungChung duLieu) {
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
    public void luuNhanVien(NhanVien nhanVien, boolean themMoi) {
        synchronized (duLieu) {
            NhanVien giaTri = duLieu.saoChep(nhanVien);
            if (themMoi)
                giaTri.setMaNhanVien(duLieu.taoMaTheoNgay("NV", LocalDate.now(), duLieu.danhSachNhanVien::containsKey));
            else if (!duLieu.danhSachNhanVien.containsKey(giaTri.getMaNhanVien()))
                throw new IllegalArgumentException("Nhân viên không tồn tại.");
            duLieu.danhSachNhanVien.put(giaTri.getMaNhanVien(), giaTri);
        }
    }

    @Override
    public void xoaNhanVien(String ma) {
        synchronized (duLieu) {
            if (duLieu.danhSachTaiKhoan.values().stream().anyMatch(t -> t.getMaNhanVien().equals(ma))
                    || duLieu.nhanVienDaThamChieu.contains(ma))
                throw new IllegalArgumentException("Nhân viên đang có tài khoản hoặc phiếu khám, không thể xóa.");
            if (duLieu.danhSachNhanVien.remove(ma) == null)
                throw new IllegalArgumentException("Nhân viên không tồn tại.");
        }
    }
}
