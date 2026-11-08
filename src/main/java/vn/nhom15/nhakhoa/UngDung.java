package vn.nhom15.nhakhoa;

import java.awt.Font;
import java.nio.file.Path;
import java.util.Arrays;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.connect.KetNoiCSDL;
import vn.nhom15.nhakhoa.dao.*;
import vn.nhom15.nhakhoa.gui.GD_Chinh;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Composition root: nối các implementation tại một nơi duy nhất. */
public final class UngDung {
    public static void main(String[] thamSo) {
        new UngDung().khoiDong(Arrays.asList(thamSo).contains("--demo"));
    }

    public void khoiDong(boolean duLieuMau) {
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        PhongKhamDAO phongKhamDAO = duLieuMau ? new PhongKhamDuLieuMauDAO(boBamMatKhau)
                : new PhongKhamDAO(
                        new KetNoiCSDL(Path.of(System.getProperty("db.config", "db.properties"))));
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        DangNhapBUS dangNhapBUS = new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien);
        NhanSuBUS nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        DanhMucBUS danhMucBUS = new DanhMucBUS(phongKhamDAO, quanLyPhien);
        BaoCaoBUS baoCaoBUS = new BaoCaoBUS(phongKhamDAO, quanLyPhien);
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
            new GD_Chinh(dangNhapBUS, quanLyPhien, nhanSuBUS, danhMucBUS, baoCaoBUS, duLieuMau)
                    .setVisible(true);
        });
    }
}
