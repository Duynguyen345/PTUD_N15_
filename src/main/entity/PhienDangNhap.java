package entity;

/** Thông tin phiên do DangNhapBUS tạo sau khi xác thực thành công. */
public class PhienDangNhap {
    private final String maNhanVien;
    private final String hoTen;
    private final ChucVu chucVu;

    public PhienDangNhap(String maNhanVien, String hoTen, ChucVu chucVu) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    @Override
    public String toString() {
        return hoTen + " (" + chucVu + ")";
    }
}
