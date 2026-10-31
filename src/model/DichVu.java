
package model;

/**
 * Đại diện cho một dịch vụ trong hệ thống quản lý
 * phòng khám nha khoa.
 *
 * Quản lý tên dịch vụ và trạng thái phục vụ.
 * Mã dịch vụ và tính duy nhất của tên được đảm bảo
 * thông qua tầng nghiệp vụ và cơ sở dữ liệu.
 */
public class DichVu {

    private String maDichVu;
    private String tenDichVu;
    private TrangThaiDichVu trangThai;

    /**
     * Khởi tạo đối tượng dịch vụ chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public DichVu() {
    }

    /**
     * Khởi tạo dịch vụ mới chưa được cấp mã.
     *
     * @param tenDichVu tên dịch vụ
     * @param trangThai trạng thái phục vụ
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public DichVu(String tenDichVu, TrangThaiDichVu trangThai) {
        setTenDichVu(tenDichVu);
        setTrangThai(trangThai);
    }

    /**
     * Sao chép thông tin từ một dịch vụ khác.
     *
     * @param original dịch vụ nguồn
     * @throws IllegalArgumentException nếu dịch vụ nguồn null
     */
    public DichVu(DichVu original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Dịch vụ nguồn không được null."
            );
        }

        this.maDichVu = original.maDichVu;
        this.tenDichVu = original.tenDichVu;
        this.trangThai = original.trangThai;
    }

    /**
     * Trả về mã dịch vụ.
     *
     * @return mã dịch vụ
     */
    public String getMaDichVu() {
        return maDichVu;
    }

    /**
     * Trả về tên dịch vụ.
     *
     * @return tên dịch vụ
     */
    public String getTenDichVu() {
        return tenDichVu;
    }

    /**
     * Cập nhật tên dịch vụ.
     * Chuẩn hóa khoảng trắng trước khi lưu.
     *
     * @param tenDichVu tên dịch vụ mới
     * @throws IllegalArgumentException nếu tên không hợp lệ
     */
    public void setTenDichVu(String tenDichVu) {
        if (tenDichVu == null || tenDichVu.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ không hợp lệ hoặc bị trùng."
            );
        }

        this.tenDichVu = tenDichVu.trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Trả về trạng thái dịch vụ.
     *
     * @return trạng thái phục vụ hiện tại
     */
    public TrangThaiDichVu getTrangThai() {
        return trangThai;
    }

    /**
     * Cập nhật trạng thái phục vụ của dịch vụ.
     *
     * @param trangThai trạng thái mới
     * @throws IllegalArgumentException nếu trạng thái null
     */
    public void setTrangThai(TrangThaiDichVu trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException(
                    "Trạng thái dịch vụ không hợp lệ."
            );
        }

        this.trangThai = trangThai;
    }

    /**
     * Kiểm tra dịch vụ có đang được cung cấp không.
     *
     * @return true nếu đang phục vụ, ngược lại false
     */
    public boolean dangPhucVu() {
        return trangThai == TrangThaiDichVu.DANG_PHUC_VU;
    }

    /**
     * Biểu diễn thông tin dịch vụ dưới dạng chuỗi.
     *
     * @return chuỗi mô tả dịch vụ
     */
    @Override
    public String toString() {
        return "DichVu [maDichVu=" + maDichVu
                + ", tenDichVu=" + tenDichVu
                + ", trangThai=" + trangThai + "]";
    }
}
