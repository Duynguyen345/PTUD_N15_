package bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.math.BigDecimal;
import connectDB.ConnectDB;
import dao.BenhNhan_DAO;
import dao.BoDem_DAO;
import dao.ChiDinhDichVu_DAO;
import dao.ChiTietCapThuoc_DAO;
import dao.DichVu_DAO;
import dao.GhiNhanKham_DAO;
import dao.KeHoachDieuTri_DAO;
import dao.KetQuaKham_DAO;
import dao.PhieuKham_DAO;
import dao.Thuoc_DAO;
import entity.BenhNhan;
import entity.ChiDinhDichVu;
import entity.ChiTietCapThuoc;
import entity.DichVu;
import entity.GhiNhanKham;
import entity.KeHoachDieuTri;
import entity.KetQuaKham;
import entity.PhieuKham;
import entity.ChucVu;
import entity.SessionContext;
import entity.Thuoc;
import entity.TrangThaiDichVu;
import entity.TrangThaiPhieuKham;
import util.MaThongBao;
import util.QuyenUtil;

/** Luồng bác sĩ: chỉ sửa phiếu được phụ tá xác nhận, đúng bác sĩ, đúng phiên bản. */
public class KhamBenh_BUS {
    public static List<PhieuKham> danhSachKhamCuaToi(SessionContext session) {
        return danhSachKhamCuaToi(session, LocalDate.now());
    }

