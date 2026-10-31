
package model;

/**
 * Đại diện cho một phòng khám trong hệ thống quản lý
 * phòng khám nha khoa.
 *
 * Quản lý thông tin phòng và trạng thái tiếp nhận bệnh nhân.
 * Việc cấp mã phòng, kiểm tra trùng tên và phân quyền cập nhật
 * được xử lý tại tầng nghiệp vụ và cơ sở dữ liệu.
 */
public class PhongKham {

    // Thuộc tính
    private String maPhong;
    private String tenPhong;
    private boolean dangNhanBenh;

    /**
     * Khởi tạo phòng khám chưa có đầy đủ thông tin.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public PhongKham() {
    }

    /**
     * Khởi tạo phòng khám mới, chưa được cấp mã.
     *
     * @param tenPhong tên phòng khám
     * @param dangNhanBenh trạng thái tiếp nhận bệnh nhân
     * @throws IllegalArgumentException nếu tên phòng không hợp lệ
     */
    public PhongKham(String tenPhong, boolean dangNhanBenh) {
        setTenPhong(tenPhong);
        setDangNhanBenh(dangNhanBenh);
    }

    /**
     * Sao chép thông tin từ một phòng khám khác.
     *
     * @param original phòng khám nguồn
     * @throws IllegalArgumentException nếu phòng khám nguồn null
     */
    public PhongKham(PhongKham original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Phòng khám nguồn không được null."
            );
        }

        this.maPhong = original.maPhong;
        this.tenPhong = original.tenPhong;
        this.dangNhanBenh = original.dangNhanBenh;
    }

    /**
     * Trả về mã định danh của phòng khám.
     *
     * @return mã phòng khám
     */
    public String getMaPhong() {
        return maPhong;
    }

    /**
     * Trả về tên phòng khám.
     *
     * @return tên phòng khám
     */
    public String getTenPhong() {
        return tenPhong;
    }

    /**
     * Cập nhật tên phòng khám.
     *
     * Loại bỏ khoảng trắng thừa trước khi lưu.
     * Việc kiểm tra tên trùng được thực hiện tại BUS/DAO.
     *
     * @param tenPhong tên phòng khám mới
     * @throws IllegalArgumentException nếu tên null hoặc rỗng
     */
    public void setTenPhong(String tenPhong) {
        if (tenPhong == null || tenPhong.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên phòng khám không hợp lệ hoặc trùng."
            );
        }

        this.tenPhong = tenPhong.trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Trả về trạng thái tiếp nhận bệnh nhân.
     *
     * @return true nếu còn nhận bệnh, false nếu tạm ngừng
     */
    public boolean getDangNhanBenh() {
        return dangNhanBenh;
    }

    /**
     * Cập nhật trạng thái tiếp nhận bệnh nhân.
     *
     * Quyền quản lý phòng phải được kiểm tra ở tầng BUS
     * trước khi cho phép thực hiện cập nhật.
     *
     * @param dangNhanBenh trạng thái tiếp nhận mới
     */
    public void setDangNhanBenh(boolean dangNhanBenh) {
        this.dangNhanBenh = dangNhanBenh;
    }

    /**
     * Kiểm tra phòng khám có thể tiếp nhận bệnh nhân mới không.
     *
     * @return true nếu phòng còn nhận bệnh, ngược lại false
     */
    public boolean coTheTiepNhan() {
        return dangNhanBenh;
    }

    /**
     * Biểu diễn thông tin phòng khám dưới dạng chuỗi.
     *
     * @return chuỗi mô tả phòng khám
     */
    @Override
    public String toString() {
        return "PhongKham [maPhong=" + maPhong
                + ", tenPhong=" + tenPhong
                + ", dangNhanBenh=" + dangNhanBenh
                + "]";
    }
}
