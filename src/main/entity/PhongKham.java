package entity;

/** Phòng nhận bệnh; trạng thái nhận bệnh độc lập với việc phòng có người đang khám. */
public record PhongKham(String maPhong, String tenPhong, String maBacSi, String tenBacSi,
        boolean dangNhanBenh) {
    @Override
    public String toString() {
        return tenPhong + (dangNhanBenh ? " — Đang nhận" : " — Tạm ngưng");
    }
}
