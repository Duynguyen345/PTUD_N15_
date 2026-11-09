package vn.nhom15.nhakhoa;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.gui.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Luồng lễ tân trên kho demo: thu phí → lập phiếu → thanh toán → chốt ca. */
class NghiepVuLeTanTest {
    private DangNhapBUS dangNhapBUS;
    private LeTanBUS leTanBUS;

    @BeforeEach
    void chuanBi() {
        BoBamMatKhau bam = new BoBamMatKhau();
        PhongKhamDAO quanLy = new PhongKhamDuLieuMauDAO(bam);
        QuanLyPhienDangNhap phien = new QuanLyPhienDangNhap(quanLy);
        dangNhapBUS = new DangNhapBUS(quanLy, bam, phien);
        leTanBUS = new LeTanBUS(new LeTanDuLieuMauDAO(quanLy), quanLy, phien);
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
                () -> leTanBUS.thuPhi("BN2601010001", "TIEN_MAT", "-5"));
    }

    @Test
    void luongLeTanDayDu() {
        dangNhapBUS.dangNhap("TK2601010002", "123456".toCharArray());
        if (leTanBUS.caDangMo() == null)
            leTanBUS.moCa();
        assertFalse(leTanBUS.timBenhNhan("Phúc").isEmpty());
        var phi = leTanBUS.mucPhiKham();
        HoaDon hd = leTanBUS.thuPhi("BN2601010001", "TIEN_MAT", phi.toPlainString());
        assertNull(hd.getMaPhieuKham());
        PhongKham phong = leTanBUS.danhSachPhong().stream()
                .filter(p -> p.isDangNhanBenh() && p.getMaBacSi() != null && p.getMaPhuTa() != null)
                .findFirst()
                .orElseThrow();
        PhieuKham pk = leTanBUS.lapPhieu("BN2601010001", phong.getMaPhong(), "Đau răng");
        assertEquals(hd.getMaHoaDon(), pk.getMaHoaDonPhiKham());
        assertFalse(leTanBUS.danhSachLuot(phong.getMaPhong()).isEmpty());

        PhieuKham cho = leTanBUS.phieuChoThanhToan().getFirst();
        var tong = leTanBUS.dichVuTheoPhieu(cho.getMaPhieuKham()).stream().map(ChiDinhDichVu::getDonGia)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        HoaDon hdDv = leTanBUS.thanhToanDichVu(cho.getMaPhieuKham(), "CHUYEN_KHOAN",
                tong.toPlainString());
        assertNotNull(hdDv.getMaPhieuKham());

        CaThuNgan ca = leTanBUS.caDangMo();
        assertTrue(leTanBUS.hoaDonTrongCa(ca).size() >= 2);
        CaThuNgan daChot = leTanBUS.chotCa(ca.getMaCa());
        assertFalse(daChot.isDangMo());
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
        PhongKhamDAO quanLy = new PhongKhamDuLieuMauDAO(bam);
        QuanLyPhienDangNhap phien = new QuanLyPhienDangNhap(quanLy);
        DangNhapBUS dn = new DangNhapBUS(quanLy, bam, phien);
        NhanSuBUS ns = new NhanSuBUS(quanLy, phien, bam);
        DanhMucBUS dm = new DanhMucBUS(quanLy, phien);
        BaoCaoBUS bc = new BaoCaoBUS(quanLy, phien);
        LeTanBUS lt = new LeTanBUS(new LeTanDuLieuMauDAO(quanLy), quanLy, phien);
        AtomicReference<GD_Chinh> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GD_Chinh f = new GD_Chinh(dn, phien, ns, dm, bc, lt, true);
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

