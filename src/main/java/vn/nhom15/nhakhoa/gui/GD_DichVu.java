package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.DanhMucBUS;
import vn.nhom15.nhakhoa.entity.*;

public final class GD_DichVu extends ManHinhCoSo {
    private final DanhMucBUS nghiepVu;
    private final BangDuLieu<DichVu> bang;
    private final JTextField ma = new JTextField();
    private final JTextField ten = new JTextField();
    private final JTextField donGia = new JTextField();
    private final JComboBox<TrangThaiDichVu> oTrangThai = new JComboBox<>(TrangThaiDichVu.values());
    private final JTextField oTimKiem = new JTextField(24);

    public GD_DichVu(DanhMucBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Danh mục dịch vụ", "Giá niêm yết bằng VND. Ngừng phục vụ thay cho xóa dịch vụ.", boXuLy);
        this.nghiepVu = nghiepVu;
        ma.setEditable(false);
        donGia.setToolTipText("Ví dụ: 250000 hoặc 250000.50; không nhập dấu phân cách hàng nghìn.");
        bang = new BangDuLieu<>(new String[] { "Mã DV", "Tên dịch vụ", "Giá niêm yết (VND)", "Trạng thái" },
                d -> new Object[] { d.getMaDichVu(), d.getTenDichVu(), d.getDonGiaNiemYet(),
                        d.getTrangThai() });
        bang.ganTimKiem(oTimKiem);
        JPanel thanhCongCu = taoThanhThaoTac(taoNut("Tìm kiếm", this::timKiem),
                taoNut("Xóa tìm kiếm", this::lamMoi));
        thanhCongCu.add(new JLabel("Từ khóa:"));
        thanhCongCu.add(oTimKiem);
        oTimKiem.addActionListener(suKien -> timKiem());
        noiDung.add(thanhCongCu, BorderLayout.NORTH);
        noiDung.add(bang, BorderLayout.CENTER);
        JPanel vungPhai = new JPanel(new BorderLayout(0, 16));
        vungPhai.add(taoBieuMau(new String[] { "Mã tự động", "Tên dịch vụ *", "Đơn giá *", "Trạng thái" }, ma,
                ten, donGia, oTrangThai), BorderLayout.NORTH);
        vungPhai.add(taoThanhThaoTac(taoNut("Thêm", () -> luu(true)), taoNut("Cập nhật", () -> luu(false)),
                taoNut("Làm mới", this::xoaBieuMau)), BorderLayout.SOUTH);
        noiDung.add(vungPhai, BorderLayout.EAST);
        bang.khiChonDong(d -> {
            ma.setText(d.getMaDichVu());
            ten.setText(d.getTenDichVu());
            donGia.setText(d.getDonGiaNiemYet().toPlainString());
            oTrangThai.setSelectedItem(d.getTrangThai());
        });
    }

    private void luu(boolean themMoi) {
        String khoa = themMoi ? null : bang.layDongDangChon().getMaDichVu();
        BigDecimal soTien;
        try {
            soTien = new BigDecimal(donGia.getText().trim());
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Đơn giá phải là số, ví dụ 250000.");
        }
        DichVu giaTri = new DichVu(khoa, ten.getText(), soTien,
                (TrangThaiDichVu) oTrangThai.getSelectedItem());
        boXuLy.thucHien(this, () -> {
            nghiepVu.luuDichVu(giaTri, themMoi);
            return true;
        }, ok -> {
            lamMoi();
            trangThai.setText("Đã lưu dịch vụ. Nhập từ khóa để tìm lại.");
        });
    }

    private void xoaBieuMau() {
        ma.setText("");
        ten.setText("");
        donGia.setText("");
        oTrangThai.setSelectedIndex(0);
        bang.boChonDong();
    }

    private void timKiem() {
        bang.capNhatDuLieu(java.util.List.of());
        xoaBieuMau();
        if (oTimKiem.getText().isBlank()) {
            trangThai.setText("Nhập từ khóa trước khi tìm kiếm.");
            return;
        }
        boXuLy.thucHien(this, nghiepVu::danhSachDichVu, cacDong -> {
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
