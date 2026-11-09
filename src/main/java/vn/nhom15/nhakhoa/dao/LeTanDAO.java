package vn.nhom15.nhakhoa.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import vn.nhom15.nhakhoa.connect.NguonKetNoi;
import vn.nhom15.nhakhoa.entity.*;

/**
 * Kho JDBC của lễ tân — giữ nguyên đúng cấu trúc bảng/cột SQL và các Entity OOP
 * (BenhNhan, HoaDon, ChiTietHoaDon, ChiDinhDichVu, PhieuKham, PhongKham, CaThuNgan, TrangThaiPhieuKham).
 */
public class LeTanDAO {
    private final NguonKetNoi nguonKetNoi;

    public LeTanDAO(NguonKetNoi nguonKetNoi) {
        this.nguonKetNoi = nguonKetNoi;
    }

    @FunctionalInterface
    private interface TacVu<T> {
        T chay(Connection c) throws SQLException;
    }

    @FunctionalInterface
    private interface AnhXa<T> {
        T doc(ResultSet r) throws SQLException;
    }

    private <T> T doc(TacVu<T> tacVu) {
        try (Connection c = nguonKetNoi.moKetNoi()) {
            return tacVu.chay(c);
        } catch (SQLException loi) {
            throw loiDuLieu(loi);
        }
    }

    private <T> T ghi(TacVu<T> tacVu) {
        return doc(c -> {
            c.setAutoCommit(false);
            try {
                T ketQua = tacVu.chay(c);
                c.commit();
                return ketQua;
            } catch (SQLException | RuntimeException loi) {
                try {
                    c.rollback();
                } catch (SQLException loiKhac) {
                    loi.addSuppressed(loiKhac);
                }
                throw loi;
            }
        });
    }

    private NgoaiLeTruyCapDuLieu loiDuLieu(SQLException loi) {
        String thongBao = switch (loi.getErrorCode()) {
            case 2601, 2627 -> "Dữ liệu đã tồn tại hoặc vừa được nhân viên khác cập nhật. Vui lòng tải lại.";
            case 547 -> "Dữ liệu đang được sử dụng hoặc không hợp lệ.";
            default -> "Không thực hiện được thao tác lễ tân: " + loi.getMessage();
        };
        return new NgoaiLeTruyCapDuLieu(thongBao, loi);
    }

    private PreparedStatement cauLenh(Connection c, String sql, Object... giaTri) throws SQLException {
        PreparedStatement p = c.prepareStatement(sql);
        try {
            p.setQueryTimeout(15);
            for (int i = 0; i < giaTri.length; i++) {
                Object v = giaTri[i];
                if (v instanceof LocalDate d)
                    p.setDate(i + 1, Date.valueOf(d));
                else if (v instanceof LocalDateTime d)
                    p.setTimestamp(i + 1, Timestamp.valueOf(d));
                else
                    p.setObject(i + 1, v);
            }
            return p;
        } catch (SQLException loi) {
            p.close();
            throw loi;
        }
    }

    private <T> List<T> truyVan(Connection c, String sql, AnhXa<T> anhXa, Object... giaTri) throws SQLException {
        try (PreparedStatement p = cauLenh(c, sql, giaTri); ResultSet r = p.executeQuery()) {
            List<T> ds = new ArrayList<>();
            while (r.next())
                ds.add(anhXa.doc(r));
            return ds;
        }
    }

    private int capNhat(Connection c, String sql, Object... giaTri) throws SQLException {
        try (PreparedStatement p = cauLenh(c, sql, giaTri)) {
            return p.executeUpdate();
        }
    }

    private boolean coBangBoDem(Connection c) throws SQLException {
        return !truyVan(c, "SELECT 1 WHERE OBJECT_ID(N'dbo.BoDem', N'U') IS NOT NULL", r -> r.getInt(1)).isEmpty();
    }

    private String maMoi(Connection c, String loaiMa, String bang, String cot) throws SQLException {
        LocalDate ngay = LocalDate.now();
        if (coBangBoDem(c)) {
            String ma;
            do {
                int n = tangBoDem(c, loaiMa, ngay);
                ma = String.format("%s%02d%02d%02d%04d", loaiMa, ngay.getYear() % 100,
                        ngay.getMonthValue(), ngay.getDayOfMonth(), n);
            } while (!truyVan(c, "SELECT 1 FROM " + bang + " WHERE " + cot + "=?", r -> r.getInt(1), ma).isEmpty());
            return ma;
        }
        String khoa = loaiMa + ngay.format(DateTimeFormatter.ofPattern("yyMMdd"));
        List<Integer> daCo = truyVan(c, "SELECT giaTri FROM DuyBoDem WITH(UPDLOCK,HOLDLOCK) WHERE khoa=?",
                r -> r.getInt(1), khoa);
        int so = daCo.isEmpty() ? 0 : daCo.getFirst();
        String ma;
        do {
            if (++so > 9999)
                throw new IllegalArgumentException("Đã hết dải mã " + khoa + ".");
            ma = khoa + String.format("%04d", so);
        } while (!truyVan(c, "SELECT 1 FROM " + bang + " WHERE " + cot + "=?", r -> r.getInt(1), ma).isEmpty());
        if (daCo.isEmpty())
            capNhat(c, "INSERT INTO DuyBoDem(khoa,giaTri) VALUES(?,?)", khoa, so);
        else
            capNhat(c, "UPDATE DuyBoDem SET giaTri=? WHERE khoa=?", so, khoa);
        return ma;
    }

