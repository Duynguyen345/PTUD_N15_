package model;

public class NhanVien {

    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private ChucVu chucVu;

    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String hoTen,
                    String soDienThoai, ChucVu chucVu) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.chucVu = chucVu;
    }

    public NhanVien(NhanVien other) {
        this.maNhanVien = other.maNhanVien;
        this.hoTen = other.hoTen;
        this.soDienThoai = other.soDienThoai;
        this.chucVu = other.chucVu;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu;
    }

    @Override
    public String toString() {
        return "NhanVien [maNhanVien=" + maNhanVien
                + ", hoTen=" + hoTen
                + ", soDienThoai=" + soDienThoai
                + ", chucVu=" + chucVu + "]";
    }
}
