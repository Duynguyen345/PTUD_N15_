package vn.nhom15.nhakhoa.bus;

import java.math.BigDecimal;
import java.util.List;
import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.*;

public final class DanhMucBUS {
    private final PhongKhamDAO phongKhamDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    public DanhMucBUS(PhongKhamDAO phongKhamDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.phongKhamDAO = phongKhamDAO;
        this.quanLyPhien = quanLyPhien;
    }

    public List<DichVu> danhSachDichVu() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return phongKhamDAO.danhSachDichVu();
    }

    public List<HangHoa> danhSachHangHoa() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return phongKhamDAO.danhSachHangHoa();
    }

    public void luuDichVu(DichVu dichVu, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
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
        phongKhamDAO.luuDichVu(dichVu, themMoi);
    }

    public void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (hangHoa.ten().isBlank() || hangHoa.ten().trim().length() > 100 || hangHoa.donVi().isBlank()
                || hangHoa.donVi().trim().length() > 30)
            throw new IllegalArgumentException(
                    "Tên hàng tối đa 100 và đơn vị tối đa 30 ký tự, không để trống.");
        if (hangHoa.ton() < 0 || hangHoa.toiThieu() < 0)
            throw new IllegalArgumentException("Tồn kho và ngưỡng tối thiểu phải là số nguyên không âm.");
        phongKhamDAO.luuHangHoa(new HangHoa(hangHoa.ma(), hangHoa.ten().trim(), hangHoa.donVi().trim(),
                hangHoa.ton(), hangHoa.toiThieu()), themMoi);
    }
}
