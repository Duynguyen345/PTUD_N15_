package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entity.ChiTietCapThuoc;

public class ChiTietCapThuoc_DAO {

    public static void xoaTheoPhieu(Connection con, String maPhieuKham) throws SQLException {
        try (PreparedStatement pstm = con.prepareStatement("DELETE FROM ChiTietCapThuoc WHERE maPhieuKham = ?")) {
            pstm.setString(1, maPhieuKham);
            pstm.executeUpdate();
        }
    }

    public static void themChiTiet(Connection con, ChiTietCapThuoc ct) throws SQLException {
        try (PreparedStatement pstm = con.prepareStatement(
                "INSERT INTO ChiTietCapThuoc (maPhieuKham, maThuoc, soLuong, ghiChuCapThuoc,tenThuoc,donViTinh,hamLuongDangDung,lieuMoiLan,duongDung,soLanNgay,soNgay,buoiDung,chanDoanLienQuan,ngayKe) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            pstm.setString(1, ct.getMaPhieuKham());
            pstm.setString(2, ct.getMaThuoc());
            pstm.setInt(3, ct.getSoLuong());
            pstm.setString(4, ct.getGhiChuCapThuoc());
            pstm.setString(5, ct.getTenThuoc());
            pstm.setString(6, ct.getDonViTinh());
            pstm.setString(7, ct.getHamLuongDangDung());
            pstm.setString(8, ct.getLieuMoiLan());
            pstm.setString(9, ct.getDuongDung());
            pstm.setObject(10, ct.getSoLanNgay(), java.sql.Types.INTEGER);
            pstm.setObject(11, ct.getSoNgay(), java.sql.Types.INTEGER);
            pstm.setString(12, ct.getBuoiDung());
            pstm.setString(13, ct.getChanDoanLienQuan());
            pstm.setObject(14, ct.getNgayKe());
            pstm.executeUpdate();
        }
    }

    public static ArrayList<ChiTietCapThuoc> layTheoPhieu(String maPhieuKham) {
        String sql = "SELECT ct.maPhieuKham, ct.maThuoc, ct.soLuong, ct.ghiChuCapThuoc, COALESCE(ct.tenThuoc,t.tenThuoc), COALESCE(ct.donViTinh,t.donViTinh),ct.hamLuongDangDung,ct.lieuMoiLan,ct.duongDung,ct.soLanNgay,ct.soNgay,ct.buoiDung,ct.chanDoanLienQuan,ct.ngayKe"
                + " FROM ChiTietCapThuoc ct JOIN Thuoc t ON t.maThuoc = ct.maThuoc WHERE ct.maPhieuKham = ?";
        ArrayList<ChiTietCapThuoc> ds = new ArrayList<>();
        Connection con = connectDB.ConnectDB.getInstance().getConnection();
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maPhieuKham);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    ChiTietCapThuoc ct = new ChiTietCapThuoc(rs.getString(1), rs.getString(2), rs.getInt(3),
                            rs.getString(4));
                    entity.Thuoc th = new entity.Thuoc(rs.getString(2), rs.getString(5), rs.getString(6));
                    ct.setThuoc(th);
                    ct.setHamLuongDangDung(rs.getString(7));
                    ct.setLieuMoiLan(rs.getString(8));
                    ct.setDuongDung(rs.getString(9));
                    ct.setSoLanNgay((Integer) rs.getObject(10));
                    ct.setSoNgay((Integer) rs.getObject(11));
                    ct.setBuoiDung(rs.getString(12));
                    ct.setChanDoanLienQuan(rs.getString(13));
                    if (rs.getTimestamp(14) != null)
                        ct.setNgayKe(rs.getTimestamp(14).toLocalDateTime());
                    ds.add(ct);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi truy vấn ChiTietCapThuoc", ex);
        }
        return ds;
    }

    private ChiTietCapThuoc_DAO() {
    }
}
