package vn.nhom15.nhakhoa;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.connect.KetNoiCSDL;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Chỉ chạy với database kiểm thử N15_DUY_TEST_* được chuẩn bị bằng schema + seed. */
class TichHopJdbcTest {
    @Test
    void kiemTraNghiepVuSQLVaSinhMaDongThoi() throws Exception {
        String duongDanCauHinh = System.getProperty("duy.jdbc.config");
        assumeTrue(duongDanCauHinh != null, "Không có cấu hình database kiểm thử riêng.");
        KetNoiCSDL nguonKetNoi = new KetNoiCSDL(Path.of(duongDanCauHinh));
        try (var ketNoi = nguonKetNoi.moKetNoi()) {
            assertTrue(ketNoi.getCatalog().startsWith("N15_DUY_TEST_"), "Chỉ cho phép ghi database kiểm thử");
        }
        PhongKhamDAO phongKhamDAO = new PhongKhamDAO(nguonKetNoi);
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        DangNhapBUS dangNhapBUS = new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien);
        NhanSuBUS nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        DanhMucBUS danhMucBUS = new DanhMucBUS(phongKhamDAO, quanLyPhien);
        BaoCaoBUS baoCaoBUS = new BaoCaoBUS(phongKhamDAO, quanLyPhien);
        phongKhamDAO.kiemTraKetNoi();
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
        // Đối chiếu SQL seed với cùng bộ dữ liệu dùng trong DEMO, trước khi test sửa dữ liệu.
        var duLieuMau = new PhongKhamDuLieuMauDAO(boBamMatKhau);
        assertEquals(
                duLieuMau.danhSachNhanVien().stream()
                        .map(n -> java.util.List.of(n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(),
                                n.getNgaySinh(), n.getGioiTinh(), n.getCccd(), n.getChucVu(),
                                n.isConLamViec()))
                        .toList(),
                phongKhamDAO.danhSachNhanVien().stream()
                        .map(n -> java.util.List.of(n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(),
                                n.getNgaySinh(), n.getGioiTinh(), n.getCccd(), n.getChucVu(),
                                n.isConLamViec()))
                        .toList());
        assertEquals(duLieuMau.danhSachHangHoa(), phongKhamDAO.danhSachHangHoa());
        assertEquals(duLieuMau.danhSachTaiKhoan().stream()
                .map(a -> java.util.List.of(a.getMaTaiKhoan(), a.getMaNhanVien(), a.getTrangThai())).toList(),
                phongKhamDAO.danhSachTaiKhoan().stream()
                        .map(a -> java.util.List.of(a.getMaTaiKhoan(), a.getMaNhanVien(), a.getTrangThai()))
                        .toList());
        assertEquals(
                duLieuMau.danhSachDichVu().stream()
                        .map(d -> java.util.List.of(d.getMaDichVu(), d.getTenDichVu(),
                                d.getDonGiaNiemYet().stripTrailingZeros(), d.getTrangThai()))
                        .toList(),
                phongKhamDAO.danhSachDichVu().stream().map(d -> java.util.List.of(d.getMaDichVu(),
                        d.getTenDichVu(), d.getDonGiaNiemYet().stripTrailingZeros(), d.getTrangThai()))
                        .toList());
        var demoReport = duLieuMau.lapBaoCao(LocalDate.now().minusDays(6), LocalDate.now());
        var sqlReport = phongKhamDAO.lapBaoCao(LocalDate.now().minusDays(6), LocalDate.now());
        assertEquals(demoReport.danhSachLuotKham(), sqlReport.danhSachLuotKham());
        assertEquals(new java.util.HashSet<>(demoReport.danhSachDichVu()),
                new java.util.HashSet<>(sqlReport.danhSachDichVu()));
        String dauHieu = Long.toString(System.nanoTime());
        NhanVien nhanVienMoi = new NhanVien(null, "JDBC " + dauHieu, "0912345678", LocalDate.of(1990, 1, 1),
                true, "", ChucVu.BAC_SI, true);
        nhanSuBUS.luuNhanVien(nhanVienMoi, true);
        NhanVien daThem = nhanSuBUS.layDanhSachNhanVien().stream()
                .filter(n -> n.getHoTen().equals(nhanVienMoi.getHoTen())).findFirst().orElseThrow();
        assertTrue(daThem.getMaNhanVien().matches("NV\\d{10}"));
        daThem.setHoTen("Updated " + dauHieu);
        nhanSuBUS.luuNhanVien(daThem, false);
        assertTrue(nhanSuBUS.layDanhSachNhanVien().stream()
                .anyMatch(n -> n.getHoTen().equals("Updated " + dauHieu)));
        String maTaiKhoan = nhanSuBUS.taoTaiKhoan(daThem.getMaNhanVien(), "newpass1".toCharArray());
        assertTrue(maTaiKhoan.matches("TK\\d{10}"));
        assertThrows(NgoaiLeTruyCapDuLieu.class,
                () -> nhanSuBUS.taoTaiKhoan(daThem.getMaNhanVien(), "newpass1".toCharArray()));
        assertTrue(phongKhamDAO.timTaiKhoan(maTaiKhoan).getMatKhau().startsWith("pbkdf2$"));
        assertThrows(NgoaiLeTruyCapDuLieu.class, () -> nhanSuBUS.xoaNhanVien(daThem.getMaNhanVien()));
        nhanSuBUS.doiTrangThaiTaiKhoan(phongKhamDAO.timTaiKhoan(maTaiKhoan));
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap(maTaiKhoan, "newpass1".toCharArray()));
        nhanSuBUS.doiTrangThaiTaiKhoan(phongKhamDAO.timTaiKhoan(maTaiKhoan));
        assertEquals(ChucVu.BAC_SI, dangNhapBUS.dangNhap(maTaiKhoan, "newpass1".toCharArray()).getChucVu());
        assertThrows(SecurityException.class, danhMucBUS::danhSachDichVu);
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
        DichVu nghiepVu = new DichVu(null, "JDBC Service " + dauHieu, new BigDecimal("123456.78"),
                TrangThaiDichVu.DANG_PHUC_VU);
        danhMucBUS.luuDichVu(nghiepVu, true);
        assertThrows(NgoaiLeTruyCapDuLieu.class, () -> danhMucBUS.luuDichVu(nghiepVu, true));
        DichVu saved = danhMucBUS.danhSachDichVu().stream()
                .filter(d -> d.getTenDichVu().equals(nghiepVu.getTenDichVu())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("123456.78"), saved.getDonGiaNiemYet());
        saved.setTrangThai(TrangThaiDichVu.NGUNG_PHUC_VU);
        danhMucBUS.luuDichVu(saved, false);
        danhMucBUS.luuHangHoa(new HangHoa(null, "JDBC Stock " + dauHieu, "Hộp", 5, 5), true);
        HangHoa hangHoa = danhMucBUS.danhSachHangHoa().stream()
                .filter(h -> h.ten().equals("JDBC Stock " + dauHieu)).findFirst().orElseThrow();
        assertEquals(12, hangHoa.ma().length());
        assertTrue(hangHoa.canBoSung());
        danhMucBUS.luuHangHoa(new HangHoa(hangHoa.ma(), hangHoa.ten(), hangHoa.donVi(), 8, 5), false);
        try (ExecutorService nhomLuong = Executors.newFixedThreadPool(2)) {
            Future<?> dau = nhomLuong.submit(() -> danhMucBUS.luuDichVu(
                    new DichVu(null, "Concurrent A " + dauHieu, BigDecimal.TEN, TrangThaiDichVu.DANG_PHUC_VU),
                    true));
            Future<?> second = nhomLuong.submit(() -> danhMucBUS.luuDichVu(
                    new DichVu(null, "Concurrent B " + dauHieu, BigDecimal.TEN, TrangThaiDichVu.DANG_PHUC_VU),
                    true));
            dau.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
        }
        NhanVien nhanVienA = new NhanVien(null, "Account A " + dauHieu, "0912345678",
                LocalDate.of(1990, 1, 1), true, "", ChucVu.LE_TAN, true);
        NhanVien nhanVienB = new NhanVien(null, "Account B " + dauHieu, "0912345678",
                LocalDate.of(1990, 1, 1), true, "", ChucVu.LE_TAN, true);
        nhanSuBUS.luuNhanVien(nhanVienA, true);
        nhanSuBUS.luuNhanVien(nhanVienB, true);
        String maNhanVienA = nhanSuBUS.layDanhSachNhanVien().stream()
                .filter(n -> n.getHoTen().equals(nhanVienA.getHoTen())).findFirst().orElseThrow()
                .getMaNhanVien();
        String maNhanVienB = nhanSuBUS.layDanhSachNhanVien().stream()
                .filter(n -> n.getHoTen().equals(nhanVienB.getHoTen())).findFirst().orElseThrow()
                .getMaNhanVien();
        try (ExecutorService nhomLuong = Executors.newFixedThreadPool(2)) {
            Future<String> dau = nhomLuong
                    .submit(() -> nhanSuBUS.taoTaiKhoan(maNhanVienA, "newpass1".toCharArray()));
            Future<String> second = nhomLuong
                    .submit(() -> nhanSuBUS.taoTaiKhoan(maNhanVienB, "newpass1".toCharArray()));
            String idA = dau.get(20, TimeUnit.SECONDS), idB = second.get(20, TimeUnit.SECONDS);
            assertNotEquals(idA, idB);
            assertTrue(idA.matches("TK\\d{10}"));
            assertTrue(idB.matches("TK\\d{10}"));
        }
        var tatCa = danhMucBUS.danhSachDichVu();
        assertEquals(tatCa.size(), tatCa.stream().map(DichVu::getMaDichVu).distinct().count());
        DuLieuBaoCao lapBaoCao = baoCaoBUS.layBaoCao(LocalDate.now(), LocalDate.now());
        assertEquals(2,
                lapBaoCao.danhSachLuotKham().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        assertEquals(4,
                lapBaoCao.danhSachDichVu().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        quanLyPhien.dangXuat();
        assertThrows(SecurityException.class, nhanSuBUS::layDanhSachNhanVien);
    }
}
