package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import connect.NguonKetNoi;
import entity.*;

/** Kho JDBC của lễ tân. Mỗi nghiệp vụ ghi dùng một kết nối và một transaction. */
public class LeTanDAO {
    private final NguonKetNoi nguonKetNoi;

    public LeTanDAO(NguonKetNoi nguonKetNoi) { this.nguonKetNoi = nguonKetNoi; }

    @FunctionalInterface private interface TacVu<T> { T chay(Connection c) throws SQLException; }
    @FunctionalInterface private interface AnhXa<T> { T doc(ResultSet r) throws SQLException; }

    private <T> T doc(TacVu<T> tacVu) {
        try (Connection c = nguonKetNoi.moKetNoi()) { return tacVu.chay(c); }
        catch (SQLException loi) { throw loiDuLieu(loi); }
    }

    private <T> T ghi(TacVu<T> tacVu) {
        return doc(c -> {
            c.setAutoCommit(false);
            try {
                T ketQua = tacVu.chay(c);
                c.commit();
                return ketQua;
            } catch (SQLException | RuntimeException loi) {
                try { c.rollback(); } catch (SQLException loiKhac) { loi.addSuppressed(loiKhac); }
                throw loi;
            }
        });
    }

    private NgoaiLeTruyCapDuLieu loiDuLieu(SQLException loi) {
        String thongBao = switch (loi.getErrorCode()) {
            case 2601, 2627 -> "Dữ liệu đã tồn tại hoặc vừa được nhân viên khác cập nhật. Vui lòng tải lại.";
            case 547 -> "Dữ liệu đang được sử dụng hoặc không hợp lệ.";
            case 208, 207 -> "Thiếu bảng/cột lễ tân. Chạy DATA/schema/040_le_tan.sql trước.";
            default -> "Không thực hiện được thao tác lễ tân. Kiểm tra kết nối và thử lại.";
        };
        return new NgoaiLeTruyCapDuLieu(thongBao, loi);
    }

    private PreparedStatement cauLenh(Connection c, String sql, Object... giaTri) throws SQLException {
        PreparedStatement p = c.prepareStatement(sql);
        try {
            p.setQueryTimeout(15);
            for (int i = 0; i < giaTri.length; i++) {
                Object v = giaTri[i];
                if (v instanceof LocalDate d) p.setDate(i + 1, Date.valueOf(d));
                else if (v instanceof LocalDateTime d) p.setTimestamp(i + 1, Timestamp.valueOf(d));
                else if (v instanceof HinhThucThanhToan h) p.setString(i + 1, h.name());
                else p.setObject(i + 1, v);
            }
            return p;
        } catch (SQLException loi) { p.close(); throw loi; }
    }

    private <T> List<T> truyVan(Connection c, String sql, AnhXa<T> anhXa, Object... giaTri) throws SQLException {
        try (PreparedStatement p = cauLenh(c, sql, giaTri); ResultSet r = p.executeQuery()) {
            List<T> ds = new ArrayList<>();
            while (r.next()) ds.add(anhXa.doc(r));
            return ds;
        }
    }

    private int capNhat(Connection c, String sql, Object... giaTri) throws SQLException {
        try (PreparedStatement p = cauLenh(c, sql, giaTri)) { return p.executeUpdate(); }
    }

    /** Cùng DuyBoDem và cách khóa dải mã của DAO quản lý. */
    private String maMoi(Connection c, String tienTo, String bang, String cot) throws SQLException {
        String khoa = tienTo + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        List<Integer> daCo = truyVan(c, "SELECT giaTri FROM DuyBoDem WITH(UPDLOCK,HOLDLOCK) WHERE khoa=?",
                r -> r.getInt(1), khoa);
        int so = daCo.isEmpty() ? 0 : daCo.getFirst();
        String ma;
        do {
            if (++so > 9999) throw new IllegalArgumentException("Đã hết dải mã " + khoa + ".");
            ma = khoa + String.format("%04d", so);
        } while (!truyVan(c, "SELECT 1 FROM " + bang + " WHERE " + cot + "=?", r -> r.getInt(1), ma).isEmpty());
        if (daCo.isEmpty()) capNhat(c, "INSERT INTO DuyBoDem(khoa,giaTri) VALUES(?,?)", khoa, so);
        else capNhat(c, "UPDATE DuyBoDem SET giaTri=? WHERE khoa=?", so, khoa);
        return ma;
    }

