

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import bus.*;
import dao.*;
import entity.*;
import gui.*;
import util.BoBamMatKhau;

/** Luồng lễ tân trên kho demo: thu phí → lập phiếu → thanh toán → chốt ca. */
class NghiepVuLeTanTest {
    private DangNhapBUS dangNhapBUS;
    private LeTanBUS leTanBUS;

    @BeforeEach
    void chuanBi() {
        BoBamMatKhau bam = new BoBamMatKhau();
        BoDAO quanLy = BoDAO.duLieuMau(bam);
        QuanLyPhienDangNhap phien = new QuanLyPhienDangNhap(quanLy.dangNhap());
        dangNhapBUS = new DangNhapBUS(quanLy.dangNhap(), bam, phien);
        leTanBUS = new LeTanBUS(new LeTanDuLieuMauDAO(quanLy), quanLy.dichVu(), phien);
    }

    @Test
    void quanLyKhongDungDuocChucNangLeTan() {
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
        assertThrows(SecurityException.class, () -> leTanBUS.timBenhNhan("Phúc"));
    }

    @Test
    void validationHoSoVaTien() {
        dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray());
        assertThrows(IllegalArgumentException.class, () -> leTanBUS.luuBenhNhan(
                new BenhNhan(null, "A", "123", null, null, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> leTanBUS.thuPhi("BN2601010001", HinhThucThanhToan.TIEN_MAT, "-5"));
    }

    @Test
    void luongLeTanDayDu() {
        dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray());
        if (leTanBUS.caDangMo() == null)
            leTanBUS.moCa();
        assertFalse(leTanBUS.timBenhNhan("Phúc").isEmpty());
        var phi = leTanBUS.mucPhiKham();
        HoaDonLeTan hd = leTanBUS.thuPhi("BN2601010001", HinhThucThanhToan.TIEN_MAT, phi.toPlainString());
        assertTrue(hd.laPhiKham());
        PhongLeTan phong = leTanBUS.danhSachPhong().stream().filter(PhongLeTan::sanSang).findFirst()
                .orElseThrow();
        PhieuLeTan pk = leTanBUS.lapPhieu("BN2601010001", phong.maPhong(), "Đau răng");
        assertEquals(hd.maHoaDon(), pk.maHoaDonPhiKham());
        assertFalse(leTanBUS.danhSachLuot(phong.maPhong()).isEmpty());

        PhieuLeTan cho = leTanBUS.phieuChoThanhToan().getFirst();
        var tong = leTanBUS.dichVuTheoPhieu(cho.maPhieuKham()).stream().map(DichVuThanhToan::donGia)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        HoaDonLeTan hdDv = leTanBUS.thanhToanDichVu(cho.maPhieuKham(), HinhThucThanhToan.CHUYEN_KHOAN,
                tong.toPlainString());
        assertFalse(hdDv.laPhiKham());

        CaThuNgan ca = leTanBUS.caDangMo();
        assertTrue(leTanBUS.hoaDonTrongCa(ca).size() >= 2);
        CaThuNgan daChot = leTanBUS.chotCa(ca.maCa());
        assertFalse(daChot.dangMo());
    }

