package entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import util.MaThongBao;

/** Dịch vụ được chỉ định trong một phiếu khám, giữ đơn giá tại thời điểm lưu. */
public class ChiDinhDichVu extends DongDichVu {
    private KeHoachDieuTri.HangMuc hangMuc;
    private final List<GhiNhanKham> dsGhiNhan = new ArrayList<>();
    private PhieuKham phieuKham;
    private boolean giaThamKhao;
    private String soRang; // so rang bi dieu tri (FDI, vd "36,37")

    public ChiDinhDichVu() {
    }

    public ChiDinhDichVu(String maPhieuKham, String maDichVu) {
        setMaPhieuKham(maPhieuKham);
        setMaDichVu(maDichVu);
    }

    public List<GhiNhanKham> getDsGhiNhan() {
        return Collections.unmodifiableList(dsGhiNhan);
    }

    public void setDsGhiNhan(Collection<GhiNhanKham> ds) {
        List<GhiNhanKham> moi = ds == null ? List.of() : new ArrayList<>(ds);
        if (moi.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(MaThongBao.EN02);
        }
        dsGhiNhan.clear();
        dsGhiNhan.addAll(moi);
    }

    public List<KeHoachDieuTri.HangMuc> getDsHangMuc() {
        Map<String, KeHoachDieuTri.HangMuc> ds = new LinkedHashMap<>();
        if (hangMuc != null)
            ds.put(hangMuc.getMaHangMuc(), hangMuc);
        for (GhiNhanKham g : dsGhiNhan)
            if (g.getHangMuc() != null)
                ds.put(g.getHangMuc().getMaHangMuc(), g.getHangMuc());
        return new ArrayList<>(ds.values());
    }

    public KeHoachDieuTri.HangMuc getHangMuc() {
        return hangMuc;
    }

    public void setHangMuc(KeHoachDieuTri.HangMuc hangMuc) {
        this.hangMuc = hangMuc;
    }

    public GhiNhanKham getGhiNhan() {
        return dsGhiNhan.isEmpty() ? null : dsGhiNhan.getFirst();
    }

    public void setGhiNhan(GhiNhanKham value) {
        dsGhiNhan.clear();
        if (value != null)
            dsGhiNhan.add(value);
    }

    public boolean isGiaThamKhao() {
        return giaThamKhao;
    }

    public void setGiaThamKhao(boolean giaThamKhao) {
        this.giaThamKhao = giaThamKhao;
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

    public String getSoRang() {
        return soRang;
    }

    public void setSoRang(String soRang) {
        this.soRang = soRang;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiDinhDichVu other = (ChiDinhDichVu) obj;
        return getMaPhieuKham() != null && !getMaPhieuKham().isBlank()
                && getMaPhieuKham().equals(other.getMaPhieuKham())
                && getMaDichVu() != null && !getMaDichVu().isBlank() && getMaDichVu().equals(other.getMaDichVu());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
