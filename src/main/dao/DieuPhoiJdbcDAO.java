package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import connect.NguonKetNoi;
import entity.*;

/** SQL Server repository: PreparedStatement, try-with-resources và khóa theo phòng. */
public final class DieuPhoiJdbcDAO implements DieuPhoiDAO {
    private final NguonKetNoi nguonKetNoi;
    private static final String SELECT_HANG_CHO = """
            SELECT pk.maPhieuKham,pk.ngayKham,pk.maPhong,pk.soThuTu,pk.maBenhNhan,b.hoTen,
                   pk.daThanhToanPhiKham,pk.thoiDiemGoi,
                   CASE WHEN pk.trangThai=N'HOAN_TAT' THEN N'HOAN_TAT'
                        WHEN pk.trangThai=N'DANG_KHAM' THEN N'DANG_KHAM'
                        ELSE pk.trangThaiHangCho END AS trangThaiCho
            FROM PhieuKham pk JOIN BenhNhan b ON b.maBenhNhan=pk.maBenhNhan
            """;

    /** Nhận nguồn kết nối thay vì giữ một Connection dùng chung giữa các worker. */
    public DieuPhoiJdbcDAO(NguonKetNoi nguonKetNoi) { this.nguonKetNoi = nguonKetNoi; }

    @FunctionalInterface
    private interface TacVu<T> { T thucHien(Connection ketNoi) throws SQLException; }

    @FunctionalInterface
    private interface AnhXa<T> { T doc(ResultSet ketQua) throws SQLException; }

    /** Mỗi thao tác có connection riêng; ghi commit/rollback trọn vẹn. */
    private <T> T truyCap(boolean ghi, TacVu<T> tacVu) {
        try (Connection ketNoi = nguonKetNoi.moKetNoi()) {
            if (ghi) ketNoi.setAutoCommit(false);
            try {
                T ketQua = tacVu.thucHien(ketNoi);
                if (ghi) ketNoi.commit();
                return ketQua;
            } catch (SQLException | RuntimeException loi) {
                if (ghi) {
                    try { ketNoi.rollback(); }
                    catch (SQLException loiHoanTac) { loi.addSuppressed(loiHoanTac); }
                }
                throw loi;
            }
        } catch (SQLException loi) {
            throw new NgoaiLeTruyCapDuLieu(
                    "Không truy cập được dữ liệu điều phối. Kiểm tra SQL Server, db.properties và DATA/schema/040_le_tan.sql, 040_thanh_dieu_phoi.sql và 050_tich_hop_thanh_huy.sql.", loi);
        }
    }

    private PreparedStatement taoLenh(Connection ketNoi, String sql, Object... giaTri) throws SQLException {
        PreparedStatement lenh = ketNoi.prepareStatement(sql);
        try {
            lenh.setQueryTimeout(15);
            for (int i = 0; i < giaTri.length; i++) {
                if (giaTri[i] instanceof LocalDate ngay) lenh.setDate(i + 1, Date.valueOf(ngay));
                else if (giaTri[i] instanceof LocalDateTime luc) lenh.setTimestamp(i + 1, Timestamp.valueOf(luc));
                else lenh.setObject(i + 1, giaTri[i]);
            }
            return lenh;
        } catch (SQLException loi) {
            lenh.close();
            throw loi;
        }
    }

    private <T> List<T> docDanhSach(Connection ketNoi, String sql, AnhXa<T> anhXa, Object... giaTri)
            throws SQLException {
        try (PreparedStatement lenh = taoLenh(ketNoi, sql, giaTri); ResultSet ketQua = lenh.executeQuery()) {
            List<T> danhSach = new ArrayList<>();
            while (ketQua.next()) danhSach.add(anhXa.doc(ketQua));
            return List.copyOf(danhSach);
        }
    }

    private void capNhat(Connection ketNoi, String sql, Object... giaTri) throws SQLException {
        try (PreparedStatement lenh = taoLenh(ketNoi, sql, giaTri)) {
            if (lenh.executeUpdate() != 1)
                throw new IllegalArgumentException("Dữ liệu vừa thay đổi. Vui lòng tải lại.");
        }
    }

