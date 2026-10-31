
package model;

/**
 * Đại diện cho một dịch vụ được chỉ định trong phiếu khám.
 *
 * Mỗi chỉ định liên kết một phiếu khám với một dịch vụ.
 * Việc kiểm tra dữ liệu tồn tại trong CSDL được thực hiện
 * tại tầng BUS/DAO.
 */
public class ChiDinhDichVu {

    private PhieuKham phieuKham;
    private DichVu dichVu;

    /**
     * Khởi tạo chỉ định dịch vụ chưa có thông tin.
     * Các thuộc tính phải hợp lệ trước khi lưu.
     */
    public ChiDinhDichVu() {
    }

    /**
     * Khởi tạo chỉ định dịch vụ mới.
     *
     * @param phieuKham phiếu khám đang thực hiện
     * @param dichVu dịch vụ được chỉ định
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public ChiDinhDichVu(PhieuKham phieuKham, DichVu dichVu) {
        setPhieuKham(phieuKham);
        setDichVu(dichVu);
    }

    /**
     * Sao chép thông tin từ một chỉ định dịch vụ khác.
     *
     * @param original chỉ định nguồn
     * @throws IllegalArgumentException nếu đối tượng nguồn null
     */
    public ChiDinhDichVu(ChiDinhDichVu original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Chỉ định dịch vụ nguồn không được null."
            );
        }

        this.phieuKham = original.phieuKham;
        this.dichVu = original.dichVu;
    }

    /**
     * Trả về phiếu khám được chỉ định dịch vụ.
     *
     * @return phiếu khám
     */
    public PhieuKham getPhieuKham() {
        return phieuKham;
    }

    /**
     * Thiết lập phiếu khám cho chỉ định dịch vụ.
     *
     * Phiếu khám phải ở trạng thái DANG_KHAM.
     *
     * @param phieuKham phiếu khám cần liên kết
     * @throws IllegalArgumentException nếu phiếu khám không hợp lệ
     */
    public void setPhieuKham(PhieuKham phieuKham) {
        if (phieuKham == null
                || phieuKham.getTrangThai()
                != TrangThaiPhieuKham.DANG_KHAM) {

            throw new IllegalArgumentException(
                    "Không được chỉ định dịch vụ cho phiếu khám này."
            );
        }

        this.phieuKham = phieuKham;
    }

    /**
     * Trả về dịch vụ được chỉ định.
     *
     * @return dịch vụ
     */
    public DichVu getDichVu() {
        return dichVu;
    }

    /**
     * Thiết lập dịch vụ cho chỉ định.
     *
     * Dịch vụ phải đang trong trạng thái phục vụ.
     *
     * @param dichVu dịch vụ cần chỉ định
     * @throws IllegalArgumentException nếu dịch vụ không hợp lệ
     */
    public void setDichVu(DichVu dichVu) {
        if (dichVu == null || !dichVu.dangPhucVu()) {
            throw new IllegalArgumentException(
                    "Dịch vụ không tồn tại hoặc đã ngừng phục vụ."
            );
        }

        this.dichVu = dichVu;
    }

    /**
     * Biểu diễn thông tin chỉ định dịch vụ.
     *
     * Chỉ sử dụng mã định danh của các đối tượng tham chiếu
     * nhằm tránh hiển thị dữ liệu lồng nhau không cần thiết.
     *
     * @return chuỗi mô tả chỉ định dịch vụ
     */
    @Override
    public String toString() {
        return "ChiDinhDichVu [maPhieuKham="
                + (phieuKham == null
                ? null : phieuKham.getMaPhieuKham())
                + ", maDichVu="
                + (dichVu == null
                ? null : dichVu.getMaDichVu())
                + "]";
    }
}
