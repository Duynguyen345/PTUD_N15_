package entity;

public enum HinhThucThanhToan {
    TIEN_MAT("Tiền mặt"), CHUYEN_KHOAN("Chuyển khoản");
    private final String nhan;
    HinhThucThanhToan(String nhan) { this.nhan = nhan; }
    @Override public String toString() { return nhan; }
}
