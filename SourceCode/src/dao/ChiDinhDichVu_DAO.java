package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entity.ChiDinhDichVu;

public class ChiDinhDichVu_DAO {

    public static void xoaTheoPhieu(Connection con, String maPhieuKham) throws SQLException {
        try (PreparedStatement p = con.prepareStatement("DELETE FROM ChiDinhGhiNhan WHERE maPhieuKham=?")) {
            p.setString(1, maPhieuKham);
            p.executeUpdate();
        }
        try (PreparedStatement pstm = con.prepareStatement("DELETE FROM ChiDinhDichVu WHERE maPhieuKham = ?")) {
            pstm.setString(1, maPhieuKham);
            pstm.executeUpdate();
        }
    }

    public static void themChiDinh(Connection con, ChiDinhDichVu cd) throws SQLException {
        try (PreparedStatement pstm = con.prepareStatement(
                "INSERT INTO ChiDinhDichVu (maPhieuKham, maDichVu, soRang,soLuong,donGia,tenDichVu,donViTinh,maHangMuc,maGhiNhan) VALUES (?, ?, ?,?,?,?,?,?,?)")) {
            pstm.setString(1, cd.getMaPhieuKham());
            pstm.setString(2, cd.getMaDichVu());
            pstm.setString(3, cd.getSoRang());
            pstm.setInt(4, cd.getSoLuong());
            pstm.setBigDecimal(5, cd.getDonGia());
            pstm.setString(6, cd.getTenDichVu());
            pstm.setString(7, cd.getDonViTinh());
            pstm.setString(8, cd.getHangMuc() == null ? null : cd.getHangMuc().getMaHangMuc());
            pstm.setString(9, cd.getGhiNhan() == null ? null : cd.getGhiNhan().getMaGhiNhan());
            pstm.executeUpdate();
        }
    }

    public static void themLienKet(Connection con, ChiDinhDichVu cd) throws SQLException {
        try (PreparedStatement p = con
                .prepareStatement("INSERT INTO ChiDinhGhiNhan(maPhieuKham,maDichVu,maGhiNhan) VALUES(?,?,?)")) {
            for (entity.GhiNhanKham g : cd.getDsGhiNhan()) {
                p.setString(1, cd.getMaPhieuKham());
                p.setString(2, cd.getMaDichVu());
                p.setString(3, g.getMaGhiNhan());
                p.addBatch();
            }
            p.executeBatch();
        }
    }

    public static ArrayList<ChiDinhDichVu> layTheoPhieu(String maPhieuKham) {
        String sql = "SELECT cd.maPhieuKham, cd.maDichVu, COALESCE(cd.tenDichVu,dv.tenDichVu) AS tenDichVu, cd.soRang,"
                + " CASE WHEN cd.maDichVu = 'DV0001' AND pk.maHoaDonPhiKham IS NOT NULL"
                + " THEN CAST(0 AS decimal(18,2)) ELSE COALESCE(cd.donGia,giaCu.donGia,dv.donGiaNiemYet) END AS donGia, COALESCE(giaCu.soLuong,cd.soLuong) AS soLuong,COALESCE(cd.donViTinh,dv.donViTinh) AS donViTinh,cd.maHangMuc,cd.maGhiNhan, CASE WHEN cd.donGia IS NULL AND giaCu.donGia IS NULL AND NOT (cd.maDichVu='DV0001' AND pk.maHoaDonPhiKham IS NOT NULL) THEN 1 ELSE 0 END AS giaThamKhao"
                + " FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu = cd.maDichVu"
                + " JOIN PhieuKham pk ON pk.maPhieuKham = cd.maPhieuKham"
                + " OUTER APPLY (SELECT TOP 1 ct.donGia,ct.soLuong FROM ChiTietHoaDon ct JOIN HoaDon hd ON hd.maHoaDon=ct.maHoaDon WHERE hd.maPhieuKham=cd.maPhieuKham AND ct.maDichVu=cd.maDichVu ORDER BY hd.ngayThu DESC,hd.maHoaDon DESC) giaCu"
                + " WHERE cd.maPhieuKham = ?";
        ArrayList<ChiDinhDichVu> ds = new ArrayList<>();
        Connection con = connectDB.ConnectDB.getInstance().getConnection();
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maPhieuKham);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    ChiDinhDichVu cd = new ChiDinhDichVu(rs.getString(1), rs.getString(2));
                    entity.DichVu dv = new entity.DichVu(rs.getString(2));
                    dv.setTenDichVu(rs.getString(3));
                    cd.setDichVu(dv);
                    cd.setSoRang(rs.getString(4));
                    cd.setDonGia(rs.getBigDecimal(5));
                    cd.setGiaThamKhao(rs.getBoolean("giaThamKhao"));
                    cd.setSoLuong(rs.getInt(6));
                    cd.setDonViTinh(rs.getString(7));
                    if (rs.getString(8) != null)
                        cd.setHangMuc(new entity.KeHoachDieuTri.HangMuc(rs.getString(8)));
                    if (rs.getString(9) != null)
                        cd.setGhiNhan(new entity.GhiNhanKham(rs.getString(9)));
                    ds.add(cd);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi truy vấn ChiDinhDichVu", ex);
        }
        java.util.Map<String, java.util.List<entity.GhiNhanKham>> lienKet = new java.util.HashMap<>();
        try (PreparedStatement p = con.prepareStatement(
                "SELECT l.maDichVu,l.maGhiNhan,g.maHangMuc FROM ChiDinhGhiNhan l JOIN GhiNhanKham g ON g.maGhiNhan=l.maGhiNhan WHERE l.maPhieuKham=?")) {
            p.setString(1, maPhieuKham);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    entity.GhiNhanKham g = new entity.GhiNhanKham(r.getString(2));
                    if (r.getString(3) != null)
                        g.setHangMuc(new entity.KeHoachDieuTri.HangMuc(r.getString(3)));
                    lienKet.computeIfAbsent(r.getString(1), x -> new java.util.ArrayList<>()).add(g);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi đọc liên kết dịch vụ - ghi nhận", ex);
        }
        for (ChiDinhDichVu cd : ds)
            if (lienKet.containsKey(cd.getMaDichVu()))
                cd.setDsGhiNhan(lienKet.get(cd.getMaDichVu()));
        return ds;
    }

    private ChiDinhDichVu_DAO() {
    }
}
