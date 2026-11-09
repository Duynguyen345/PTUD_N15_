package dao;

public final class NgoaiLeTruyCapDuLieu extends RuntimeException {
    public NgoaiLeTruyCapDuLieu(String thongBao, Throwable nguyenNhan) {
        super(thongBao, nguyenNhan);
    }
}
