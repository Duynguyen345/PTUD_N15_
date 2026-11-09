package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CaThuNgan(String maCa, String maNhanVien, String tenNhanVien,
        LocalDateTime moLuc, LocalDateTime chotLuc,
        BigDecimal doanhThuDichVu, BigDecimal phiKhamThuHo) {
    public boolean dangMo() { return chotLuc == null; }
}
