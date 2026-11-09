package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;

/** DAO dữ liệu mẫu cho luồng HangHoa. */
public final class HangHoaDuLieuMauDAO extends HangHoaDAO {
    private final DuLieuMauDungChung duLieu;

    HangHoaDuLieuMauDAO(DuLieuMauDungChung duLieu) {
        super(() -> { throw new java.sql.SQLException("DEMO không sử dụng JDBC."); });
        this.duLieu = duLieu;
    }

    @Override
    public List<HangHoa> danhSachHangHoa() {
        synchronized (duLieu) {
            return List.copyOf(duLieu.danhSachHangHoa.values());
        }
    }

    @Override
    public void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        synchronized (duLieu) {
            if (duLieu.danhSachHangHoa.values().stream().anyMatch(
                    h -> h.ten().equalsIgnoreCase(hangHoa.ten()) && (themMoi || !h.ma().equals(hangHoa.ma()))))
                throw new IllegalArgumentException("Tên hàng đã tồn tại.");
            String ma = themMoi ? duLieu.taoMaTheoNgay("HH", LocalDate.now(), duLieu.danhSachHangHoa::containsKey)
                    : hangHoa.ma();
            if (!themMoi && !duLieu.danhSachHangHoa.containsKey(ma))
                throw new IllegalArgumentException("Hàng hóa không tồn tại.");
            duLieu.danhSachHangHoa.put(ma,
                    new HangHoa(ma, hangHoa.ten(), hangHoa.donVi(), hangHoa.ton(), hangHoa.toiThieu()));
        }
    }
}
