package entity;

import java.time.LocalDate;

public record BenhNhan(String maBenhNhan, String hoTen, LocalDate ngaySinh, String gioiTinh,
        String soDienThoai, String diaChi, String tienSu, String diUng, String cccd, String tienSuDiUng) {
    public BenhNhan(String ma, String ten, LocalDate sinh, String gioi, String sdt,
            String diaChi, String tienSu, String diUng) {
        this(ma, ten, sinh, gioi, sdt, diaChi, tienSu, diUng, null,
                String.join("; ", tienSu == null ? "" : tienSu, diUng == null ? "" : diUng));
    }
    public BenhNhan(String ma, String ten, String sdt, LocalDate sinh, String cccd,
            String diaChi, String tienSuDiUng) {
        this(ma, ten, sinh, null, sdt, diaChi, null, null, cccd, tienSuDiUng);
    }
    @Override public String toString() { return maBenhNhan + " · " + hoTen + " · " + soDienThoai; }
}