package vn.nhom15.nhakhoa;

import java.awt.Font;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.connect.KetNoiCSDL;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.gui.GD_Chinh;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Composition root: nối các implementation tại một nơi duy nhất. */
public final class UngDung {
    public static void main(String[] thamSo) {
        new UngDung().khoiDong();
    }

    public void khoiDong() {
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        KetNoiCSDL nguonKetNoi = new KetNoiCSDL(Path.of(System.getProperty("db.config", "db.properties")));
        PhongKhamDAO phongKhamDAO = new PhongKhamDAO(nguonKetNoi);
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        DangNhapBUS dangNhapBUS = new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien);
        NhanSuBUS nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        DanhMucBUS danhMucBUS = new DanhMucBUS(phongKhamDAO, quanLyPhien);
        BaoCaoBUS baoCaoBUS = new BaoCaoBUS(phongKhamDAO, quanLyPhien);
        Clock dongHo = Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
        DieuPhoiDAO dieuPhoiDAO = new DieuPhoiJdbcDAO(nguonKetNoi);
        DieuPhoiBUS dieuPhoiBUS = new DieuPhoiBUS(dieuPhoiDAO, quanLyPhien, dongHo);
        TraCuuHoSoBUS traCuuBUS = new TraCuuHoSoBUS(dieuPhoiDAO, quanLyPhien);
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo giaoDien : UIManager.getInstalledLookAndFeels())
                    if (giaoDien.getName().equals("Nimbus")) {
                        UIManager.setLookAndFeel(giaoDien.getClassName());
                        break;
                    }
            } catch (Exception boQua) {
                // Giao diện mặc định vẫn dùng được.
            }
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            new GD_Chinh(dangNhapBUS, quanLyPhien, nhanSuBUS, danhMucBUS, baoCaoBUS, dieuPhoiBUS, traCuuBUS)
                    .setVisible(true);
        });
    }
}
