package vn.nhom15.nhakhoa.entity;

/** Các trạng thái điều phối; hoàn tất được cập nhật bởi luồng bác sĩ. */
public enum TrangThaiHangCho {
    CHO_KHAM("Chờ khám"), DANG_GOI("Đang gọi"), DANG_KHAM("Đang khám"),
    VANG_MAT("Vắng mặt"), HOAN_TAT("Hoàn tất");

    private final String nhan;

    TrangThaiHangCho(String nhan) { this.nhan = nhan; }

    @Override
    public String toString() { return nhan; }
}
