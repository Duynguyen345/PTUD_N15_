package vn.nhom15.nhakhoa;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.dao.PhongKhamDuLieuMauDAO;
import vn.nhom15.nhakhoa.gui.GD_Chinh;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

class LuongGiaoDienTest {
    @Test
    void dangNhapDieuHuongVaTaoDichVu() throws Exception {
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        var phongKhamDAO = new PhongKhamDuLieuMauDAO(boBamMatKhau);
        var quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        var dangNhapBUS = new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien);
        var nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        var danhMucBUS = new DanhMucBUS(phongKhamDAO, quanLyPhien);
        var baoCaoBUS = new BaoCaoBUS(phongKhamDAO, quanLyPhien);
        AtomicReference<GD_Chinh> thamChieu = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GD_Chinh khungCuaSo = new GD_Chinh(dangNhapBUS, quanLyPhien, nhanSuBUS, danhMucBUS, baoCaoBUS);
            khungCuaSo.addNotify();
            khungCuaSo.validate();
            thamChieu.set(khungCuaSo);
            List<JTextField> cacTruong = layCacThanhPhan(khungCuaSo.getContentPane(), JTextField.class);
            cacTruong.stream().filter(f -> !(f instanceof JPasswordField)).findFirst().orElseThrow()
                    .setText("TK2601010001");
            layCacThanhPhan(khungCuaSo.getContentPane(), JPasswordField.class).getFirst().setText("123456");
            taoNut(khungCuaSo, "Đăng nhập").doClick();
        });
        GD_Chinh khungCuaSo = thamChieu.get();
        try {
            choHoanTat(() -> layCacThanhPhan(khungCuaSo.getContentPane(), JButton.class).stream()
                    .anyMatch(b -> b.getText().equals("Dịch vụ") && b.isEnabled()));
            SwingUtilities.invokeAndWait(() -> taoNut(khungCuaSo, "Dịch vụ").doClick());
            SwingUtilities.invokeAndWait(() -> {
                assertEquals(0,
                        layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).getFirst().getRowCount());
                taoNut(khungCuaSo, "Tìm kiếm").doClick();
                assertEquals(0,
                        layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).getFirst().getRowCount());
                layCacThanhPhan(khungCuaSo.getContentPane(), JTextField.class).getFirst().setText("DV000");
                assertEquals(0,
                        layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).getFirst().getRowCount());
                taoNut(khungCuaSo, "Tìm kiếm").doClick();
            });
            choHoanTat(() -> layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).stream()
                    .anyMatch(t -> t.getRowCount() == 8 && t.isEnabled()));
            SwingUtilities.invokeAndWait(() -> {
                List<JTextField> cacTruong = layCacThanhPhan(khungCuaSo.getContentPane(), JTextField.class);
                // Form: tìm kiếm, mã chỉ đọc, tên, giá.
                cacTruong.get(2).setText("Dịch vụ từ GUI");
                cacTruong.get(3).setText("150000");
                taoNut(khungCuaSo, "Thêm").doClick();
            });
            choHoanTat(() -> layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).stream()
                    .anyMatch(t -> t.getRowCount() == 0 && t.isEnabled()));
            assertTrue(phongKhamDAO.danhSachDichVu().stream()
                    .anyMatch(d -> d.getTenDichVu().equals("Dịch vụ từ GUI")));
            SwingUtilities.invokeAndWait(() -> {
                layCacThanhPhan(khungCuaSo.getContentPane(), JTextField.class).getFirst()
                        .setText("Dịch vụ từ GUI");
                taoNut(khungCuaSo, "Tìm kiếm").doClick();
            });
            choHoanTat(() -> layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).stream()
                    .anyMatch(t -> t.getRowCount() == 1 && t.isEnabled()));
            SwingUtilities.invokeAndWait(() -> {
                layCacThanhPhan(khungCuaSo.getContentPane(), JTextField.class).getFirst().setText("");
                assertEquals(0,
                        layCacThanhPhan(khungCuaSo.getContentPane(), JTable.class).getFirst().getRowCount());
            });
            SwingUtilities.invokeAndWait(() -> {
                khungCuaSo.validate();
                var vungNoiDung = khungCuaSo.getContentPane();
                BufferedImage anh = new BufferedImage(vungNoiDung.getWidth(), vungNoiDung.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = anh.createGraphics();
                vungNoiDung.printAll(g);
                g.dispose();
                try {
                    Path tep = Path.of("target", "workspace-preview.png");
                    ImageIO.write(anh, "png", tep.toFile());
                } catch (Exception loi) {
                    throw new RuntimeException(loi);
                }
            });
        } finally {
            SwingUtilities.invokeAndWait(khungCuaSo::dispose);
        }
    }

    private JButton taoNut(JFrame khungCuaSo, String vanBan) {
        return layCacThanhPhan(khungCuaSo.getContentPane(), JButton.class).stream()
                .filter(b -> b.getText().equals(vanBan)).findFirst().orElseThrow();
    }

    private <T> List<T> layCacThanhPhan(Component thanhPhanCha, Class<T> loai) {
        List<T> ketQua = new ArrayList<>();
        if (loai.isInstance(thanhPhanCha))
            ketQua.add(loai.cast(thanhPhanCha));
        if (thanhPhanCha instanceof Container vungChua)
            for (Component thanhPhanCon : vungChua.getComponents())
                ketQua.addAll(layCacThanhPhan(thanhPhanCon, loai));
        return ketQua;
    }

    private void choHoanTat(BooleanSupplier sanSang) throws Exception {
        long hanCho = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < hanCho) {
            AtomicReference<Boolean> hoanTat = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> hoanTat.set(sanSang.getAsBoolean()));
            if (hoanTat.get())
                return;
            Thread.sleep(20);
        }
        fail("Swing workflow timeout");
    }
}
