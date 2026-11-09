package dao;

import java.util.List;
import connect.NguonKetNoi;
import entity.*;

/** Truy cập SQL cho luồng HangHoa. */
public class HangHoaDAO extends JdbcDAO {
    public HangHoaDAO(NguonKetNoi nguonKetNoi) {
        super(nguonKetNoi);
    }

    public List<HangHoa> danhSachHangHoa() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maHangHoa,tenHangHoa,donViTinh,soLuongTon,mucTonToiThieu FROM HangHoa ORDER BY maHangHoa",
                r -> new HangHoa(r.getString(1), r.getString(2), r.getString(3), r.getInt(4), r.getInt(5))));
    }

    public void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi)
                thucHienCapNhat(c,
                        "INSERT INTO HangHoa(maHangHoa,tenHangHoa,donViTinh,soLuongTon,mucTonToiThieu) VALUES (?,?,?,?,?)",
                        taoMaTiepTheo(c, "HH", "HangHoa", "maHangHoa"), hangHoa.ten(), hangHoa.donVi(),
                        hangHoa.ton(), hangHoa.toiThieu());
            else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE HangHoa SET tenHangHoa=?,donViTinh=?,soLuongTon=?,mucTonToiThieu=? WHERE maHangHoa=?",
                        hangHoa.ten(), hangHoa.donVi(), hangHoa.ton(), hangHoa.toiThieu(), hangHoa.ma()));
            return null;
        });
    }
}
