package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import connect.NguonKetNoi;
import entity.*;
import entity.DuLieuBaoCao.DongThongKe;

/** Truy cập SQL cho luồng BaoCao. */
public class BaoCaoDAO extends JdbcDAO {
    public BaoCaoDAO(NguonKetNoi nguonKetNoi) {
        super(nguonKetNoi);
    }

    public DuLieuBaoCao lapBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        return thucHienGiaoDich(c -> {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            List<DongThongKe> danhSachLuotKham = truyVan(c,
                    "SELECT ngayKham,COUNT_BIG(*) FROM PhieuKham WHERE ngayKham>=? AND ngayKham<=? GROUP BY ngayKham ORDER BY ngayKham",
                    r -> new DongThongKe(r.getDate(1).toLocalDate().toString(), r.getLong(2)), tuNgay,
                    denNgay);
            List<DongThongKe> danhSachDichVu = truyVan(c,
                    "SELECT dv.tenDichVu,COUNT_BIG(*) FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu=cd.maDichVu JOIN PhieuKham pk ON pk.maPhieuKham=cd.maPhieuKham WHERE pk.ngayKham>=? AND pk.ngayKham<=? GROUP BY dv.maDichVu,dv.tenDichVu ORDER BY COUNT_BIG(*) DESC,dv.tenDichVu",
                    r -> new DongThongKe(r.getString(1), r.getLong(2)), tuNgay, denNgay);
            return new DuLieuBaoCao(tuNgay, denNgay, danhSachLuotKham, danhSachDichVu);
        });
    }
}
