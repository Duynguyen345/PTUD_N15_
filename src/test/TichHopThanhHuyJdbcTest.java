import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import connect.*;
import dao.*;
import entity.*;

/** Database test rieng da chay schema 001, hai migration 040 va 050. Moi ghi deu rollback. */
class TichHopThanhHuyJdbcTest {
    @Test
    void thuPhiLapPhieuTraCuuVaGoiSoDungChungDuLieu() throws Exception {
        String config = System.getProperty("merge.jdbc.config");
        assumeTrue(config != null, "Chua cau hinh SQL Server test rieng.");
        try (Connection c = new KetNoiCSDL(Path.of(config)).moKetNoi()) {
            assertTrue(c.getCatalog().startsWith("N15_MERGE_TEST_"), "Chi cho phep database test rieng");
            c.setAutoCommit(false);
            // DAO van dung transaction API; test giu mot transaction ngoai cung de rollback tat ca.
            NguonKetNoi nguon = () -> (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
                        if (m.getName().equals("close") || m.getName().equals("commit")) return null;
                        try { return m.invoke(c, a); }
                        catch (InvocationTargetException e) { throw e.getCause(); }
                    });
            try {
                try (var s = c.createStatement()) {
                    s.executeUpdate("INSERT INTO NhanVien(maNhanVien,hoTen,chucVu,conLamViec) VALUES"
                            + "('NV9912319901',N'Merge doctor','BAC_SI',1),"
                            + "('NV9912319902',N'Merge assistant','PHU_TA',1),"
                            + "('NV9912319903',N'Merge reception','LE_TAN',1)");
                    s.executeUpdate("INSERT INTO PhongKham(maPhong,tenPhong,maBacSi,maPhuTa,dangNhanBenh)"
                            + " VALUES('M999',N'Merge test','NV9912319901','NV9912319902',1)");
                    s.executeUpdate("IF NOT EXISTS(SELECT 1 FROM DichVu WHERE maDichVu='DV0001')"
                            + " INSERT INTO DichVu VALUES('DV0001',N'Merge examination',100000,'DANG_PHUC_VU')");
                    s.executeUpdate("UPDATE DichVu SET donGiaNiemYet=100000,trangThai='DANG_PHUC_VU' WHERE maDichVu='DV0001'");
                }
                LeTanDAO leTan = new LeTanDAO(nguon);
                DieuPhoiJdbcDAO dieuPhoi = new DieuPhoiJdbcDAO(nguon);
                BenhNhan bn = leTan.luuBenhNhan(new BenhNhan(null, "Merge patient", "0999999901",
                        null, null, null, "Penicillin"));
                if (leTan.caDangMo() == null) leTan.moCa("NV9912319903");
                leTan.thuPhi(bn.maBenhNhan(), HinhThucThanhToan.TIEN_MAT, new BigDecimal("100000"), "NV9912319903");
                PhieuLeTan pk = leTan.lapPhieu(bn.maBenhNhan(), "M999", "Kiem tra");
                assertTrue(dieuPhoi.layHangCho("M999", LocalDate.now()).getFirst().daThanhToanPhiKham());
                BenhNhan traCuu = dieuPhoi.timBenhNhan(bn.maBenhNhan()).getFirst();
                assertNull(traCuu.ngaySinh());
                assertEquals("Penicillin", traCuu.tienSu());
                assertEquals(pk.maPhieuKham(), dieuPhoi.goiTiepTheo("M999", LocalDate.now(), LocalDateTime.now()).maPhieuKham());
                dieuPhoi.chuyenTrangThai(pk.maPhieuKham(), "M999", LocalDate.now(), TrangThaiHangCho.DANG_KHAM, LocalDateTime.now());
                assertEquals("DANG_KHAM", leTan.danhSachLuot("M999", LocalDate.now()).getFirst().trangThai());
                assertEquals(1, dieuPhoi.layLichSuKham(bn.maBenhNhan()).size());
            } finally {
                c.rollback();
            }
        }
    }
}