    private BenhNhan benhNhan(ResultSet r) throws SQLException {
        Date sinh = r.getDate("ngaySinh");
        return new BenhNhan(r.getString("maBenhNhan"), r.getString("hoTen"),
                r.getString("soDienThoai"), sinh == null ? null : sinh.toLocalDate(),
                r.getString("cccd"), r.getString("diaChi"), r.getString("tienSuDiUng"));
    }

    public List<BenhNhan> danhSachBenhNhan() {
        return doc(c -> truyVan(c,
                "SELECT TOP (200) maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,COALESCE(tienSuDiUng,CONCAT(tienSu,N'; ',diUng)) AS tienSuDiUng"
                + " FROM BenhNhan ORDER BY maBenhNhan DESC", this::benhNhan));
    }

    public List<BenhNhan> traCuuBenhNhan(String tieuChi, String tuKhoa) {
        String cot = switch (tieuChi) {
            case "SĐT" -> "soDienThoai";
            case "Họ tên" -> "hoTen";
            default -> "maBenhNhan";
        };
        return doc(c -> truyVan(c,
                "SELECT TOP (100) maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,COALESCE(tienSuDiUng,CONCAT(tienSu,N'; ',diUng)) AS tienSuDiUng"
                + " FROM BenhNhan WHERE " + cot + " LIKE ? ORDER BY maBenhNhan DESC",
                this::benhNhan, "%" + tuKhoa + "%"));
    }

    public List<PhieuLeTan> lichSuPhieuKham(String maBenhNhan) {
        return doc(c -> truyVan(c, "SELECT " + COT_PHIEU + JOIN_PHIEU
                + " WHERE pk.maBenhNhan=? ORDER BY pk.ngayKham DESC, pk.soThuTu DESC",
                this::phieu, maBenhNhan));
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) {
        return doc(c -> truyVan(c,
                "SELECT TOP (100) maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,COALESCE(tienSuDiUng,CONCAT(tienSu,N'; ',diUng)) AS tienSuDiUng"
                + " FROM BenhNhan WHERE maBenhNhan LIKE ? OR soDienThoai LIKE ? OR cccd LIKE ?"
                + " OR hoTen LIKE ? ORDER BY maBenhNhan DESC", this::benhNhan,
                "%" + tuKhoa + "%", "%" + tuKhoa + "%", "%" + tuKhoa + "%", "%" + tuKhoa + "%"));
    }

    public BenhNhan luuBenhNhan(BenhNhan bn) {
        return ghi(c -> {
            String ma = bn.maBenhNhan() == null ? maMoi(c, "BN", "BenhNhan", "maBenhNhan") : bn.maBenhNhan();
            if (bn.maBenhNhan() == null)
                capNhat(c, "INSERT INTO BenhNhan(maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,tienSuDiUng)"
                        + " VALUES(?,?,?,?,?,?,?)", ma, bn.hoTen(), bn.soDienThoai(), bn.ngaySinh(),
                        bn.cccd(), bn.diaChi(), bn.tienSuDiUng());
            else if (capNhat(c, "UPDATE BenhNhan SET hoTen=?,soDienThoai=?,ngaySinh=?,cccd=?,diaChi=?,"
                    + "tienSuDiUng=? WHERE maBenhNhan=?", bn.hoTen(), bn.soDienThoai(), bn.ngaySinh(),
                    bn.cccd(), bn.diaChi(), bn.tienSuDiUng(), ma) != 1)
                throw new IllegalArgumentException("Hồ sơ bệnh nhân không còn tồn tại.");
            return new BenhNhan(ma, bn.hoTen(), bn.soDienThoai(), bn.ngaySinh(), bn.cccd(),
                    bn.diaChi(), bn.tienSuDiUng());
        });
    }

