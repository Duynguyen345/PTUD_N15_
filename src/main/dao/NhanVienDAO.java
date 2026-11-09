package dao;

import java.sql.*;
import java.util.List;
import connect.NguonKetNoi;
import entity.*;

/** Truy cập SQL cho luồng NhanVien. */
public class NhanVienDAO extends JdbcDAO {
    public NhanVienDAO(NguonKetNoi nguonKetNoi) {
        super(nguonKetNoi);
    }

    public List<NhanVien> danhSachNhanVien() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec FROM NhanVien ORDER BY maNhanVien",
                r -> {
                    Date ngaySinh = r.getDate("ngaySinh");
                    Boolean gioiTinh = r.getObject("gioiTinh") == null ? null : r.getBoolean("gioiTinh");
                    return new NhanVien(r.getString("maNhanVien"), r.getString("hoTen"),
                            r.getString("soDienThoai"), ngaySinh == null ? null : ngaySinh.toLocalDate(),
                            gioiTinh, r.getString("cccd"), ChucVu.valueOf(r.getString("chucVu")),
                            r.getBoolean("conLamViec"));
                }));
    }

    public void luuNhanVien(NhanVien n, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi) {
                String ma = taoMaTiepTheo(c, "NV", "NhanVien", "maNhanVien");
                thucHienCapNhat(c,
                        "INSERT INTO NhanVien(maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec) VALUES (?,?,?,?,?,?,?,?)",
                        ma, n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(), n.getGioiTinh(), n.getCccd(),
                        n.getChucVu().name(), n.isConLamViec());
            } else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE NhanVien SET hoTen=?,soDienThoai=?,ngaySinh=?,gioiTinh=?,cccd=?,chucVu=?,conLamViec=? WHERE maNhanVien=?",
                        n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(), n.getGioiTinh(), n.getCccd(),
                        n.getChucVu().name(), n.isConLamViec(), n.getMaNhanVien()));
            return null;
        });
    }

    public void xoaNhanVien(String ma) {
        thucHienGiaoDich(c -> {
            kiemTraSoDongCapNhat(thucHienCapNhat(c, "DELETE FROM NhanVien WHERE maNhanVien=?", ma));
            return null;
        });
    }
}
