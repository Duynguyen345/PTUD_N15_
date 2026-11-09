package bus;

import java.math.BigDecimal;
import java.util.List;
import dao.DichVuDAO;
import entity.*;

public final class DichVuBUS {
    private final DichVuDAO dichVuDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    public DichVuBUS(DichVuDAO dichVuDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.dichVuDAO = dichVuDAO;
        this.quanLyPhien = quanLyPhien;
    }

    public List<DichVu> danhSachDichVu() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return dichVuDAO.danhSachDichVu();
    }

    public void luuDichVu(DichVu dichVu, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (dichVu == null || dichVu.getTenDichVu() == null)
            throw new IllegalArgumentException("Nhập tên dịch vụ.");
        if (!themMoi && (dichVu.getMaDichVu() == null || dichVu.getMaDichVu().isBlank()))
            throw new IllegalArgumentException("Chọn dịch vụ cần cập nhật.");
        dichVu.setTenDichVu(dichVu.getTenDichVu().trim());
        if (dichVu.getTenDichVu().isEmpty() || dichVu.getTenDichVu().length() > 100)
            throw new IllegalArgumentException("Tên dịch vụ cần từ 1 đến 100 ký tự.");
        BigDecimal donGia = dichVu.getDonGiaNiemYet();
        if (donGia == null || donGia.signum() <= 0 || donGia.stripTrailingZeros().scale() > 2
                || donGia.compareTo(new BigDecimal("9999999999999999.99")) > 0)
            throw new IllegalArgumentException(
                    "Đơn giá phải lớn hơn 0, tối đa 2 chữ số thập phân và nằm trong decimal(18,2).");
        if (themMoi)
            dichVu.setTrangThai(TrangThaiDichVu.DANG_PHUC_VU);
        if (dichVu.getTrangThai() == null)
            throw new IllegalArgumentException("Chọn trạng thái dịch vụ.");
        dichVuDAO.luuDichVu(dichVu, themMoi);
    }
}
