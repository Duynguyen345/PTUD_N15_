
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Đại diện cho hóa đơn thanh toán của bệnh nhân.
 *
 * Quản lý thông tin thu tiền, danh sách chi tiết hóa đơn,
 * thuế và trạng thái thanh toán.
 *
 * Mã hóa đơn được cấp tại cơ sở dữ liệu.
 * Quyền xác nhận thanh toán phải được kiểm tra ở tầng BUS.
 */
public class HoaDon {

    private String maHoaDon;
    private BenhNhan benhNhan;
    private NhanVien nhanVienLap;
    private LocalDate ngayThu;
    private String hinhThucThanhToan;
    private boolean daThanhToan;
    private double tienKhachDua;

    // Bổ sung để thực hiện các phép tính theo đặc tả.
    private final List<ChiTietHoaDon> chiTietHoaDons =
            new ArrayList<>();
    private Double thueSuat;

    /**
     * Khởi tạo hóa đơn chưa có dữ liệu.
     * Hóa đơn mới mặc định chưa thanh toán.
     */
    public HoaDon() {
        this.daThanhToan = false;
    }

    /**
     * Khởi tạo hóa đơn với thông tin theo đặc tả.
     *
     * @param benhNhan bệnh nhân thanh toán
     * @param nhanVienLap lễ tân lập hóa đơn
     * @param ngayThu ngày thu tiền
     * @param hinhThucThanhToan TIEN_MAT hoặc CHUYEN_KHOAN
     * @param daThanhToan trạng thái thanh toán
     * @param tienKhachDua số tiền khách giao
     */
    public HoaDon(BenhNhan benhNhan,
                  NhanVien nhanVienLap,
                  LocalDate ngayThu,
                  String hinhThucThanhToan,
                  boolean daThanhToan,
                  double tienKhachDua) {

        setBenhNhan(benhNhan);
        setNhanVienLap(nhanVienLap);
        setNgayThu(ngayThu);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTienKhachDua(tienKhachDua);

        // Không cho khởi tạo hóa đơn đã thanh toán
        // khi chưa có chi tiết và thuế suất.
        setDaThanhToan(daThanhToan);
    }

