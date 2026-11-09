package bus;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import dao.TaiKhoanDAO;
import entity.*;
import util.BoBamMatKhau;

public final class TaiKhoanBUS {
    private final TaiKhoanDAO taiKhoanDAO;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final BoBamMatKhau boBamMatKhau;

    public TaiKhoanBUS(TaiKhoanDAO taiKhoanDAO, QuanLyPhienDangNhap quanLyPhien, BoBamMatKhau boBamMatKhau) {
        this.taiKhoanDAO = taiKhoanDAO;
        this.quanLyPhien = quanLyPhien;
        this.boBamMatKhau = boBamMatKhau;
    }

    public List<NhanVien> timNhanVienChuaCoTaiKhoan(String tuKhoa) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (tuKhoa == null || tuKhoa.isBlank())
            return List.of();
        String tuCanTim = tuKhoa.trim().toLowerCase(Locale.ROOT);
        var daCoTaiKhoan = taiKhoanDAO.danhSachTaiKhoan().stream()
                .map(TaiKhoan::getMaNhanVien).collect(Collectors.toSet());
        return taiKhoanDAO.danhSachNhanVien().stream()
                .filter(NhanVien::isConLamViec)
                .filter(n -> !daCoTaiKhoan.contains(n.getMaNhanVien()))
                .filter(n -> n.getMaNhanVien().toLowerCase(Locale.ROOT).contains(tuCanTim)
                        || n.getHoTen().toLowerCase(Locale.ROOT).contains(tuCanTim))
                .toList();
    }

    public List<TaiKhoan> danhSachTaiKhoan() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return taiKhoanDAO.danhSachTaiKhoan();
    }

    public String taoTaiKhoan(String maNhanVien, char[] matKhau) {
        try {
            quanLyPhien.kiemTraQuyenQuanLy();
            if (matKhau == null || matKhau.length < 6 || matKhau.length > 128)
                throw new IllegalArgumentException("Mật khẩu cần từ 6 đến 128 ký tự.");
            if (taiKhoanDAO.danhSachNhanVien().stream()
                    .noneMatch(n -> n.getMaNhanVien().equals(maNhanVien) && n.isConLamViec()))
                throw new IllegalArgumentException("Chọn nhân viên đang làm việc.");
            return taiKhoanDAO.taoTaiKhoan(maNhanVien, boBamMatKhau.bamMatKhau(matKhau));
        } finally {
            if (matKhau != null)
                Arrays.fill(matKhau, '\0');
        }
    }

    public void doiTrangThaiTaiKhoan(TaiKhoan taiKhoan) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (taiKhoan == null || taiKhoan.getMaTaiKhoan() == null)
            throw new IllegalArgumentException("Chọn tài khoản cần đổi trạng thái.");
        TaiKhoan moiNhat = taiKhoanDAO.timTaiKhoan(taiKhoan.getMaTaiKhoan());
        if (moiNhat == null)
            throw new IllegalArgumentException("Tài khoản không còn tồn tại.");
        if (moiNhat.getMaNhanVien().equals(quanLyPhien.phienHienTai().getMaNhanVien()))
            throw new IllegalArgumentException("Không thể khóa tài khoản đang sử dụng.");
        taiKhoanDAO.capNhatTrangThaiTaiKhoan(moiNhat.getMaTaiKhoan(),
                moiNhat.getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG ? TrangThaiTaiKhoan.BI_KHOA
                        : TrangThaiTaiKhoan.HOAT_DONG);
    }
}
