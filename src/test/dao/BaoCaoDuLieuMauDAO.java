package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;
import entity.DuLieuBaoCao.DongThongKe;

/** DAO dữ liệu mẫu cho luồng BaoCao. */
public final class BaoCaoDuLieuMauDAO extends BaoCaoDAO {
    private final DuLieuMauDungChung duLieu;

    BaoCaoDuLieuMauDAO(DuLieuMauDungChung duLieu) {
        super(() -> { throw new java.sql.SQLException("DEMO không sử dụng JDBC."); });
        this.duLieu = duLieu;
    }

    @Override
    public DuLieuBaoCao lapBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        synchronized (duLieu) {
            Map<LocalDate, Long> soLuotKhamTheoNgay = new TreeMap<>();
            Map<String, Long> soLanChiDinh = new HashMap<>();
            for (BoDuLieuMau.LuotKhamMau luotKham : duLieu.danhSachLuotKham) {
                if (luotKham.ngay().isBefore(tuNgay) || luotKham.ngay().isAfter(denNgay))
                    continue;
                soLuotKhamTheoNgay.merge(luotKham.ngay(), 1L, Long::sum);
                for (String ma : luotKham.danhSachMaDichVu())
                    soLanChiDinh.merge(ma, 1L, Long::sum);
            }
            List<DongThongKe> thongKeTheoNgay = soLuotKhamTheoNgay.entrySet().stream()
                    .map(muc -> new DongThongKe(muc.getKey().toString(), muc.getValue())).toList();
            List<DongThongKe> tanSuat = soLanChiDinh.entrySet().stream()
                    .map(muc -> new DongThongKe(duLieu.danhSachDichVu.get(muc.getKey()).getTenDichVu(), muc.getValue()))
                    .sorted(Comparator.comparingLong(DongThongKe::soLuong).reversed()
                            .thenComparing(DongThongKe::nhan))
                    .toList();
            return new DuLieuBaoCao(tuNgay, denNgay, thongKeTheoNgay, tanSuat);
        }
    }
}
