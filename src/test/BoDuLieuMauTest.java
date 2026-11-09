import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dao.*;
import entity.*;
import util.BoBamMatKhau;

class BoDuLieuMauTest {
    @Test
    void kiemTraTinhDongBoBoDuLieuMau() {
        LocalDate homNay = LocalDate.now();
        BoDuLieuMau duLieu = new BoDuLieuMau(homNay);
        assertEquals(10, duLieu.danhSachNhanVien().size());
        assertEquals(6, duLieu.danhSachTaiKhoan().size());
        assertEquals(8, duLieu.danhSachDichVu().size());
        assertEquals(8, duLieu.danhSachHangHoa().size());
        assertEquals(14, duLieu.danhSachLuotKham().size());
        assertEquals(10,
                duLieu.danhSachNhanVien().stream().map(NhanVien::getSoDienThoai).distinct().count());
        assertEquals(10,
                duLieu.danhSachNhanVien().stream().map(NhanVien::getCccd).distinct().count());
        assertEquals(2, duLieu.danhSachNhanVien().stream().filter(n -> n.getHoTen().contains("Bình"))
                .count());
        assertEquals(3, duLieu
                .danhSachNhanVien().stream().filter(NhanVien::isConLamViec).filter(n -> duLieu
                        .danhSachTaiKhoan().stream().noneMatch(a -> a.maNhanVien().equals(n.getMaNhanVien())))
                .count());
        assertEquals(1, duLieu.danhSachTaiKhoan().stream()
                .filter(a -> a.trangThai() == TrangThaiTaiKhoan.BI_KHOA).count());
        assertEquals(4, duLieu.danhSachHangHoa().stream().filter(HangHoa::canBoSung).count());
        assertEquals(2, duLieu.danhSachDichVu().stream()
                .filter(d -> d.getTrangThai() == TrangThaiDichVu.NGUNG_PHUC_VU).count());
        for (var luotKhamMau : duLieu.danhSachLuotKham()) {
            assertTrue(duLieu.danhSachNhanVien().stream()
                    .anyMatch(n -> n.getMaNhanVien().equals(luotKhamMau.maNhanVien())
                            && n.getChucVu() == ChucVu.BAC_SI));
            assertEquals(luotKhamMau.danhSachMaDichVu().size(),
                    new HashSet<>(luotKhamMau.danhSachMaDichVu()).size());
            assertTrue(duLieu.danhSachDichVu().stream().map(DichVu::getMaDichVu).toList()
                    .containsAll(luotKhamMau.danhSachMaDichVu()));
        }
        var boDAO = BoDAO.duLieuMau(new BoBamMatKhau());
        var lapBaoCao = boDAO.baoCao().lapBaoCao(homNay.minusDays(6), homNay);
        assertEquals(14,
                lapBaoCao.danhSachLuotKham().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        assertEquals(28,
                lapBaoCao.danhSachDichVu().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        DichVu nghiepVu = boDAO.dichVu().danhSachDichVu().get(1);
        nghiepVu.setTenDichVu("Lấy cao răng đã đổi tên");
        boDAO.dichVu().luuDichVu(nghiepVu, false);
        assertTrue(boDAO.baoCao().lapBaoCao(homNay.minusDays(6), homNay).danhSachDichVu().stream()
                .anyMatch(dong -> dong.nhan().equals(nghiepVu.getTenDichVu()) && dong.soLuong() == 5));
    }
}
