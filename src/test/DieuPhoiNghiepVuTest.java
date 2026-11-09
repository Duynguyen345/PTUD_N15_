

import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import bus.*;
import dao.*;
import entity.*;
import util.BoBamMatKhau;

/** Kiểm chứng use case, phân quyền, trạng thái và tranh chấp giữa các máy gọi số. */
class DieuPhoiNghiepVuTest {
    private final Clock dongHo = Clock.fixed(Instant.parse("2026-11-08T02:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private BoDAO taiKhoanDAO;
    private DieuPhoiDuLieuMauDAO dieuPhoiDAO;
    private QuanLyPhienDangNhap phien;
    private DangNhapBUS dangNhap;
    private DieuPhoiBUS dieuPhoi;
    private TraCuuHoSoBUS traCuu;

    @BeforeEach
    void chuanBi() {
        var bam = new BoBamMatKhau();
        taiKhoanDAO = BoDAO.duLieuMau(bam);
        dieuPhoiDAO = new DieuPhoiDuLieuMauDAO(LocalDate.now(dongHo));
        phien = new QuanLyPhienDangNhap(taiKhoanDAO.dangNhap());
        dangNhap = new DangNhapBUS(taiKhoanDAO.dangNhap(), bam, phien);
        dieuPhoi = new DieuPhoiBUS(dieuPhoiDAO, phien, dongHo);
        traCuu = new TraCuuHoSoBUS(dieuPhoiDAO, phien);
    }

    private void vao(String soDuoi) { dangNhap.dangNhap("TK260101000" + soDuoi, "123456".toCharArray()); }

    @Test
    void chuaDangNhapKhongDocDuLieu() {
        assertThrows(SecurityException.class, dieuPhoi::layDanhSachPhong);
        assertThrows(SecurityException.class, () -> traCuu.timBenhNhan("Chau"));
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));
    }

    @Test
    void maTranQuyenCuaBonVaiTro() {
        vao("2"); // Lễ tân: tra cứu, không điều phối.
        assertEquals(1, traCuu.timBenhNhan("Chau").size());
        assertThrows(SecurityException.class, dieuPhoi::layDanhSachPhong);
        assertThrows(SecurityException.class, () -> dieuPhoi.datNhanBenh("P01", false));
        vao("3"); // Bác sĩ: xem, tra cứu, chỉ đổi phòng phụ trách.
        assertEquals(3, dieuPhoi.layDanhSachPhong().size());
        assertEquals(1, traCuu.timBenhNhan("Chau").size());
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        dieuPhoi.datNhanBenh("P01", false);
        assertThrows(SecurityException.class, () -> dieuPhoi.datNhanBenh("P03", true));
        vao("4"); // Phụ tá: điều phối; không mở bệnh án.
        assertThrows(SecurityException.class, () -> traCuu.timBenhNhan("Chau"));
        assertThrows(SecurityException.class, () -> traCuu.layLichSuKham("BN2601010001"));
        dieuPhoi.datNhanBenh("P01", true);
        assertEquals(1, dieuPhoi.goiTiepTheo("P01").soThuTu());
        vao("1");
        assertThrows(SecurityException.class, () -> traCuu.timBenhNhan("BN"));
        assertThrows(SecurityException.class, dieuPhoi::layDanhSachPhong);
        assertThrows(SecurityException.class, () -> dieuPhoi.datNhanBenh("P01", true));
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        phien.kiemTraQuyenQuanLy();
    }

    @Test
    void dangXuatThuHoiQuyen() {
        vao("4");
        phien.dangXuat();
        assertNull(phien.phienHienTai());
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        assertThrows(SecurityException.class, () -> dieuPhoi.layHangCho("P01"));
    }

