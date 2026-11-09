package dao;

import java.util.List;
import connect.NguonKetNoi;
import entity.*;

/** Truy cập SQL cho luồng DichVu. */
public class DichVuDAO extends JdbcDAO {
    public DichVuDAO(NguonKetNoi nguonKetNoi) {
        super(nguonKetNoi);
    }

    public List<DichVu> danhSachDichVu() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maDichVu,tenDichVu,donGiaNiemYet,trangThai FROM DichVu ORDER BY maDichVu",
                r -> new DichVu(r.getString(1), r.getString(2), r.getBigDecimal(3),
                        TrangThaiDichVu.valueOf(r.getString(4)))));
    }

    public void luuDichVu(DichVu dichVu, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi)
                thucHienCapNhat(c,
                        "INSERT INTO DichVu(maDichVu,tenDichVu,donGiaNiemYet,trangThai) VALUES (?,?,?,?)",
                        taoMaTiepTheo(c, "DV", "DichVu", "maDichVu"), dichVu.getTenDichVu(),
                        dichVu.getDonGiaNiemYet(), dichVu.getTrangThai().name());
            else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE DichVu SET tenDichVu=?,donGiaNiemYet=?,trangThai=? WHERE maDichVu=?",
                        dichVu.getTenDichVu(), dichVu.getDonGiaNiemYet(), dichVu.getTrangThai().name(),
                        dichVu.getMaDichVu()));
            return null;
        });
    }
}