    private LuotChoKham docLuot(ResultSet r) throws SQLException {
        Timestamp luc = r.getTimestamp("thoiDiemGoi");
        return new LuotChoKham(r.getString("maPhieuKham"), r.getDate("ngayKham").toLocalDate(),
                r.getString("maPhong"), r.getInt("soThuTu"), r.getString("maBenhNhan"), r.getString("hoTen"),
                r.getBoolean("daThanhToanPhiKham"), TrangThaiHangCho.valueOf(r.getString("trangThaiCho")),
                luc == null ? null : luc.toLocalDateTime());
    }

    @Override
    public List<PhongKham> layDanhSachPhong() {
        return truyCap(false, c -> docDanhSach(c, """
                SELECT p.maPhong,p.tenPhong,p.maBacSi,n.hoTen,p.dangNhanBenh
                FROM PhongKham p JOIN NhanVien n ON n.maNhanVien=p.maBacSi ORDER BY p.maPhong
                """, r -> new PhongKham(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getBoolean(5))));
    }

    private List<LuotChoKham> docHangCho(Connection c, String maPhong, LocalDate ngay) throws SQLException {
        return docDanhSach(c, SELECT_HANG_CHO + " WHERE pk.maPhong=? AND pk.ngayKham=? ORDER BY pk.soThuTu",
                this::docLuot, maPhong, ngay);
    }

    @Override
    public List<LuotChoKham> layHangCho(String maPhong, LocalDate ngay) {
        return truyCap(false, c -> docHangCho(c, maPhong, ngay));
    }

    @Override
    public List<BenhNhan> timBenhNhan(String tuKhoa) {
        // Escape wildcard để %, _ và [ có nghĩa ký tự, giống tìm kiếm ở bản bộ nhớ.
        String mau = "%" + tuKhoa.replace("~", "~~").replace("%", "~%")
                .replace("_", "~_").replace("[", "~[") + "%";
        // Collation Latin bo ca dau mu/dau trang va D gach ngang; Vietnamese chi bo dau thanh.
        return truyCap(false, c -> docDanhSach(c, """
                SELECT maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,COALESCE(NULLIF(tienSuDiUng,N''),tienSu) AS tienSu,diUng FROM BenhNhan
                WHERE maBenhNhan COLLATE Latin1_General_100_CI_AI LIKE ? ESCAPE '~'
                   OR hoTen COLLATE Latin1_General_100_CI_AI LIKE ? ESCAPE '~'
                   OR soDienThoai LIKE ? ESCAPE '~' ORDER BY maBenhNhan
                """, r -> new BenhNhan(r.getString(1), r.getString(2), r.getDate(3) == null ? null : r.getDate(3).toLocalDate(),
                        r.getString(4), r.getString(5), r.getString(6), r.getString(7), r.getString(8)), mau, mau, mau));
    }

    @Override
    public List<LichSuKham> layLichSuKham(String maBenhNhan) {
        return truyCap(false, c -> docDanhSach(c, """
                SELECT pk.maPhieuKham,pk.ngayKham,n.hoTen,COALESCE(h.chanDoan,N'Chưa có kết quả'),
                       COALESCE(h.dieuTri,N'Chưa có kết quả'),COALESCE(h.ghiChu,N''),
                       CASE WHEN pk.trangThai=N'HOAN_TAT' THEN N'HOAN_TAT'
                            WHEN pk.trangThai=N'DANG_KHAM' THEN N'DANG_KHAM' ELSE pk.trangThaiHangCho END
                FROM PhieuKham pk JOIN NhanVien n ON n.maNhanVien=pk.maNhanVien
                LEFT JOIN HoSoKham h ON h.maPhieuKham=pk.maPhieuKham
                WHERE pk.maBenhNhan=? ORDER BY pk.ngayKham DESC,pk.maPhieuKham
                """, r -> new LichSuKham(r.getString(1), r.getDate(2).toLocalDate(), r.getString(3),
                        r.getString(4), r.getString(5), r.getString(6),
                        TrangThaiHangCho.valueOf(r.getString(7)).toString()), maBenhNhan));
    }

