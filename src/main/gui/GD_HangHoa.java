package gui;

import java.awt.*;
import javax.swing.*;
import bus.HangHoaBUS;
import entity.HangHoa;

public final class GD_HangHoa extends ManHinhCoSo {
    private final HangHoaBUS nghiepVu;
    private final BangDuLieu<HangHoa> bang;
    private final JTextField ma = new JTextField();
    private final JTextField ten = new JTextField();
    private final JTextField donVi = new JTextField();
    private final JTextField oSoLuong = new JTextField("0");
    private final JTextField toiThieu = new JTextField("0");
    private final JTextField oTimKiem = new JTextField(24);

    public GD_HangHoa(HangHoaBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Hàng hóa & tồn kho", "Cảnh báo khi tồn kho nhỏ hơn hoặc bằng ngưỡng tối thiểu.", boXuLy);
        this.nghiepVu = nghiepVu;
        ma.setEditable(false);
        bang = new BangDuLieu<>(
                new String[] { "Mã hàng", "Tên hàng", "Đơn vị", "Tồn kho", "Tối thiểu", "Tình trạng" },
                h -> new Object[] { h.ma(), h.ten(), h.donVi(), h.ton(), h.toiThieu(),
                        h.canBoSung() ? "Cần bổ sung" : "Đủ hàng" });
        bang.ganTimKiem(oTimKiem);
        JPanel thanhCongCu = taoThanhThaoTac(taoNut("Tìm kiếm", this::timKiem),
                taoNut("Xóa tìm kiếm", this::lamMoi));
        thanhCongCu.add(new JLabel("Từ khóa:"));
        thanhCongCu.add(oTimKiem);
        oTimKiem.addActionListener(suKien -> timKiem());
        noiDung.add(thanhCongCu, BorderLayout.NORTH);
        noiDung.add(bang, BorderLayout.CENTER);
        JPanel vungPhai = new JPanel(new BorderLayout(0, 16));
        vungPhai.add(taoBieuMau(new String[] { "Mã tự động", "Tên hàng *", "Đơn vị tính *", "Số lượng tồn *",
                "Mức tối thiểu *" }, ma, ten, donVi, oSoLuong, toiThieu), BorderLayout.NORTH);
        vungPhai.add(taoThanhThaoTac(taoNut("Thêm", () -> luu(true)), taoNut("Cập nhật", () -> luu(false)),
                taoNut("Làm mới", this::xoaBieuMau)), BorderLayout.SOUTH);
        noiDung.add(vungPhai, BorderLayout.EAST);
        bang.khiChonDong(h -> {
            ma.setText(h.ma());
            ten.setText(h.ten());
            donVi.setText(h.donVi());
            oSoLuong.setText(Integer.toString(h.ton()));
            toiThieu.setText(Integer.toString(h.toiThieu()));
        });
    }

    private void luu(boolean themMoi) {
        String khoa = themMoi ? null : bang.layDongDangChon().ma();
        int soLuong, nguongToiThieu;
        try {
            soLuong = Integer.parseInt(oSoLuong.getText().trim());
            nguongToiThieu = Integer.parseInt(toiThieu.getText().trim());
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Số lượng và ngưỡng phải là số nguyên trong phạm vi int.");
        }
        HangHoa giaTri = new HangHoa(khoa, ten.getText(), donVi.getText(), soLuong, nguongToiThieu);
        boXuLy.thucHien(this, () -> {
            nghiepVu.luuHangHoa(giaTri, themMoi);
            return true;
        }, ok -> {
            lamMoi();
            trangThai.setText("Đã lưu hàng hóa. Nhập từ khóa để tìm lại.");
        });
    }

    private void xoaBieuMau() {
        ma.setText("");
        ten.setText("");
        donVi.setText("");
        oSoLuong.setText("0");
        toiThieu.setText("0");
        bang.boChonDong();
    }

    private void timKiem() {
        bang.capNhatDuLieu(java.util.List.of());
        xoaBieuMau();
        if (oTimKiem.getText().isBlank()) {
            trangThai.setText("Nhập từ khóa trước khi tìm kiếm.");
            return;
        }
        boXuLy.thucHien(this, nghiepVu::danhSachHangHoa, cacDong -> {
            bang.capNhatDuLieu(cacDong);
            trangThai.setText(bang.soDongHienThi() + " kết quả tìm kiếm.");
        });
    }

    @Override
    public void lamMoi() {
        oTimKiem.setText("");
        bang.capNhatDuLieu(java.util.List.of());
        xoaBieuMau();
        trangThai.setText("Chưa tìm kiếm. Nhập từ khóa rồi bấm Tìm kiếm hoặc Enter.");
    }
}
