import java.awt.Font;
import java.nio.file.Path;
import javax.swing.*;
import bus.*;
import connect.KetNoiCSDL;
import dao.*;
import gui.GD_Chinh;
import util.BoBamMatKhau;

/** Composition root: nối các implementation tại một nơi duy nhất. */
public final class UngDung {
    public static void main(String[] thamSo) {
        if (thamSo.length != 0)
            throw new IllegalArgumentException("Ứng dụng chỉ dùng SQL Server. Chạy không kèm tham số; cấu hình kết nối trong db.properties.");
        new UngDung().khoiDong();
    }

    public void khoiDong() {
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        KetNoiCSDL nguonKetNoi = new KetNoiCSDL(
                Path.of(System.getProperty("db.config", "db.properties")));
        DangNhapDAO dangNhapDAO = new DangNhapDAO(nguonKetNoi);
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(dangNhapDAO);
        DangNhapBUS dangNhapBUS = new DangNhapBUS(dangNhapDAO, boBamMatKhau, quanLyPhien);
        NhanVienBUS nhanVienBUS = new NhanVienBUS(new NhanVienDAO(nguonKetNoi), quanLyPhien);
        TaiKhoanBUS taiKhoanBUS = new TaiKhoanBUS(new TaiKhoanDAO(nguonKetNoi), quanLyPhien, boBamMatKhau);
        DichVuBUS dichVuBUS = new DichVuBUS(new DichVuDAO(nguonKetNoi), quanLyPhien);
        HangHoaBUS hangHoaBUS = new HangHoaBUS(new HangHoaDAO(nguonKetNoi), quanLyPhien);
        BaoCaoBUS baoCaoBUS = new BaoCaoBUS(new BaoCaoDAO(nguonKetNoi), quanLyPhien);
        DieuPhoiDAO dieuPhoiDAO = new DieuPhoiJdbcDAO(nguonKetNoi);
        DieuPhoiBUS dieuPhoiBUS = new DieuPhoiBUS(dieuPhoiDAO, quanLyPhien, java.time.Clock.systemDefaultZone());
        TraCuuHoSoBUS traCuuBUS = new TraCuuHoSoBUS(dieuPhoiDAO, quanLyPhien);
        LeTanBUS leTanBUS = new LeTanBUS(new LeTanDAO(nguonKetNoi), new DichVuDAO(nguonKetNoi), quanLyPhien);
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
            new GD_Chinh(dangNhapBUS, quanLyPhien, nhanVienBUS, taiKhoanBUS, dichVuBUS, hangHoaBUS, baoCaoBUS, dieuPhoiBUS, traCuuBUS, leTanBUS)
                    .setVisible(true);
        });
    }
}