    public static List<PhieuKham> danhSachKhamCuaToi(SessionContext session, LocalDate ngay) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return PhieuKham_DAO.layDanhSachTheoBacSi(session.getMaNhanVien(), Objects.requireNonNull(ngay));
    }

    public static List<DichVu> timDichVu(SessionContext s, String tu, String nhom, int trang) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        return DichVu_DAO.timDichVu(tu == null ? "" : tu.trim(), nhom == null ? "" : nhom, Math.max(0, trang));
    }

    public static List<String> nhomDichVu(SessionContext s) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        return DichVu_DAO.layNhom();
    }

    public static BigDecimal donGiaTrongLuot(PhieuKham phieu, DichVu dichVu) {
        return phieu != null && phieu.getMaHoaDonPhiKham() != null
                && HoaDon_BUS.MA_DICH_VU_PHI_KHAM.equals(dichVu.getMaDichVu())
                        ? BigDecimal.ZERO : dichVu.getDonGiaNiemYet();
    }

    public static List<Thuoc> danhSachThuoc(SessionContext session) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return Thuoc_DAO.getAllThuoc();
    }

    public static PhieuKham layPhieuCuaToi(SessionContext session, String ma) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        PhieuKham pk = PhieuKham_DAO.layPhieuKhamTheoMa(ma);
        if (pk == null || !session.getMaNhanVien().equals(pk.getMaNhanVien()))
            throw new SecurityException(MaThongBao.TB08);
        return pk;
    }

    public static List<ChiDinhDichVu> layChiDinhCuaToi(SessionContext session, String ma) {
        PhieuKham phieu = layPhieuCuaToi(session, ma);
        List<ChiDinhDichVu> ds = ChiDinhDichVu_DAO.layTheoPhieu(ma);
        if (phieu.getMaHoaDonPhiKham() != null)
            ds.stream().filter(d -> HoaDon_BUS.MA_DICH_VU_PHI_KHAM.equals(d.getMaDichVu()))
                    .forEach(d -> d.setDonGia(BigDecimal.ZERO));
        return ds;
    }

    public static List<ChiTietCapThuoc> layThuocCapCuaToi(SessionContext session, String ma) {
        layPhieuCuaToi(session, ma);
        return ChiTietCapThuoc_DAO.layTheoPhieu(ma);
    }

    public static List<GhiNhanKham> layGhiNhan(SessionContext session, String ma) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return GhiNhanKham_DAO.layTheoPhieu(ma);
    }

    public static List<ChiTietCapThuoc> layThuocLichSu(SessionContext session, String ma) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return ChiTietCapThuoc_DAO.layTheoPhieu(ma);
    }

    public static List<String> layNhatKy(SessionContext session, String ma) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return GhiNhanKham_DAO.layNhatKy(ma);
    }

    public static BenhNhan layBenhNhan(SessionContext session, String ma) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        return BenhNhan_DAO.layBenhNhanTheoMa(ma);
    }

    public static List<BenhNhan> timBenhNhan(SessionContext session, String tuKhoa) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        if (tuKhoa == null || tuKhoa.isBlank())
            return List.of();
        return BenhNhan_DAO.timTheoTieuChi(tuKhoa.trim(), 0);
    }

    public static List<BenhNhan> timBenhNhan(SessionContext s, String tu, int trang) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        if (tu == null || tu.isBlank())
            return List.of();
        if (trang < 0 || trang > 1000000)
            throw new IllegalArgumentException(MaThongBao.VL05);
        return BenhNhan_DAO.timTheoTieuChi(tu.trim(), trang);
    }

    public static List<PhieuKham> lichSu(SessionContext session, String ma, LocalDate tu, LocalDate den, int trang) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        if (tu != null && den != null && tu.isAfter(den))
            throw new IllegalArgumentException(MaThongBao.VL10);
        if (ma == null || ma.isBlank() || trang < 0 || trang > 1000000)
            throw new IllegalArgumentException(MaThongBao.VL05);
        return PhieuKham_DAO.lichSuBenhNhan(ma, tu, den, trang, 30);
    }

    public static final int SO_LUOT_LICH_SU = 10;

    /** Tra cứu trực tiếp lượt hoàn tất; 10 dòng và một dòng báo còn trang sau. */
    public static List<PhieuKham> traCuuLichSu(SessionContext session, String tuKhoa, LocalDate tu, LocalDate den,
            int trang) {
        QuyenUtil.kiemTraQuyen(session, ChucVu.BAC_SI);
        if (tu != null && den != null && tu.isAfter(den))
            throw new IllegalArgumentException(MaThongBao.VL10);
        if (trang < 0 || trang > 1000000)
            throw new IllegalArgumentException(MaThongBao.VL05);
        return PhieuKham_DAO.timLichSuHoanTat(tuKhoa == null ? "" : tuKhoa.trim(), tu, den, trang, SO_LUOT_LICH_SU);
    }

    public static boolean laRangHopLe(String ma) {
        if (ma == null || !ma.matches("[1-8][1-8]"))
            return false;
        int q = ma.charAt(0) - '0', n = ma.charAt(1) - '0';
        return q <= 4 || n <= 5;
    }

    public static void kiemTraGhiNhan(GhiNhanKham g) {
        if (g == null || g.getPhamVi() == null || g.getTrangThai() == null)
            throw new IllegalArgumentException(MaThongBao.BS02);
        if (g.getPhamVi() == GhiNhanKham.PhamVi.THEO_RANG) {
            if (g.getDsRang().isEmpty() || g.getDsRang().stream().anyMatch(r -> !laRangHopLe(r)))
                throw new IllegalArgumentException(MaThongBao.BS02);
        } else if (!g.getDsRang().isEmpty())
            throw new IllegalArgumentException(MaThongBao.BS02);
        if (rong(g.getChanDoan()) && rong(g.getNoiDung()))
            throw new IllegalArgumentException(MaThongBao.BS03);
        if ((g.getTrangThai() == GhiNhanKham.TrangThai.DA_THUC_HIEN
                || g.getTrangThai() == GhiNhanKham.TrangThai.CO_KET_QUA) && rong(g.getKetQua()))
            throw new IllegalArgumentException(MaThongBao.BS04);
        boolean thucHien = g.getTrangThai() == GhiNhanKham.TrangThai.DA_THUC_HIEN
                || g.getTrangThai() == GhiNhanKham.TrangThai.CO_KET_QUA;
        if (thucHien && g.getNgayThucHien() == null)
            throw new IllegalArgumentException(MaThongBao.BS14);
        if (g.getNgayThucHien() != null && g.getNgayThucHien().isAfter(java.time.LocalDateTime.now()))
            throw new IllegalArgumentException(MaThongBao.BS14);
        gioiHan(g.getChanDoan(), 1000);
        gioiHan(g.getNoiDung(), 2000);
        gioiHan(g.getKetQua(), 2000);
        gioiHan(g.getViTriKhac(), 300);
    }

    /** API cũ vẫn dùng được; lưu nháp không bắt buộc chẩn đoán/dịch vụ/thuốc. */
    public static void luuKham(SessionContext s, String ma, String cd, String kq, String thuocTuMua,
            List<ChiDinhDichVu> dichVu, List<ChiTietCapThuoc> thuoc, String phacDo, LocalDate hen) {
        PhieuKham pk = layPhieuCuaToi(s, ma);
        pk.setChanDoan(cd);
        pk.setKetQuaDieuTri(kq);
        pk.setGhiChuThuocTuMua(thuocTuMua);
        pk.setPhacDieuTri(phacDo);
        pk.setNgayHenTiep(hen);
        luuKham(s, pk, layGhiNhan(s, ma), dichVu, thuoc, false);
    }

    /** Một transaction cho toàn bộ phiếu, bệnh án, ghi nhận, dịch vụ và thuốc; hoàn tất là tùy chọn của cùng giao dịch. */
    public static void luuKham(SessionContext s, PhieuKham data, List<GhiNhanKham> ghiNhan, List<ChiDinhDichVu> dichVu,
            List<ChiTietCapThuoc> thuoc, boolean hoanTat) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        if (data == null || data.getMaPhieuKham() == null)
            throw new IllegalArgumentException(MaThongBao.VL05);
        String ma = data.getMaPhieuKham();
        List<GhiNhanKham> dsG = ghiNhan == null ? List.of() : ghiNhan;
        List<ChiDinhDichVu> dsD = dichVu == null ? List.of() : dichVu;
        List<ChiTietCapThuoc> dsT = thuoc == null ? List.of() : thuoc;
        data.setChanDoan(chuanHoa(data.getChanDoan()));
        data.setKetQuaDieuTri(chuanHoa(data.getKetQuaDieuTri()));
        data.setTrieuChung(chuanHoa(data.getTrieuChung()));
        data.setKhamLamSang(chuanHoa(data.getKhamLamSang()));
        data.setTienSuTaiLanKham(chuanHoa(data.getTienSuTaiLanKham()));
        data.setDiUngTaiLanKham(chuanHoa(data.getDiUngTaiLanKham()));
        data.setThuocDangDung(chuanHoa(data.getThuocDangDung()));
        data.setDanDo(chuanHoa(data.getDanDo()));
        for (GhiNhanKham g : dsG) {
            g.setChanDoan(chuanHoa(g.getChanDoan()));
            g.setNoiDung(chuanHoa(g.getNoiDung()));
            g.setKetQua(chuanHoa(g.getKetQua()));
            g.setViTriKhac(chuanHoa(g.getViTriKhac()));
            kiemTraGhiNhan(g);
        }
        gioiHan(data.getChanDoan(), 500);
        gioiHan(data.getKetQuaDieuTri(), 500);
        gioiHan(data.getPhacDieuTri(), 500);
        gioiHan(data.getGhiChuThuocTuMua(), 500);
        gioiHan(data.getTrieuChung(), 1000);
        gioiHan(data.getKhamLamSang(), 2000);
        gioiHan(data.getTienSuTaiLanKham(), 1000);
        gioiHan(data.getDiUngTaiLanKham(), 1000);
        gioiHan(data.getThuocDangDung(), 1000);
        gioiHan(data.getDanDo(), 2000);
        if (hoanTat && rong(data.getChanDoan()) && dsG.stream()
                .noneMatch(g -> !rong(g.getChanDoan()) || g.getTrangThai() == GhiNhanKham.TrangThai.CHO_THUC_HIEN))
            throw new IllegalArgumentException(MaThongBao.BS07);
        Set<String> seen = new HashSet<>();
        for (ChiDinhDichVu d : dsD) {
            if (d == null || d.getMaDichVu() == null || !seen.add(d.getMaDichVu()))
                throw new IllegalArgumentException(MaThongBao.BS06);
            if (!ma.equals(d.getMaPhieuKham()))
                throw new IllegalArgumentException(MaThongBao.BS05);
            if (d.getSoLuong() < 1)
                throw new IllegalArgumentException(MaThongBao.VL12);
            if (!rong(d.getSoRang()))
                for (String r : d.getSoRang().split(","))
                    if (!laRangHopLe(r.trim()))
                        throw new IllegalArgumentException(MaThongBao.BS02);
        }
        seen.clear();
        for (ChiTietCapThuoc t : dsT) {
            if (t == null || t.getMaThuoc() == null || !seen.add(t.getMaThuoc()))
                throw new IllegalArgumentException(MaThongBao.BS06);
            if (!ma.equals(t.getMaPhieuKham()))
                throw new IllegalArgumentException(MaThongBao.BS05);
            if (t.getSoLuong() < 1)
                throw new IllegalArgumentException(MaThongBao.VL12);
            kiemTraThuoc(t);
            if (hoanTat && rong(t.getHuongDanString()))
                throw new IllegalArgumentException(MaThongBao.BS16);
        }
        Connection con = ConnectDB.getInstance().getConnection();
        // Source dùng một Connection singleton. Không cho hai thao tác bác sĩ trộn transaction trên cùng phiên.
        synchronized (con) {
            Map<GhiNhanKham, String> maMoi = new IdentityHashMap<>();
            boolean batDau = false;
            try {
                if (!con.getAutoCommit())
                    throw new IllegalStateException(MaThongBao.TB09);
                con.setAutoCommit(false);
                batDau = true;
                PhieuKham_DAO.khoaPhieu(con, ma);
                PhieuKham pk = layPhieuCuaToi(s, ma);
                if (pk.getTrangThai() != TrangThaiPhieuKham.DANG_KHAM)
                    throw new IllegalArgumentException(MaThongBao.TB10);
                if (pk.getPhienBan() != data.getPhienBan())
                    throw new IllegalArgumentException(MaThongBao.BS01);
                for (ChiDinhDichVu chiDinh : dsD)
                    chiDinh.setPhieuKham(pk);
                for (ChiTietCapThuoc capThuoc : dsT)
                    capThuoc.setPhieuKham(pk);
                Map<String, GhiNhanKham> cu = new LinkedHashMap<>();
                for (GhiNhanKham g : GhiNhanKham_DAO.layTheoPhieu(ma))
                    cu.put(g.getMaGhiNhan(), g);
                seen.clear();
                for (GhiNhanKham g : dsG) {
                    KeHoachDieuTri_BUS.kiemTraLienKet(s, con, pk, g.getHangMuc());
                    GhiNhanKham truoc = g.getMaGhiNhan() == null ? null : cu.get(g.getMaGhiNhan());
                    boolean doiHangMuc = truoc == null
                            || !Objects.equals(truoc.getHangMuc() == null ? null : truoc.getHangMuc().getMaHangMuc(),
                                    g.getHangMuc() == null ? null : g.getHangMuc().getMaHangMuc());
                    boolean doiThucHien = doiHangMuc || truoc.getTrangThai() != g.getTrangThai()
                            || !Objects.equals(truoc.getNgayThucHien(), g.getNgayThucHien());
                    if (doiHangMuc || doiThucHien)
                        KeHoachDieuTri_BUS.kiemTraThucHien(s, con, pk, g, doiHangMuc);
                    if (g.getPhieuKham() != null && !ma.equals(g.getPhieuKham().getMaPhieuKham()))
                        throw new IllegalArgumentException(MaThongBao.BS05);
                    if (g.getMaGhiNhan() != null && (!cu.containsKey(g.getMaGhiNhan()) || !seen.add(g.getMaGhiNhan())))
                        throw new IllegalArgumentException(MaThongBao.BS05);
                }
                Map<String, ChiDinhDichVu> dvCu = new HashMap<>();
                for (ChiDinhDichVu d : ChiDinhDichVu_DAO.layTheoPhieu(ma))
                    dvCu.put(d.getMaDichVu(), d);
                List<ChiDinhDichVu> dvLuu = new ArrayList<>();
                for (ChiDinhDichVu d : dsD) {
                    List<GhiNhanKham> lienKet = new ArrayList<>();
                    Set<GhiNhanKham> ids = Collections.newSetFromMap(new IdentityHashMap<>());
                    for (GhiNhanKham ref : d.getDsGhiNhan()) {
                        GhiNhanKham actual = dsG.stream()
                                .filter(g -> g == ref
                                        || (g.getMaGhiNhan() != null && g.getMaGhiNhan().equals(ref.getMaGhiNhan())))
                                .findFirst().orElseThrow(() -> new IllegalArgumentException(MaThongBao.BS05));
                        if (!ids.add(actual))
                            throw new IllegalArgumentException(MaThongBao.BS06);
                        lienKet.add(actual);
                    }
                    if (d.getHangMuc() != null && !lienKet.isEmpty()
                            && lienKet.stream().noneMatch(g -> g.getHangMuc() != null
                                    && Objects.equals(g.getHangMuc().getMaHangMuc(), d.getHangMuc().getMaHangMuc())))
                        throw new IllegalArgumentException(MaThongBao.BS05);
                    d.setDsGhiNhan(lienKet);
                    if (!lienKet.isEmpty())
                        d.setSoRang(String.join(",",
                                lienKet.stream().flatMap(g -> g.getDsRang().stream()).distinct().sorted().toList()));
                    for (KeHoachDieuTri.HangMuc h : d.getDsHangMuc()) {
                        KeHoachDieuTri_BUS.kiemTraLienKet(s, con, pk, h);
                        String phieuDaTinh = KeHoachDieuTri_DAO.phieuDaTinhPhi(con, h.getMaHangMuc(), d.getMaDichVu());
                        if (phieuDaTinh != null && !ma.equals(phieuDaTinh))
                            throw new IllegalArgumentException(MaThongBao.BS15 + " " + phieuDaTinh);
                    }
                    DichVu dv = DichVu_DAO.layDichVuTheoMa(d.getMaDichVu());
                    if (dv == null || dv.getTrangThai() != TrangThaiDichVu.DANG_PHUC_VU)
                        throw new IllegalArgumentException(MaThongBao.TB15);
                    kiemTraPhamViDichVu(dv, d);
                    ChiDinhDichVu n = new ChiDinhDichVu(ma, d.getMaDichVu());
                    n.setDichVu(dv);
                    n.setSoLuong(d.getSoLuong());
                    n.setSoRang(d.getSoRang());
                    n.setDonViTinh(dv.getDonViTinh());
                    n.setHangMuc(d.getHangMuc());
                    n.setDsGhiNhan(d.getDsGhiNhan());
                    n.setDonGia(dvCu.containsKey(d.getMaDichVu()) ? dvCu.get(d.getMaDichVu()).getDonGia()
                            : dv.getDonGiaNiemYet());
                    if (HoaDon_BUS.MA_DICH_VU_PHI_KHAM.equals(d.getMaDichVu()) && pk.getMaHoaDonPhiKham() != null)
                        n.setDonGia(donGiaTrongLuot(pk, dv));
                    dvLuu.add(n);
                }
                Map<String, Thuoc> danhMucThuoc = new HashMap<>();
                for (Thuoc t : Thuoc_DAO.getAllThuoc())
                    danhMucThuoc.put(t.getMaThuoc(), t);
                for (ChiTietCapThuoc t : dsT)
                    if (!danhMucThuoc.containsKey(t.getMaThuoc()))
                        throw new IllegalArgumentException(MaThongBao.BS06);
                GhiNhanKham_DAO.ghiNhatKy(con, ma, s.getMaNhanVien(), "TRUOC_LUU", null, null);
                data.setMaHoSo(HoSoBenhAn_BUS.taoHoSoNeuChuaCo(con, pk.getMaBenhNhan(), data.getTienSuTaiLanKham()));
                if (PhieuKham_DAO.luuChuyenMon(con, data, s.getMaNhanVien(), hoanTat) != 1)
                    throw new IllegalArgumentException(MaThongBao.BS01);
                KeHoachDieuTri_DAO.xoaLienKetPhi(con, ma);
                ChiDinhDichVu_DAO.xoaTheoPhieu(con, ma);
                for (GhiNhanKham g : dsG) {
                    boolean moi = g.getMaGhiNhan() == null;
                    String id = moi ? BoDem_DAO.layMaMoi(con, "GN", LocalDate.now()) : g.getMaGhiNhan();
                    GhiNhanKham_DAO.luu(con, id, ma, s.getMaNhanVien(), g, moi);
                    if (moi)
                        maMoi.put(g, id);
                    else
                        cu.remove(id);
                }
                Set<String> coKetQua = new HashSet<>();
                for (KetQuaKham k : KetQuaKham_DAO.layTheoPhieu(ma))
                    coKetQua.add(k.getGhiNhan().getMaGhiNhan());
                for (String id : cu.keySet()) {
                    if (coKetQua.contains(id))
                        throw new IllegalArgumentException(MaThongBao.BS22);
                    GhiNhanKham_DAO.xoa(con, id, ma);
                }
                for (ChiDinhDichVu d : dvLuu) {
                    List<GhiNhanKham> luuLienKet = new ArrayList<>();
                    for (GhiNhanKham g : d.getDsGhiNhan()) {
                        if (maMoi.containsKey(g)) {
                            GhiNhanKham n = new GhiNhanKham(maMoi.get(g));
                            n.setHangMuc(g.getHangMuc());
                            luuLienKet.add(n);
                        } else
                            luuLienKet.add(g);
                    }
                    d.setDsGhiNhan(luuLienKet);
                    ChiDinhDichVu_DAO.themChiDinh(con, d);
                    ChiDinhDichVu_DAO.themLienKet(con, d);
                    KeHoachDieuTri_DAO.ghiLienKetPhi(con, d);
                }
                ChiTietCapThuoc_DAO.xoaTheoPhieu(con, ma);
                for (ChiTietCapThuoc t : dsT) {
                    ChiTietCapThuoc n = t.saoChep(ma);
                    if (n.getNgayKe() == null)
                        n.setNgayKe(java.time.LocalDateTime.now());
                    n.setThuoc(danhMucThuoc.get(t.getMaThuoc()));
                    ChiTietCapThuoc_DAO.themChiTiet(con, n);
                }
                if (hoanTat)
                    GhiNhanKham_DAO.ghiNhatKy(con, ma, s.getMaNhanVien(), "HOAN_TAT", null, null);
                con.commit();
                maMoi.forEach(GhiNhanKham::setMaGhiNhan);
                data.setPhienBan(data.getPhienBan() + 1);
                data.setTrangThai(hoanTat ? TrangThaiPhieuKham.HOAN_TAT : TrangThaiPhieuKham.DANG_KHAM);
            } catch (SQLException | RuntimeException ex) {
                if (batDau)
                    try {
                        con.rollback();
                    } catch (SQLException rollback) {
                        ex.addSuppressed(rollback);
                    }
                throw ex instanceof RuntimeException re ? re : new RuntimeException(MaThongBao.TB09, ex);
            } finally {
                if (batDau)
                    try {
                        con.setAutoCommit(true);
                    } catch (SQLException ex) {
                        throw new RuntimeException(MaThongBao.TB09, ex);
                    }
            }
        }
    }

    public static void hoanTat(SessionContext s, String ma) {
        PhieuKham pk = layPhieuCuaToi(s, ma);
        luuKham(s, pk, layGhiNhan(s, ma), layChiDinhCuaToi(s, ma), layThuocCapCuaToi(s, ma), true);
    }

    public static void boSungSauHoanTat(SessionContext s, String ma, String lyDo, String noiDung) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        if (rong(lyDo) || rong(noiDung))
            throw new IllegalArgumentException(MaThongBao.BS08);
        gioiHan(lyDo, 1000);
        gioiHan(noiDung, 4000);
        Connection con = ConnectDB.getInstance().getConnection();
        synchronized (con) {
            boolean batDau = false;
            try {
                if (!con.getAutoCommit())
                    throw new IllegalStateException(MaThongBao.TB09);
                con.setAutoCommit(false);
                batDau = true;
                PhieuKham_DAO.khoaPhieu(con, ma);
                if (layPhieuCuaToi(s, ma).getTrangThai() != TrangThaiPhieuKham.HOAN_TAT)
                    throw new IllegalArgumentException(MaThongBao.TB10);
                GhiNhanKham_DAO.ghiNhatKy(con, ma, s.getMaNhanVien(), "BO_SUNG", lyDo.trim(), noiDung.trim());
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                if (batDau)
                    try {
                        con.rollback();
                    } catch (SQLException rollback) {
                        ex.addSuppressed(rollback);
                    }
                throw ex instanceof RuntimeException re ? re : new RuntimeException(MaThongBao.TB09, ex);
            } finally {
                if (batDau)
                    try {
                        con.setAutoCommit(true);
                    } catch (SQLException ex) {
                        throw new RuntimeException(MaThongBao.TB09, ex);
                    }
            }
        }
    }

    /** Giữ tương thích API cũ; đây là số lượt khám, tuyệt đối không dùng làm số buổi của kế hoạch. */
    @Deprecated
    public static int lanDieuTriThuHienTai(SessionContext s, String ma) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        return PhieuKham_DAO.demTheoBenhNhan(ma);
    }

    public static List<GhiNhanKham> lichSuRang(SessionContext s, String benhNhan, String rang) {
        QuyenUtil.kiemTraQuyen(s, ChucVu.BAC_SI);
        if (!laRangHopLe(rang))
            throw new IllegalArgumentException(MaThongBao.BS02);
        return GhiNhanKham_DAO.lichSuRang(benhNhan, rang);
    }

    public static void kiemTraPhamViDichVu(DichVu dv, ChiDinhDichVu d) {
        if (dv.getPhamViApDung() == DichVu.PhamViApDung.THEO_RANG && (rong(d.getSoRang())
                || d.getDsGhiNhan().stream().anyMatch(x -> x.getPhamVi() != GhiNhanKham.PhamVi.THEO_RANG)))
            throw new IllegalArgumentException(MaThongBao.VL19);
        if (dv.getPhamViApDung() == DichVu.PhamViApDung.TOAN_CANH
                && (!rong(d.getSoRang()) || d.getDsGhiNhan().stream().anyMatch(
                        x -> x.getPhamVi() != GhiNhanKham.PhamVi.HAI_HAM && x.getPhamVi() != GhiNhanKham.PhamVi.KHAC)))
            throw new IllegalArgumentException(MaThongBao.BS17);
    }

    public static void kiemTraThuoc(ChiTietCapThuoc t) {
        if (t == null || t.getMaThuoc() == null || t.getSoLuong() < 1)
            throw new IllegalArgumentException(MaThongBao.VL12);
        gioiHan(t.getGhiChuCapThuoc(), 300);
        gioiHan(t.getHamLuongDangDung(), 100);
        gioiHan(t.getLieuMoiLan(), 100);
        gioiHan(t.getDuongDung(), 50);
        gioiHan(t.getBuoiDung(), 100);
        gioiHan(t.getChanDoanLienQuan(), 500);
        if ((t.getSoLanNgay() != null && t.getSoLanNgay() < 1) || (t.getSoNgay() != null && t.getSoNgay() < 1))
            throw new IllegalArgumentException(MaThongBao.VL12);
    }

    private static String chuanHoa(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static boolean rong(String s) {
        return s == null || s.isBlank();
    }

    static void gioiHan(String s, int max) {
        if (s != null && s.length() > max)
            throw new IllegalArgumentException(MaThongBao.BS10);
    }

    private KhamBenh_BUS() {
    }
}
