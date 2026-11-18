package entity;

import java.time.LocalDateTime;

/** Thuốc trong một phiếu khám và hướng dẫn sử dụng do bác sĩ nhập. */
public class ChiTietCapThuoc {
    private String hamLuongDangDung;
    private String lieuMoiLan;
    private String duongDung;
    private Integer soLanNgay;
    private Integer soNgay;
    private String buoiDung;
    private String chanDoanLienQuan;
    private LocalDateTime ngayKe;
    private PhieuKham phieuKham;
    private Thuoc thuoc; // quan he voi Thuoc
    private int soLuong;
    private String ghiChuCapThuoc;

    public ChiTietCapThuoc() {
    }

    public ChiTietCapThuoc(String maPhieuKham, String maThuoc, int soLuong, String ghiChuCapThuoc) {
        setMaPhieuKham(maPhieuKham);
        setMaThuoc(maThuoc);
        this.soLuong = soLuong;
        this.ghiChuCapThuoc = ghiChuCapThuoc;
    }

    public String getHamLuongDangDung() {
        return hamLuongDangDung;
    }

    public void setHamLuongDangDung(String hamLuongDangDung) {
        this.hamLuongDangDung = hamLuongDangDung;
    }

    public String getLieuMoiLan() {
        return lieuMoiLan;
    }

    public void setLieuMoiLan(String lieuMoiLan) {
        this.lieuMoiLan = lieuMoiLan;
    }

    public String getDuongDung() {
        return duongDung;
    }

    public void setDuongDung(String duongDung) {
        this.duongDung = duongDung;
    }

    public Integer getSoLanNgay() {
        return soLanNgay;
    }

    public void setSoLanNgay(Integer soLanNgay) {
        this.soLanNgay = soLanNgay;
    }

    public Integer getSoNgay() {
        return soNgay;
    }

    public void setSoNgay(Integer soNgay) {
        this.soNgay = soNgay;
    }

    public String getBuoiDung() {
        return buoiDung;
    }

    public void setBuoiDung(String buoiDung) {
        this.buoiDung = buoiDung;
    }

    public String getChanDoanLienQuan() {
        return chanDoanLienQuan;
    }

    public void setChanDoanLienQuan(String chanDoanLienQuan) {
        this.chanDoanLienQuan = chanDoanLienQuan;
    }

    public LocalDateTime getNgayKe() {
        return ngayKe;
    }

    public void setNgayKe(LocalDateTime ngayKe) {
        this.ngayKe = ngayKe;
    }

    public String getHuongDanString() {
        return java.util.stream.Stream
                .of(hamLuongDangDung, lieuMoiLan == null ? null : "Liều/lần: " + lieuMoiLan, duongDung,
                        soLanNgay == null ? null : soLanNgay + " lần/ngày",
                        soNgay == null ? null : soNgay + " ngày",
                        buoiDung, ghiChuCapThuoc)
                .filter(s -> s != null && !s.isBlank()).collect(java.util.stream.Collectors.joining(" · "));
    }

    public ChiTietCapThuoc saoChep(String maPhieu) {
        ChiTietCapThuoc t = new ChiTietCapThuoc(maPhieu, getMaThuoc(), soLuong, ghiChuCapThuoc);
        if (thuoc != null)
            t.setThuoc(new Thuoc(thuoc.getMaThuoc(), thuoc.getTenThuoc(), thuoc.getDonViTinh()));
        t.hamLuongDangDung = hamLuongDangDung;
        t.lieuMoiLan = lieuMoiLan;
        t.duongDung = duongDung;
        t.soLanNgay = soLanNgay;
        t.soNgay = soNgay;
        t.buoiDung = buoiDung;
        t.chanDoanLienQuan = chanDoanLienQuan;
        t.ngayKe = ngayKe;
        return t;
    }

    public PhieuKham getPhieuKham() {
        return phieuKham;
    }

    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
    }

    public String getMaPhieuKham() {
        return phieuKham == null ? null : phieuKham.getMaPhieuKham();
    }

    public void setMaPhieuKham(String maPhieuKham) {
        if (maPhieuKham == null) {
            phieuKham = null;
        } else if (phieuKham == null || !maPhieuKham.equals(phieuKham.getMaPhieuKham())) {
            phieuKham = new PhieuKham(maPhieuKham);
        }
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChuCapThuoc() {
        return ghiChuCapThuoc;
    }

    public void setGhiChuCapThuoc(String ghiChuCapThuoc) {
        this.ghiChuCapThuoc = ghiChuCapThuoc;
    }

    public Thuoc getThuoc() {
        return thuoc;
    }

    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
    }

    public String getMaThuoc() {
        return thuoc != null ? thuoc.getMaThuoc() : null;
    }

    public void setMaThuoc(String maThuoc) {
        if (maThuoc == null) {
            thuoc = null;
        } else if (thuoc == null || !maThuoc.equals(thuoc.getMaThuoc())) {
            thuoc = new Thuoc(maThuoc);
        }
    }

    public String getTenThuoc() {
        return thuoc != null ? thuoc.getTenThuoc() : null;
    }

    public String getDonViTinh() {
        return thuoc != null ? thuoc.getDonViTinh() : null;
    }

    public String getThuocString() {
        return thuoc == null ? "" : thuoc.getMaThuoc() + " - " + thuoc.getTenThuoc();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiTietCapThuoc other = (ChiTietCapThuoc) obj;
        return getMaPhieuKham() != null && !getMaPhieuKham().isBlank()
                && getMaPhieuKham().equals(other.getMaPhieuKham())
                && getMaThuoc() != null && !getMaThuoc().isBlank() && getMaThuoc().equals(other.getMaThuoc());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
