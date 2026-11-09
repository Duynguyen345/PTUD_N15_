import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import bus.*;
import dao.*;
import entity.*;
import util.BoBamMatKhau;

class NghiepVuQuanLyTest {
    private BoBamMatKhau boBamMatKhau;
    private BoDAO boDAO;
    private QuanLyPhienDangNhap quanLyPhien;
    private DangNhapBUS dangNhapBUS;
    private NhanVienBUS nhanVienBUS;
    private TaiKhoanBUS taiKhoanBUS;
    private HangHoaBUS hangHoaBUS;
    private DichVuBUS dichVuBUS;
    private BaoCaoBUS baoCaoBUS;
    @TempDir
    Path thuMucTam;

    @BeforeEach
    void chuanBi() {
        boBamMatKhau = new BoBamMatKhau();
        boDAO = BoDAO.duLieuMau(boBamMatKhau);
        quanLyPhien = new QuanLyPhienDangNhap(boDAO.dangNhap());
        dangNhapBUS = new DangNhapBUS(boDAO.dangNhap(), boBamMatKhau, quanLyPhien);
        nhanVienBUS = new NhanVienBUS(boDAO.nhanVien(), quanLyPhien);
        taiKhoanBUS = new TaiKhoanBUS(boDAO.taiKhoan(), quanLyPhien, boBamMatKhau);
        dichVuBUS = new DichVuBUS(boDAO.dichVu(), quanLyPhien);
        hangHoaBUS = new HangHoaBUS(boDAO.hangHoa(), quanLyPhien);
        baoCaoBUS = new BaoCaoBUS(boDAO.baoCao(), quanLyPhien);
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
    }

    private NhanVien taoNhanVienMau() {
        return new NhanVien(null, "Nguyễn Tuấn Duy", "0912345678", LocalDate.of(1995, 1, 1), true, "",
                ChucVu.BAC_SI, true);
    }

    @Test
    void khongTuKhoaBangThongTinNhanVienGiaTrenManHinh() {
        TaiKhoan gia = new TaiKhoan("TK2601010001", "", TrangThaiTaiKhoan.HOAT_DONG, "NV_KHAC");
        assertThrows(IllegalArgumentException.class, () -> taiKhoanBUS.doiTrangThaiTaiKhoan(gia));
        assertEquals(TrangThaiTaiKhoan.HOAT_DONG,
                boDAO.taiKhoan().timTaiKhoan("TK2601010001").getTrangThai());
    }

    @Test
    void locNhanVienTaoTaiKhoanTaiBUS() {
        assertTrue(taiKhoanBUS.timNhanVienChuaCoTaiKhoan(" ").isEmpty());
        assertTrue(taiKhoanBUS.timNhanVienChuaCoTaiKhoan("Hoàng Minh").isEmpty());
        assertTrue(taiKhoanBUS.timNhanVienChuaCoTaiKhoan("Gia Bảo").isEmpty());
        var ketQua = taiKhoanBUS.timNhanVienChuaCoTaiKhoan("  NGỌC MAI  ");
        assertEquals(1, ketQua.size());
        taiKhoanBUS.taoTaiKhoan(ketQua.getFirst().getMaNhanVien(), "secret1".toCharArray());
        assertTrue(taiKhoanBUS.timNhanVienChuaCoTaiKhoan("Ngọc Mai").isEmpty());
        quanLyPhien.dangXuat();
        assertThrows(SecurityException.class, () -> taiKhoanBUS.timNhanVienChuaCoTaiKhoan("Mai"));
    }

