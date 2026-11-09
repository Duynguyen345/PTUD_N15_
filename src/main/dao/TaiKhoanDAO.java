package dao;

import java.sql.*;
import java.util.List;
import connect.NguonKetNoi;
import entity.*;

/** Truy cập SQL cho luồng TaiKhoan. */
public class TaiKhoanDAO extends JdbcDAO {
    public TaiKhoanDAO(NguonKetNoi nguonKetNoi) {
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

    public List<TaiKhoan> danhSachTaiKhoan() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maTaiKhoan,matKhau,trangThai,maNhanVien FROM TaiKhoan ORDER BY maTaiKhoan",
                this::docTaiKhoan));
    }

    public TaiKhoan timTaiKhoan(String maTaiKhoan) {
        return docDuLieu(c -> truyVan(c,
                "SELECT maTaiKhoan,matKhau,trangThai,maNhanVien FROM TaiKhoan WHERE maTaiKhoan=?",
                this::docTaiKhoan, maTaiKhoan).stream().findFirst().orElse(null));
    }

    public String taoTaiKhoan(String maNhanVien, String matKhauDaBam) {
        return thucHienGiaoDich(c -> {
            String maTaiKhoan = taoMaTiepTheo(c, "TK", "TaiKhoan", "maTaiKhoan");
            List<Integer> dangLamViec = truyVan(c,
                    "SELECT 1 FROM NhanVien WITH (UPDLOCK,HOLDLOCK) WHERE maNhanVien=? AND conLamViec=1",
                    r -> r.getInt(1), maNhanVien);
            if (dangLamViec.isEmpty())
                throw new IllegalArgumentException("Nhân viên không còn làm việc.");
            thucHienCapNhat(c,
                    "INSERT INTO TaiKhoan(maTaiKhoan,matKhau,trangThai,maNhanVien) VALUES (?,?,?,?)",
                    maTaiKhoan, matKhauDaBam, TrangThaiTaiKhoan.HOAT_DONG.name(), maNhanVien);
            return maTaiKhoan;
        });
    }

    public void capNhatTrangThaiTaiKhoan(String maTaiKhoan, TrangThaiTaiKhoan trangThai) {
        thucHienGiaoDich(c -> {
            kiemTraSoDongCapNhat(thucHienCapNhat(c, "UPDATE TaiKhoan SET trangThai=? WHERE maTaiKhoan=?",
                    trangThai.name(), maTaiKhoan));
            return null;
        });
    }

    private TaiKhoan docTaiKhoan(ResultSet r) throws SQLException {
        return new TaiKhoan(r.getString("maTaiKhoan"), r.getString("matKhau"),
                TrangThaiTaiKhoan.valueOf(r.getString("trangThai")), r.getString("maNhanVien"));
    }
}