    @Test
    void taiKhoanBiKhoaSauDangNhapBiChanNgay() {
        vao("4");
        taiKhoanDAO.taiKhoan().capNhatTrangThaiTaiKhoan("TK2601010004", TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(SecurityException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        assertNull(phien.phienHienTai());
    }

    @Test
    void doiVaiTroHoacNghiViecHuyPhienCu() {
        vao("4");
        NhanVien nhanVien = taiKhoanDAO.nhanVien().danhSachNhanVien().get(3);
        nhanVien.setChucVu(ChucVu.LE_TAN);
        taiKhoanDAO.nhanVien().luuNhanVien(nhanVien, false);
        assertThrows(SecurityException.class, () -> dieuPhoi.layHangCho("P01"));
        assertNull(phien.phienHienTai());
        vao("2");
        nhanVien = taiKhoanDAO.nhanVien().danhSachNhanVien().get(1);
        nhanVien.setConLamViec(false);
        taiKhoanDAO.nhanVien().luuNhanVien(nhanVien, false);
        assertThrows(SecurityException.class, () -> traCuu.timBenhNhan("BN"));
    }

    @Test
    void dangNhapSaiKhongGiuPhienTruocVaXoaMatKhau() {
        vao("1");
        char[] matKhau = "saimatkhau".toCharArray();
        assertThrows(IllegalArgumentException.class, () -> dangNhap.dangNhap("TK2601010004", matKhau));
        assertEquals('\0', matKhau[0]);
        assertNull(phien.phienHienTai());
        assertThrows(IllegalArgumentException.class, () -> dangNhap.dangNhap("TK2601010004", null));
    }

    @Test
    void fifoGoiLaiVangMatVaBoQuaLuotChuaTraPhi() {
        vao("4");
        var dau = dieuPhoi.goiTiepTheo("P01");
        assertEquals(1, dau.soThuTu());
        assertEquals(TrangThaiHangCho.DANG_GOI, dau.trangThai());
        assertEquals(dau.maPhieuKham(), dieuPhoi.goiLai(dau.maPhieuKham(), "P01").maPhieuKham());
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        dieuPhoi.danhDauVang(dau.maPhieuKham(), "P01");
        var hai = dieuPhoi.goiTiepTheo("P01");
        assertEquals(2, hai.soThuTu());
        dieuPhoi.danhDauVang(hai.maPhieuKham(), "P01");
        var bon = dieuPhoi.goiTiepTheo("P01");
        assertEquals(4, bon.soThuTu());
        dieuPhoi.danhDauVang(bon.maPhieuKham(), "P01");
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        assertEquals(TrangThaiHangCho.CHO_KHAM, dieuPhoi.layHangCho("P01").get(2).trangThai());
    }

    @Test
    void xacNhanChiChoLuotDangGoiVaKhongXacNhanHaiLan() {
        vao("4");
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.xacNhanVaoKham("PK2601019001", "P01"));
        var luot = dieuPhoi.goiTiepTheo("P01");
        assertEquals(TrangThaiHangCho.DANG_KHAM, dieuPhoi.xacNhanVaoKham(luot.maPhieuKham(), "P01").trangThai());
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.xacNhanVaoKham(luot.maPhieuKham(), "P01"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiLai(luot.maPhieuKham(), "P01"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P01"));
    }

    @Test
    void khongThaoTacSaiPhongHoacSaiNgay() {
        vao("4");
        var luot = dieuPhoi.goiTiepTheo("P01");
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.xacNhanVaoKham(luot.maPhieuKham(), "P02"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoiDAO.chuyenTrangThai(luot.maPhieuKham(), "P01",
                LocalDate.now(dongHo).minusDays(1), TrangThaiHangCho.DANG_KHAM, LocalDateTime.now(dongHo)));
        assertEquals(TrangThaiHangCho.DANG_GOI, dieuPhoi.layHangCho("P01").getFirst().trangThai());
    }

    @Test
    void tatPhongGiuHangChoVaChanGoiSo() {
        vao("4");
        List<LuotChoKham> truoc = dieuPhoi.layHangCho("P01");
        dieuPhoi.datNhanBenh("P01", false);
        assertEquals(truoc, dieuPhoi.layHangCho("P01"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P01"));
        dieuPhoi.datNhanBenh("P01", true);
        assertEquals(1, dieuPhoi.goiTiepTheo("P01").soThuTu());
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.datNhanBenh("P01", false));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.datNhanBenh("P02", false));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P02"));
    }

    @Test
    void duLieuDauVaoVaPhongKhongTonTai() {
        vao("4");
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo(" "));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.goiTiepTheo("P99"));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.datNhanBenh("P99", true));
        assertThrows(IllegalArgumentException.class, () -> dieuPhoi.xacNhanVaoKham("", "P01"));
    }

    @Test
    void timTheoMaTenKhongDauSdtVaKyTuDacBiet() {
        vao("2");
        assertEquals("BN2601010001", traCuu.timBenhNhan("nguyen minh chau").getFirst().maBenhNhan());
        assertEquals("BN2601010006", traCuu.timBenhNhan("dang gia huy").getFirst().maBenhNhan());
        assertEquals(1, traCuu.timBenhNhan("0911110001").size());
        assertEquals(1, traCuu.timBenhNhan("BN2601010001").size());
        assertTrue(traCuu.timBenhNhan("khong co").isEmpty());
        assertTrue(traCuu.timBenhNhan("%").isEmpty());
        assertTrue(traCuu.timBenhNhan("' OR 1=1 --").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> traCuu.timBenhNhan(" "));
        assertThrows(IllegalArgumentException.class, () -> traCuu.timBenhNhan("a".repeat(101)));
    }

    @Test
    void lichSuDungBenhNhanSapMoiTruocVaChiDoc() {
        vao("2");
        var lichSu = traCuu.layLichSuKham("BN2601010001");
        assertEquals(3, lichSu.size());
        assertEquals("PK2601019001", lichSu.getFirst().maPhieuKham());
        assertEquals("Kiểm tra sau điều trị", lichSu.get(1).chanDoan());
        assertEquals(1, traCuu.layLichSuKham("BN2601010007").size());
        assertThrows(UnsupportedOperationException.class, () -> lichSu.clear());
        assertThrows(IllegalArgumentException.class, () -> traCuu.layLichSuKham("sai"));
    }

    @Test
    void goiDongThoiChiMotYeuCauThanhCong() throws Exception {
        vao("4");
        try (ExecutorService nhom = Executors.newFixedThreadPool(8)) {
            CountDownLatch batDau = new CountDownLatch(1);
            var ketQua = new java.util.ArrayList<Future<Boolean>>();
            for (int i = 0; i < 16; i++) ketQua.add(nhom.submit(() -> {
                batDau.await();
                try { dieuPhoi.goiTiepTheo("P01"); return true; }
                catch (IllegalArgumentException loi) { return false; }
            }));
            batDau.countDown();
            int thanhCong = 0;
            for (Future<Boolean> tacVu : ketQua) if (tacVu.get(10, TimeUnit.SECONDS)) thanhCong++;
            assertEquals(1, thanhCong);
            assertEquals(1, dieuPhoi.layHangCho("P01").stream().filter(l -> l.trangThai() == TrangThaiHangCho.DANG_GOI).count());
        }
    }
}
