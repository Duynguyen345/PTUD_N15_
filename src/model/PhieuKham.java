
package model;

import java.time.LocalDate;

/**
 * Đại diện cho một lượt khám của bệnh nhân.
 *
 * Phiếu khám quản lý thông tin tiếp nhận, bác sĩ,
 * phòng khám, nội dung lâm sàng và trạng thái khám.
 *
 * Mã phiếu và số thứ tự duy nhất được cấp tại CSDL.
 */
public class PhieuKham {

    private String maPhieuKham;
    private BenhNhan benhNhan;
    private NhanVien bacSi;
    private PhongKham phongKham;
    private LocalDate ngayKham;
    private int soThuTu;
    private String lyDoKham;
    private String chanDoan;
    private String ketQuaDieuTri;
    private String ghiChuThuocSuDung;
    private String ghiChuThuocTuMua;
    private TrangThaiPhieuKham trangThai;

    /**
     * Khởi tạo phiếu khám chưa có dữ liệu.
     * Các thuộc tính bắt buộc phải hợp lệ trước khi lưu.
     */
    public PhieuKham() {
    }

    /**
     * Khởi tạo phiếu khám với các thông tin theo đặc tả.
     *
     * @param benhNhan bệnh nhân được khám
     * @param bacSi bác sĩ phụ trách
     * @param phongKham phòng thực hiện khám
     * @param ngayKham ngày tiếp nhận
     * @param soThuTu số thứ tự trong ngày tại phòng
     * @param lyDoKham lý do đến khám
     * @param chanDoan nội dung chẩn đoán
     * @param ketQuaDieuTri kết quả điều trị
     * @param ghiChuThuocSuDung thuốc sử dụng tại phòng
     * @param ghiChuThuocTuMua gợi ý thuốc tự mua
     * @param trangThai trạng thái phiếu khám
     */
    public PhieuKham(
            BenhNhan benhNhan,
            NhanVien bacSi,
            PhongKham phongKham,
            LocalDate ngayKham,
            int soThuTu,
            String lyDoKham,
            String chanDoan,
            String ketQuaDieuTri,
            String ghiChuThuocSuDung,
            String ghiChuThuocTuMua,
            TrangThaiPhieuKham trangThai) {

        setBenhNhan(benhNhan);
        setBacSi(bacSi);
        setPhongKham(phongKham);
        setNgayKham(ngayKham);
        setSoThuTu(soThuTu);
        setLyDoKham(lyDoKham);

        // Nội dung lâm sàng có thể chưa được nhập khi tiếp nhận.
        setChanDoan(chanDoan);
        setKetQuaDieuTri(ketQuaDieuTri);
        setGhiChuThuocSuDung(ghiChuThuocSuDung);
        setGhiChuThuocTuMua(ghiChuThuocTuMua);

        if (trangThai == null) {
            throw new IllegalArgumentException(
                    "Trạng thái phiếu khám không hợp lệ."
            );
        }

        // Constructor khôi phục trạng thái ban đầu của đối tượng.
        // Chuyển trạng thái sau đó phải đi qua setter.
        if (trangThai == TrangThaiPhieuKham.HOAN_TAT
                && (this.chanDoan == null
                || this.chanDoan.isBlank())) {
            throw new IllegalArgumentException(
                    "Chẩn đoán bắt buộc trước khi hoàn tất phiếu."
            );
        }

        this.trangThai = trangThai;
    }

    /**
     * Sao chép thông tin từ một phiếu khám khác.
     *
     * @param original phiếu khám nguồn
     */
    public PhieuKham(PhieuKham original) {
        if (original == null) {
            throw new IllegalArgumentException(
                    "Phiếu khám nguồn không được null."
            );
        }

        this.maPhieuKham = original.maPhieuKham;
        this.benhNhan = original.benhNhan;
        this.bacSi = original.bacSi;
        this.phongKham = original.phongKham;
        this.ngayKham = original.ngayKham;
        this.soThuTu = original.soThuTu;
        this.lyDoKham = original.lyDoKham;
        this.chanDoan = original.chanDoan;
        this.ketQuaDieuTri = original.ketQuaDieuTri;
        this.ghiChuThuocSuDung = original.ghiChuThuocSuDung;
        this.ghiChuThuocTuMua = original.ghiChuThuocTuMua;
        this.trangThai = original.trangThai;
    }

    // Getter và Setter

    public String getMaPhieuKham() {
        return maPhieuKham;
    }

    public BenhNhan getBenhNhan() {
        return benhNhan;
    }

    public void setBenhNhan(BenhNhan benhNhan) {
        if (benhNhan == null) {
            throw new IllegalArgumentException(
                    "Bệnh nhân trên phiếu khám không hợp lệ."
            );
        }
        this.benhNhan = benhNhan;
    }

    public NhanVien getBacSi() {
        return bacSi;
    }

    public void setBacSi(NhanVien bacSi) {
        if (bacSi == null
                || bacSi.getChucVu() != ChucVu.BAC_SI) {
            throw new IllegalArgumentException(
                    "Bác sĩ phụ trách không hợp lệ."
            );
        }
        this.bacSi = bacSi;
    }