    public List<PhongLeTan> danhSachPhong() {
        return doc(c -> truyVan(c,
                "SELECT p.maPhong,p.tenPhong,p.dangNhanBenh,p.maBacSi,bs.hoTen tenBacSi,"
                + "p.maPhuTa,pt.hoTen tenPhuTa,"
                + "SUM(CASE WHEN pk.trangThai='CHO_KHAM' THEN 1 ELSE 0 END) soCho,"
                + "SUM(CASE WHEN pk.trangThai='DANG_KHAM' THEN 1 ELSE 0 END) soDang"
                + " FROM PhongKham p LEFT JOIN NhanVien bs ON bs.maNhanVien=p.maBacSi"
                + " LEFT JOIN NhanVien pt ON pt.maNhanVien=p.maPhuTa"
                + " LEFT JOIN PhieuKham pk ON pk.maPhong=p.maPhong AND pk.ngayKham=CAST(GETDATE() AS date)"
                + " GROUP BY p.maPhong,p.tenPhong,p.dangNhanBenh,p.maBacSi,bs.hoTen,p.maPhuTa,pt.hoTen"
                + " ORDER BY p.maPhong", r -> new PhongLeTan(r.getString("maPhong"), r.getString("tenPhong"),
                r.getBoolean("dangNhanBenh"), r.getString("maBacSi"), r.getString("tenBacSi"),
                r.getString("maPhuTa"), r.getString("tenPhuTa"), r.getInt("soCho"), r.getInt("soDang"))));
    }

    private PhieuLeTan phieu(ResultSet r) throws SQLException {
        return new PhieuLeTan(r.getString("maPhieuKham"), r.getDate("ngayKham").toLocalDate(),
                r.getInt("soThuTu"), r.getString("maPhong"), r.getString("maBenhNhan"),
                r.getString("tenBenhNhan"), r.getString("maNhanVien"), r.getString("tenBacSi"),
                r.getString("lyDoKham"), r.getString("trangThai"), r.getString("maHoaDonPhiKham"));
    }

    private static final String COT_PHIEU = "pk.maPhieuKham,pk.ngayKham,pk.soThuTu,pk.maPhong,"
            + "pk.maBenhNhan,bn.hoTen tenBenhNhan,pk.maNhanVien,bs.hoTen tenBacSi,"
            + "pk.lyDoKham,pk.trangThai,pk.maHoaDonPhiKham";
    private static final String JOIN_PHIEU = " FROM PhieuKham pk JOIN BenhNhan bn ON bn.maBenhNhan=pk.maBenhNhan"
            + " JOIN NhanVien bs ON bs.maNhanVien=pk.maNhanVien";

    public List<PhieuLeTan> danhSachLuot(String maPhong, LocalDate ngay) {
        return doc(c -> truyVan(c, "SELECT " + COT_PHIEU + JOIN_PHIEU
                + " WHERE pk.maPhong=? AND pk.ngayKham=? AND pk.trangThai IN('CHO_KHAM','DANG_KHAM')"
                + " ORDER BY pk.soThuTu", this::phieu, maPhong, ngay));
    }

    public List<PhieuLeTan> phieuChoThanhToan() {
        return doc(c -> truyVan(c, "SELECT " + COT_PHIEU + JOIN_PHIEU
                + " WHERE pk.trangThai='HOAN_TAT' AND NOT EXISTS"
                + " (SELECT 1 FROM HoaDon hd WHERE hd.maPhieuKham=pk.maPhieuKham)"
                + " AND EXISTS(SELECT 1 FROM ChiDinhDichVu cd WHERE cd.maPhieuKham=pk.maPhieuKham)"
                + " ORDER BY pk.ngayKham DESC,pk.maPhieuKham DESC", this::phieu));
    }

    private HoaDonLeTan hoaDon(ResultSet r) throws SQLException {
        Timestamp ngay = r.getTimestamp("ngayThu");
        return new HoaDonLeTan(r.getString("maHoaDon"), r.getString("maBenhNhan"),
                r.getString("tenBenhNhan"), r.getString("maPhieuKham"), ngay.toLocalDateTime(),
                r.getBigDecimal("tongTien"), r.getBigDecimal("tienKhachDua"),
                HinhThucThanhToan.valueOf(r.getString("hinhThucThanhToan")), r.getString("maNhanVien"));
    }

