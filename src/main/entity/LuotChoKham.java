package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Một số thứ tự trong ngày, thuộc một phòng và một phiếu khám. */
public record LuotChoKham(String maPhieuKham, LocalDate ngayKham, String maPhong, int soThuTu,
        String maBenhNhan, String hoTen, boolean daThanhToanPhiKham, TrangThaiHangCho trangThai,
        LocalDateTime thoiDiemGoi) {

    /** Chỉ cho phép chuyển trạng thái đã được đặc tả trong use case điều phối. */
    public LuotChoKham chuyenTrangThai(TrangThaiHangCho dich, LocalDateTime thoiDiem) {
        boolean hopLe = switch (trangThai) {
            case CHO_KHAM -> dich == TrangThaiHangCho.DANG_GOI;
            case DANG_GOI -> dich == TrangThaiHangCho.DANG_GOI || dich == TrangThaiHangCho.DANG_KHAM
                    || dich == TrangThaiHangCho.VANG_MAT;
            case DANG_KHAM, VANG_MAT, HOAN_TAT -> false;
        };
        if (!hopLe) throw new IllegalArgumentException("Lượt khám đã đổi trạng thái. Vui lòng tải lại hàng chờ.");
        if (!daThanhToanPhiKham)
            throw new IllegalArgumentException("Bệnh nhân chưa thanh toán phí khám.");
        return new LuotChoKham(maPhieuKham, ngayKham, maPhong, soThuTu, maBenhNhan, hoTen,
                daThanhToanPhiKham, dich, dich == TrangThaiHangCho.DANG_GOI ? thoiDiem : thoiDiemGoi);
    }
}
