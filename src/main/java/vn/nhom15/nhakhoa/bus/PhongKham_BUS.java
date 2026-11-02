package vn.nhom15.nhakhoa.bus;

import java.util.ArrayList;
import vn.nhom15.nhakhoa.dao.PhongKham_DAO;
import vn.nhom15.nhakhoa.model.ChucVu;
import vn.nhom15.nhakhoa.model.PhongKham;
import vn.nhom15.nhakhoa.model.SessionContext;
import vn.nhom15.nhakhoa.util.QuyenUtil;

public class PhongKham_BUS {

    public static ArrayList<PhongKham> danhSachPhongDangNhanBenh(SessionContext session) {
        return PhongKham_DAO.layPhongDangNhanBenh();
    }

    public static ArrayList<PhongKham> danhSachTatCaPhong(SessionContext session) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        return PhongKham_DAO.getAllPhongKham();
    }

    public static void capNhatNhanBenh(SessionContext session, String maPhong, boolean dangNhanBenh) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.PHU_TA, ChucVu.QUAN_LY);
        PhongKham_DAO.capNhatNhanBenh(maPhong, dangNhanBenh);
    }

    private PhongKham_BUS() { }
}
