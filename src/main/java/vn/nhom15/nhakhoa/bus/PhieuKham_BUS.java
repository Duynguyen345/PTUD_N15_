package vn.nhom15.nhakhoa.bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import vn.nhom15.nhakhoa.connect.ConnectDB;
import vn.nhom15.nhakhoa.dao.BoDem_DAO;
import vn.nhom15.nhakhoa.dao.HoaDon_DAO;
import vn.nhom15.nhakhoa.dao.NhanVien_DAO;
import vn.nhom15.nhakhoa.dao.PhieuKham_DAO;
import vn.nhom15.nhakhoa.dao.PhongKham_DAO;
import vn.nhom15.nhakhoa.model.BenhNhan;
import vn.nhom15.nhakhoa.model.ChucVu;
import vn.nhom15.nhakhoa.model.HoaDon;
import vn.nhom15.nhakhoa.model.NhanVien;
import vn.nhom15.nhakhoa.model.PhieuKham;
import vn.nhom15.nhakhoa.model.PhongKham;
import vn.nhom15.nhakhoa.model.SessionContext;
import vn.nhom15.nhakhoa.model.TrangThaiTaiKhoan;
import vn.nhom15.nhakhoa.model.TrangThaiPhieuKham;
import vn.nhom15.nhakhoa.util.MaThongBao;
import vn.nhom15.nhakhoa.util.QuyenUtil;
import vn.nhom15.nhakhoa.util.ValidateUtil;

public class PhieuKham_BUS {

    /** UC003 - lap phieu kham: ma PK + STT (BoDem) + INSERT trong 1 transaction, trang thai CHO_KHAM. */
    public static PhieuKham lapPhieuKham(SessionContext session, BenhNhan benhNhan, String maBacSi,
                                         String maPhong, String lyDoKham, HoaDon hoaDonPhi) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.LE_TAN);
        if (benhNhan == null || benhNhan.getMaBenhNhan() == null) {
            throw new IllegalArgumentException(MaThongBao.VL05);
        }
        String lyDo = ValidateUtil.khongRong(lyDoKham, MaThongBao.VL08);
        if (maBacSi == null || maBacSi.isBlank() || maPhong == null || maPhong.isBlank()) {
            throw new IllegalArgumentException(MaThongBao.VL07);
        }

        NhanVien bacSi = NhanVien_DAO.layNhanVienTheoMa(maBacSi);
        PhongKham phong = PhongKham_DAO.layPhongTheoMa(maPhong);
        boolean bacSiTrongCa = bacSi != null && bacSi.isConLamViec() && bacSi.getChucVu() == ChucVu.BAC_SI
                && vn.nhom15.nhakhoa.dao.TaiKhoan_DAO.layTaiKhoanTheoMaNhanVien(maBacSi) != null
                && vn.nhom15.nhakhoa.dao.TaiKhoan_DAO.layTaiKhoanTheoMaNhanVien(maBacSi)
                        .getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG;
        if (!bacSiTrongCa || phong == null || !phong.isDangNhanBenh()) {
            throw new IllegalArgumentException(MaThongBao.TB03);
        }

        // Hoa don phi phai da thanh toan va CHUA gan phieu nao
        HoaDon chuaDung = HoaDon_DAO.layHoaDonPhiChuaDungTheoBN(benhNhan.getMaBenhNhan());
        if (hoaDonPhi == null || chuaDung == null
                || !chuaDung.getMaHoaDon().equals(hoaDonPhi.getMaHoaDon())) {
            throw new IllegalArgumentException(MaThongBao.TB04);
        }

        LocalDate ngayKham = LocalDate.now();
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            String maPK = BoDem_DAO.layMaMoi(con, "PK", ngayKham);
            int stt = BoDem_DAO.laySoThuTuTiepTheo(con, ngayKham, maPhong);
            PhieuKham pk = new PhieuKham();
            pk.setMaPhieuKham(maPK);
            pk.setNgayKham(ngayKham);
            pk.setSoThuTu(stt);
            pk.setLyDoKham(lyDo);
            pk.setTrangThai(TrangThaiPhieuKham.CHO_KHAM);
            pk.setMaBenhNhan(benhNhan.getMaBenhNhan());
            pk.setMaNhanVien(maBacSi);
            pk.setMaPhong(maPhong);
            pk.setMaHoaDonPhiKham(hoaDonPhi.getMaHoaDon());
            PhieuKham_DAO.themPhieuKham(con, pk);
            con.commit();
            con.setAutoCommit(true);
            return PhieuKham_DAO.layPhieuKhamTheoMa(maPK);
        } catch (SQLException ex) {
            rollback(con);
            throw new RuntimeException("Lỗi lập phiếu khám: " + ex.getMessage(), ex);
        }
    }

    private static void rollback(Connection con) {
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException ignored) { }
    }

    private PhieuKham_BUS() { }
}