    /** Cùng khóa phòng cho mọi lệnh ghi, tránh gọi trùng giữa nhiều máy. */
    private boolean khoaPhong(Connection c, String maPhong) throws SQLException {
        List<Boolean> ketQua = docDanhSach(c,
                "SELECT dangNhanBenh FROM PhongKham WITH (UPDLOCK,HOLDLOCK) WHERE maPhong=?", r -> r.getBoolean(1), maPhong);
        if (ketQua.isEmpty()) throw new IllegalArgumentException("Không tìm thấy phòng khám.");
        return ketQua.getFirst();
    }

    private boolean coLuotDangPhucVu(List<LuotChoKham> cacLuot) {
        return cacLuot.stream().anyMatch(l -> l.trangThai() == TrangThaiHangCho.DANG_GOI
                || l.trangThai() == TrangThaiHangCho.DANG_KHAM);
    }

    @Override
    public void datNhanBenh(String maPhong, boolean nhanBenh, LocalDate ngay) {
        truyCap(true, c -> {
            khoaPhong(c, maPhong);
            if (!nhanBenh && coLuotDangPhucVu(docHangCho(c, maPhong, ngay)))
                throw new IllegalArgumentException("Phòng còn bệnh nhân đang gọi/đang khám; chưa thể tắt nhận bệnh.");
            capNhat(c, "UPDATE PhongKham SET dangNhanBenh=? WHERE maPhong=?", nhanBenh, maPhong);
            return null;
        });
    }

    @Override
    public LuotChoKham goiTiepTheo(String maPhong, LocalDate ngay, LocalDateTime thoiDiem) {
        return truyCap(true, c -> {
            if (!khoaPhong(c, maPhong)) throw new IllegalArgumentException("Phòng đang tắt nhận bệnh.");
            List<LuotChoKham> cacLuot = docHangCho(c, maPhong, ngay);
            if (coLuotDangPhucVu(cacLuot))
                throw new IllegalArgumentException("Phòng đang gọi hoặc đang khám. Hoàn tất lượt hiện tại trước.");
            LuotChoKham luot = cacLuot.stream().filter(l -> l.trangThai() == TrangThaiHangCho.CHO_KHAM
                    && l.daThanhToanPhiKham()).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không còn bệnh nhân đã trả phí đang chờ."));
            LuotChoKham ketQua = luot.chuyenTrangThai(TrangThaiHangCho.DANG_GOI, thoiDiem);
            ghiTrangThai(c, luot, ketQua);
            return ketQua;
        });
    }

    private void ghiTrangThai(Connection c, LuotChoKham cu, LuotChoKham moi) throws SQLException {
        capNhat(c, """
                UPDATE PhieuKham SET trangThaiHangCho=?,thoiDiemGoi=?,trangThai=?
                WHERE maPhieuKham=? AND maPhong=? AND ngayKham=? AND trangThaiHangCho=?
                  AND trangThai=N'CHO_KHAM' AND daThanhToanPhiKham=1
                """, moi.trangThai().name(), moi.thoiDiemGoi(),
                moi.trangThai() == TrangThaiHangCho.DANG_KHAM ? "DANG_KHAM" : "CHO_KHAM",
                cu.maPhieuKham(), cu.maPhong(), cu.ngayKham(), cu.trangThai().name());
    }

    @Override
    public LuotChoKham chuyenTrangThai(String maPhieuKham, String maPhong, LocalDate ngay,
            TrangThaiHangCho dich, LocalDateTime thoiDiem) {
        return truyCap(true, c -> {
            if (!khoaPhong(c, maPhong)) throw new IllegalArgumentException("Phòng đang tắt nhận bệnh.");
            LuotChoKham luot = docHangCho(c, maPhong, ngay).stream().filter(l -> l.maPhieuKham().equals(maPhieuKham))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Lượt khám không thuộc phòng/ngày đang chọn."));
            if (luot.trangThai() != TrangThaiHangCho.DANG_GOI)
                throw new IllegalArgumentException("Chỉ thao tác trên bệnh nhân đang được gọi.");
            LuotChoKham ketQua = luot.chuyenTrangThai(dich, thoiDiem);
            ghiTrangThai(c, luot, ketQua);
            return ketQua;
        });
    }
}