    private static final String COT_HOA_DON = "hd.maHoaDon,hd.maBenhNhan,bn.hoTen tenBenhNhan,"
            + "hd.maPhieuKham,hd.ngayThu,hd.tongTien,hd.tienKhachDua,hd.hinhThucThanhToan,hd.maNhanVien"
            + " FROM HoaDon hd JOIN BenhNhan bn ON bn.maBenhNhan=hd.maBenhNhan";

    public HoaDonLeTan phiChuaDung(String maBenhNhan) {
        return doc(c -> truyVan(c, "SELECT TOP(1) " + COT_HOA_DON
                + " WHERE hd.maBenhNhan=? AND hd.maPhieuKham IS NULL"
                + " AND NOT EXISTS(SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham=hd.maHoaDon)"
                + " ORDER BY hd.ngayThu DESC", this::hoaDon, maBenhNhan)
                .stream().findFirst().orElse(null));
    }

    private void yeuCauCaMo(Connection c) throws SQLException {
        if (truyVan(c, "SELECT maCa FROM CaThuNgan WITH(UPDLOCK,HOLDLOCK) WHERE thoiGianChot IS NULL",
                r -> r.getString(1)).isEmpty())
            throw new IllegalArgumentException("Chưa nhận ca thu ngân. Mở ca trước khi thu tiền.");
    }

    public HoaDonLeTan thuPhi(String maBenhNhan, HinhThucThanhToan hinhThuc,
            BigDecimal tienKhachDua, String maNhanVien) {
        return ghi(c -> {
            yeuCauCaMo(c);
            List<BenhNhan> bn = truyVan(c, "SELECT maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,COALESCE(tienSuDiUng,CONCAT(tienSu,N'; ',diUng)) AS tienSuDiUng"
                    + " FROM BenhNhan WITH(UPDLOCK,HOLDLOCK) WHERE maBenhNhan=?", this::benhNhan, maBenhNhan);
            if (bn.isEmpty()) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
            if (!truyVan(c, "SELECT hd.maHoaDon FROM HoaDon hd WITH(UPDLOCK,HOLDLOCK)"
                    + " WHERE hd.maBenhNhan=? AND hd.maPhieuKham IS NULL"
                    + " AND NOT EXISTS(SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham=hd.maHoaDon)",
                    r -> r.getString(1), maBenhNhan).isEmpty())
                throw new IllegalArgumentException("Bệnh nhân đã có phí khám chưa dùng.");
            List<BigDecimal> gia = truyVan(c, "SELECT donGiaNiemYet FROM DichVu"
                    + " WHERE maDichVu='DV0001' AND trangThai='DANG_PHUC_VU'", r -> r.getBigDecimal(1));
            if (gia.isEmpty()) throw new IllegalArgumentException("Chưa có dịch vụ phí khám DV0001 đang phục vụ.");
            BigDecimal phi = gia.getFirst();
            if (tienKhachDua.compareTo(phi) < 0) throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");
            String ma = maMoi(c, "HD", "HoaDon", "maHoaDon");
            LocalDateTime lucThu = LocalDateTime.now();
            capNhat(c, "INSERT INTO HoaDon(maHoaDon,maBenhNhan,maNhanVien,ngayThu,hinhThucThanhToan,"
                    + "tongTien,tienKhachDua) VALUES(?,?,?,?,?,?,?)", ma, maBenhNhan, maNhanVien,
                    lucThu, hinhThuc, phi, tienKhachDua);
            capNhat(c, "INSERT INTO ChiTietHoaDon(maHoaDon,maDichVu,donGia) VALUES(?,'DV0001',?)", ma, phi);
            return new HoaDonLeTan(ma, maBenhNhan, bn.getFirst().hoTen(), null, lucThu,
                    phi, tienKhachDua, hinhThuc, maNhanVien);
        });
    }

