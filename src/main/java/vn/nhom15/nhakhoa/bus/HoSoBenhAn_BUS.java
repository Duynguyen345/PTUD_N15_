package vn.nhom15.nhakhoa.bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import vn.nhom15.nhakhoa.connect.ConnectDB;
import vn.nhom15.nhakhoa.dao.BoDem_DAO;
import vn.nhom15.nhakhoa.dao.HoSoBenhAn_DAO;
import vn.nhom15.nhakhoa.model.HoSoBenhAn;
import vn.nhom15.nhakhoa.model.SessionContext;

public class HoSoBenhAn_BUS {

    public static HoSoBenhAn layTheoBenhNhan(SessionContext session, String maBenhNhan) {
        return HoSoBenhAn_DAO.layTheoBenhNhan(maBenhNhan);
    }

    /** 1 BN - 1 ho so: da co thi tra ve ho so cu, chua co thi tao moi (ma HSyyMMddNNNN) trong cung transaction. */
    public static String taoHoSoNeuChuaCo(Connection con, String maBenhNhan, String tienSuBenhLy) throws SQLException {
        HoSoBenhAn hs = HoSoBenhAn_DAO.layTheoBenhNhan(maBenhNhan);
        if (hs != null) {
            return hs.getMaHoSo();
        }
        String maHS = BoDem_DAO.layMaMoi(con, "HS", LocalDate.now());
        HoSoBenhAn moi = new HoSoBenhAn(maHS, tienSuBenhLy == null || tienSuBenhLy.isBlank()
                ? "Tạo tự động khi khám lần đầu" : tienSuBenhLy, null, maBenhNhan);
        HoSoBenhAn_DAO.themHoSo(con, moi);
        return maHS;
    }

    private HoSoBenhAn_BUS() { }
}
