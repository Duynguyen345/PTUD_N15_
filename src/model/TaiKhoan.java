
package model;

/**
 * Đại diện cho tài khoản đăng nhập của nhân viên.
 *
 * Mỗi tài khoản liên kết với một nhân viên và có
 * trạng thái hoạt động hoặc bị khóa.
 *
 * Thuộc tính matKhau chỉ lưu chuỗi mật khẩu đã băm.
 */
public class TaiKhoan {

    private String tenDangNhap;
    private String matKhau;
    private TrangThaiTaiKhoan trangThai;
    private NhanVien nhanVien;

    /**
     * Khởi tạo tài khoản chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public TaiKhoan() {
    }

    /**
     * Khởi tạo tài khoản với đầy đủ thông tin.
     *
     * @param tenDangNhap tên đăng nhập
     * @param matKhau chuỗi mật khẩu đã băm
     * @param trangThai trạng thái tài khoản
     * @param nhanVien nhân viên sở hữu tài khoản
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public TaiKhoan(String tenDangNhap, String matKhau,
                    TrangThaiTaiKhoan trangThai,
                    NhanVien nhanVien) {

        setTenDangNhap(tenDangNhap);
        setMatKhau(matKhau);
        setTrangThai(trangThai);
        setNhanVien(nhanVien);
    }

    /**
     * Sao chép thông tin từ tài khoản khác.
     *
     * @param original tài khoản nguồn
     * @throws IllegalArgumentException nếu tài khoản nguồn null
     */
    public TaiKhoan(TaiKhoan original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Tài khoản nguồn không được null."
            );
        }

        this.tenDangNhap = original.tenDangNhap;
        this.matKhau = original.matKhau;
        this.trangThai = original.trangThai;
        this.nhanVien = original.nhanVien;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    /**
     * Thiết lập tên đăng nhập theo định dạng quy định.
     * Không cho phép đổi tên đăng nhập đã được thiết lập.
     *
     * @param tenDangNhap tên đăng nhập cần thiết lập
     */
    public void setTenDangNhap(String tenDangNhap) {

        if (tenDangNhap == null ||
                !tenDangNhap.matches("^[A-Za-z0-9._]{5,30}$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập sai định dạng hoặc đã tồn tại."
            );
        }

        if (this.tenDangNhap != null &&
                !this.tenDangNhap.equals(tenDangNhap)) {
            throw new IllegalArgumentException(
                    "Không được tự ý thay đổi tên đăng nhập."
            );
        }

        this.tenDangNhap = tenDangNhap;
    }

    /**
     * Trả về chuỗi mật khẩu đã băm.
     *
     * Chỉ tầng xác thực được phép sử dụng giá trị này.
     *
     * @return chuỗi hash của mật khẩu
     */
    public String getMatKhau() {
        return matKhau;
    }

    /**
     * Thiết lập mật khẩu đã được băm bằng BCrypt
     * hoặc Argon2id.
     *
     * @param matKhau chuỗi hash
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public void setMatKhau(String matKhau) {
        if (matKhau == null || matKhau.isBlank()) {
            throw new IllegalArgumentException(
                    "Mật khẩu đã băm không hợp lệ."
            );
        }

        this.matKhau = matKhau;
    }
    public TrangThaiTaiKhoan getTrangThai() {
        return trangThai;
    }

    /**
     * Cập nhật trạng thái tài khoản.
     *
     * @param trangThai trạng thái mới
     */
    public void setTrangThai(TrangThaiTaiKhoan trangThai) {

        if (trangThai == null) {
            throw new IllegalArgumentException(
                    "Trạng thái tài khoản không hợp lệ."
            );
        }

        this.trangThai = trangThai;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    /**
     * Thiết lập nhân viên sở hữu tài khoản.
     *
     * @param nhanVien nhân viên được liên kết
     */
    public void setNhanVien(NhanVien nhanVien) {

        if (nhanVien == null) {
            throw new IllegalArgumentException(
                    "Nhân viên không hợp lệ hoặc đã có tài khoản."
            );
        }

        this.nhanVien = nhanVien;
    }

    /**
     * Kiểm tra tài khoản có đang hoạt động không.
     *
     * @return true nếu hoạt động, false nếu bị khóa
     */
    public boolean dangHoatDong() {
        return trangThai == TrangThaiTaiKhoan.HOAT_DONG;
    }

    /**
     * Biểu diễn thông tin tài khoản mà không tiết lộ mật khẩu.
     *
     * @return chuỗi mô tả tài khoản
     */
    @Override
    public String toString() {
        return "TaiKhoan [tenDangNhap=" + tenDangNhap
                + ", trangThai=" + trangThai
                + ", maNhanVien="
                + (nhanVien == null
                ? null
                : nhanVien.getMaNhanVien())
                + "]";
    }
}

