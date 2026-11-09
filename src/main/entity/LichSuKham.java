package entity;

import java.time.LocalDate;

/** Kết quả tra cứu một phiếu khám, không cung cấp thao tác sửa ở màn hình tra cứu. */
public record LichSuKham(String maPhieuKham, LocalDate ngayKham, String tenBacSi,
        String chanDoan, String dieuTri, String ghiChu, String trangThai) {
}
