package vn.nhom15.nhakhoa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

class BoDuLieuMauTest {
    @Test
    void kiemTraTinhDongBoBoDuLieuMau() {
        LocalDate homNay = LocalDate.now();
        BoDuLieuMau DuLieuTaiKhoan = new BoDuLieuMau(homNay);
        assertEquals(10, DuLieuTaiKhoan.danhSachNhanVien().size());
        assertEquals(6, DuLieuTaiKhoan.danhSachTaiKhoan().size());
        assertEquals(8, DuLieuTaiKhoan.danhSachDichVu().size());
        assertEquals(8, DuLieuTaiKhoan.danhSachHangHoa().size());
        assertEquals(14, DuLieuTaiKhoan.danhSachLuotKham().size());
        assertEquals(10,
                DuLieuTaiKhoan.danhSachNhanVien().stream().map(NhanVien::getSoDienThoai).distinct().count());
        assertEquals(10,
                DuLieuTaiKhoan.danhSachNhanVien().stream().map(NhanVien::getCccd).distinct().count());
        assertEquals(2, DuLieuTaiKhoan.danhSachNhanVien().stream().filter(n -> n.getHoTen().contains("Bình"))
                .count());
        assertEquals(3, DuLieuTaiKhoan
                .danhSachNhanVien().stream().filter(NhanVien::isConLamViec).filter(n -> DuLieuTaiKhoan
                        .danhSachTaiKhoan().stream().noneMatch(a -> a.maNhanVien().equals(n.getMaNhanVien())))
                .count());
        assertEquals(1, DuLieuTaiKhoan.danhSachTaiKhoan().stream()
                .filter(a -> a.trangThai() == TrangThaiTaiKhoan.BI_KHOA).count());
        assertEquals(4, DuLieuTaiKhoan.danhSachHangHoa().stream().filter(HangHoa::canBoSung).count());
        assertEquals(2, DuLieuTaiKhoan.danhSachDichVu().stream()
                .filter(d -> d.getTrangThai() == TrangThaiDichVu.NGUNG_PHUC_VU).count());
        for (var LuotKhamMau : DuLieuTaiKhoan.danhSachLuotKham()) {
            assertTrue(DuLieuTaiKhoan.danhSachNhanVien().stream()
                    .anyMatch(n -> n.getMaNhanVien().equals(LuotKhamMau.maNhanVien())
                            && n.getChucVu() == ChucVu.BAC_SI));
            assertEquals(LuotKhamMau.danhSachMaDichVu().size(),
                    new HashSet<>(LuotKhamMau.danhSachMaDichVu()).size());
            assertTrue(DuLieuTaiKhoan.danhSachDichVu().stream().map(DichVu::getMaDichVu).toList()
                    .containsAll(LuotKhamMau.danhSachMaDichVu()));
        }
        var phongKhamDAO = new PhongKhamDuLieuMauDAO(new BoBamMatKhau());
        var lapBaoCao = phongKhamDAO.lapBaoCao(homNay.minusDays(6), homNay);
        assertEquals(14,
                lapBaoCao.danhSachLuotKham().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        assertEquals(28,
                lapBaoCao.danhSachDichVu().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        DichVu nghiepVu = phongKhamDAO.danhSachDichVu().get(1);
        nghiepVu.setTenDichVu("Lấy cao răng đã đổi tên");
        phongKhamDAO.luuDichVu(nghiepVu, false);
        assertTrue(phongKhamDAO.lapBaoCao(homNay.minusDays(6), homNay).danhSachDichVu().stream()
                .anyMatch(dong -> dong.nhan().equals(nghiepVu.getTenDichVu()) && dong.soLuong() == 5));
    }
}