    private int tangBoDem(Connection c, String loaiMa, LocalDate ngay) throws SQLException {
        List<Integer> daCo = truyVan(c, "SELECT giaTri FROM BoDem WITH (UPDLOCK, HOLDLOCK) WHERE loaiMa=? AND ngay=?",
                r -> r.getInt(1), loaiMa, ngay);
        if (daCo.isEmpty()) {
            capNhat(c, "INSERT INTO BoDem (loaiMa, ngay, giaTri) VALUES (?, ?, 1)", loaiMa, ngay);
            return 1;
        }
        int moi = daCo.getFirst() + 1;
        if (moi > 9999)
            throw new IllegalArgumentException("Đã hết dải mã " + loaiMa + " ngày " + ngay + ".");
        capNhat(c, "UPDATE BoDem SET giaTri=? WHERE loaiMa=? AND ngay=?", moi, loaiMa, ngay);
        return moi;
    }

    private int soThuTuTiepTheo(Connection c, LocalDate ngay, String maPhong) throws SQLException {
        if (coBangBoDem(c)) {
            int stt;
            do {
                stt = tangBoDem(c, "STT-" + maPhong, ngay);
            } while (!truyVan(c, "SELECT 1 FROM PhieuKham WHERE ngayKham=? AND maPhong=? AND soThuTu=?",
                    r -> r.getInt(1), ngay, maPhong, stt).isEmpty());
            return stt;
        }
        List<Integer> max = truyVan(c,
                "SELECT COALESCE(MAX(soThuTu), 0) FROM PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE ngayKham=? AND maPhong=?",
                r -> r.getInt(1), ngay, maPhong);
        return (max.isEmpty() ? 0 : max.getFirst()) + 1;
    }

    // ---- BenhNhan ----
    private static final String COLS_BN = "maBenhNhan, hoTen, soDienThoai, cccd, ngaySinh, diaChi, tienSuDiUng";

    private BenhNhan mapBenhNhan(ResultSet r) throws SQLException {
        Date sinh = r.getDate("ngaySinh");
        return new BenhNhan(
                r.getString("maBenhNhan"),
                r.getString("hoTen"),
                r.getString("soDienThoai"),
                r.getString("cccd"),
                sinh == null ? null : sinh.toLocalDate(),
                r.getString("diaChi"),
                r.getString("tienSuDiUng"));
    }

    public List<BenhNhan> danhSachBenhNhan() {
        return doc(c -> truyVan(c,
                "SELECT " + COLS_BN + " FROM BenhNhan ORDER BY maBenhNhan DESC",
                this::mapBenhNhan));
    }

    public List<BenhNhan> traCuuBenhNhan(String tieuChi, String tuKhoa) {
        return doc(c -> switch (tieuChi == null ? "" : tieuChi) {
            case "Mã" -> truyVan(c, "SELECT " + COLS_BN + " FROM BenhNhan WHERE maBenhNhan = ?",
                    this::mapBenhNhan, tuKhoa);
            case "SĐT" -> truyVan(c, "SELECT " + COLS_BN + " FROM BenhNhan WHERE soDienThoai = ?",
                    this::mapBenhNhan, tuKhoa);
            default -> truyVan(c,
                    "SELECT " + COLS_BN + " FROM BenhNhan WHERE hoTen COLLATE Latin1_General_CI_AI LIKE ?"
                            + " ORDER BY maBenhNhan DESC",
                    this::mapBenhNhan, "%" + tuKhoa + "%");
        });
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) {
        return doc(c -> truyVan(c,
                "SELECT TOP (100) " + COLS_BN + " FROM BenhNhan"
                        + " WHERE maBenhNhan = ? OR soDienThoai LIKE ? OR cccd = ?"
                        + " OR hoTen COLLATE Latin1_General_CI_AI LIKE ?"
                        + " ORDER BY maBenhNhan DESC",
                this::mapBenhNhan, tuKhoa, tuKhoa + "%", tuKhoa, "%" + tuKhoa + "%"));
    }