    @Test
    void duLieuThieuDuocBaoLoiNghiepVu() {
        assertThrows(IllegalArgumentException.class, () -> dangNhapBUS.dangNhap("TK2601010001", null));
        assertNull(quanLyPhien.phienHienTai());
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
        assertThrows(IllegalArgumentException.class, () -> taiKhoanBUS.taoTaiKhoan("NV", null));
        assertThrows(IllegalArgumentException.class, () -> nhanVienBUS.luuNhanVien(null, true));
        assertThrows(IllegalArgumentException.class, () -> dichVuBUS.luuDichVu(null, true));
        assertThrows(IllegalArgumentException.class, () -> hangHoaBUS.luuHangHoa(null, true));
        NhanVien n = taoNhanVienMau();
        n.setCccd(null);
        nhanVienBUS.luuNhanVien(n, true);
        assertEquals("", nhanVienBUS.layDanhSachNhanVien().getLast().getCccd());
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
        boDAO.taiKhoan().capNhatTrangThaiTaiKhoan("TK2601010003", TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap("TK2601010003", "123456".toCharArray()));
        NhanVien n = boDAO.nhanVien().danhSachNhanVien().get(1);
        n.setConLamViec(false);
        boDAO.nhanVien().luuNhanVien(n, false);
        assertThrows(IllegalArgumentException.class,
                () -> dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray()));
    }

    @Test
    void kiemTraQuyenTaiTangBUS() {
        for (String chucVu : List.of("TK2601010002", "TK2601010003", "TK2601010004")) {
            dangNhapBUS.dangNhap(chucVu, "123456".toCharArray());
            assertThrows(SecurityException.class, nhanVienBUS::layDanhSachNhanVien);
            assertThrows(SecurityException.class, dichVuBUS::danhSachDichVu);
            assertThrows(SecurityException.class,
                    () -> baoCaoBUS.layBaoCao(LocalDate.now(), LocalDate.now()));
        }
    }

    @Test
    void tuChoiPhienDaDangXuatHoacBiThuHoi() {
        boDAO.taiKhoan().capNhatTrangThaiTaiKhoan("TK2601010001", TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(SecurityException.class, hangHoaBUS::danhSachHangHoa);
        quanLyPhien.dangXuat();
        assertNull(quanLyPhien.phienHienTai());
        assertThrows(SecurityException.class, nhanVienBUS::layDanhSachNhanVien);
    }

    @Test
    void themSuaXoaNhanVien() {
        nhanVienBUS.luuNhanVien(taoNhanVienMau(), true);
        NhanVien n = nhanVienBUS.layDanhSachNhanVien().getLast();
        assertNotNull(n.getMaNhanVien());
        n.setHoTen("Tên đã sửa");
        nhanVienBUS.luuNhanVien(n, false);
        assertEquals("Tên đã sửa", nhanVienBUS.layDanhSachNhanVien().getLast().getHoTen());
        nhanVienBUS.xoaNhanVien(n.getMaNhanVien());
        assertEquals(10, nhanVienBUS.layDanhSachNhanVien().size());
    }

    @Test
    void kiemTraDuLieuNhanVien() {
        NhanVien n = taoNhanVienMau();
        n.setSoDienThoai("123");
        assertThrows(IllegalArgumentException.class, () -> nhanVienBUS.luuNhanVien(n, true));
        n.setSoDienThoai("0912345678");
        n.setNgaySinh(LocalDate.now().minusYears(17));
        assertThrows(IllegalArgumentException.class, () -> nhanVienBUS.luuNhanVien(n, true));
        n.setNgaySinh(LocalDate.of(1995, 1, 1));
        n.setCccd("123");
        assertThrows(IllegalArgumentException.class, () -> nhanVienBUS.luuNhanVien(n, true));
    }

    @Test
    void baoVeTaiKhoanHienTaiVaDuLieuThamChieu() {
        TaiKhoan taiKhoanBanThan = boDAO.taiKhoan().timTaiKhoan("TK2601010001");
        assertThrows(IllegalArgumentException.class, () -> taiKhoanBUS.doiTrangThaiTaiKhoan(taiKhoanBanThan));
        assertThrows(IllegalArgumentException.class,
                () -> nhanVienBUS.xoaNhanVien(taiKhoanBanThan.getMaNhanVien()));
        assertThrows(IllegalArgumentException.class,
                () -> nhanVienBUS.xoaNhanVien(boDAO.taiKhoan().timTaiKhoan("TK2601010003").getMaNhanVien()));
        NhanVien banThan = nhanVienBUS.layDanhSachNhanVien().getFirst();
        banThan.setChucVu(ChucVu.LE_TAN);
        assertThrows(IllegalArgumentException.class, () -> nhanVienBUS.luuNhanVien(banThan, false));
    }

    @Test
    void taoTaiKhoanVaKiemTraTrung() {
        nhanVienBUS.luuNhanVien(taoNhanVienMau(), true);
        String maNhanVien = nhanVienBUS.layDanhSachNhanVien().getLast().getMaNhanVien();
        String maTaiKhoan = taiKhoanBUS.taoTaiKhoan(maNhanVien, "secret1".toCharArray());
        assertTrue(maTaiKhoan.matches("TK\\d{10}"));
        assertTrue(boDAO.taiKhoan().timTaiKhoan(maTaiKhoan).getMatKhau().startsWith("pbkdf2$"));
        assertThrows(IllegalArgumentException.class,
                () -> taiKhoanBUS.taoTaiKhoan(maNhanVien, "secret1".toCharArray()));
        assertEquals(ChucVu.BAC_SI, dangNhapBUS.dangNhap(maTaiKhoan, "secret1".toCharArray()).getChucVu());
    }

    @Test
    void tuChoiTenDangNhapCu() {
        for (String oldName : List.of("quanly", "bacsi1", "letan")) {
            assertNull(boDAO.taiKhoan().timTaiKhoan(oldName));
            assertThrows(IllegalArgumentException.class,
                    () -> dangNhapBUS.dangNhap(oldName, "123456".toCharArray()));
        }
    }

    @Test
    void maTaiKhoanTuSinhDuyNhatVaLienTiep() {
        nhanVienBUS.luuNhanVien(taoNhanVienMau(), true);
        String dau = taiKhoanBUS.taoTaiKhoan(nhanVienBUS.layDanhSachNhanVien().getLast().getMaNhanVien(),
                "secret1".toCharArray());
        nhanVienBUS.luuNhanVien(taoNhanVienMau(), true);
        String second = taiKhoanBUS.taoTaiKhoan(nhanVienBUS.layDanhSachNhanVien().getLast().getMaNhanVien(),
                "secret1".toCharArray());
        assertNotEquals(dau, second);
        assertEquals(Integer.parseInt(dau.substring(8)) + 1, Integer.parseInt(second.substring(8)));
        assertEquals(boDAO.taiKhoan().danhSachTaiKhoan().size(),
                boDAO.taiKhoan().danhSachTaiKhoan().stream().map(TaiKhoan::getMaTaiKhoan).distinct().count());
    }

    @Test
    void khoaVaMoTaiKhoan() {
        TaiKhoan bacSi = boDAO.taiKhoan().timTaiKhoan("TK2601010003");
        taiKhoanBUS.doiTrangThaiTaiKhoan(bacSi);
        assertEquals(TrangThaiTaiKhoan.BI_KHOA, boDAO.taiKhoan().timTaiKhoan("TK2601010003").getTrangThai());
        taiKhoanBUS.doiTrangThaiTaiKhoan(bacSi);
        assertEquals(TrangThaiTaiKhoan.HOAT_DONG, boDAO.taiKhoan().timTaiKhoan("TK2601010003").getTrangThai());
    }

    @Test
    void kiemTraDuLieuVaTrangThaiDichVu() {
        DichVu d = new DichVu(null, "Dịch vụ mới", BigDecimal.ZERO, TrangThaiDichVu.DANG_PHUC_VU);
        assertThrows(IllegalArgumentException.class, () -> dichVuBUS.luuDichVu(d, true));
        d.setDonGiaNiemYet(new BigDecimal("120000"));
        dichVuBUS.luuDichVu(d, true);
        assertThrows(IllegalArgumentException.class, () -> dichVuBUS.luuDichVu(d, true));
        DichVu daThem = dichVuBUS.danhSachDichVu().getLast();
        daThem.setTrangThai(TrangThaiDichVu.NGUNG_PHUC_VU);
        dichVuBUS.luuDichVu(daThem, false);
        assertEquals(TrangThaiDichVu.NGUNG_PHUC_VU, dichVuBUS.danhSachDichVu().getLast().getTrangThai());
    }

    @Test
    void kiemTraNguongTonVaHangTrung() {
        HangHoa h = new HangHoa(null, "Vật tư mới", "Hộp", 5, 5);
        hangHoaBUS.luuHangHoa(h, true);
        assertTrue(hangHoaBUS.danhSachHangHoa().getLast().canBoSung());
        assertThrows(IllegalArgumentException.class, () -> hangHoaBUS.luuHangHoa(h, true));
        assertThrows(IllegalArgumentException.class,
                () -> hangHoaBUS.luuHangHoa(new HangHoa(null, "Khác", "Hộp", -1, 0), true));
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
        DuLieuBaoCao duLieu = new DuLieuBaoCao(LocalDate.now(), LocalDate.now(),
                List.of(new DuLieuBaoCao.DongThongKe(LocalDate.now().toString(), 2)),
                List.of(new DuLieuBaoCao.DongThongKe("=SUM(1;2)", 3),
                        new DuLieuBaoCao.DongThongKe("Tên \"dịch vụ\";\nxuống dòng", 1)));
        Path tep = thuMucTam.resolve("report.csv");
        baoCaoBUS.xuatCSV(duLieu, tep);
        String vanBan = Files.readString(tep);
        assertEquals('\ufeff', vanBan.charAt(0));
        assertTrue(vanBan.contains("\"'=SUM(1;2)\";3"));
        assertTrue(vanBan.contains("\"Tên \"\"dịch vụ\"\";\nxuống dòng\";1"));
        assertTrue(vanBan.contains(";2\r\n"));
        assertEquals(1, Files.list(thuMucTam).count());
    }

    @Test
    void baoVeDuLieuKhoiThayDoiBenNgoai() {
        NhanVien n = boDAO.nhanVien().danhSachNhanVien().getFirst();
        n.setHoTen("tampered");
        assertNotEquals("tampered", boDAO.nhanVien().danhSachNhanVien().getFirst().getHoTen());
    }
}
