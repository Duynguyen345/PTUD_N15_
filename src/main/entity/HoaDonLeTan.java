package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoaDonLeTan(String maHoaDon, String maBenhNhan, String tenBenhNhan,
        String maPhieuKham, LocalDateTime ngayThu, BigDecimal tongTien,
        BigDecimal tienKhachDua, HinhThucThanhToan hinhThuc, String maNhanVien) {
    public BigDecimal tienThua() { return tienKhachDua.subtract(tongTien); }
    public boolean laPhiKham() { return maPhieuKham == null; }
}