    public PhieuLeTan lapPhieu(String maBenhNhan, String maPhong, String lyDo) {
        return ghi(c -> {
            List<PhongLeTan> phong = truyVan(c, "SELECT p.maPhong,p.tenPhong,p.dangNhanBenh,p.maBacSi,"
                    + "bs.hoTen tenBacSi,p.maPhuTa,pt.hoTen tenPhuTa,0 soCho,0 soDang"
                    + " FROM PhongKham p WITH(UPDLOCK,HOLDLOCK)"
                    + " LEFT JOIN NhanVien bs ON bs.maNhanVien=p.maBacSi"
                    + " LEFT JOIN NhanVien pt ON pt.maNhanVien=p.maPhuTa WHERE p.maPhong=?",
                    r -> new PhongLeTan(r.getString("maPhong"), r.getString("tenPhong"),
                            r.getBoolean("dangNhanBenh"), r.getString("maBacSi"), r.getString("tenBacSi"),
                            r.getString("maPhuTa"), r.getString("tenPhuTa"), 0, 0), maPhong);
            if (phong.isEmpty() || !phong.getFirst().sanSang())
                throw new IllegalArgumentException("Phòng chưa nhận bệnh hoặc chưa phân công nhân sự.");
            PhongLeTan p = phong.getFirst();
            if (truyVan(c, "SELECT 1 FROM NhanVien bs JOIN NhanVien pt ON pt.maNhanVien=?"
                    + " WHERE bs.maNhanVien=? AND bs.chucVu='BAC_SI' AND pt.chucVu='PHU_TA'"
                    + " AND bs.conLamViec=1 AND pt.conLamViec=1", r -> r.getInt(1),
                    p.maPhuTa(), p.maBacSi()).isEmpty())
                throw new IllegalArgumentException("Bác sĩ hoặc phụ tá không còn làm việc đúng phân công.");
            List<BenhNhan> bn = truyVan(c, "SELECT maBenhNhan,hoTen,soDienThoai,ngaySinh,cccd,diaChi,COALESCE(tienSuDiUng,CONCAT(tienSu,N'; ',diUng)) AS tienSuDiUng"
                    + " FROM BenhNhan WHERE maBenhNhan=?", this::benhNhan, maBenhNhan);
            if (bn.isEmpty()) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
            List<String> phi = truyVan(c, "SELECT TOP(1) hd.maHoaDon FROM HoaDon hd WITH(UPDLOCK,HOLDLOCK)"
                    + " WHERE hd.maBenhNhan=? AND hd.maPhieuKham IS NULL"
                    + " AND NOT EXISTS(SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham=hd.maHoaDon)"
                    + " ORDER BY hd.ngayThu DESC", r -> r.getString(1), maBenhNhan);
            if (phi.isEmpty()) throw new IllegalArgumentException("Bệnh nhân chưa có phí khám đã thu và chưa dùng.");
            LocalDate ngay = LocalDate.now();
            List<Integer> soCu = truyVan(c, "SELECT giaTri FROM SoThuTuPhong WITH(UPDLOCK,HOLDLOCK)"
                    + " WHERE ngayKham=? AND maPhong=?", r -> r.getInt(1), ngay, maPhong);
            int maxDaCap = truyVan(c, "SELECT COALESCE(MAX(soThuTu),0) FROM PhieuKham WHERE ngayKham=? AND maPhong=?",
                    r -> r.getInt(1), ngay, maPhong).getFirst();
            int stt = Math.max(soCu.isEmpty() ? 0 : soCu.getFirst(), maxDaCap) + 1;
            if (soCu.isEmpty()) capNhat(c, "INSERT INTO SoThuTuPhong(ngayKham,maPhong,giaTri) VALUES(?,?,?)",
                    ngay, maPhong, stt);
            else capNhat(c, "UPDATE SoThuTuPhong SET giaTri=? WHERE ngayKham=? AND maPhong=?",
                    stt, ngay, maPhong);
            String ma = maMoi(c, "PK", "PhieuKham", "maPhieuKham");
            capNhat(c, "INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,soThuTu,"
                    + "maBenhNhan,maPhong,lyDoKham,maHoaDonPhiKham,daThanhToanPhiKham) VALUES(?,?,'CHO_KHAM',?,?,?,?,?,?,1)",
                    ma, ngay, p.maBacSi(), stt, maBenhNhan, maPhong, lyDo, phi.getFirst());
            return new PhieuLeTan(ma, ngay, stt, maPhong, maBenhNhan, bn.getFirst().hoTen(),
                    p.maBacSi(), p.tenBacSi(), lyDo, "CHO_KHAM", phi.getFirst());
        });
    }

