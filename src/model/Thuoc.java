
package model;

import java.util.Locale;

/**
 * Đại diện cho một loại thuốc trong danh mục
 * của hệ thống quản lý phòng khám nha khoa.
 *
 * Quản lý tên thuốc và đơn vị tính.
 * Mã thuốc được cấp tại cơ sở dữ liệu và không
 * cho phép thay đổi thông qua setter công khai.
 */
public class Thuoc {

    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;

    /**
     * Khởi tạo đối tượng thuốc chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public Thuoc() {
    }

    /**
     * Khởi tạo thuốc mới chưa được cấp mã.
     *
     * @param tenThuoc tên thuốc
     * @param donViTinh đơn vị tính của thuốc
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public Thuoc(String tenThuoc, String donViTinh) {
        setTenThuoc(tenThuoc);
        setDonViTinh(donViTinh);
    }

    /**
     * Sao chép thông tin từ đối tượng thuốc khác.
     *
     * @param original thuốc nguồn
     * @throws IllegalArgumentException nếu thuốc nguồn null
     */
    public Thuoc(Thuoc original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Thuốc nguồn không được null."
            );
        }

        this.maThuoc = original.maThuoc;
        this.tenThuoc = original.tenThuoc;
        this.donViTinh = original.donViTinh;
    }

    /**
     * Trả về mã định danh của thuốc.
     *
     * @return mã thuốc
     */
    public String getMaThuoc() {
        return maThuoc;
    }

    /**
     * Trả về tên thuốc.
     *
     * @return tên thuốc
     */
    public String getTenThuoc() {
        return tenThuoc;
    }

    /**
     * Thiết lập tên thuốc.
     *
     * Tên thuốc phải chứa ít nhất một chữ cái.
     * Khoảng trắng thừa được chuẩn hóa trước khi lưu.
     *
     * @param tenThuoc tên thuốc cần thiết lập
     * @throws IllegalArgumentException nếu tên không hợp lệ
     */
    public void setTenThuoc(String tenThuoc) {
        if (tenThuoc == null || tenThuoc.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên thuốc không hợp lệ."
            );
        }

        String tenChuanHoa = tenThuoc.trim()
                .replaceAll("\\s+", " ");

        if (tenChuanHoa.codePoints()
                .noneMatch(Character::isLetter)) {
            throw new IllegalArgumentException(
                    "Tên thuốc không hợp lệ."
            );
        }

        this.tenThuoc = tenChuanHoa;
    }

    /**
     * Trả về đơn vị tính của thuốc.
     *
     * @return đơn vị tính
     */
    public String getDonViTinh() {
        return donViTinh;
    }

    /**
     * Thiết lập đơn vị tính của thuốc.
     *
     * Chỉ chấp nhận: viên, ống, lọ, tuýp, gói, chai.
     *
     * @param donViTinh đơn vị tính cần thiết lập
     * @throws IllegalArgumentException nếu đơn vị không hợp lệ
     */
    public void setDonViTinh(String donViTinh) {
        if (donViTinh == null || donViTinh.isBlank()) {
            throw new IllegalArgumentException(
                    "Đơn vị tính thuốc không hợp lệ."
            );
        }

        String donViChuanHoa = donViTinh.trim()
                .toLowerCase(Locale.ROOT);

        switch (donViChuanHoa) {
            case "viên":
            case "ống":
            case "lọ":
            case "tuýp":
            case "gói":
            case "chai":
                this.donViTinh = donViChuanHoa;
                break;

            default:
                throw new IllegalArgumentException(
                        "Đơn vị tính thuốc không hợp lệ."
                );
        }
    }

    /**
     * Biểu diễn thông tin thuốc dưới dạng chuỗi.
     *
     * @return chuỗi mô tả thuốc
     */
    @Override
    public String toString() {
        return "Thuoc [maThuoc=" + maThuoc
                + ", tenThuoc=" + tenThuoc
                + ", donViTinh=" + donViTinh
                + "]";
    }
}
