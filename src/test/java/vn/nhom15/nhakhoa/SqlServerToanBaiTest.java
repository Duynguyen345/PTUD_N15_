package vn.nhom15.nhakhoa;

import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.connect.KetNoiCSDL;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Chay tren database rieng N15_NHAKHOA_TEST_* duoc nap bang nhakhoa_sqlserver.sql. */
class SqlServerToanBaiTest {
    @Test
    void duLieuMauPhanQuyenVaDieuPhoiDuocLuuTrucTiepTrongSQLServer() throws Exception {
        String cauHinh = System.getProperty("nhakhoa.jdbc.config");
        assumeTrue(cauHinh != null, "Chua chi dinh database SQL Server kiem thu rieng.");
        var nguon = new KetNoiCSDL(Path.of(cauHinh));
        Clock dongHo = Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
        try (Connection c = nguon.moKetNoi()) {
            assertTrue(c.getCatalog().startsWith("N15_NHAKHOA_TEST_"), "Chi ghi database kiem thu rieng");
            for (String bang : List.of("NhanVien", "TaiKhoan", "DichVu", "HangHoa", "DuyBoDem",
                    "BenhNhan", "PhongKham", "HoSoKham", "ChiDinhDichVu", "PhieuKham")) {
                try (var lenh = c.createStatement(); var r = lenh.executeQuery("SELECT COUNT(*) FROM dbo." + bang)) {
                    assertTrue(r.next());
                    assertEquals(bang.equals("PhieuKham") ? 12 : 6, r.getInt(1), bang);
                }
            }
        }
        var dao = new PhongKhamDAO(nguon);
        var phien = new QuanLyPhienDangNhap(dao);
        var dangNhap = new DangNhapBUS(dao, new BoBamMatKhau(), phien);
        var dieuPhoi = new DieuPhoiBUS(new DieuPhoiJdbcDAO(nguon), phien, dongHo);
        var traCuu = new TraCuuHoSoBUS(new DieuPhoiJdbcDAO(nguon), phien);

        dangNhap.dangNhap("TK2601010001", "123456".toCharArray());
        assertEquals(6, new NhanSuBUS(dao, phien, new BoBamMatKhau()).layDanhSachNhanVien().size());
        assertEquals(6, new DanhMucBUS(dao, phien).danhSachDichVu().size());
        assertEquals(6, new DanhMucBUS(dao, phien).danhSachHangHoa().size());
        var baoCao = new BaoCaoBUS(dao, phien).layBaoCao(LocalDate.now(dongHo).minusDays(6), LocalDate.now(dongHo));
        assertEquals(12, baoCao.danhSachLuotKham().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());
        assertEquals(6, baoCao.danhSachDichVu().stream().mapToLong(DuLieuBaoCao.DongThongKe::soLuong).sum());

        dangNhap.dangNhap("TK2601010002", "123456".toCharArray());
        assertEquals("BN2601010001", traCuu.timBenhNhan("nguyen minh chau").getFirst().maBenhNhan());
        assertEquals("BN2601010006", traCuu.timBenhNhan("dang gia huy").getFirst().maBenhNhan());
        assertEquals(1, traCuu.timBenhNhan("0911110001").size());
        assertTrue(traCuu.timBenhNhan("%_").isEmpty());
        assertEquals(2, traCuu.layLichSuKham("BN2601010001").size());
        assertTrue(traCuu.layLichSuKham("BN2601010001").stream()
                .anyMatch(l -> l.chanDoan().equals("Sâu răng 36")));
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));

        dangNhap.dangNhap("TK2601010003", "123456".toCharArray());
        dieuPhoi.datNhanBenh("P04", false);
        assertFalse(new DieuPhoiJdbcDAO(nguon).layDanhSachPhong().stream()
                .filter(p -> p.maPhong().equals("P04")).findFirst().orElseThrow().dangNhanBenh());
        dieuPhoi.datNhanBenh("P04", true);
        assertThrows(SecurityException.class, () -> dieuPhoi.datNhanBenh("P03", true));

        dangNhap.dangNhap("TK2601010004", "123456".toCharArray());
        assertThrows(SecurityException.class, () -> traCuu.timBenhNhan("Châu"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P02"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P03"));
        try {
            // Hai ket noi JDBC cung goi mot phong: chi mot yeu cau duoc thanh cong.
            try (var nhom = Executors.newFixedThreadPool(2)) {
                Callable<Boolean> goi = () -> {
                    try { dieuPhoi.goiTiepTheo("P01"); return true; }
                    catch (IllegalArgumentException loi) { return false; }
                };
                var ketQua = nhom.invokeAll(List.of(goi, goi));
                assertEquals(1, ketQua.stream().filter(f -> {
                    try { return f.get(); }
                    catch (Exception loi) { throw new AssertionError(loi); }
                }).count());
            }
            // Doc bang mot DAO/connection moi de chung minh trang thai da commit vao SQL.
            var luot = new DieuPhoiJdbcDAO(nguon).layHangCho("P01", LocalDate.now(dongHo)).getFirst();
            assertEquals(TrangThaiHangCho.DANG_GOI, luot.trangThai());
            assertEquals("PK2601010001", luot.maPhieuKham());
            // Chay lai bo SQL bang quyen ung dung khong duoc reset luot dang goi/tao trung mau.
            try (Connection c = nguon.moKetNoi()) {
                String sql = Files.readString(Path.of("DATA/nhakhoa_sqlserver.sql"))
                        .replace("N15_NHAKHOA_DB", c.getCatalog());
                for (String batch : sql.split("(?im)^GO\\s*$")) {
                    if (!batch.isBlank()) {
                        try (var lenh = c.createStatement()) { lenh.execute(batch); }
                    }
                }
                try (var lenh = c.createStatement(); var r = lenh.executeQuery("SELECT COUNT(*) FROM dbo.PhieuKham")) {
                    assertTrue(r.next());
                    assertEquals(12, r.getInt(1));
                }
            }
            assertEquals(TrangThaiHangCho.DANG_GOI, new DieuPhoiJdbcDAO(nguon)
                    .layHangCho("P01", LocalDate.now(dongHo)).getFirst().trangThai());
            assertEquals(luot.soThuTu(), dieuPhoi.goiLai(luot.maPhieuKham(), "P01").soThuTu());
            assertThrows(IllegalArgumentException.class, () -> dieuPhoi.datNhanBenh("P01", false));
            dieuPhoi.danhDauVang(luot.maPhieuKham(), "P01");
            var tiep = dieuPhoi.goiTiepTheo("P01");
            assertEquals("PK2601010002", tiep.maPhieuKham());
            dieuPhoi.xacNhanVaoKham(tiep.maPhieuKham(), "P01");
            assertEquals(TrangThaiHangCho.DANG_KHAM, new DieuPhoiJdbcDAO(nguon)
                    .layHangCho("P01", LocalDate.now(dongHo)).get(1).trangThai());
        } finally {
            // Tra lai hai luot test da thay doi; khong sua database ung dung.
            try (Connection c = nguon.moKetNoi(); var lenh = c.prepareStatement("""
                    UPDATE dbo.PhieuKham SET trangThai=N'CHO_KHAM',trangThaiHangCho='CHO_KHAM',thoiDiemGoi=NULL
                    WHERE maPhieuKham IN('PK2601010001','PK2601010002')
                    """)) { lenh.executeUpdate(); }
        }
        phien.dangXuat();
        assertThrows(SecurityException.class, dieuPhoi::layDanhSachPhong);
        assertThrows(IllegalArgumentException.class,
                () -> dangNhap.dangNhap("TK2601010006", "123456".toCharArray()));
    }
}