    /**
     * Sao chép thông tin từ hóa đơn khác.
     *
     * @param original hóa đơn nguồn
     * @throws IllegalArgumentException nếu nguồn null
     */
    public HoaDon(HoaDon original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Hóa đơn nguồn không được null."
            );
        }

        this.maHoaDon = original.maHoaDon;
        this.benhNhan = original.benhNhan;
        this.nhanVienLap = original.nhanVienLap;
        this.ngayThu = original.ngayThu;
        this.hinhThucThanhToan = original.hinhThucThanhToan;
        this.daThanhToan = original.daThanhToan;
        this.tienKhachDua = original.tienKhachDua;
        this.thueSuat = original.thueSuat;

        for (ChiTietHoaDon ct : original.chiTietHoaDons) {
            // Bản sao vẫn tham chiếu hóa đơn nguồn;
            // không dùng để tạo hóa đơn mới độc lập.
            this.chiTietHoaDons.add(new ChiTietHoaDon(ct));
        }
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public BenhNhan getBenhNhan() {
        return benhNhan;
    }

    /**
     * Thiết lập bệnh nhân của hóa đơn.
     *
     * @param benhNhan bệnh nhân cần liên kết
     */
    public void setBenhNhan(BenhNhan benhNhan) {
        kiemTraChuaThanhToan();

        if (benhNhan == null) {
            throw new IllegalArgumentException(
                    "Bệnh nhân của hóa đơn không hợp lệ."
            );
        }

        this.benhNhan = benhNhan;
    }

    public NhanVien getNhanVienLap() {
        return nhanVienLap;
    }

    /**
     * Thiết lập lễ tân phụ trách lập hóa đơn.
     *
     * @param nhanVienLap nhân viên lập hóa đơn
     */
    public void setNhanVienLap(NhanVien nhanVienLap) {
        kiemTraChuaThanhToan();

        if (nhanVienLap == null
                || nhanVienLap.getChucVu() != ChucVu.LE_TAN) {
            throw new IllegalArgumentException(
                    "Nhân viên lập hóa đơn phải là lễ tân."
            );
        }

        this.nhanVienLap = nhanVienLap;
    }

    public LocalDate getNgayThu() {
        return ngayThu;
    }

    /**
     * Thiết lập ngày thu, không cho phép ngày tương lai.
     *
     * @param ngayThu ngày thu tiền
     */
    public void setNgayThu(LocalDate ngayThu) {
        kiemTraChuaThanhToan();

        if (ngayThu == null
                || ngayThu.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày thu hóa đơn không hợp lệ."
            );
        }

        this.ngayThu = ngayThu;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    /**
     * Thiết lập hình thức thanh toán được hỗ trợ.
     *
     * @param hinhThucThanhToan TIEN_MAT hoặc CHUYEN_KHOAN
     */
    public void setHinhThucThanhToan(
            String hinhThucThanhToan) {
        kiemTraChuaThanhToan();

        if (!"TIEN_MAT".equals(hinhThucThanhToan)
                && !"CHUYEN_KHOAN".equals(hinhThucThanhToan)) {
            throw new IllegalArgumentException(
                    "Hình thức thanh toán không hợp lệ."
            );
        }

        if ("CHUYEN_KHOAN".equals(hinhThucThanhToan)
                && tienKhachDua != 0) {
            throw new IllegalArgumentException(
                    "Chuyển khoản không được có tiền khách đưa."
            );
        }

        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public boolean getDaThanhToan() {
        return daThanhToan;
    }

    /**
     * Xác nhận trạng thái thanh toán.
     *
     * Hóa đơn chỉ được xác nhận khi có chi tiết
     * và đáp ứng điều kiện của hình thức thanh toán.
     *
     * Việc xác minh quyền của lễ tân thuộc tầng BUS.
     *
     * @param daThanhToan trạng thái cần cập nhật
     */
    public void setDaThanhToan(boolean daThanhToan) {
        if (this.daThanhToan) {
            if (!daThanhToan) {
                throw new IllegalArgumentException(
                        "Không được hủy trạng thái đã thanh toán."
                );
            }
            return;
        }

        if (daThanhToan) {
            if (chiTietHoaDons.isEmpty()) {
                throw new IllegalArgumentException(
                        "Chưa đủ điều kiện xác nhận thanh toán."
                );
            }

            if ("TIEN_MAT".equals(hinhThucThanhToan)) {
                tinhTienThua();
            } else if (!"CHUYEN_KHOAN".equals(
                    hinhThucThanhToan)) {
                throw new IllegalArgumentException(
                        "Chưa đủ điều kiện xác nhận thanh toán."
                );
            } else {
                // Vẫn cần xác nhận thực tế đã nhận tiền tại BUS.
                tinhTongTien();
            }
        }

        this.daThanhToan = daThanhToan;
    }

    public double getTienKhachDua() {
        return tienKhachDua;
    }

    /**
     * Thiết lập số tiền mặt khách đưa.
     *
     * @param tienKhachDua số tiền không âm và hữu hạn
     */
    public void setTienKhachDua(double tienKhachDua) {
        kiemTraChuaThanhToan();

        if (!Double.isFinite(tienKhachDua)
                || tienKhachDua < 0
                || ("CHUYEN_KHOAN".equals(hinhThucThanhToan)
                && tienKhachDua != 0)) {
            throw new IllegalArgumentException(
                    "Tiền khách đưa không hợp lệ."
            );
        }

        this.tienKhachDua = tienKhachDua;
    }

    /**
     * Thiết lập thuế suất áp dụng cho hóa đơn.
     * Thuế suất phải được lấy từ cấu hình nghiệp vụ.
     *
     * @param thueSuat giá trị từ 0 đến 1
     */
    public void setThueSuat(double thueSuat) {
        kiemTraChuaThanhToan();

        if (!Double.isFinite(thueSuat)
                || thueSuat < 0 || thueSuat > 1) {
            throw new IllegalArgumentException(
                    "Thuế suất không hợp lệ."
            );
        }

        this.thueSuat = thueSuat;
    }

    public Double getThueSuat() {
        return thueSuat;
    }

    /**
     * Thêm chi tiết vào hóa đơn chưa thanh toán.
     *
     * @param chiTiet chi tiết cần thêm
     */
    public void themChiTiet(ChiTietHoaDon chiTiet) {
        kiemTraChuaThanhToan();

        if (chiTiet == null || chiTiet.getHoaDon() != this) {
            throw new IllegalArgumentException(
                    "Chi tiết không thuộc hóa đơn này."
            );
        }

        if (chiTietHoaDons.contains(chiTiet)) {
            throw new IllegalArgumentException(
                    "Chi tiết đã được thêm vào hóa đơn."
            );
        }

        chiTietHoaDons.add(chiTiet);
    }

    /**
     * Trả về danh sách chi tiết chỉ đọc.
     *
     * @return danh sách chi tiết hóa đơn
     */
    public List<ChiTietHoaDon> getChiTietHoaDons() {
        return Collections.unmodifiableList(chiTietHoaDons);
    }

    /**
     * Tính số tiền thuế theo thuế suất đã cung cấp.
     *
     * @return tiền thuế được làm tròn đến đơn vị VND
     */
    public double Tinhthue() {
        if (thueSuat == null) {
            throw new IllegalStateException(
                    "Chưa thiết lập thuế suất cho hóa đơn."
            );
        }

        BigDecimal tienThue = tinhTamTinh()
                .multiply(BigDecimal.valueOf(thueSuat))
                .setScale(0, RoundingMode.HALF_UP);

        return chuyenThanhDouble(tienThue);
    }

    /**
     * Tính tổng tiền bao gồm thuế.
     *
     * @return tổng tiền hóa đơn
     */
    public double tinhTongTien() {
        BigDecimal tongTien = tinhTamTinh()
                .add(BigDecimal.valueOf(Tinhthue()));

        return chuyenThanhDouble(tongTien);
    }

    public double getTongTien() {
        return tinhTongTien();
    }

    /**
     * Tính tiền thừa khi thanh toán tiền mặt.
     * Với chuyển khoản, tiền thừa bằng 0.
     *
     * @return số tiền thừa của khách
     */
    public double tinhTienThua() {
        if ("CHUYEN_KHOAN".equals(hinhThucThanhToan)) {
            return 0;
        }

        if (!"TIEN_MAT".equals(hinhThucThanhToan)) {
            throw new IllegalArgumentException(
                    "Hình thức thanh toán không hợp lệ."
            );
        }

        BigDecimal tienThua =
                BigDecimal.valueOf(tienKhachDua)
                        .subtract(BigDecimal.valueOf(tinhTongTien()));

        if (tienThua.signum() < 0) {
            throw new IllegalArgumentException(
                    "Tiền khách đưa không đủ thanh toán."
            );
        }

        return chuyenThanhDouble(tienThua);
    }

    public double getTienThua() {
        return tinhTienThua();
    }

    /**
     * Tính tổng tiền các dịch vụ trước thuế.
     *
     * @return tạm tính dưới dạng BigDecimal
     */
    private BigDecimal tinhTamTinh() {
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietHoaDon ct : chiTietHoaDons) {
            tong = tong.add(
                    BigDecimal.valueOf(ct.tinhThanhTien())
            );
        }

        return tong;
    }

    /**
     * Chuyển kết quả sang double và kiểm tra tràn số.
     *
     * @param giaTri số tiền cần chuyển
     * @return giá trị hữu hạn
     */
    private double chuyenThanhDouble(BigDecimal giaTri) {
        double ketQua = giaTri.doubleValue();

        if (!Double.isFinite(ketQua)) {
            throw new IllegalArgumentException(
                    "Số tiền vượt phạm vi cho phép."
            );
        }

        return ketQua;
    }

    /**
     * Ngăn chỉnh sửa hóa đơn đã thanh toán.
     */
    private void kiemTraChuaThanhToan() {
        if (daThanhToan) {
            throw new IllegalArgumentException(
                    "Không thể sửa hóa đơn đã thanh toán."
            );
        }
    }

    @Override
    public String toString() {
        return "HoaDon [maHoaDon=" + maHoaDon
                + ", maBenhNhan="
                + (benhNhan == null ? null
                : benhNhan.getMaBenhNhan())
                + ", maNhanVienLap="
                + (nhanVienLap == null ? null
                : nhanVienLap.getMaNhanVien())
                + ", ngayThu=" + ngayThu
                + ", hinhThucThanhToan="
                + hinhThucThanhToan
                + ", daThanhToan=" + daThanhToan
                + ", tienKhachDua=" + tienKhachDua
                + "]";
    }
}

