
package model;

/**
 * Đại diện cho hồ sơ bệnh án của một bệnh nhân.
 *
 * Mỗi bệnh nhân có tối đa một hồ sơ bệnh án.
 * Việc cấp mã và kiểm tra tính duy nhất được xử lý
 * trong quy trình nghiệp vụ và tại cơ sở dữ liệu.
 */
public class HoSoBenhAn {

    // Thuộc tính
    private String maHoSo;
    private BenhNhan benhNhan;
    private String tienSuBenhLy;
    private String ghiChuChuyenMon;

    /**
     * Khởi tạo đối tượng hồ sơ bệnh án chưa có dữ liệu.
     * Các trường bắt buộc phải hợp lệ trước khi lưu.
     */
    public HoSoBenhAn() {
    }

    /**
     * Khởi tạo hồ sơ bệnh án mới, chưa được cấp mã.
     *
     * @param benhNhan bệnh nhân sở hữu hồ sơ
     * @param tienSuBenhLy tiền sử bệnh lý
     * @param ghiChuChuyenMon ghi chú chuyên môn
     * @throws IllegalArgumentException nếu bệnh nhân không hợp lệ
     */
    public HoSoBenhAn(BenhNhan benhNhan,
                      String tienSuBenhLy,
                      String ghiChuChuyenMon) {

        setBenhNhan(benhNhan);
        setTienSuBenhLy(tienSuBenhLy);
        setGhiChuChuyenMon(ghiChuChuyenMon);
    }

    /**
     * Sao chép thông tin từ hồ sơ bệnh án khác.
     *
     * @param original hồ sơ nguồn
     * @throws IllegalArgumentException nếu hồ sơ nguồn null
     */
    public HoSoBenhAn(HoSoBenhAn original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Hồ sơ bệnh án nguồn không được null."
            );
        }

        this.maHoSo = original.maHoSo;
        this.benhNhan = original.benhNhan;
        this.tienSuBenhLy = original.tienSuBenhLy;
        this.ghiChuChuyenMon = original.ghiChuChuyenMon;
    }

    // Getter và Setter

    public String getMaHoSo() {
        return maHoSo;
    }

    public BenhNhan getBenhNhan() {
        return benhNhan;
    }

    /**
     * Thiết lập bệnh nhân sở hữu hồ sơ.
     *
     * @param benhNhan bệnh nhân cần liên kết
     * @throws IllegalArgumentException nếu bệnh nhân null
     */
    public void setBenhNhan(BenhNhan benhNhan) {

        if (benhNhan == null) {
            throw new IllegalArgumentException(
                    "Bệnh nhân không hợp lệ."
            );
        }

        this.benhNhan = benhNhan;
    }

    public String getTienSuBenhLy() {
        return tienSuBenhLy;
    }

    /**
     * Cập nhật tiền sử bệnh lý.
     * Cho phép null hoặc chuỗi rỗng khi chưa khai thác.
     *
     * @param tienSuBenhLy thông tin tiền sử bệnh lý
     */
    public void setTienSuBenhLy(String tienSuBenhLy) {

        this.tienSuBenhLy = tienSuBenhLy == null
                ? null
                : tienSuBenhLy.trim();
    }

    public String getGhiChuChuyenMon() {
        return ghiChuChuyenMon;
    }

    /**
     * Cập nhật ghi chú chuyên môn.
     *
     * Quyền chỉnh sửa phải được xác minh tại BUS
     * trước khi gọi phương thức này.
     *
     * @param ghiChuChuyenMon nội dung ghi chú
     */
    public void setGhiChuChuyenMon(String ghiChuChuyenMon) {
        this.ghiChuChuyenMon = ghiChuChuyenMon;
    }

    /**
     * Biểu diễn thông tin hồ sơ bệnh án.
     * Chỉ hiển thị mã bệnh nhân để tránh in toàn bộ
     * thông tin đối tượng tham chiếu.
     *
     * @return chuỗi mô tả hồ sơ bệnh án
     */
    @Override
    public String toString() {
        return "HoSoBenhAn [maHoSo=" + maHoSo
                + ", maBenhNhan="
                + (benhNhan == null
                ? null
                : benhNhan.getMaBenhNhan())
                + ", tienSuBenhLy=" + tienSuBenhLy
                + ", ghiChuChuyenMon=" + ghiChuChuyenMon
                + "]";
    }
}
