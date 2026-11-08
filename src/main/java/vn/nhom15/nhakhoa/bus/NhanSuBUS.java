package vn.nhom15.nhakhoa.bus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

public final class NhanSuBUS {
    private final PhongKhamDAO phongKhamDAO;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final BoBamMatKhau boBamMatKhau;

    public NhanSuBUS(PhongKhamDAO phongKhamDAO, QuanLyPhienDangNhap quanLyPhien, BoBamMatKhau boBamMatKhau) {
        this.phongKhamDAO = phongKhamDAO;
        this.quanLyPhien = quanLyPhien;
        this.boBamMatKhau = boBamMatKhau;
    }

    public List<NhanVien> layDanhSachNhanVien() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return phongKhamDAO.danhSachNhanVien();
    }

    public List<TaiKhoan> danhSachTaiKhoan() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return phongKhamDAO.danhSachTaiKhoan();
    }

    public void luuNhanVien(NhanVien nhanVien, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
        nhanVien.setHoTen(nhanVien.getHoTen().trim());
        nhanVien.setSoDienThoai(nhanVien.getSoDienThoai().trim());
        nhanVien.setCccd(nhanVien.getCccd().trim());
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
        phongKhamDAO.luuNhanVien(nhanVien, themMoi);
    }

    public void xoaNhanVien(String ma) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (ma.equals(quanLyPhien.phienHienTai().getMaNhanVien()))
            throw new IllegalArgumentException("Không thể xóa chính mình.");
        phongKhamDAO.xoaNhanVien(ma);
    }

    public String taoTaiKhoan(String maNhanVien, char[] matKhau) {
        try {
            quanLyPhien.kiemTraQuyenQuanLy();
            if (matKhau.length < 6 || matKhau.length > 128)
                throw new IllegalArgumentException("Mật khẩu cần từ 6 đến 128 ký tự.");
            if (phongKhamDAO.danhSachNhanVien().stream()
                    .noneMatch(n -> n.getMaNhanVien().equals(maNhanVien) && n.isConLamViec()))
                throw new IllegalArgumentException("Chọn nhân viên đang làm việc.");
            return phongKhamDAO.taoTaiKhoan(maNhanVien, boBamMatKhau.bamMatKhau(matKhau));
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    public void doiTrangThaiTaiKhoan(TaiKhoan taiKhoan) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (taiKhoan.getMaNhanVien().equals(quanLyPhien.phienHienTai().getMaNhanVien()))
            throw new IllegalArgumentException("Không thể khóa tài khoản đang sử dụng.");
        TaiKhoan moiNhat = phongKhamDAO.timTaiKhoan(taiKhoan.getMaTaiKhoan());
        if (moiNhat == null)
            throw new IllegalArgumentException("Tài khoản không còn tồn tại.");
        phongKhamDAO.capNhatTrangThaiTaiKhoan(moiNhat.getMaTaiKhoan(),
                moiNhat.getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG ? TrangThaiTaiKhoan.BI_KHOA
                        : TrangThaiTaiKhoan.HOAT_DONG);
    }
}
