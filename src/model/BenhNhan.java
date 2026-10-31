
package model;

import java.time.LocalDate;

public class BenhNhan {

    // 1. Thuộc tính
    private String maBenhNhan;
    private String hoTen;
    private String soDienThoai;
    private String cccd;
    private LocalDate ngaySinh;
    private String diaChi;
    private String tienSuDiUng;

    // 2. Constructor mặc định
    public BenhNhan() {
    }

    // 3. Constructor tạo bệnh nhân mới (chưa có mã)
    public BenhNhan(String hoTen, String soDienThoai,
                    String cccd, LocalDate ngaySinh,
                    String diaChi, String tienSuDiUng) {

        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setCccd(cccd);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
        setTienSuDiUng(tienSuDiUng);
    }

    // 4. Constructor dùng khi đọc bệnh nhân từ CSDL
    public BenhNhan(String maBenhNhan, String hoTen,
                    String soDienThoai, String cccd,
                    LocalDate ngaySinh, String diaChi,
                    String tienSuDiUng) {

        this(hoTen, soDienThoai, cccd,
                ngaySinh, diaChi, tienSuDiUng);

        if (maBenhNhan == null
                || !maBenhNhan.matches("^BN[0-9]{6}(?!0000)[0-9]{4}$")) {
            throw new IllegalArgumentException(
                    "Mã bệnh nhân không hợp lệ."
            );
        }

        this.maBenhNhan = maBenhNhan;
    }

    // 5. Copy constructor
    public BenhNhan(BenhNhan original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Bệnh nhân nguồn không được null."
            );
        }

        this.maBenhNhan = original.maBenhNhan;
        setHoTen(original.hoTen);
        setSoDienThoai(original.soDienThoai);
        setCccd(original.cccd);
        setNgaySinh(original.ngaySinh);
        setDiaChi(original.diaChi);
        setTienSuDiUng(original.tienSuDiUng);
    }

    // 6. Getter và Setter

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()
                || hoTen.codePoints().noneMatch(Character::isLetter)) {
            throw new IllegalArgumentException(
                    "Họ tên bệnh nhân không hợp lệ."
            );
        }

        this.hoTen = hoTen.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null
                || !soDienThoai.matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException(
                    "Số điện thoại bệnh nhân không hợp lệ."
            );
        }

        this.soDienThoai = soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            this.cccd = null;
            return;
        }

        String value = cccd.trim();

        if (!value.matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException(
                    "CCCD không hợp lệ."
            );
        }

        this.cccd = value;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        if (ngaySinh == null || ngaySinh.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày sinh bệnh nhân không hợp lệ."
            );
        }

        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Địa chỉ bệnh nhân không hợp lệ."
            );
        }

        this.diaChi = diaChi.trim();
    }

    public String getTienSuDiUng() {
        return tienSuDiUng;
    }

    public void setTienSuDiUng(String tienSuDiUng) {
        this.tienSuDiUng = tienSuDiUng == null
                ? null
                : tienSuDiUng.trim();
    }

    // 7. Phương thức kiểm tra số điện thoại
    public boolean soDienThoaiHopLe() {
        return soDienThoai != null
                && soDienThoai.matches("^0[0-9]{9}$");
    }

    // 8. Biểu diễn thông tin đối tượng
    @Override
    public String toString() {
        return "BenhNhan [maBenhNhan=" + maBenhNhan
                + ", hoTen=" + hoTen
                + ", soDienThoai=" + soDienThoai
                + ", cccd=" + cccd
                + ", ngaySinh=" + ngaySinh
                + ", diaChi=" + diaChi
                + ", tienSuDiUng=" + tienSuDiUng + "]";
    }
}
