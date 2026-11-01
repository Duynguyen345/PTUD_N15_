package vn.nhom15.nhakhoa.bus;

import java.time.LocalDate;
import java.util.List;
import vn.nhom15.nhakhoa.dao.PhieuKham_DAO;
import vn.nhom15.nhakhoa.model.ChucVu;
import vn.nhom15.nhakhoa.model.PhieuKham;
import vn.nhom15.nhakhoa.model.SessionContext;
import vn.nhom15.nhakhoa.model.TrangThaiPhieuKham;
import vn.nhom15.nhakhoa.util.MaThongBao;
import vn.nhom15.nhakhoa.util.QuyenUtil;
import vn.nhom15.nhakhoa.util.ValidateUtil;

public class HangCho_BUS {

    /** UC009 - danh sach CHO_KHAM theo phong/ngay (rong -> GUI bao TB07). */
    public static List<PhieuKham> danhSachCho(SessionContext session, LocalDate ngay, String maPhong) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        if (maPhong == null || maPhong.isBlank()) {
            throw new IllegalArgumentException(MaThongBao.VL07);
        }
        return PhieuKham_DAO.layDanhSachTheoNgayVaPhong(ngay, maPhong, TrangThaiPhieuKham.CHO_KHAM);
    }

    public static List<PhieuKham> danhSachDangKham(SessionContext session, LocalDate ngay, String maPhong) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        return PhieuKham_DAO.layDanhSachTheoNgayVaPhong(ngay, maPhong, TrangThaiPhieuKham.DANG_KHAM);
    }

    /** Goi so: KHONG doi trang thai phieu (goi != vao kham). Chi phi CHO_KHAM duoc goi. */
    public static PhieuKham goiSo(SessionContext session, String maPhieuKham) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        PhieuKham pk = PhieuKham_DAO.layPhieuKhamTheoMa(maPhieuKham);
        if (pk == null || pk.getTrangThai() != TrangThaiPhieuKham.CHO_KHAM) {
            throw new IllegalArgumentException(MaThongBao.TB10);
        }
        return pk;
    }

    /** Xac nhan vao kham: CHO_KHAM -> DANG_KHAM bang UPDATE co dieu kien, 0 dong = TB10. */
    public static void tiepNhanVaoKham(SessionContext session, String maPhieuKham) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        ValidateUtil.khongRong(maPhieuKham, MaThongBao.VL05);
        int n = PhieuKham_DAO.capNhatTrangThai(maPhieuKham, TrangThaiPhieuKham.DANG_KHAM,
                TrangThaiPhieuKham.CHO_KHAM);
        if (n == 0) {
            throw new IllegalArgumentException(MaThongBao.TB10);
        }
    }

    private HangCho_BUS() { }
}
