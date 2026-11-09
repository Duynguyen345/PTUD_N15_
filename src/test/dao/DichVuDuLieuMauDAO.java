package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;

/** DAO dữ liệu mẫu cho luồng DichVu. */
public final class DichVuDuLieuMauDAO extends DichVuDAO {
    private final DuLieuMauDungChung duLieu;

    DichVuDuLieuMauDAO(DuLieuMauDungChung duLieu) {
        super(() -> { throw new java.sql.SQLException("DEMO không sử dụng JDBC."); });
        this.duLieu = duLieu;
    }

    @Override
    public List<DichVu> danhSachDichVu() {
        synchronized (duLieu) {
            return duLieu.danhSachDichVu.values().stream().map(duLieu::saoChep).toList();
        }
    }

    @Override
    public void luuDichVu(DichVu dichVu, boolean themMoi) {
        synchronized (duLieu) {
            if (duLieu.danhSachDichVu.values().stream()
                    .anyMatch(d -> d.getTenDichVu().equalsIgnoreCase(dichVu.getTenDichVu())
                            && (themMoi || !d.getMaDichVu().equals(dichVu.getMaDichVu()))))
                throw new IllegalArgumentException("Tên dịch vụ đã tồn tại.");
            DichVu giaTri = duLieu.saoChep(dichVu);
            if (themMoi)
                giaTri.setMaDichVu("DV" + String.format("%04d", ++duLieu.boDemDichVu));
            else if (!duLieu.danhSachDichVu.containsKey(giaTri.getMaDichVu()))
                throw new IllegalArgumentException("Dịch vụ không tồn tại.");
            duLieu.danhSachDichVu.put(giaTri.getMaDichVu(), giaTri);
        }
    }
}
