package dao;

import java.sql.*;
import connect.NguonKetNoi;
import entity.*;

/** Truy cập SQL cho luồng DangNhap. */
public class DangNhapDAO extends JdbcDAO {
    public DangNhapDAO(NguonKetNoi nguonKetNoi) {
        super(nguonKetNoi);
    }

    public void kiemTraKetNoi() {
        docDuLieu(c -> {
            if (!c.isValid(5))
                throw new SQLException("Kết nối không hợp lệ.");
            return null;
        });
    }

    public NhanVien timNhanVien(String maNhanVien) {
        return docDuLieu(c -> truyVan(c,
                "SELECT maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec FROM NhanVien WHERE maNhanVien=?",
                r -> {
                    Date ngaySinh = r.getDate("ngaySinh");
                    Boolean gioiTinh = r.getObject("gioiTinh") == null ? null : r.getBoolean("gioiTinh");
                    return new NhanVien(r.getString("maNhanVien"), r.getString("hoTen"),
                            r.getString("soDienThoai"), ngaySinh == null ? null : ngaySinh.toLocalDate(),
                            gioiTinh, r.getString("cccd"), ChucVu.valueOf(r.getString("chucVu")),
                            r.getBoolean("conLamViec"));
                }, maNhanVien).stream().findFirst().orElse(null));
    }

    public TaiKhoan timTaiKhoan(String maTaiKhoan) {
        return docDuLieu(c -> truyVan(c,
                "SELECT maTaiKhoan,matKhau,trangThai,maNhanVien FROM TaiKhoan WHERE maTaiKhoan=?",
                this::docTaiKhoan, maTaiKhoan).stream().findFirst().orElse(null));
    }

    private TaiKhoan docTaiKhoan(ResultSet r) throws SQLException {
        return new TaiKhoan(r.getString("maTaiKhoan"), r.getString("matKhau"),
                TrangThaiTaiKhoan.valueOf(r.getString("trangThai")), r.getString("maNhanVien"));
    }
}
