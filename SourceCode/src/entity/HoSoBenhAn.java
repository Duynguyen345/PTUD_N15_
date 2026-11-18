package entity;

import java.util.Objects;
import util.MaThongBao;

public class HoSoBenhAn {
    private String maHoSo;
    private String tienSuBenhLy;
    private String ghiChuChuyenMon;
    private BenhNhan benhNhan;

    public HoSoBenhAn() {
    }

    public HoSoBenhAn(String maHoSo) {
        this.maHoSo = maHoSo;
    }

    public HoSoBenhAn(String maHoSo, String tienSuBenhLy, String ghiChuChuyenMon, String maBenhNhan) {
        this.maHoSo = maHoSo;
        this.tienSuBenhLy = tienSuBenhLy;
        this.ghiChuChuyenMon = ghiChuChuyenMon;
        setMaBenhNhan(maBenhNhan);
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        if (this.maHoSo != null && !Objects.equals(this.maHoSo, maHoSo)) {
            throw new IllegalArgumentException(MaThongBao.EN01);
        }
        this.maHoSo = maHoSo;
    }

    public String getTienSuBenhLy() {
        return tienSuBenhLy;
    }

    public void setTienSuBenhLy(String tienSuBenhLy) {
        this.tienSuBenhLy = tienSuBenhLy;
    }

    public String getGhiChuChuyenMon() {
        return ghiChuChuyenMon;
    }

    public void setGhiChuChuyenMon(String ghiChuChuyenMon) {
        this.ghiChuChuyenMon = ghiChuChuyenMon;
    }

    public String getMaBenhNhan() {
        return benhNhan == null ? null : benhNhan.getMaBenhNhan();
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) {
            benhNhan = null;
        } else if (benhNhan == null || !maBenhNhan.equals(benhNhan.getMaBenhNhan())) {
            benhNhan = new BenhNhan(maBenhNhan);
        }
    }

    public BenhNhan getBenhNhan() {
        return benhNhan;
    }

    public void setBenhNhan(BenhNhan benhNhan) {
        this.benhNhan = benhNhan;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HoSoBenhAn other = (HoSoBenhAn) obj;
        return maHoSo != null && !maHoSo.isBlank() && maHoSo.equals(other.maHoSo);
    }

    @Override
    public int hashCode() {
        // Giữ nguyên bucket khi đối tượng mới được cấp mã sau lúc khởi tạo.
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "HoSoBenhAn[" + Objects.toString(maHoSo, "") + "]";
    }
}
