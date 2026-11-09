

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import bus.*;
import dao.*;
import gui.*;
import util.BoBamMatKhau;

/** Kiểm thử Swing thật: đăng nhập, quyền menu, gọi số, tra cứu và ảnh kiểm tra trong thư mục build. */
class DieuPhoiGiaoDienTest {
    private record BoCanh(QuanLyPhienDangNhap phien, DangNhapBUS dangNhap, DieuPhoiBUS dieuPhoi,
            TraCuuHoSoBUS traCuu, NhanVienBUS nhanSu, TaiKhoanBUS taiKhoan, DichVuBUS danhMuc, HangHoaBUS hangHoa, BaoCaoBUS baoCao) { }

    private BoCanh taoBoCanh() {
        var bam = new BoBamMatKhau();
        var dao = BoDAO.duLieuMau(bam);
        var phien = new QuanLyPhienDangNhap(dao.dangNhap());
        Clock dongHo = Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
        var dieuPhoiDAO = new DieuPhoiDuLieuMauDAO(LocalDate.now(dongHo));
        return new BoCanh(phien, new DangNhapBUS(dao.dangNhap(), bam, phien), new DieuPhoiBUS(dieuPhoiDAO, phien, dongHo),
                new TraCuuHoSoBUS(dieuPhoiDAO, phien), new NhanVienBUS(dao.nhanVien(), phien), new TaiKhoanBUS(dao.taiKhoan(), phien, bam),
                new DichVuBUS(dao.dichVu(), phien), new HangHoaBUS(dao.hangHoa(), phien), new BaoCaoBUS(dao.baoCao(), phien));
    }

    private GD_Chinh taoKhung(BoCanh boCanh) {
        GD_Chinh win = new GD_Chinh(boCanh.dangNhap(), boCanh.phien(), boCanh.nhanSu(), boCanh.taiKhoan(), boCanh.danhMuc(), boCanh.hangHoa(),
                boCanh.baoCao(), boCanh.dieuPhoi(), boCanh.traCuu(), null);
        win.addNotify();
        win.validate();
        return win;
    }

