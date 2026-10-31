
package model;

/**
 * Đại diện cho một dòng thuốc đã cấp thực tế
 * trong quá trình khám bệnh.
 *
 * Mỗi chi tiết liên kết một phiếu khám với một
 * loại thuốc, lưu số lượng, ghi chú và đơn giá
 * niêm yết tại thời điểm cấp.
 *
 * Việc kiểm tra thuốc và phiếu tồn tại trong CSDL
 * được thực hiện ở tầng BUS/DAO.
 */
public class ChiTietCapThuoc {

    private PhieuKham phieuKham;
    private Thuoc thuoc;
    private int soLuong;
    private String ghiChuCapThuoc;
    private double donGiaNiemYet;

    /**
     * Khởi tạo chi tiết cấp thuốc chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public ChiTietCapThuoc() {
    }

    /**
     * Khởi tạo chi tiết cấp thuốc với đầy đủ thông tin.
     *
     * @param phieuKham phiếu khám đang thực hiện
     * @param thuoc thuốc được cấp
     * @param soLuong số lượng thuốc cấp thực tế
     * @param ghiChuCapThuoc ghi chú khi cấp thuốc
     * @param donGiaNiemYet đơn giá tại thời điểm cấp
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public ChiTietCapThuoc(PhieuKham phieuKham,
                           Thuoc thuoc,
                           int soLuong,
                           String ghiChuCapThuoc,
                           double donGiaNiemYet) {

        setPhieuKham(phieuKham);
        setThuoc(thuoc);
        setSoLuong(soLuong);
        setGhiChuCapThuoc(ghiChuCapThuoc);
        setDonGiaNiemYet(donGiaNiemYet);
    }

    /**
     * Sao chép thông tin từ chi tiết cấp thuốc khác.
     *
     * @param original chi tiết nguồn
     * @throws IllegalArgumentException nếu đối tượng nguồn null
     */
    public ChiTietCapThuoc(ChiTietCapThuoc original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Chi tiết cấp thuốc nguồn không được null."
            );
        }

        this.phieuKham = original.phieuKham;
        this.thuoc = original.thuoc;
        this.soLuong = original.soLuong;
        this.ghiChuCapThuoc = original.ghiChuCapThuoc;
        this.donGiaNiemYet = original.donGiaNiemYet;
    }

    /**
     * Trả về phiếu khám liên kết.
     *
     * @return phiếu khám
     */
    public PhieuKham getPhieuKham() {
        return phieuKham;
    }

    /**
     * Thiết lập phiếu khám khi cấp thuốc.
     *
     * Chỉ được tạo chi tiết nếu phiếu đang DANG_KHAM.
     *
     * @param phieuKham phiếu khám cần liên kết
     * @throws IllegalArgumentException nếu phiếu không hợp lệ
     */
    public void setPhieuKham(PhieuKham phieuKham) {

        if (phieuKham == null
                || phieuKham.getTrangThai()
                != TrangThaiPhieuKham.DANG_KHAM) {

            throw new IllegalArgumentException(
                    "Không thể cấp thuốc cho phiếu khám này."
            );
        }

        this.phieuKham = phieuKham;
    }

    /**
     * Trả về thuốc đã cấp.
     *
     * @return thuốc được liên kết
     */
    public Thuoc getThuoc() {
        return thuoc;
    }

    /**
     * Thiết lập loại thuốc được cấp.
     *
     * @param thuoc thuốc trong danh mục
     * @throws IllegalArgumentException nếu thuốc null
     */
    public void setThuoc(Thuoc thuoc) {

        if (thuoc == null) {
            throw new IllegalArgumentException(
                    "Thuốc cấp không hợp lệ."
            );
        }

        this.thuoc = thuoc;
    }

    /**
     * Trả về số lượng thuốc đã cấp.
     *
     * @return số lượng thuốc
     */
    public int getSoLuong() {
        return soLuong;
    }

    /**
     * Cập nhật số lượng thuốc cấp thực tế.
     *
     * @param soLuong số lượng nguyên dương
     * @throws IllegalArgumentException nếu số lượng không hợp lệ
     */
    public void setSoLuong(int soLuong) {

        if (soLuong <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng thuốc cấp phải lớn hơn 0."
            );
        }

        this.soLuong = soLuong;
    }

    /**
     * Trả về ghi chú cấp thuốc.
     *
     * @return ghi chú, có thể là null
     */
    public String getGhiChuCapThuoc() {
        return ghiChuCapThuoc;
    }

    /**
     * Thiết lập ghi chú cấp thuốc.
     *
     * Cho phép null hoặc rỗng khi không có hướng dẫn thêm.
     *
     * @param ghiChuCapThuoc nội dung ghi chú
     */
    public void setGhiChuCapThuoc(String ghiChuCapThuoc) {

        this.ghiChuCapThuoc = ghiChuCapThuoc == null
                ? null
                : ghiChuCapThuoc.trim();
    }

    /**
     * Trả về đơn giá niêm yết tại thời điểm cấp thuốc.
     *
     * @return đơn giá niêm yết
     */
    public double getDonGiaNiemYet() {
        return donGiaNiemYet;
    }

    /**
     * Thiết lập đơn giá niêm yết của thuốc.
     *
     * @param donGiaNiemYet đơn giá hữu hạn, không âm
     * @throws IllegalArgumentException nếu đơn giá không hợp lệ
     */
    public void setDonGiaNiemYet(double donGiaNiemYet) {

        if (!Double.isFinite(donGiaNiemYet)
                || donGiaNiemYet < 0) {
            throw new IllegalArgumentException(
                    "Đơn giá niêm yết thuốc không hợp lệ."
            );
        }

        this.donGiaNiemYet = donGiaNiemYet;
    }

    /**
     * Biểu diễn thông tin chi tiết cấp thuốc.
     *
     * Các đối tượng tham chiếu chỉ hiển thị mã định danh
     * nhằm tránh in lồng toàn bộ thông tin.
     *
     * @return chuỗi mô tả chi tiết cấp thuốc
     */
    @Override
    public String toString() {

        return "ChiTietCapThuoc [maPhieuKham="
                + (phieuKham == null
                ? null
                : phieuKham.getMaPhieuKham())
                + ", maThuoc="
                + (thuoc == null
                ? null
                : thuoc.getMaThuoc())
                + ", soLuong=" + soLuong
                + ", ghiChuCapThuoc=" + ghiChuCapThuoc
                + ", donGiaNiemYet=" + donGiaNiemYet
                + "]";
    }
}
