package entity;

import java.util.Objects;
import util.MaThongBao;

public class Thuoc {
    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;

    public Thuoc() {
    }

    public Thuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public Thuoc(String maThuoc, String tenThuoc, String donViTinh) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.donViTinh = donViTinh;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        if (this.maThuoc != null && !Objects.equals(this.maThuoc, maThuoc)) {
            throw new IllegalArgumentException(MaThongBao.EN01);
        }
        this.maThuoc = maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Thuoc other = (Thuoc) obj;
        return maThuoc != null && !maThuoc.isBlank() && maThuoc.equals(other.maThuoc);
    }

    @Override
    public int hashCode() {
        // Giữ nguyên bucket khi đối tượng mới được cấp mã sau lúc khởi tạo.
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return maThuoc + " - " + tenThuoc + " (" + donViTinh + ")";
    }
}
