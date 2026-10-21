package model;

import java.time.LocalDate;

public class BenhNhan {

    private String maBenhNhan;
    private String hoTen;
    private String soDienThoai;
    private String cccd;
    private LocalDate ngaySinh;
    private String diaChi;
    private String tienSuDiUng;

    // Constructor mặc định
    public BenhNhan() {
    }


    // Constructor đầy đủ, dùng khi đã có mã hoặc đọc từ CSDL
    public BenhNhan(String maBenhNhan, String hoTen,
                    String soDienThoai, String cccd,
                    LocalDate ngaySinh, String diaChi,
                    String tienSuDiUng) {
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.ngaySinh = ngaySinh;
        this.diaChi = diaChi;
        this.tienSuDiUng = tienSuDiUng;
    }


    // Getters và setters
    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getTienSuDiUng() {
        return tienSuDiUng;
    }

    public void setTienSuDiUng(String tienSuDiUng) {
        this.tienSuDiUng = tienSuDiUng;
    }

    // Phương thức được mô tả trong báo cáo
    public boolean soDienThoaiHopLe() {
        return soDienThoai != null
                && soDienThoai.matches("^0[0-9]{9}$");
    }

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