    public PhongKham getPhongKham() {
        return phongKham;
    }

    public void setPhongKham(PhongKham phongKham) {
        if (phongKham == null
                || !phongKham.coTheTiepNhan()) {
            throw new IllegalArgumentException(
                    "Phòng khám không hợp lệ hoặc ngừng nhận bệnh."
            );
        }
        this.phongKham = phongKham;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        if (ngayKham == null
                || !ngayKham.equals(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày khám không hợp lệ."
            );
        }
        this.ngayKham = ngayKham;
    }

    public int getSoThuTu() {
        return soThuTu;
    }

    public void setSoThuTu(int soThuTu) {
        if (soThuTu <= 0) {
            throw new IllegalArgumentException(
                    "Số thứ tự khám không hợp lệ hoặc trùng."
            );
        }
        this.soThuTu = soThuTu;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        if (lyDoKham == null || lyDoKham.isBlank()) {
            throw new IllegalArgumentException(
                    "Lý do khám không được rỗng."
            );
        }
        this.lyDoKham = lyDoKham.trim();
    }

    public String getChanDoan() {
        return chanDoan;
    }

    public void setChanDoan(String chanDoan) {
        String value = chanDoan == null
                ? null : chanDoan.trim();

        if (trangThai == TrangThaiPhieuKham.HOAN_TAT
                && (value == null || value.isBlank())) {
            throw new IllegalArgumentException(
                    "Chẩn đoán bắt buộc trước khi hoàn tất phiếu."
            );
        }

        this.chanDoan = value;
    }

    public String getKetQuaDieuTri() {
        return ketQuaDieuTri;
    }

    public void setKetQuaDieuTri(String ketQuaDieuTri) {
        // Việc kiểm tra có chỉ định dịch vụ thuộc BUS.
        this.ketQuaDieuTri = ketQuaDieuTri == null
                ? null : ketQuaDieuTri.trim();
    }

    public String getGhiChuThuocSuDung() {
        return ghiChuThuocSuDung;
    }

    public void setGhiChuThuocSuDung(
            String ghiChuThuocSuDung) {
        this.ghiChuThuocSuDung = ghiChuThuocSuDung == null
                ? null : ghiChuThuocSuDung.trim();
    }

    public String getGhiChuThuocTuMua() {
        return ghiChuThuocTuMua;
    }

    public void setGhiChuThuocTuMua(
            String ghiChuThuocTuMua) {
        this.ghiChuThuocTuMua = ghiChuThuocTuMua == null
                ? null : ghiChuThuocTuMua.trim();
    }

    public TrangThaiPhieuKham getTrangThai() {
        return trangThai;
    }

    /**
     * Chuyển trạng thái theo quy trình:
     * CHO_KHAM -> DANG_KHAM -> HOAN_TAT.
     *
     * Không cho phép bỏ bước hoặc chuyển ngược.
     *
     * @param trangThai trạng thái đích
     */
    public void setTrangThai(TrangThaiPhieuKham trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException(
                    "Chuyển trạng thái phiếu khám không hợp lệ."
            );
        }

        boolean hopLe =
                (this.trangThai == TrangThaiPhieuKham.CHO_KHAM
                        && trangThai == TrangThaiPhieuKham.DANG_KHAM)
                        ||
                        (this.trangThai == TrangThaiPhieuKham.DANG_KHAM
                                && trangThai == TrangThaiPhieuKham.HOAN_TAT);

        if (!hopLe) {
            throw new IllegalArgumentException(
                    "Chuyển trạng thái phiếu khám không hợp lệ."
            );
        }

        if (trangThai == TrangThaiPhieuKham.HOAN_TAT
                && (chanDoan == null || chanDoan.isBlank())) {
            throw new IllegalArgumentException(
                    "Chẩn đoán bắt buộc trước khi hoàn tất phiếu."
            );
        }

        // BUS phải kiểm tra kết quả điều trị khi có chỉ định dịch vụ.
        this.trangThai = trangThai;
    }

    /**
     * Kiểm tra phiếu khám đã hoàn tất hay chưa.
     *
     * @return true nếu phiếu khám đã hoàn tất
     */
    public boolean daHoanTat() {
        return trangThai == TrangThaiPhieuKham.HOAN_TAT;
    }

    @Override
    public String toString() {
        return "PhieuKham [maPhieuKham=" + maPhieuKham
                + ", maBenhNhan="
                + (benhNhan == null ? null
                : benhNhan.getMaBenhNhan())
                + ", maBacSi="
                + (bacSi == null ? null
                : bacSi.getMaNhanVien())
                + ", maPhong="
                + (phongKham == null ? null
                : phongKham.getMaPhong())
                + ", ngayKham=" + ngayKham
                + ", soThuTu=" + soThuTu
                + ", lyDoKham=" + lyDoKham
                + ", chanDoan=" + chanDoan
                + ", ketQuaDieuTri=" + ketQuaDieuTri
                + ", ghiChuThuocSuDung=" + ghiChuThuocSuDung
                + ", ghiChuThuocTuMua=" + ghiChuThuocTuMua
                + ", trangThai=" + trangThai + "]";
    }
}
