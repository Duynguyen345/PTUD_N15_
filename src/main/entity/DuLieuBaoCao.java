package entity;

import java.time.LocalDate;
import java.util.List;

public record DuLieuBaoCao(LocalDate tuNgay, LocalDate denNgay, List<DongThongKe> danhSachLuotKham,
        List<DongThongKe> danhSachDichVu) {
    public DuLieuBaoCao {
        danhSachLuotKham = List.copyOf(danhSachLuotKham);
        danhSachDichVu = List.copyOf(danhSachDichVu);
    }

    public record DongThongKe(String nhan, long soLuong) {
    }
}
