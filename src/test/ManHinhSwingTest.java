import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import bus.*;
import dao.*;
import gui.*;
import util.BoBamMatKhau;

class ManHinhSwingTest {
    @Test
    void hienThiCacManHinhVaTaiDuLieuNen() throws Exception {
        BoBamMatKhau bamMatKhau = new BoBamMatKhau();
        BoDAO boDAO = BoDAO.duLieuMau(bamMatKhau);
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(boDAO.dangNhap());
        DangNhapBUS dangNhapBUS = new DangNhapBUS(boDAO.dangNhap(), bamMatKhau, quanLyPhien);
        dangNhapBUS.dangNhap("TK2601010001", "123456".toCharArray());
        NhanVienBUS nhanVienBUS = new NhanVienBUS(boDAO.nhanVien(), quanLyPhien);
        TaiKhoanBUS taiKhoanBUS = new TaiKhoanBUS(boDAO.taiKhoan(), quanLyPhien, bamMatKhau);
        DichVuBUS dichVuBUS = new DichVuBUS(boDAO.dichVu(), quanLyPhien);
        HangHoaBUS hangHoaBUS = new HangHoaBUS(boDAO.hangHoa(), quanLyPhien);
        BaoCaoBUS baoCaoBUS = new BaoCaoBUS(boDAO.baoCao(), quanLyPhien);
        BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
        AtomicReference<JFrame> khungChua = new AtomicReference<>();
        List<ManHinhCoSo> cacManHinh = new ArrayList<>();
        Path thuMuc = Path.of("target", "ui-preview");
        Files.createDirectories(thuMuc);
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception loi) {
                throw new RuntimeException(loi);
            }
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            cacManHinh.add(new GD_DangNhap(dangNhapBUS, boXuLy, () -> {
            }));
            cacManHinh.add(new GD_TongQuan(nhanVienBUS, dichVuBUS, hangHoaBUS, boXuLy));
            cacManHinh.add(new GD_NhanVien(nhanVienBUS, boXuLy));
            cacManHinh.add(new GD_TaiKhoan(taiKhoanBUS, boXuLy));
            cacManHinh.add(new GD_DichVu(dichVuBUS, boXuLy));
            cacManHinh.add(new GD_HangHoa(hangHoaBUS, boXuLy));
            cacManHinh.add(new GD_BaoCao(baoCaoBUS, boXuLy));
            JFrame khungCuaSo = new JFrame();
            khungCuaSo.setSize(1160, 760);
            khungCuaSo.addNotify();
            khungChua.set(khungCuaSo);
        });
        try {
            for (ManHinhCoSo manHinh : cacManHinh) {
                SwingUtilities.invokeAndWait(() -> {
                    khungChua.get().setContentPane(manHinh);
                    khungChua.get().validate();
                    manHinh.lamMoi();
                });
                long hanCho = System.nanoTime() + 10_000_000_000L;
                while (true) {
                    AtomicReference<Boolean> dangThucHien = new AtomicReference<>();
                    SwingUtilities.invokeAndWait(() -> dangThucHien.set(boXuLy.dangXuLy()));
                    if (!dangThucHien.get())
                        break;
                    if (System.nanoTime() > hanCho)
                        fail("Worker timeout");
                    Thread.sleep(20);
                }
                SwingUtilities.invokeAndWait(() -> {
                    if (manHinh instanceof GD_NhanVien || manHinh instanceof GD_TaiKhoan
                            || manHinh instanceof GD_DichVu || manHinh instanceof GD_HangHoa) {
                        kiemTraBangTrong(manHinh);
                    }
                    khungChua.get().validate();
                    BufferedImage anh = new BufferedImage(manHinh.getWidth(), manHinh.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = anh.createGraphics();
                    manHinh.printAll(g);
                    g.dispose();
                    try {
                        ImageIO.write(anh, "png",
                                thuMuc.resolve(manHinh.getClass().getSimpleName() + ".png").toFile());
                    } catch (Exception loi) {
                        throw new RuntimeException(loi);
                    }
                    assertTrue(manHinh.getWidth() > 1000);
                    assertTrue(manHinh.getHeight() > 650);
                });
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> khungChua.get().dispose());
        }
        assertEquals(7, Files.list(thuMuc).filter(p -> p.toString().endsWith(".png")).count());
    }

    private void kiemTraBangTrong(Component thanhPhan) {
        if (thanhPhan instanceof JTable bang)
            assertEquals(0, bang.getRowCount(), "Danh sách phải trống trước khi tìm kiếm");
        if (thanhPhan instanceof JComboBox<?> combo && thanhPhan.getParent() != null
                && combo.getItemCount() > 0
                && combo.getItemAt(0) instanceof entity.NhanVien)
            fail("Không tự nạp danh sách nhân viên");
        if (thanhPhan instanceof Container vungChua)
            for (Component thanhPhanCon : vungChua.getComponents())
                kiemTraBangTrong(thanhPhanCon);
    }
}
