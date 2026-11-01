package vn.nhom15.nhakhoa.bus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import vn.nhom15.nhakhoa.connect.ConnectDB;
import vn.nhom15.nhakhoa.dao.BoDem_DAO;
import vn.nhom15.nhakhoa.dao.ChiTietHoaDon_DAO;
import vn.nhom15.nhakhoa.dao.DichVu_DAO;
import vn.nhom15.nhakhoa.dao.HoaDon_DAO;
import vn.nhom15.nhakhoa.model.ChiTietHoaDon;
import vn.nhom15.nhakhoa.model.ChucVu;
import vn.nhom15.nhakhoa.model.DichVu;
import vn.nhom15.nhakhoa.model.HoaDon;
import vn.nhom15.nhakhoa.model.SessionContext;
import vn.nhom15.nhakhoa.util.MaThongBao;
import vn.nhom15.nhakhoa.util.QuyenUtil;
import vn.nhom15.nhakhoa.util.ValidateUtil;

public class HoaDon_BUS {

    public static final String MA_DICH_VU_PHI_KHAM = "DV0001";

    /** UC005 - thu phi kham ban dau. Tra ve hoa don da thanh toan (chua gan phieu). */
    public static HoaDon thuPhiKham(SessionContext session, String maBenhNhan, String hinhThucThanhToan,
                                    String tienKhachDua) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.LE_TAN);
        String maBN = ValidateUtil.khongRong(maBenhNhan, MaThongBao.VL05);
        BigDecimal tienDua = ValidateUtil.donGia(tienKhachDua);

        DichVu phiKham = DichVu_DAO.layDichVuTheoMa(MA_DICH_VU_PHI_KHAM);
        if (phiKham == null) {
            throw new IllegalStateException("Thiếu dịch vụ seed 'Phí khám ban đầu' (DV0001). Chạy 003_seed_dichvu.sql.");
        }
        if (HoaDon_DAO.layHoaDonPhiChuaDungTheoBN(maBN) != null) {
            throw new IllegalArgumentException(MaThongBao.TB04);
        }
        if (tienDua.compareTo(phiKham.getDonGiaNiemYet()) < 0) {
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ mức phí khám.");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            String maHD = BoDem_DAO.layMaMoi(con, "HD", LocalDateTime.now().toLocalDate());
            HoaDon hd = new HoaDon(maHD);
            hd.setNgayThu(LocalDateTime.now());
            hd.setHinhThucThanhToan(hinhThucThanhToan);
            hd.setDaThanhToan(true);
            hd.setTienKhachDua(tienDua);
            hd.setMaBenhNhan(maBN);
            hd.setMaNhanVien(session.getMaNhanVien());
            HoaDon_DAO.themHoaDon(con, hd);
            ChiTietHoaDon_DAO.themChiTiet(con,
                    new ChiTietHoaDon(maHD, MA_DICH_VU_PHI_KHAM, 1, phiKham.getDonGiaNiemYet()));
            con.commit();
            con.setAutoCommit(true);
            hd.setTongTien(phiKham.getDonGiaNiemYet());
            return hd;
        } catch (SQLException ex) {
            rollback(con);
            throw new RuntimeException("Lỗi thu phí khám: " + ex.getMessage(), ex);
        }
    }

    /** Hoa don phi kham da thu CHUA gan phieu - dung cho nut "Dung hoa don da thu". */
    public static HoaDon layHoaDonPhiChuaDung(SessionContext session, String maBenhNhan) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.LE_TAN);
        return HoaDon_DAO.layHoaDonPhiChuaDungTheoBN(maBenhNhan);
    }

    private static void rollback(Connection con) {
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException ignored) { }
    }

    private HoaDon_BUS() { }
}