    public List<DichVuThanhToan> dichVuTheoPhieu(String maPhieu) {
        return doc(c -> truyVan(c, "SELECT dv.maDichVu,dv.tenDichVu,COALESCE(cd.donGia,dv.donGiaNiemYet) donGia"
                + " FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu=cd.maDichVu"
                + " WHERE cd.maPhieuKham=? ORDER BY dv.maDichVu", r -> new DichVuThanhToan(
                        r.getString(1), r.getString(2), r.getBigDecimal(3)), maPhieu));
    }

    public HoaDonLeTan thanhToanDichVu(String maPhieu, HinhThucThanhToan hinhThuc,
            BigDecimal tienKhachDua, String maNhanVien) {
        return ghi(c -> {
            yeuCauCaMo(c);
            List<PhieuLeTan> ds = truyVan(c, "SELECT " + COT_PHIEU + JOIN_PHIEU
                    + " WITH(UPDLOCK,HOLDLOCK) WHERE pk.maPhieuKham=?", this::phieu, maPhieu);
            if (ds.isEmpty() || !"HOAN_TAT".equals(ds.getFirst().trangThai()))
                throw new IllegalArgumentException("Lượt khám chưa hoàn tất hoặc không tồn tại.");
            if (!truyVan(c, "SELECT maHoaDon FROM HoaDon WHERE maPhieuKham=?", r -> r.getString(1),
                    maPhieu).isEmpty()) throw new IllegalArgumentException("Lượt khám đã thanh toán.");
            List<DichVuThanhToan> dichVu = truyVan(c,
                    "SELECT dv.maDichVu,dv.tenDichVu,COALESCE(cd.donGia,dv.donGiaNiemYet) donGia"
                    + " FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu=cd.maDichVu"
                    + " WHERE cd.maPhieuKham=?", r -> new DichVuThanhToan(
                            r.getString(1), r.getString(2), r.getBigDecimal(3)), maPhieu);
            if (dichVu.isEmpty()) throw new IllegalArgumentException("Lượt khám chưa có dịch vụ cần thu.");
            BigDecimal tong = dichVu.stream().map(DichVuThanhToan::donGia)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (tienKhachDua.compareTo(tong) < 0) throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");
            String ma = maMoi(c, "HD", "HoaDon", "maHoaDon");
            LocalDateTime lucThu = LocalDateTime.now();
            PhieuLeTan pk = ds.getFirst();
            capNhat(c, "INSERT INTO HoaDon(maHoaDon,maBenhNhan,maPhieuKham,maNhanVien,ngayThu,"
                    + "hinhThucThanhToan,tongTien,tienKhachDua) VALUES(?,?,?,?,?,?,?,?)",
                    ma, pk.maBenhNhan(), maPhieu, maNhanVien, lucThu, hinhThuc, tong, tienKhachDua);
            for (DichVuThanhToan dv : dichVu)
                capNhat(c, "INSERT INTO ChiTietHoaDon(maHoaDon,maDichVu,donGia) VALUES(?,?,?)",
                        ma, dv.maDichVu(), dv.donGia());
            return new HoaDonLeTan(ma, pk.maBenhNhan(), pk.tenBenhNhan(), maPhieu,
                    lucThu, tong, tienKhachDua, hinhThuc, maNhanVien);
        });
    }

    private CaThuNgan ca(ResultSet r) throws SQLException {
        Timestamp chot = r.getTimestamp("thoiGianChot");
        return new CaThuNgan(r.getString("maCa"), r.getString("maNhanVien"),
                r.getString("tenNhanVien"), r.getTimestamp("thoiGianMo").toLocalDateTime(),
                chot == null ? null : chot.toLocalDateTime(), r.getBigDecimal("doanhThuDichVu"),
                r.getBigDecimal("phiKhamThuHo"));
    }

