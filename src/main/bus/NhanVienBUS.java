package bus;

import java.time.LocalDate;
import java.util.List;
import dao.NhanVienDAO;
import entity.*;

public final class NhanVienBUS {
    private final NhanVienDAO nhanVienDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    public NhanVienBUS(NhanVienDAO nhanVienDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.nhanVienDAO = nhanVienDAO;
        this.quanLyPhien = quanLyPhien;
    }

    public List<NhanVien> layDanhSachNhanVien() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return nhanVienDAO.danhSachNhanVien();
    }

    public void luuNhanVien(NhanVien nhanVien, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (nhanVien == null || nhanVien.getHoTen() == null || nhanVien.getSoDienThoai() == null)
            throw new IllegalArgumentException("Nhập đầy đủ họ tên và số điện thoại nhân viên.");
        if (!themMoi && (nhanVien.getMaNhanVien() == null || nhanVien.getMaNhanVien().isBlank()))
            throw new IllegalArgumentException("Chọn nhân viên cần cập nhật.");
        nhanVien.setHoTen(nhanVien.getHoTen().trim());
        nhanVien.setSoDienThoai(nhanVien.getSoDienThoai().trim());
        nhanVien.setCccd(nhanVien.getCccd() == null ? "" : nhanVien.getCccd().trim());
        if (nhanVien.getHoTen().isEmpty() || nhanVien.getHoTen().length() > 100)
            throw new IllegalArgumentException("Họ tên cần từ 1 đến 100 ký tự.");
        if (!nhanVien.getSoDienThoai().matches("0\\d{9}"))
            throw new IllegalArgumentException("Số điện thoại gồm 10 số, bắt đầu bằng 0.");
        if (!nhanVien.getCccd().isEmpty() && !nhanVien.getCccd().matches("\\d{12}"))
            throw new IllegalArgumentException("CCCD phải gồm 12 số hoặc để trống.");
        if (nhanVien.getNgaySinh() == null || nhanVien.getNgaySinh().isAfter(LocalDate.now().minusYears(18)))
            throw new IllegalArgumentException("Nhân viên phải đủ 18 tuổi.");
        if (nhanVien.getChucVu() == null)
            throw new IllegalArgumentException("Chọn chức vụ.");
        if (!themMoi && nhanVien.getMaNhanVien().equals(quanLyPhien.phienHienTai().getMaNhanVien())
                && (!nhanVien.isConLamViec() || nhanVien.getChucVu() != ChucVu.QUAN_LY))
            throw new IllegalArgumentException("Không thể tự ngừng làm việc hoặc hạ quyền của mình.");
        nhanVienDAO.luuNhanVien(nhanVien, themMoi);
    }

    public void xoaNhanVien(String ma) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (ma == null || ma.isBlank())
            throw new IllegalArgumentException("Chọn nhân viên cần xóa.");
        if (ma.equals(quanLyPhien.phienHienTai().getMaNhanVien()))
            throw new IllegalArgumentException("Không thể xóa chính mình.");
        nhanVienDAO.xoaNhanVien(ma);
    }
}