    public BenhNhan luuBenhNhan(BenhNhan bn) {
        return ghi(c -> {
            String ma = bn.getMaBenhNhan();
            if (ma != null && !truyVan(c, "SELECT 1 FROM BenhNhan WHERE soDienThoai=? AND maBenhNhan<>?",
                    r -> r.getInt(1), bn.getSoDienThoai(), ma).isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại ở hồ sơ bệnh nhân khác.");
            }
            if (ma == null && !truyVan(c, "SELECT 1 FROM BenhNhan WHERE soDienThoai=?",
                    r -> r.getInt(1), bn.getSoDienThoai()).isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại ở hồ sơ bệnh nhân khác.");
            }
            if (bn.getCccd() != null) {
                List<String> trung = truyVan(c, "SELECT maBenhNhan FROM BenhNhan WHERE cccd=?",
                        r -> r.getString(1), bn.getCccd());
                if (!trung.isEmpty() && (ma == null || !trung.getFirst().equals(ma))) {
                    throw new IllegalArgumentException("Số CCCD đã tồn tại ở hồ sơ bệnh nhân khác.");
                }
            }
            if (ma == null) {
                ma = maMoi(c, "BN", "BenhNhan", "maBenhNhan");
                capNhat(c,
                        "INSERT INTO BenhNhan (maBenhNhan, hoTen, soDienThoai, cccd, ngaySinh, diaChi, tienSuDiUng)"
                                + " VALUES (?, ?, ?, ?, ?, ?, ?)",
                        ma, bn.getHoTen(), bn.getSoDienThoai(), bn.getCccd(),
                        bn.getNgaySinh(), bn.getDiaChi(), bn.getTienSuDiUng());
            } else {
                if (capNhat(c,
                        "UPDATE BenhNhan SET hoTen=?, soDienThoai=?, cccd=?, ngaySinh=?, diaChi=?, tienSuDiUng=?"
                                + " WHERE maBenhNhan=?",
                        bn.getHoTen(), bn.getSoDienThoai(), bn.getCccd(),
                        bn.getNgaySinh(), bn.getDiaChi(), bn.getTienSuDiUng(), ma) != 1) {
                    throw new IllegalArgumentException("Hồ sơ bệnh nhân không còn tồn tại.");
                }
            }
            return new BenhNhan(ma, bn.getHoTen(), bn.getSoDienThoai(), bn.getCccd(),
                    bn.getNgaySinh(), bn.getDiaChi(), bn.getTienSuDiUng());
        });
    }

    // ---- PhongKham ----
    public List<PhongKham> danhSachPhong() {
        return doc(c -> truyVan(c,
                "SELECT maPhong, tenPhong, dangNhanBenh, maBacSi, maPhuTa FROM PhongKham ORDER BY maPhong",
                r -> new PhongKham(r.getString("maPhong"), r.getString("tenPhong"),
                        r.getBoolean("dangNhanBenh"), r.getString("maBacSi"), r.getString("maPhuTa"))));
    }

    // ---- PhieuKham ----
    private static final String COLS_PK = "pk.maPhieuKham, pk.ngayKham, pk.soThuTu, pk.lyDoKham, pk.chanDoan,"
            + " pk.ketQuaDieuTri, pk.ghiChuThuocSuDung, pk.ghiChuThuocTuMua, pk.trangThai, pk.maBenhNhan,"
            + " pk.phacDieuTri, pk.ngayHenTiep, pk.maNhanVien, pk.maPhong, pk.maHoSo, pk.maHoaDonPhiKham,"
            + " bn.hoTen AS tenBenhNhan, nv.hoTen AS tenBacSi, p.tenPhong AS tenPhong";
    private static final String JOINS_PK = " FROM PhieuKham pk"
            + " JOIN BenhNhan bn ON bn.maBenhNhan = pk.maBenhNhan"
            + " JOIN NhanVien nv ON nv.maNhanVien = pk.maNhanVien"
            + " JOIN PhongKham p ON p.maPhong = pk.maPhong";

