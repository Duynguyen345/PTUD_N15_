package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.*;
import vn.nhom15.nhakhoa.entity.HangHoa;

public final class GD_TongQuan extends ManHinhCoSo {
    private final NhanSuBUS nhanSuBUS;
    private final DanhMucBUS danhMucBUS;
    private final JLabel danhSachNhanVien = new JLabel("—");
    private final JLabel danhSachDichVu = new JLabel("—");
    private final JLabel danhSachHangHoa = new JLabel("—");

    public GD_TongQuan(NhanSuBUS nhanSuBUS, DanhMucBUS danhMucBUS, BoXuLyTacVuGiaoDien boXuLy) {
        super("Tổng quan quản lý", "", boXuLy);
        this.nhanSuBUS = nhanSuBUS;
        this.danhMucBUS = danhMucBUS;
        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.add(taoTheThongKe("NHÂN VIÊN", danhSachNhanVien));
        cards.add(taoTheThongKe("DỊCH VỤ", danhSachDichVu));
        cards.add(taoTheThongKe("HÀNG CẦN BỔ SUNG", danhSachHangHoa));
        noiDung.add(cards, BorderLayout.NORTH);
        noiDung.add(taoThanhThaoTac(taoNut("Cập nhật số liệu", this::lamMoi)), BorderLayout.SOUTH);
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel giaTri) {
        JPanel taoTheThongKe = new JPanel(new BorderLayout(0, 12));
        taoTheThongKe.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 226, 232)),
                        BorderFactory.createEmptyBorder(22, 22, 22, 22)));
        taoTheThongKe.setBackground(Color.WHITE);
        taoTheThongKe.add(new JLabel(tieuDe), BorderLayout.NORTH);
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 38));
        giaTri.setForeground(new Color(13, 132, 124));
        taoTheThongKe.add(giaTri, BorderLayout.CENTER);
        return taoTheThongKe;
    }

    @Override
    public void lamMoi() {
        boXuLy.thucHien(this,
                () -> new long[] { nhanSuBUS.layDanhSachNhanVien().size(), danhMucBUS.danhSachDichVu().size(),
                        danhMucBUS.danhSachHangHoa().stream().filter(HangHoa::canBoSung).count() },
                cacSoLuong -> {
                    danhSachNhanVien.setText(Long.toString(cacSoLuong[0]));
                    danhSachDichVu.setText(Long.toString(cacSoLuong[1]));
                    danhSachHangHoa.setText(Long.toString(cacSoLuong[2]));
                    trangThai.setText("Số liệu đã cập nhật.");
                });
    }
}
