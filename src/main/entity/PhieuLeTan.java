package entity;

import java.time.LocalDate;

public record PhieuLeTan(String maPhieuKham, LocalDate ngayKham, int soThuTu,
        String maPhong, String maBenhNhan, String tenBenhNhan, String maBacSi,
        String tenBacSi, String lyDoKham, String trangThai, String maHoaDonPhiKham) {
}