    private PhieuKham mapPhieuKham(ResultSet rs) throws SQLException {
        PhieuKham pk = new PhieuKham();
        pk.setMaPhieuKham(rs.getString("maPhieuKham"));
        pk.setNgayKham(rs.getDate("ngayKham").toLocalDate());
        pk.setSoThuTu(rs.getInt("soThuTu"));
        pk.setLyDoKham(rs.getString("lyDoKham"));
        pk.setChanDoan(rs.getString("chanDoan"));
        pk.setKetQuaDieuTri(rs.getString("ketQuaDieuTri"));
        pk.setGhiChuThuocSuDung(rs.getString("ghiChuThuocSuDung"));
        pk.setGhiChuThuocTuMua(rs.getString("ghiChuThuocTuMua"));
        pk.setTrangThai(TrangThaiPhieuKham.valueOf(rs.getString("trangThai")));

        BenhNhan bn = new BenhNhan(rs.getString("maBenhNhan"));
        bn.setHoTen(rs.getString("tenBenhNhan"));
        pk.setBenhNhan(bn);

        NhanVien nv = new NhanVien(rs.getString("maNhanVien"));
        nv.setHoTen(rs.getString("tenBacSi"));
        pk.setNhanVien(nv);

        PhongKham phong = new PhongKham(rs.getString("maPhong"));
        phong.setTenPhong(rs.getString("tenPhong"));
        pk.setPhong(phong);

        pk.setMaHoaDonPhiKham(rs.getString("maHoaDonPhiKham"));
        pk.setMaHoSo(rs.getString("maHoSo"));
        pk.setPhacDieuTri(rs.getString("phacDieuTri"));
        Date hen = rs.getDate("ngayHenTiep");
        pk.setNgayHenTiep(hen == null ? null : hen.toLocalDate());
        return pk;
    }

    public List<PhieuKham> lichSuPhieuKham(String maBenhNhan) {
        return doc(c -> truyVan(c,
                "SELECT " + COLS_PK + JOINS_PK + " WHERE pk.maBenhNhan = ? ORDER BY pk.ngayKham DESC, pk.soThuTu",
                this::mapPhieuKham, maBenhNhan));
    }

    public List<PhieuKham> danhSachLuot(String maPhong, LocalDate ngay) {
        return doc(c -> truyVan(c,
                "SELECT " + COLS_PK + JOINS_PK
                        + " WHERE pk.maPhong = ? AND pk.ngayKham = ? AND pk.trangThai IN ('CHO_KHAM', 'DANG_KHAM')"
                        + " ORDER BY pk.soThuTu",
                this::mapPhieuKham, maPhong, ngay));
    }

    public List<PhieuKham> phieuChoThanhToan() {
        return doc(c -> truyVan(c,
                "SELECT " + COLS_PK + JOINS_PK
                        + " WHERE pk.trangThai = 'HOAN_TAT'"
                        + " AND EXISTS (SELECT 1 FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu = cd.maDichVu"
                        + " WHERE cd.maPhieuKham = pk.maPhieuKham"
                        + " AND CASE WHEN cd.maDichVu = 'DV0001' AND pk.maHoaDonPhiKham IS NOT NULL"
                        + " THEN 0 ELSE dv.donGiaNiemYet END > 0)"
                        + " AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maPhieuKham = pk.maPhieuKham)"
                        + " ORDER BY pk.ngayKham DESC",
                this::mapPhieuKham));
    }

    // ---- ChiDinhDichVu ----
    public List<ChiDinhDichVu> dichVuTheoPhieu(String maPhieuKham) {
        return doc(c -> truyVan(c,
                "SELECT cd.maPhieuKham, cd.maDichVu, dv.tenDichVu, cd.soRang,"
                        + " CASE WHEN cd.maDichVu = 'DV0001' AND pk.maHoaDonPhiKham IS NOT NULL"
                        + " THEN CAST(0 AS decimal(18,2)) ELSE dv.donGiaNiemYet END AS donGia"
                        + " FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu = cd.maDichVu"
                        + " JOIN PhieuKham pk ON pk.maPhieuKham = cd.maPhieuKham WHERE cd.maPhieuKham = ?",
                r -> {
                    ChiDinhDichVu cd = new ChiDinhDichVu(r.getString(1), r.getString(2));
                    DichVu dv = new DichVu(r.getString(2));
                    dv.setTenDichVu(r.getString(3));
                    cd.setDichVu(dv);
                    cd.setSoRang(r.getString(4));
                    cd.setDonGia(r.getBigDecimal(5));
                    return cd;
                }, maPhieuKham).stream()
                .filter(cd -> cd.getDonGia() != null && cd.getDonGia().signum() > 0)
                .toList());
    }

