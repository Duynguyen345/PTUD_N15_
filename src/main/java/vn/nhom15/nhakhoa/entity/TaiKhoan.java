package vn.nhom15.nhakhoa.entity;

public class TaiKhoan {
    private String maTaiKhoan;
    private String matKhau; // hash SHA-256
    private TrangThaiTaiKhoan trangThai;
    private String maNhanVien;
    private NhanVien nhanVien; // hien thi, khong luu DB

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String matKhau, TrangThaiTaiKhoan trangThai, String maNhanVien) {
        this.maTaiKhoan = maTaiKhoan;
        this.matKhau = matKhau;
        this.trangThai = trangThai;
        this.maNhanVien = maNhanVien;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public TrangThaiTaiKhoan getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiTaiKhoan trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TaiKhoan other))
            return false;
        return java.util.Objects.equals(maTaiKhoan, other.maTaiKhoan);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(maTaiKhoan);
    }
}
