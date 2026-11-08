package vn.nhom15.nhakhoa;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.dao.PhongKhamDuLieuMauDAO;
import vn.nhom15.nhakhoa.gui.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

class TimKiemNhanVienTest {
    @Test
    void timNhanVienTheoThongTinDinhDanh() throws Exception {
        BoBamMatKhau boBamMatKhau = new BoBamMatKhau();
        PhongKhamDuLieuMauDAO phongKhamDAO = new PhongKhamDuLieuMauDAO(boBamMatKhau);
        QuanLyPhienDangNhap quanLyPhien = new QuanLyPhienDangNhap(phongKhamDAO);
        new DangNhapBUS(phongKhamDAO, boBamMatKhau, quanLyPhien).dangNhap("TK2601010001",
                "123456".toCharArray());
        NhanSuBUS nhanSuBUS = new NhanSuBUS(phongKhamDAO, quanLyPhien, boBamMatKhau);
        BoXuLyTacVuGiaoDien boXuLy = new BoXuLyTacVuGiaoDien();
        AtomicReference<GD_NhanVien> thamChieu = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GD_NhanVien khung = new GD_NhanVien(nhanSuBUS, boXuLy);
            khung.lamMoi();
            thamChieu.set(khung);
            assertEquals(0, layCacThanhPhan(khung, JTable.class).getFirst().getRowCount());
        });
        GD_NhanVien khung = thamChieu.get();
        // Bộ mới có An và Anh thật; không khớp nhầm trạng thái Đang làm.
        timKiem(khung, boXuLy, "An", 2);
        timKiem(khung, boXuLy, "Bình", 2);
        timKiem(khung, boXuLy, "Đang làm", 0);
        timKiem(khung, boXuLy, "1995", 0);
        timKiem(khung, boXuLy, "  nGUYỄN VĂN AN  ", 1);
        timKiem(khung, boXuLy, "0987654321", 1);
        timKiem(khung, boXuLy, "079000000003", 1); // CCCD không có cột hiển thị vẫn tìm được.
        timKiem(khung, boXuLy, phongKhamDAO.danhSachNhanVien().getLast().getMaNhanVien(), 1);
        timKiem(khung, boXuLy, "Phụ tá", 0); // Không tìm theo chức vụ.
        timKiem(khung, boXuLy, ".*", 0);
        SwingUtilities.invokeAndWait(() -> {
            layCacThanhPhan(khung, JTextField.class).getFirst().setText("");
            assertEquals(0, layCacThanhPhan(khung, JTable.class).getFirst().getRowCount());
        });
    }

    private void timKiem(GD_NhanVien khung, BoXuLyTacVuGiaoDien boXuLy, String tuKhoa, int mongDoi)
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            layCacThanhPhan(khung, JTextField.class).getFirst().setText(tuKhoa);
            assertEquals(0, layCacThanhPhan(khung, JTable.class).getFirst().getRowCount());
            layCacThanhPhan(khung, JButton.class).stream().filter(b -> b.getText().equals("Tìm kiếm"))
                    .findFirst().orElseThrow().doClick();
        });
        long hanCho = System.nanoTime() + 10_000_000_000L;
        while (true) {
            AtomicReference<Boolean> dangThucHien = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> dangThucHien.set(boXuLy.dangXuLy()));
            if (!dangThucHien.get())
                break;
            if (System.nanoTime() > hanCho)
                fail("Search timeout");
            Thread.sleep(20);
        }
        SwingUtilities.invokeAndWait(() -> assertEquals(mongDoi,
                layCacThanhPhan(khung, JTable.class).getFirst().getRowCount(), tuKhoa));
    }

    private <T> List<T> layCacThanhPhan(Component thanhPhan, Class<T> loai) {
        List<T> ketQua = new ArrayList<>();
        if (loai.isInstance(thanhPhan))
            ketQua.add(loai.cast(thanhPhan));
        if (thanhPhan instanceof Container vungChua)
            for (Component thanhPhanCon : vungChua.getComponents())
                ketQua.addAll(layCacThanhPhan(thanhPhanCon, loai));
        return ketQua;
    }
}