    // ---- HoaDon & ChiTietHoaDon ----
    private static final String COLS_HD = "hd.maHoaDon, hd.ngayThu, hd.hinhThucThanhToan, hd.daThanhToan,"
            + " hd.tienKhachDua, hd.maBenhNhan, hd.maNhanVien, hd.maPhieuKham,"
            + " bn.hoTen AS tenBenhNhan, bn.soDienThoai AS bnSdt, nv.hoTen AS tenNhanVien";

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getString("maHoaDon"));
        Timestamp ts = rs.getTimestamp("ngayThu");
        hd.setNgayThu(ts == null ? null : ts.toLocalDateTime());
        hd.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
        hd.setDaThanhToan(rs.getBoolean("daThanhToan"));
        hd.setTienKhachDua(rs.getBigDecimal("tienKhachDua"));
        hd.setMaBenhNhan(rs.getString("maBenhNhan"));
        hd.setMaNhanVien(rs.getString("maNhanVien"));
        hd.setMaPhieuKham(rs.getString("maPhieuKham"));
        hd.setBenhNhan(new BenhNhan(rs.getString("maBenhNhan"), rs.getString("tenBenhNhan"),
                rs.getString("bnSdt"), null, null, null, null));
        NhanVien nv = new NhanVien(rs.getString("maNhanVien"));
        nv.setHoTen(rs.getString("tenNhanVien"));
        hd.setNhanVien(nv);
        return hd;
    }

    private HoaDon napChiTiet(Connection c, HoaDon hd) throws SQLException {
        if (hd == null)
            return null;
        List<ChiTietHoaDon> ds = truyVan(c,
                "SELECT ct.maHoaDon, ct.maDichVu, ct.soLuong, ct.donGia, ct.thanhTien, dv.tenDichVu"
                        + " FROM ChiTietHoaDon ct JOIN DichVu dv ON dv.maDichVu = ct.maDichVu WHERE ct.maHoaDon = ?",
                r -> {
                    ChiTietHoaDon ct = new ChiTietHoaDon(r.getString(1), r.getString(2),
                            r.getInt(3), r.getBigDecimal(4));
                    DichVu dv = new DichVu(r.getString(2));
                    dv.setTenDichVu(r.getString(6));
                    ct.setDichVu(dv);
                    return ct;
                }, hd.getMaHoaDon());
        for (ChiTietHoaDon ct : ds)
            hd.themChiTiet(ct);
        return hd;
    }

    public HoaDon phiChuaDung(String maBenhNhan) {
        return doc(c -> {
            List<HoaDon> ds = truyVan(c,
                    "SELECT " + COLS_HD + " FROM HoaDon hd"
                            + " JOIN BenhNhan bn ON bn.maBenhNhan = hd.maBenhNhan"
                            + " LEFT JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien"
                            + " JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon AND ct.maDichVu = 'DV0001'"
                            + " WHERE hd.maBenhNhan = ? AND hd.daThanhToan = 1"
                            + " AND NOT EXISTS (SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham = hd.maHoaDon)"
                            + " ORDER BY hd.ngayThu DESC",
                    this::mapHoaDon, maBenhNhan);
            return ds.isEmpty() ? null : napChiTiet(c, ds.getFirst());
        });
    }

    public HoaDon thuPhi(String maBenhNhan, String hinhThucThanhToan, BigDecimal tienKhachDua, String maNhanVien) {
        return ghi(c -> {
            List<BenhNhan> bns = truyVan(c, "SELECT " + COLS_BN + " FROM BenhNhan WHERE maBenhNhan = ?",
                    this::mapBenhNhan, maBenhNhan);
            if (bns.isEmpty())
                throw new IllegalArgumentException("Không tìm thấy hồ sơ bệnh nhân.");
            if (!truyVan(c,
                    "SELECT 1 FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon AND ct.maDichVu = 'DV0001'"
                            + " WHERE hd.maBenhNhan = ? AND hd.daThanhToan = 1"
                            + " AND NOT EXISTS (SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham = hd.maHoaDon)",
                    r -> r.getInt(1), maBenhNhan).isEmpty()) {
                throw new IllegalArgumentException("Bệnh nhân đã thanh toán phí khám trước đó (chưa dùng).");
            }
            List<BigDecimal> gias = truyVan(c,
                    "SELECT donGiaNiemYet FROM DichVu WHERE maDichVu = 'DV0001' AND trangThai = 'DANG_PHUC_VU'",
                    r -> r.getBigDecimal(1));
            if (gias.isEmpty())
                throw new IllegalStateException("Thiếu dịch vụ 'Phí khám ban đầu' (DV0001).");
            BigDecimal phi = gias.getFirst();
            if (tienKhachDua.compareTo(phi) < 0)
                throw new IllegalArgumentException("Tiền khách đưa chưa đủ mức phí khám.");

            String maHD = maMoi(c, "HD", "HoaDon", "maHoaDon");
            HoaDon hd = new HoaDon(maHD);
            hd.setNgayThu(LocalDateTime.now());
            hd.setHinhThucThanhToan(hinhThucThanhToan);
            hd.setDaThanhToan(true);
            hd.setTienKhachDua(tienKhachDua);
            hd.setBenhNhan(bns.getFirst());
            hd.setMaNhanVien(maNhanVien);

            ChiTietHoaDon cdPhi = new ChiTietHoaDon(maHD, "DV0001", 1, phi);
            DichVu dv = new DichVu("DV0001");
            dv.setTenDichVu("Phí khám ban đầu");
            cdPhi.setDichVu(dv);
            hd.themChiTiet(cdPhi);

            capNhat(c,
                    "INSERT INTO HoaDon (maHoaDon, ngayThu, hinhThucThanhToan, daThanhToan, tienKhachDua,"
                            + " maBenhNhan, maNhanVien, maPhieuKham) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    hd.getMaHoaDon(), hd.getNgayThu(), hd.getHinhThucThanhToan(), hd.isDaThanhToan(),
                    hd.getTienKhachDua(), hd.getMaBenhNhan(), hd.getMaNhanVien(), hd.getMaPhieuKham());
            capNhat(c,
                    "INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGia, thanhTien)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    cdPhi.getMaHoaDon(), cdPhi.getMaDichVu(), cdPhi.getSoLuong(),
                    cdPhi.getDonGia(), cdPhi.getThanhTien());
            return hd;
        });
    }

    public PhieuKham lapPhieu(String maBenhNhan, String maPhong, String lyDo) {
        return ghi(c -> {
            List<PhongKham> phongs = truyVan(c,
                    "SELECT maPhong, tenPhong, dangNhanBenh, maBacSi, maPhuTa"
                            + " FROM PhongKham WITH(UPDLOCK,HOLDLOCK) WHERE maPhong = ?",
                    r -> new PhongKham(r.getString("maPhong"), r.getString("tenPhong"),
                            r.getBoolean("dangNhanBenh"), r.getString("maBacSi"), r.getString("maPhuTa")),
                    maPhong);
            if (phongs.isEmpty())
                throw new IllegalArgumentException("Không tìm thấy phòng khám.");
            PhongKham phong = phongs.getFirst();
            if (!phong.isDangNhanBenh() || phong.getMaBacSi() == null || phong.getMaPhuTa() == null)
                throw new IllegalArgumentException("Phòng khám đang ngừng tiếp nhận hoặc chưa phân công đủ bác sĩ + phụ tá.");

            List<BenhNhan> bns = truyVan(c, "SELECT " + COLS_BN + " FROM BenhNhan WHERE maBenhNhan = ?",
                    this::mapBenhNhan, maBenhNhan);
            if (bns.isEmpty())
                throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");

            List<String> phis = truyVan(c,
                    "SELECT TOP (1) hd.maHoaDon FROM HoaDon hd"
                            + " JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon AND ct.maDichVu = 'DV0001'"
                            + " WHERE hd.maBenhNhan = ? AND hd.daThanhToan = 1"
                            + " AND NOT EXISTS (SELECT 1 FROM PhieuKham pk WHERE pk.maHoaDonPhiKham = hd.maHoaDon)"
                            + " ORDER BY hd.ngayThu DESC",
                    r -> r.getString(1), maBenhNhan);
            if (phis.isEmpty())
                throw new IllegalArgumentException("Bệnh nhân chưa thanh toán phí khám ban đầu.");

            LocalDate ngayKham = LocalDate.now();
            String maPK = maMoi(c, "PK", "PhieuKham", "maPhieuKham");
            int stt = soThuTuTiepTheo(c, ngayKham, maPhong);

            capNhat(c,
                    "INSERT INTO PhieuKham (maPhieuKham, ngayKham, soThuTu, lyDoKham, trangThai,"
                            + " maBenhNhan, maNhanVien, maPhong, maHoaDonPhiKham) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    maPK, ngayKham, stt, lyDo, TrangThaiPhieuKham.CHO_KHAM.name(),
                    maBenhNhan, phong.getMaBacSi(), maPhong, phis.getFirst());

            return truyVan(c, "SELECT " + COLS_PK + JOINS_PK + " WHERE pk.maPhieuKham = ?",
                    this::mapPhieuKham, maPK).getFirst();
        });
    }

    public HoaDon thanhToanDichVu(String maPhieuKham, String hinhThucThanhToan,
            BigDecimal tienKhachDua, String maNhanVien) {
        return ghi(c -> {
            List<PhieuKham> pks = truyVan(c, "SELECT " + COLS_PK + JOINS_PK + " WHERE pk.maPhieuKham = ?",
                    this::mapPhieuKham, maPhieuKham);
            if (pks.isEmpty() || pks.getFirst().getTrangThai() != TrangThaiPhieuKham.HOAN_TAT)
                throw new IllegalArgumentException("Chỉ thanh toán khi lượt khám đã hoàn tất.");
            if (!truyVan(c, "SELECT 1 FROM HoaDon WHERE maPhieuKham = ?", r -> r.getInt(1), maPhieuKham).isEmpty())
                throw new IllegalArgumentException("Lượt khám đã được thanh toán.");

            List<ChiDinhDichVu> chiDinh = truyVan(c,
                    "SELECT cd.maPhieuKham, cd.maDichVu, dv.tenDichVu, cd.soRang,"
                            + " CASE WHEN cd.maDichVu = 'DV0001' AND pk.maHoaDonPhiKham IS NOT NULL"
                            + " THEN CAST(0 AS decimal(18,2)) ELSE dv.donGiaNiemYet END AS donGia"
                            + " FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu = cd.maDichVu"
                            + " JOIN PhieuKham pk ON pk.maPhieuKham = cd.maPhieuKham WHERE cd.maPhieuKham = ?",
                    r -> {
                        ChiDinhDichVu cd = new ChiDinhDichVu(r.getString(1), r.getString(2));
                        DichVu dv = new DichVu(r.getString(2));
                        dv.setTenDichVu(r.getString(3));
                        cd.setDichVu(dv);
                        cd.setSoRang(r.getString(4));
                        cd.setDonGia(r.getBigDecimal(5));
                        return cd;
                    }, maPhieuKham).stream()
                    .filter(cd -> cd.getDonGia() != null && cd.getDonGia().signum() > 0)
                    .toList();
            if (chiDinh.isEmpty())
                throw new IllegalArgumentException("Phiếu khám chưa có dịch vụ chỉ định cần thanh toán.");

            BigDecimal tong = chiDinh.stream().map(ChiDinhDichVu::getDonGia).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (tienKhachDua.compareTo(tong) < 0)
                throw new IllegalArgumentException("Tiền khách đưa chưa đủ để thanh toán.");

            String maHD = maMoi(c, "HD", "HoaDon", "maHoaDon");
            PhieuKham pk = pks.getFirst();
            HoaDon hd = new HoaDon(maHD);
            hd.setNgayThu(LocalDateTime.now());
            hd.setHinhThucThanhToan(hinhThucThanhToan);
            hd.setDaThanhToan(true);
            hd.setTienKhachDua(tienKhachDua);
            hd.setBenhNhan(pk.getBenhNhan());
            hd.setMaNhanVien(maNhanVien);
            hd.setMaPhieuKham(maPhieuKham);

            capNhat(c,
                    "INSERT INTO HoaDon (maHoaDon, ngayThu, hinhThucThanhToan, daThanhToan, tienKhachDua,"
                            + " maBenhNhan, maNhanVien, maPhieuKham) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    hd.getMaHoaDon(), hd.getNgayThu(), hd.getHinhThucThanhToan(), hd.isDaThanhToan(),
                    hd.getTienKhachDua(), hd.getMaBenhNhan(), hd.getMaNhanVien(), hd.getMaPhieuKham());
            for (ChiDinhDichVu cdd : chiDinh) {
                ChiTietHoaDon ctd = new ChiTietHoaDon(maHD, cdd.getMaDichVu(), 1, cdd.getDonGia());
                ctd.setDichVu(cdd.getDichVu());
                hd.themChiTiet(ctd);
                capNhat(c,
                        "INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGia, thanhTien)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        ctd.getMaHoaDon(), ctd.getMaDichVu(), ctd.getSoLuong(),
                        ctd.getDonGia(), ctd.getThanhTien());
            }
            return hd;
        });
    }

    // ---- CaThuNgan ----
    private static final String COLS_CA = "c.maCa, c.maNhanVien, c.thoiGianMo, c.thoiGianChot,"
            + " c.doanhThuDichVu, c.phiThuHo, c.trangThai, nv.hoTen AS tenNhanVien"
            + " FROM CaThuNgan c JOIN NhanVien nv ON nv.maNhanVien = c.maNhanVien";

    private CaThuNgan mapCa(ResultSet rs) throws SQLException {
        CaThuNgan ca = new CaThuNgan(rs.getString("maCa"));
        NhanVien nv = new NhanVien(rs.getString("maNhanVien"));
        nv.setHoTen(rs.getString("tenNhanVien"));
        ca.setNhanVien(nv);
        Timestamp mo = rs.getTimestamp("thoiGianMo");
        ca.setThoiGianMo(mo == null ? null : mo.toLocalDateTime());
        Timestamp chot = rs.getTimestamp("thoiGianChot");
        ca.setThoiGianChot(chot == null ? null : chot.toLocalDateTime());
        ca.setDoanhThuDichVu(rs.getBigDecimal("doanhThuDichVu"));
        ca.setPhiThuHo(rs.getBigDecimal("phiThuHo"));
        ca.setTrangThai(rs.getString("trangThai"));
        return ca;
    }

    public CaThuNgan caDangMo() {
        return doc(c -> truyVan(c,
                "SELECT " + COLS_CA + " WHERE c.trangThai = N'DANG_MO' ORDER BY c.thoiGianMo DESC",
                this::mapCa).stream().findFirst().orElse(null));
    }

    public CaThuNgan moCa(String maNhanVien) {
        return ghi(c -> {
            if (!truyVan(c, "SELECT 1 FROM CaThuNgan WHERE trangThai = N'DANG_MO'", r -> r.getInt(1)).isEmpty())
                throw new IllegalStateException("Còn ca đang mở — hãy chốt ca trước khi nhận ca mới.");
            String maCa = maMoi(c, "CT", "CaThuNgan", "maCa");
            LocalDateTime lucMo = LocalDateTime.now();
            capNhat(c,
                    "INSERT INTO CaThuNgan (maCa, maNhanVien, thoiGianMo, trangThai) VALUES (?, ?, ?, N'DANG_MO')",
                    maCa, maNhanVien, lucMo);
            List<String> tens = truyVan(c, "SELECT hoTen FROM NhanVien WHERE maNhanVien = ?",
                    r -> r.getString(1), maNhanVien);
            CaThuNgan ca = new CaThuNgan(maCa);
            NhanVien nv = new NhanVien(maNhanVien);
            nv.setHoTen(tens.isEmpty() ? maNhanVien : tens.getFirst());
            ca.setNhanVien(nv);
            ca.setThoiGianMo(lucMo);
            ca.setTrangThai(CaThuNgan.DANG_MO);
            return ca;
        });
    }

    public List<HoaDon> hoaDonTrongCa(CaThuNgan ca) {
        if (ca == null || ca.getThoiGianMo() == null)
            return List.of();
        LocalDateTime den = ca.getThoiGianChot() == null ? LocalDateTime.now() : ca.getThoiGianChot();
        return doc(c -> {
            List<HoaDon> ds = truyVan(c,
                    "SELECT " + COLS_HD + " FROM HoaDon hd"
                            + " JOIN BenhNhan bn ON bn.maBenhNhan = hd.maBenhNhan"
                            + " LEFT JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien"
                            + " WHERE hd.daThanhToan = 1 AND hd.ngayThu >= ? AND hd.ngayThu <= ?"
                            + " ORDER BY hd.ngayThu",
                    this::mapHoaDon, ca.getThoiGianMo(), den);
            for (HoaDon hd : ds)
                napChiTiet(c, hd);
            return ds;
        });
    }

    public CaThuNgan chotCa(String maCa) {
        return ghi(c -> {
            List<CaThuNgan> ds = truyVan(c,
                    "SELECT " + COLS_CA + " WHERE c.maCa = ? AND c.trangThai = N'DANG_MO'",
                    this::mapCa, maCa);
            if (ds.isEmpty())
                throw new IllegalStateException("Không có ca đang mở để chốt.");
            CaThuNgan ca = ds.getFirst();
            LocalDateTime chot = LocalDateTime.now();
            List<HoaDon> hds = truyVan(c,
                    "SELECT " + COLS_HD + " FROM HoaDon hd"
                            + " JOIN BenhNhan bn ON bn.maBenhNhan = hd.maBenhNhan"
                            + " LEFT JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien"
                            + " WHERE hd.daThanhToan = 1 AND hd.ngayThu >= ? AND hd.ngayThu <= ?",
                    this::mapHoaDon, ca.getThoiGianMo(), chot);
            BigDecimal dtDv = BigDecimal.ZERO;
            BigDecimal phiHo = BigDecimal.ZERO;
            for (HoaDon hd : hds) {
                napChiTiet(c, hd);
                if (hd.getMaPhieuKham() != null)
                    dtDv = dtDv.add(hd.getTongTien());
                else
                    phiHo = phiHo.add(hd.getTongTien());
            }
            if (capNhat(c,
                    "UPDATE CaThuNgan SET thoiGianChot = ?, doanhThuDichVu = ?, phiThuHo = ?,"
                            + " trangThai = N'DA_CHOT' WHERE maCa = ? AND trangThai = N'DANG_MO'",
                    chot, dtDv, phiHo, maCa) != 1) {
                throw new IllegalStateException("Ca đã được chốt bởi phiên khác.");
            }
            ca.setThoiGianChot(chot);
            ca.setDoanhThuDichVu(dtDv);
            ca.setPhiThuHo(phiHo);
            ca.setTrangThai(CaThuNgan.DA_CHOT);
            return ca;
        });
    }

    public List<CaThuNgan> lichSuCa() {
        return doc(c -> truyVan(c,
                "SELECT TOP (30) " + COLS_CA + " ORDER BY c.thoiGianMo DESC",
                this::mapCa));
    }
}
