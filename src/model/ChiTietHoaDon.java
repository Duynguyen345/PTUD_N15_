
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Đại diện cho một dòng chi tiết của hóa đơn.
 *
 * Mỗi chi tiết ghi nhận dịch vụ, số lượng và đơn giá
 * tại thời điểm lập hóa đơn.
 *
 * Thành tiền được tính từ số lượng và đơn giá,
 * không lưu dưới dạng thuộc tính độc lập.
 */
public class ChiTietHoaDon {

    private HoaDon hoaDon;
    private DichVu dichVu;
    private int soLuong;
    private double donGia;

    /**
     * Khởi tạo chi tiết hóa đơn chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public ChiTietHoaDon() {
    }

    /**
     * Khởi tạo chi tiết hóa đơn với đầy đủ thông tin.
     *
     * @param hoaDon hóa đơn chứa chi tiết
     * @param dichVu dịch vụ được tính tiền
     * @param soLuong số lượng dịch vụ
     * @param donGia đơn giá tại thời điểm lập hóa đơn
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public ChiTietHoaDon(HoaDon hoaDon, DichVu dichVu,
                         int soLuong, double donGia) {

        setHoaDon(hoaDon);
        setDichVu(dichVu);
        setSoLuong(soLuong);
        setDonGia(donGia);
    }

    /**
     * Sao chép thông tin từ một chi tiết hóa đơn khác.
     *
     * @param original chi tiết hóa đơn nguồn
     * @throws IllegalArgumentException nếu đối tượng nguồn null
     */
    public ChiTietHoaDon(ChiTietHoaDon original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Chi tiết hóa đơn nguồn không được null."
            );
        }

        this.hoaDon = original.hoaDon;
        this.dichVu = original.dichVu;
        this.soLuong = original.soLuong;
        this.donGia = original.donGia;
    }

    /**
     * Trả về hóa đơn chứa chi tiết.
     *
     * @return hóa đơn liên kết
     */
    public HoaDon getHoaDon() {
        return hoaDon;
    }

    /**
     * Thiết lập hóa đơn cho chi tiết.
     *
     * @param hoaDon hóa đơn chưa thanh toán
     * @throws IllegalArgumentException nếu hóa đơn không hợp lệ
     */
    public void setHoaDon(HoaDon hoaDon) {

        if (hoaDon == null || hoaDon.getDaThanhToan()) {
            throw new IllegalArgumentException(
                    "Không thể sửa chi tiết của hóa đơn đã thanh toán."
            );
        }

        this.hoaDon = hoaDon;
    }

    /**
     * Trả về dịch vụ được tính tiền.
     *
     * @return dịch vụ của chi tiết hóa đơn
     */
    public DichVu getDichVu() {
        return dichVu;
    }

    /**
     * Thiết lập dịch vụ cho chi tiết hóa đơn.
     *
     * Không yêu cầu dịch vụ đang phục vụ, nhằm giữ
     * được dữ liệu hóa đơn lịch sử.
     *
     * @param dichVu dịch vụ được tính tiền
     * @throws IllegalArgumentException nếu dịch vụ null
     */
    public void setDichVu(DichVu dichVu) {

        if (dichVu == null) {
            throw new IllegalArgumentException(
                    "Dịch vụ của chi tiết hóa đơn không hợp lệ."
            );
        }

        this.dichVu = dichVu;
    }

    /**
     * Trả về số lượng dịch vụ.
     *
     * @return số lượng dịch vụ
     */
    public int getSoLuong() {
        return soLuong;
    }

    /**
     * Cập nhật số lượng dịch vụ.
     *
     * @param soLuong số lượng mới, phải lớn hơn 0
     * @throws IllegalArgumentException nếu số lượng không hợp lệ
     */
    public void setSoLuong(int soLuong) {

        if (soLuong <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng dịch vụ phải lớn hơn 0."
            );
        }

        this.soLuong = soLuong;
    }

    /**
     * Trả về đơn giá tại thời điểm lập hóa đơn.
     *
     * @return đơn giá dịch vụ trước thuế
     */
    public double getDonGia() {
        return donGia;
    }

    /**
     * Cập nhật đơn giá của chi tiết hóa đơn.
     *
     * @param donGia đơn giá mới, hữu hạn và không âm
     * @throws IllegalArgumentException nếu đơn giá không hợp lệ
     */
    public void setDonGia(double donGia) {

        if (!Double.isFinite(donGia) || donGia < 0) {
            throw new IllegalArgumentException(
                    "Đơn giá hóa đơn không hợp lệ."
            );
        }

        this.donGia = donGia;
    }

    /**
     * Trả về thành tiền được tính từ số lượng và đơn giá.
     *
     * @return thành tiền đã làm tròn đến đơn vị VND
     */
    public double getThanhTien() {
        return tinhThanhTien();
    }

    /**
     * Tính thành tiền trước thuế.
     *
     * Sử dụng BigDecimal để thực hiện phép nhân,
     * làm tròn HALF_UP đến đơn vị đồng.
     *
     * @return thành tiền trước thuế
     * @throws IllegalArgumentException nếu dữ liệu tính tiền
     *                                  không hợp lệ hoặc vượt miền double
     */
    public double tinhThanhTien() {

        if (soLuong <= 0
                || !Double.isFinite(donGia)
                || donGia < 0) {
            throw new IllegalArgumentException(
                    "Dữ liệu tính thành tiền không hợp lệ."
            );
        }

        BigDecimal thanhTien = BigDecimal.valueOf(donGia)
                .multiply(BigDecimal.valueOf(soLuong))
                .setScale(0, RoundingMode.HALF_UP);

        double ketQua = thanhTien.doubleValue();

        if (!Double.isFinite(ketQua)) {
            throw new IllegalArgumentException(
                    "Thành tiền vượt phạm vi cho phép."
            );
        }

        return ketQua;
    }

    /**
     * Biểu diễn chi tiết hóa đơn dưới dạng chuỗi.
     *
     * Dữ liệu tham chiếu chỉ hiển thị mã định danh,
     * tránh in lồng toàn bộ đối tượng liên kết.
     *
     * @return chuỗi mô tả chi tiết hóa đơn
     */
    @Override
    public String toString() {

        return "ChiTietHoaDon [maHoaDon="
                + (hoaDon == null
                ? null : hoaDon.getMaHoaDon())
                + ", maDichVu="
                + (dichVu == null
                ? null : dichVu.getMaDichVu())
                + ", soLuong=" + soLuong
                + ", donGia=" + donGia
                + ", thanhTien="
                + (soLuong > 0
                && Double.isFinite(donGia)
                && donGia >= 0
                ? tinhThanhTien()
                : "Chua hop le")
                + "]";
    }
}
