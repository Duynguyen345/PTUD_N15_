package vn.nhom15.nhakhoa.entity;

import java.time.LocalDate;

/** Hồ sơ hành chính bất biến; bệnh nhân có thể có nhiều lần khám. */
public record BenhNhan(String maBenhNhan, String hoTen, LocalDate ngaySinh, String gioiTinh,
                String soDienThoai, String diaChi, String tienSu, String diUng) {
}
