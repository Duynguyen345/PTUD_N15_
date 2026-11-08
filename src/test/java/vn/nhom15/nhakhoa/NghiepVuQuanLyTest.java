package vn.nhom15.nhakhoa;

import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

class NghiepVuQuanLyTest {
    private BoBamMatKhau boBamMatKhau;
    private PhongKhamDAO phongKhamDAO;
    private QuanLyPhienDangNhap quanLyPhien;
    private DangNhapBUS dangNhapBUS;
    private NhanSuBUS nhanSuBUS;
    private DanhMucBUS danhMucBUS;
    private BaoCaoBUS baoCaoBUS;
    @TempDir
    Path thuMucTam;

    @BeforeEach
    void chuanBi() {
        boBamMatKhau = new BoBamMatKhau();
        phongKhamDAO = new PhongKhamDuLieuMauDAO(boBamMatKhau);
        quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        dangNhapBUS = new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien);
        nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        danhMucBUS = new DanhMucBUS(phongKhamDAO, quanLyPhien);
        baoCaoBUS = new BaoCaoBUS(phongKhamDAO, quanLyPhien);
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
    }

    private NhanVien taoNhanVienMau() {
        return new NhanVien(null, "Nguyễn Tuấn Duy", "0912345678", LocalDate.of(1995, 1, 1), true, "",
                ChucVu.BAC_SI, true);
    }

    @Test
    void bamVaKiemTraMatKhau() {
        String dau = boBamMatKhau.bamMatKhau("secret1".toCharArray()),
                second = boBamMatKhau.bamMatKhau("secret1".toCharArray());
        assertNotEquals(dau, second);
        assertTrue(boBamMatKhau.kiemTraMatKhau("secret1".toCharArray(), dau));
        assertFalse(boBamMatKhau.kiemTraMatKhau("wrong".toCharArray(), dau));
        assertFalse(boBamMatKhau.kiemTraMatKhau("secret1".toCharArray(), "broken"));
    }

    @Test
    void dangNhapXoaMatKhauVaLayDungQuyen() {
        char[] matKhau = "123456".toCharArray();
        assertEquals(ChucVu.QUAN_LY, dangNhapBUS.dangNhap("TK2601010001", matKhau).getChucVu());
        assertEquals('\0', matKhau[0]);
    }

    @Test
    void tuChoiDangNhapSai() {
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap("TK2601010001", "wrong".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> dangNhapBUS.dangNhap("", "".toCharArray()));
    }

    @Test
    void tuChoiTaiKhoanKhoaVaNhanVienNghiViec() {
        phongKhamDAO.capNhatTrangThaiTaiKhoan("TK2601010003", TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap("TK2601010003", "123456".toCharArray()));
        NhanVien n = phongKhamDAO.danhSachNhanVien().get(1);
        n.setConLamViec(false);
        phongKhamDAO.luuNhanVien(n, false);
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray()));
    }

    @Test
    void kiemTraQuyenTaiTangBUS() {
        for (String chucVu : List.of("TK2601010002", "TK2601010003", "TK2601010004")) {
            dangNhapBUS.dangNhap(chucVu, "123456".toCharArray());
            assertThrows(SecurityException.class, nhanSuBUS::layDanhSachNhanVien);
            assertThrows(SecurityException.class, danhMucBUS::danhSachDichVu);
            assertThrows(SecurityException.class,
                    () -> baoCaoBUS.layBaoCao(LocalDate.now(), LocalDate.now()));
        }
    }

    @Test
    void tuChoiPhienDaDangXuatHoacBiThuHoi() {
        phongKhamDAO.capNhatTrangThaiTaiKhoan("TK2601010001", TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(SecurityException.class, danhMucBUS::danhSachHangHoa);
        quanLyPhien.dangXuat();
        assertNull(quanLyPhien.phienHienTai());
        assertThrows(SecurityException.class, nhanSuBUS::layDanhSachNhanVien);
    }

    @Test
    void themSuaXoaNhanVien() {
        nhanSuBUS.luuNhanVien(taoNhanVienMau(), true);
        NhanVien n = nhanSuBUS.layDanhSachNhanVien().getLast();
        assertNotNull(n.getMaNhanVien());
        n.setHoTen("Tên đã sửa");
        nhanSuBUS.luuNhanVien(n, false);
        assertEquals("Tên đã sửa", nhanSuBUS.layDanhSachNhanVien().getLast().getHoTen());
        nhanSuBUS.xoaNhanVien(n.getMaNhanVien());
        assertEquals(10, nhanSuBUS.layDanhSachNhanVien().size());
    }

    @Test
    void kiemTraDuLieuNhanVien() {
        NhanVien n = taoNhanVienMau();
        n.setSoDienThoai("123");
        assertThrows(IllegalArgumentException.class, () -> nhanSuBUS.luuNhanVien(n, true));
        n.setSoDienThoai("0912345678");
        n.setNgaySinh(LocalDate.now().minusYears(17));
        assertThrows(IllegalArgumentException.class, () -> nhanSuBUS.luuNhanVien(n, true));
        n.setNgaySinh(LocalDate.of(1995, 1, 1));
        n.setCccd("123");
        assertThrows(IllegalArgumentException.class, () -> nhanSuBUS.luuNhanVien(n, true));
    }

    @Test
    void baoVeTaiKhoanHienTaiVaDuLieuThamChieu() {
        TaiKhoan taiKhoanBanThan = phongKhamDAO.timTaiKhoan("TK2601010001");
        assertThrows(IllegalArgumentException.class, () -> nhanSuBUS.doiTrangThaiTaiKhoan(taiKhoanBanThan));
        assertThrows(IllegalArgumentException.class,
                () -> nhanSuBUS.xoaNhanVien(taiKhoanBanThan.getMaNhanVien()));
        assertThrows(IllegalArgumentException.class,
                () -> nhanSuBUS.xoaNhanVien(phongKhamDAO.timTaiKhoan("TK2601010003").getMaNhanVien()));
        NhanVien banThan = nhanSuBUS.layDanhSachNhanVien().getFirst();
        banThan.setChucVu(ChucVu.LE_TAN);
        assertThrows(IllegalArgumentException.class, () -> nhanSuBUS.luuNhanVien(banThan, false));
    }

    @Test
    void taoTaiKhoanVaKiemTraTrung() {
        nhanSuBUS.luuNhanVien(taoNhanVienMau(), true);
        String maNhanVien = nhanSuBUS.layDanhSachNhanVien().getLast().getMaNhanVien();
        String maTaiKhoan = nhanSuBUS.taoTaiKhoan(maNhanVien, "secret1".toCharArray());
        assertTrue(maTaiKhoan.matches("TK\\d{10}"));
        assertTrue(phongKhamDAO.timTaiKhoan(maTaiKhoan).getMatKhau().startsWith("pbkdf2$"));
        assertThrows(IllegalArgumentException.class,
                () -> nhanSuBUS.taoTaiKhoan(maNhanVien, "secret1".toCharArray()));
        assertEquals(ChucVu.BAC_SI, dangNhapBUS.dangNhap(maTaiKhoan, "secret1".toCharArray()).getChucVu());
    }

    @Test
    void tuChoiTenDangNhapCu() {
        for (String oldName : List.of("quanly", "bacsi1", "letan")) {
            assertNull(phongKhamDAO.timTaiKhoan(oldName));
            assertThrows(IllegalArgumentException.class,
                    () -> dangNhapBUS.dangNhap(oldName, "123456".toCharArray()));
        }
    }

    @Test
    void maTaiKhoanTuSinhDuyNhatVaLienTiep() {
        nhanSuBUS.luuNhanVien(taoNhanVienMau(), true);
        String dau = nhanSuBUS.taoTaiKhoan(nhanSuBUS.layDanhSachNhanVien().getLast().getMaNhanVien(),
                "secret1".toCharArray());
        nhanSuBUS.luuNhanVien(taoNhanVienMau(), true);
        String second = nhanSuBUS.taoTaiKhoan(nhanSuBUS.layDanhSachNhanVien().getLast().getMaNhanVien(),
                "secret1".toCharArray());
        assertNotEquals(dau, second);
        assertEquals(Integer.parseInt(dau.substring(8)) + 1, Integer.parseInt(second.substring(8)));
        assertEquals(phongKhamDAO.danhSachTaiKhoan().size(),
                phongKhamDAO.danhSachTaiKhoan().stream().map(TaiKhoan::getMaTaiKhoan).distinct().count());
    }

    @Test
    void khoaVaMoTaiKhoan() {
        TaiKhoan bacSi = phongKhamDAO.timTaiKhoan("TK2601010003");
        nhanSuBUS.doiTrangThaiTaiKhoan(bacSi);
        assertEquals(TrangThaiTaiKhoan.BI_KHOA, phongKhamDAO.timTaiKhoan("TK2601010003").getTrangThai());
        nhanSuBUS.doiTrangThaiTaiKhoan(bacSi);
        assertEquals(TrangThaiTaiKhoan.HOAT_DONG, phongKhamDAO.timTaiKhoan("TK2601010003").getTrangThai());
    }

    @Test
    void kiemTraDuLieuVaTrangThaiDichVu() {
        DichVu d = new DichVu(null, "Dịch vụ mới", BigDecimal.ZERO, TrangThaiDichVu.DANG_PHUC_VU);
        assertThrows(IllegalArgumentException.class, () -> danhMucBUS.luuDichVu(d, true));
        d.setDonGiaNiemYet(new BigDecimal("120000"));
        danhMucBUS.luuDichVu(d, true);
        assertThrows(IllegalArgumentException.class, () -> danhMucBUS.luuDichVu(d, true));
        DichVu daThem = danhMucBUS.danhSachDichVu().getLast();
        daThem.setTrangThai(TrangThaiDichVu.NGUNG_PHUC_VU);
        danhMucBUS.luuDichVu(daThem, false);
        assertEquals(TrangThaiDichVu.NGUNG_PHUC_VU, danhMucBUS.danhSachDichVu().getLast().getTrangThai());
    }

    @Test
    void kiemTraNguongTonVaHangTrung() {
        HangHoa h = new HangHoa(null, "Vật tư mới", "Hộp", 5, 5);
        danhMucBUS.luuHangHoa(h, true);
        assertTrue(danhMucBUS.danhSachHangHoa().getLast().canBoSung());
        assertThrows(IllegalArgumentException.class, () -> danhMucBUS.luuHangHoa(h, true));
        assertThrows(IllegalArgumentException.class,
                () -> danhMucBUS.luuHangHoa(new HangHoa(null, "Khác", "Hộp", -1, 0), true));
    }

    @Test
    void kiemTraKhoangNgayVaBaoCaoTrong() {
        assertThrows(IllegalArgumentException.class,
                () -> baoCaoBUS.layBaoCao(LocalDate.now(), LocalDate.now().minusDays(1)));
        DuLieuBaoCao motNgay = baoCaoBUS.layBaoCao(LocalDate.now(), LocalDate.now());
        assertEquals(1, motNgay.danhSachLuotKham().size());
        DuLieuBaoCao duLieuTrong = baoCaoBUS.layBaoCao(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 2));
        assertTrue(duLieuTrong.danhSachLuotKham().isEmpty());
        assertTrue(duLieuTrong.danhSachDichVu().isEmpty());
    }

    @Test
    void kiemTraDinhDangVaAnToanCSV() throws Exception {
        DuLieuBaoCao DuLieuTaiKhoan = new DuLieuBaoCao(LocalDate.now(), LocalDate.now(),
                List.of(new DuLieuBaoCao.DongThongKe(LocalDate.now().toString(), 2)),
                List.of(new DuLieuBaoCao.DongThongKe("=SUM(1;2)", 3),
                        new DuLieuBaoCao.DongThongKe("Tên \"dịch vụ\";\nxuống dòng", 1)));
        Path tep = thuMucTam.resolve("report.csv");
        baoCaoBUS.xuatCSV(DuLieuTaiKhoan, tep);
        String vanBan = Files.readString(tep);
        assertEquals('\ufeff', vanBan.charAt(0));
        assertTrue(vanBan.contains("\"'=SUM(1;2)\";3"));
        assertTrue(vanBan.contains("\"Tên \"\"dịch vụ\"\";\nxuống dòng\";1"));
        assertTrue(vanBan.contains(";2\r\n"));
        assertEquals(1, Files.list(thuMucTam).count());
    }

    @Test
    void baoVeDuLieuKhoiThayDoiBenNgoai() {
        NhanVien n = phongKhamDAO.danhSachNhanVien().getFirst();
        n.setHoTen("tampered");
        assertNotEquals("tampered", phongKhamDAO.danhSachNhanVien().getFirst().getHoTen());
    }
}