    @Test
    void hienThi6ManHinhLeTan() throws Exception {
        dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray());
        leTanBUS.moCa();
        BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
        AtomicReference<JFrame> khung = new AtomicReference<>();
        List<ManHinhCoSo> ds = new java.util.ArrayList<>();
        Path thuMuc = Path.of("target", "ui-preview-letan");
        Files.createDirectories(thuMuc);
        SwingUtilities.invokeAndWait(() -> {
            ds.add(new GD_TongQuanLeTan(leTanBUS, boXuLy));
            ds.add(new GD_HoSoBenhNhan(leTanBUS, boXuLy));
            ds.add(new GD_TraCuuBenhNhan(leTanBUS, boXuLy));
            ds.add(new GD_ThuTien(leTanBUS, boXuLy));
            ds.add(new GD_LapPhieuKham(leTanBUS, boXuLy));
            ds.add(new GD_ChotCa(leTanBUS, boXuLy));
            JFrame f = new JFrame();
            f.setSize(1180, 760);
            f.addNotify();
            khung.set(f);
        });
        try {
            for (ManHinhCoSo m : ds) {
                SwingUtilities.invokeAndWait(() -> {
                    khung.get().setContentPane(m);
                    khung.get().validate();
                    m.lamMoi();
                });
                long han = System.nanoTime() + 10_000_000_000L;
                while (true) {
                    AtomicReference<Boolean> b = new AtomicReference<>();
                    SwingUtilities.invokeAndWait(() -> b.set(boXuLy.dangXuLy()));
                    if (!b.get())
                        break;
                    if (System.nanoTime() > han)
                        fail("Worker timeout");
                    Thread.sleep(20);
                }
                SwingUtilities.invokeAndWait(() -> {
                    khung.get().validate();
                    BufferedImage anh = new BufferedImage(m.getWidth(), m.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = anh.createGraphics();
                    m.printAll(g);
                    g.dispose();
                    try {
                        ImageIO.write(anh, "png",
                                thuMuc.resolve(m.getClass().getSimpleName() + ".png").toFile());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> khung.get().dispose());
        }
    }

    @Test
    void hienThiKhungChinhLeTanDayDu() throws Exception {
        BoBamMatKhau bam = new BoBamMatKhau();
        BoDAO quanLy = BoDAO.duLieuMau(bam);
        QuanLyPhienDangNhap phien = new QuanLyPhienDangNhap(quanLy.dangNhap());
        DangNhapBUS dn = new DangNhapBUS(quanLy.dangNhap(), bam, phien);
        NhanVienBUS ns = new NhanVienBUS(quanLy.nhanVien(), phien);
        DichVuBUS dm = new DichVuBUS(quanLy.dichVu(), phien);
        BaoCaoBUS bc = new BaoCaoBUS(quanLy.baoCao(), phien);
        LeTanBUS lt = new LeTanBUS(new LeTanDuLieuMauDAO(quanLy), quanLy.dichVu(), phien);
        AtomicReference<GD_Chinh> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GD_Chinh f = new GD_Chinh(dn, phien, ns, new TaiKhoanBUS(quanLy.taiKhoan(), phien, bam), dm, new HangHoaBUS(quanLy.hangHoa(), phien), bc, new DieuPhoiBUS(new DieuPhoiDuLieuMauDAO(java.time.LocalDate.now()), phien, java.time.Clock.systemDefaultZone()), new TraCuuHoSoBUS(new DieuPhoiDuLieuMauDAO(java.time.LocalDate.now()), phien), lt);
            f.addNotify();
            f.validate();
            ref.set(f);
            for (Component c : tatCa(f.getContentPane())) {
                if (c instanceof JPasswordField pw)
                    pw.setText("123456");
                else if (c instanceof JTextField tf)
                    tf.setText("TK2601010002");
            }
            for (Component c : tatCa(f.getContentPane()))
                if (c instanceof JButton b && "Đăng nhập".equals(b.getText()))
                    b.doClick();
        });
        long han = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < han) {
            AtomicReference<Boolean> ok = new AtomicReference<>(false);
            SwingUtilities.invokeAndWait(() -> {
                for (Component c : tatCa(ref.get().getContentPane()))
                    if (c instanceof JButton b && "Lập phiếu khám".equals(b.getText()) && b.isEnabled())
                        ok.set(true);
            });
            if (ok.get())
                break;
            Thread.sleep(20);
        }
        SwingUtilities.invokeAndWait(() -> {
            var pane = ref.get().getContentPane();
            for (String nhan : List.of("Tra cứu hồ sơ", "Hồ sơ bệnh nhân", "Lập phiếu khám", "Thu tiền", "Ca thu ngân"))
                assertTrue(tatCa(pane).stream().anyMatch(c -> c instanceof JButton b && nhan.equals(b.getText())), nhan);
            assertFalse(tatCa(pane).stream().anyMatch(c -> c instanceof JButton b && "Nhân viên".equals(b.getText())));
            ref.get().validate();
            BufferedImage anh = new BufferedImage(pane.getWidth(), pane.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = anh.createGraphics();
            pane.printAll(g);
            g.dispose();
            try {
                ImageIO.write(anh, "png",
                        Path.of("target", "ui-preview-letan", "Workspace_LeTan.png").toFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ref.get().dispose();
        });
    }

    private static List<Component> tatCa(Component c) {
        List<Component> ds = new java.util.ArrayList<>();
        ds.add(c);
        if (c instanceof Container ct)
            for (Component con : ct.getComponents())
                ds.addAll(tatCa(con));
        return ds;
    }
}

