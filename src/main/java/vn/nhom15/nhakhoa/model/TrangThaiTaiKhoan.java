package vn.nhom15.nhakhoa.model;

public enum TrangThaiTaiKhoan {
    HOAT_DONG, BI_KHOA;

    @Override
    public String toString() {
        return this == HOAT_DONG ? "Hoạt động" : "Bị khóa";
    }
}