    @Test
    void phuTaDangNhapGoiSoVaXemPhong() throws Exception {
        BoCanh boCanh = taoBoCanh();
        AtomicReference<GD_Chinh> thamChieu = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception loi) { throw new RuntimeException(loi); }
            GD_Chinh win = taoKhung(boCanh);
            thamChieu.set(win);
            chupAnh(win, "01_dang_nhap.png");
            cacThanhPhan(win, JTextField.class).stream().filter(t -> !(t instanceof JPasswordField)).findFirst().orElseThrow().setText("TK2601010004");
            cacThanhPhan(win, JPasswordField.class).getFirst().setText("123456");
            nut(win, "Đăng nhập").doClick();
        });
        GD_Chinh win = thamChieu.get();
        try {
            cho(() -> cacThanhPhan(win, JTable.class).stream().anyMatch(t -> t.isEnabled() && t.getRowCount() == 4));
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(coNut(win, "Tra cứu hồ sơ"));
                assertFalse(coNut(win, "Nhân viên"));
                nut(win, "Gọi số tiếp theo").doClick();
            });
            cho(() -> cacThanhPhan(win, JTable.class).stream().anyMatch(t -> t.isEnabled() && t.getValueAt(0, 5).toString().equals("Đang gọi")));
            SwingUtilities.invokeAndWait(() -> {
                JTable tbl = cacThanhPhan(win, JTable.class).getFirst();
                tbl.setRowSelectionInterval(0, 0);
                assertTrue(nut(win, "Xác nhận vào khám").isEnabled());
                assertFalse(nut(win, "Gọi số tiếp theo").isEnabled());
                chupAnh(win, "02_hang_cho.png");
                nut(win, "Phòng nhận bệnh").doClick();
            });
            cho(() -> cacThanhPhan(win, JTable.class).stream().anyMatch(t -> t.isEnabled() && t.getRowCount() == 3));
            SwingUtilities.invokeAndWait(() -> chupAnh(win, "03_phong_nhan_benh.png"));
            AtomicReference<GD_BangGoiSo> bangSo = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> {
                var bang = new GD_BangGoiSo(boCanh.dieuPhoi());
                bang.addNotify(); bang.validate(); bangSo.set(bang);
            });
            try {
                cho(() -> cacThanhPhan(bangSo.get(), JLabel.class).stream().anyMatch(l -> "001".equals(l.getText())));
                SwingUtilities.invokeAndWait(() -> {
                    assertTrue(cacThanhPhan(bangSo.get(), JLabel.class).stream().noneMatch(l -> l.getText().contains("Nguyễn Minh Châu")));
                    chupAnh(bangSo.get(), "05_bang_goi_so.png");
                });
            } finally { SwingUtilities.invokeAndWait(bangSo.get()::dispose); }
        } finally { SwingUtilities.invokeAndWait(win::dispose); }
    }

    @Test
    void leTanTraCuuHoSoVaBacSiKhongCoNutDieuPhoi() throws Exception {
        BoCanh boCanh = taoBoCanh();
        AtomicReference<JFrame> thamChieu = new AtomicReference<>();
        boCanh.dangNhap().dangNhap("TK2601010002", "123456".toCharArray());
        SwingUtilities.invokeAndWait(() -> {
            JFrame win = new JFrame(); win.setSize(1380, 820);
            GD_TraCuuHoSo pnl = new GD_TraCuuHoSo(boCanh.traCuu(), new BoXuLyTacVuGiaoDien());
            win.setContentPane(pnl); win.addNotify(); win.validate(); thamChieu.set(win); pnl.lamMoi();
            cacThanhPhan(win, JTextField.class).getFirst().setText("nguyen minh chau");
            nut(win, "Tìm hồ sơ").doClick();
        });
        JFrame win = thamChieu.get();
        try {
            cho(() -> cacThanhPhan(win, JTable.class).getFirst().getRowCount() == 1 && cacThanhPhan(win, JTable.class).getFirst().isEnabled());
            SwingUtilities.invokeAndWait(() -> cacThanhPhan(win, JTable.class).getFirst().setRowSelectionInterval(0, 0));
            cho(() -> cacThanhPhan(win, JTable.class).get(1).isEnabled() && cacThanhPhan(win, JTable.class).get(1).getRowCount() == 3);
            SwingUtilities.invokeAndWait(() -> {
                cacThanhPhan(win, JTable.class).get(1).setRowSelectionInterval(1, 1);
                assertTrue(cacThanhPhan(win, JTextArea.class).stream().anyMatch(t -> t.getText().contains("Penicillin")));
                assertTrue(cacThanhPhan(win, JTextArea.class).stream().anyMatch(t -> t.getText().contains("Kiểm tra sau điều trị")));
                chupAnh(win, "04_tra_cuu_ho_so.png");
                cacThanhPhan(win, JTextField.class).getFirst().setText("khong co");
                assertEquals(0, cacThanhPhan(win, JTable.class).getFirst().getRowCount());
                assertTrue(cacThanhPhan(win, JTextArea.class).stream().allMatch(t -> t.getText().isEmpty()));
                nut(win, "Tìm hồ sơ").doClick();
            });
            cho(() -> cacThanhPhan(win, JLabel.class).stream().anyMatch(l -> l.getText().equals("Không tìm thấy hồ sơ phù hợp.")));
        } finally { SwingUtilities.invokeAndWait(win::dispose); }
        boCanh.dangNhap().dangNhap("TK2601010003", "123456".toCharArray());
        SwingUtilities.invokeAndWait(() -> {
            GD_HangCho pnl = new GD_HangCho(boCanh.dieuPhoi(), false, new BoXuLyTacVuGiaoDien(), () -> { });
            assertFalse(coNut(pnl, "Gọi số tiếp theo"));
            assertFalse(coNut(pnl, "Xác nhận vào khám"));
        });
    }

    private boolean coNut(Component goc, String nhan) { return cacThanhPhan(goc, JButton.class).stream().anyMatch(b -> nhan.equals(b.getText())); }

    private JButton nut(Component goc, String nhan) { return cacThanhPhan(goc, JButton.class).stream().filter(b -> nhan.equals(b.getText())).findFirst().orElseThrow(); }

    private <T> List<T> cacThanhPhan(Component goc, Class<T> loai) {
        List<T> ketQua = new ArrayList<>();
        if (loai.isInstance(goc)) ketQua.add(loai.cast(goc));
        if (goc instanceof Container vung) for (Component con : vung.getComponents()) ketQua.addAll(cacThanhPhan(con, loai));
        return ketQua;
    }

    private void cho(BooleanSupplier dieuKien) throws Exception {
        long han = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
        while (System.nanoTime() < han) {
            AtomicReference<Boolean> xong = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> xong.set(dieuKien.getAsBoolean()));
            if (xong.get()) return;
            Thread.sleep(25);
        }
        fail("Hết thời gian chờ Swing.");
    }

    private void chupAnh(JFrame win, String ten) {
        try {
            win.validate();
            Container pnl = win.getContentPane();
            BufferedImage anh = new BufferedImage(pnl.getWidth(), pnl.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D doHoa = anh.createGraphics();
            pnl.printAll(doHoa); doHoa.dispose();
            Path duongDan = Path.of("target", "thanh-preview", ten);
            Files.createDirectories(duongDan.getParent());
            ImageIO.setUseCache(false);
            ImageIO.write(anh, "png", duongDan.toFile());
        } catch (Exception loi) { throw new RuntimeException(loi); }
    }
}