    private static final String COT_CA = "c.maCa,c.maNhanVien,nv.hoTen tenNhanVien,c.thoiGianMo,"
            + "c.thoiGianChot,c.doanhThuDichVu,c.phiKhamThuHo FROM CaThuNgan c"
            + " JOIN NhanVien nv ON nv.maNhanVien=c.maNhanVien";

    public CaThuNgan caDangMo() {
        return doc(c -> truyVan(c, "SELECT " + COT_CA + " WHERE c.thoiGianChot IS NULL",
                this::ca).stream().findFirst().orElse(null));
    }

    public CaThuNgan moCa(String maNhanVien) {
        return ghi(c -> {
            if (!truyVan(c, "SELECT maCa FROM CaThuNgan WITH(UPDLOCK,HOLDLOCK)"
                    + " WHERE thoiGianChot IS NULL", r -> r.getString(1)).isEmpty())
                throw new IllegalArgumentException("Quầy đã có ca đang mở.");
            String ma = maMoi(c, "CT", "CaThuNgan", "maCa");
            LocalDateTime lucMo = LocalDateTime.now();
            capNhat(c, "INSERT INTO CaThuNgan(maCa,maNhanVien,thoiGianMo) VALUES(?,?,?)",
                    ma, maNhanVien, lucMo);
            List<String> ten = truyVan(c, "SELECT hoTen FROM NhanVien WHERE maNhanVien=?",
                    r -> r.getString(1), maNhanVien);
            return new CaThuNgan(ma, maNhanVien, ten.isEmpty() ? maNhanVien : ten.getFirst(),
                    lucMo, null, null, null);
        });
    }

    private BigDecimal[] tongTrongCa(Connection c, LocalDateTime tu, LocalDateTime den) throws SQLException {
        return truyVan(c, "SELECT COALESCE(SUM(CASE WHEN maPhieuKham IS NOT NULL THEN tongTien ELSE 0 END),0),"
                + " COALESCE(SUM(CASE WHEN maPhieuKham IS NULL THEN tongTien ELSE 0 END),0)"
                + " FROM HoaDon WHERE ngayThu>=? AND ngayThu<=?",
                r -> new BigDecimal[] {r.getBigDecimal(1), r.getBigDecimal(2)}, tu, den).getFirst();
    }

    public CaThuNgan chotCa(String maCa) {
        return ghi(c -> {
            List<CaThuNgan> ds = truyVan(c, "SELECT " + COT_CA + " WITH(UPDLOCK,HOLDLOCK)"
                    + " WHERE c.maCa=? AND c.thoiGianChot IS NULL", this::ca, maCa);
            if (ds.isEmpty()) throw new IllegalArgumentException("Ca đã chốt hoặc không tồn tại.");
            CaThuNgan ca = ds.getFirst();
            LocalDateTime chot = LocalDateTime.now();
            BigDecimal[] tien = tongTrongCa(c, ca.moLuc(), chot);
            if (capNhat(c, "UPDATE CaThuNgan SET thoiGianChot=?,doanhThuDichVu=?,phiKhamThuHo=?"
                    + " WHERE maCa=? AND thoiGianChot IS NULL", chot, tien[0], tien[1], maCa) != 1)
                throw new IllegalArgumentException("Ca đã được chốt ở phiên khác.");
            return new CaThuNgan(maCa, ca.maNhanVien(), ca.tenNhanVien(), ca.moLuc(),
                    chot, tien[0], tien[1]);
        });
    }

    public List<HoaDonLeTan> hoaDonTrongCa(CaThuNgan ca) {
        return doc(c -> truyVan(c, "SELECT " + COT_HOA_DON
                + " WHERE hd.ngayThu>=? AND hd.ngayThu<=? ORDER BY hd.ngayThu",
                this::hoaDon, ca.moLuc(), ca.dangMo() ? LocalDateTime.now() : ca.chotLuc()));
    }

    public List<CaThuNgan> lichSuCa() {
        return doc(c -> truyVan(c, "SELECT TOP(30) " + COT_CA
                + " ORDER BY c.thoiGianMo DESC", this::ca));
    }
}
