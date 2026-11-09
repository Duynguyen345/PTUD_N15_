package vn.nhom15.nhakhoa.dao;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import vn.nhom15.nhakhoa.entity.*;

/** Kho lễ tân trong bộ nhớ cho --demo; dùng đúng các Entity OOP chuẩn của dự án. */
public final class LeTanDuLieuMauDAO extends LeTanDAO {
    private final PhongKhamDAO khoQuanLy;
    private final Map<String, BenhNhan> benhNhan = new LinkedHashMap<>();
    private final Map<String, PhongKham> phong = new LinkedHashMap<>();
    private final Map<String, PhieuKham> phieu = new LinkedHashMap<>();
    private final Map<String, HoaDon> hoaDon = new LinkedHashMap<>();
    private final Map<String, List<ChiDinhDichVu>> dichVuPhieu = new HashMap<>();
    private final Map<String, CaThuNgan> ca = new LinkedHashMap<>();
    private final Map<String, Integer> boDem = new HashMap<>();
    private final Map<String, Integer> soThuTu = new HashMap<>();

    public LeTanDuLieuMauDAO(PhongKhamDAO khoQuanLy) {
        super(() -> {
            throw new java.sql.SQLException("Chế độ demo không dùng JDBC.");
        });
        this.khoQuanLy = khoQuanLy;
        benhNhan.put("BN2601010001", new BenhNhan("BN2601010001", "Nguyễn Minh Phúc",
                "0909000001", null, LocalDate.of(1998, 5, 18), "Thủ Đức", null));
        benhNhan.put("BN2601010002", new BenhNhan("BN2601010002", "Trần Gia Linh",
                "0909000002", null, LocalDate.of(2000, 3, 21), "Gò Vấp", "Dị ứng penicillin"));

        phong.put("P001", new PhongKham("P001", "Phòng khám 1", false, "NV2601010003", "NV2601010004"));
        phong.put("P002", new PhongKham("P002", "Phòng khám 2", false, "NV2601010008", "NV2601010010"));
        phong.put("P003", new PhongKham("P003", "Phòng khám 3", true, "NV2601010003", "NV2601010004"));
        phong.put("P004", new PhongKham("P004", "Phòng khám 4", true, "NV2601010008", "NV2601010010"));

        String ma = "PK2601019999";
        PhieuKham pkMau = new PhieuKham(ma);
        pkMau.setNgayKham(LocalDate.now());
        pkMau.setSoThuTu(1);
        pkMau.setLyDoKham("Khám và lấy cao răng");
        pkMau.setTrangThai(TrangThaiPhieuKham.HOAN_TAT);
        pkMau.setBenhNhan(benhNhan.get("BN2601010002"));
        NhanVien bs = new NhanVien("NV2601010003");
        bs.setHoTen("Nguyễn Văn An");
        pkMau.setNhanVien(bs);
        pkMau.setPhong(phong.get("P001"));
        phieu.put(ma, pkMau);

        ChiDinhDichVu cd = new ChiDinhDichVu(ma, "DV0002");
        DichVu dv = new DichVu("DV0002");
        dv.setTenDichVu("Lấy cao răng");
        cd.setDichVu(dv);
        cd.setDonGia(new BigDecimal("250000"));
        dichVuPhieu.put(ma, List.of(cd));
    }

    private String maMoi(String loai, Set<String> daCo) {
        String dau = loai + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int so = boDem.getOrDefault(dau, 0);
        String ma;
        do {
            if (++so > 9999)
                throw new IllegalArgumentException("Đã hết dải mã " + dau + ".");
            ma = dau + String.format("%04d", so);
        } while (daCo.contains(ma));
        boDem.put(dau, so);
        return ma;
    }

    @Override
    public synchronized List<BenhNhan> danhSachBenhNhan() {
        return List.copyOf(benhNhan.values());
    }

    @Override
    public synchronized List<BenhNhan> traCuuBenhNhan(String tieuChi, String tuKhoa) {
        String tim = tuKhoa.toLowerCase(Locale.ROOT);
        return benhNhan.values().stream().filter(b -> switch (tieuChi) {
            case "SĐT" -> b.getSoDienThoai().contains(tim);
            case "Họ tên" -> b.getHoTen().toLowerCase(Locale.ROOT).contains(tim);
            default -> b.getMaBenhNhan().toLowerCase(Locale.ROOT).contains(tim);
        }).toList();
    }

    @Override
    public synchronized List<PhieuKham> lichSuPhieuKham(String maBenhNhan) {
        return phieu.values().stream()
                .filter(p -> Objects.equals(p.getMaBenhNhan(), maBenhNhan))
                .sorted(Comparator.comparing(PhieuKham::getNgayKham).reversed())
                .toList();
    }

    @Override
    public synchronized List<BenhNhan> timBenhNhan(String tuKhoa) {
        String tim = tuKhoa.toLowerCase(Locale.ROOT);
        return benhNhan.values().stream()
                .filter(b -> b.getMaBenhNhan().toLowerCase(Locale.ROOT).contains(tim)
                        || b.getHoTen().toLowerCase(Locale.ROOT).contains(tim)
                        || b.getSoDienThoai().contains(tim)
                        || (b.getCccd() != null && b.getCccd().contains(tim)))
                .toList();
    }

    @Override
    public synchronized BenhNhan luuBenhNhan(BenhNhan bn) {
        for (BenhNhan cu : benhNhan.values()) {
            if (Objects.equals(cu.getMaBenhNhan(), bn.getMaBenhNhan()))
                continue;
            if (cu.getSoDienThoai().equals(bn.getSoDienThoai()))
                throw new IllegalArgumentException("Số điện thoại đã có trong hồ sơ khác.");
            if (bn.getCccd() != null && bn.getCccd().equals(cu.getCccd()))
                throw new IllegalArgumentException("CCCD đã có trong hồ sơ khác.");
        }
        if (bn.getMaBenhNhan() != null && !benhNhan.containsKey(bn.getMaBenhNhan()))
            throw new IllegalArgumentException("Hồ sơ bệnh nhân không còn tồn tại.");
        String ma = bn.getMaBenhNhan() == null ? maMoi("BN", benhNhan.keySet()) : bn.getMaBenhNhan();
        BenhNhan moi = new BenhNhan(ma, bn.getHoTen(), bn.getSoDienThoai(), bn.getCccd(),
                bn.getNgaySinh(), bn.getDiaChi(), bn.getTienSuDiUng());
        benhNhan.put(ma, moi);
        return moi;
    }

    @Override
    public synchronized List<PhongKham> danhSachPhong() {
        return List.copyOf(phong.values());
    }

    @Override
    public synchronized List<PhieuKham> danhSachLuot(String maPhong, LocalDate ngay) {
        return phieu.values().stream()
                .filter(p -> Objects.equals(p.getMaPhong(), maPhong)
                        && Objects.equals(p.getNgayKham(), ngay)
                        && p.getTrangThai() != TrangThaiPhieuKham.HOAN_TAT)
                .sorted(Comparator.comparingInt(PhieuKham::getSoThuTu))
                .toList();
    }

    @Override
    public synchronized List<PhieuKham> phieuChoThanhToan() {
        return phieu.values().stream()
                .filter(p -> p.getTrangThai() == TrangThaiPhieuKham.HOAN_TAT
                        && dichVuPhieu.containsKey(p.getMaPhieuKham())
                        && hoaDon.values().stream().noneMatch(h -> p.getMaPhieuKham().equals(h.getMaPhieuKham())))
                .toList();
    }

    @Override
    public synchronized HoaDon phiChuaDung(String maBenhNhan) {
        return hoaDon.values().stream()
                .filter(h -> h.getMaPhieuKham() == null && Objects.equals(h.getMaBenhNhan(), maBenhNhan)
                        && phieu.values().stream().noneMatch(p -> h.getMaHoaDon().equals(p.getMaHoaDonPhiKham())))
                .reduce((a, b) -> b).orElse(null);
    }

    @Override
    public synchronized HoaDon thuPhi(String maBenhNhan, String hinhThucThanhToan,
            BigDecimal tienKhachDua, String maNhanVien) {
        BenhNhan bn = benhNhan.get(maBenhNhan);
        if (bn == null)
            throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
        if (phiChuaDung(maBenhNhan) != null)
            throw new IllegalArgumentException("Bệnh nhân đã có phí khám chưa dùng.");
        DichVu dichVu = khoQuanLy.danhSachDichVu().stream()
                .filter(d -> d.getMaDichVu().equals("DV0001") && d.getTrangThai() == TrangThaiDichVu.DANG_PHUC_VU)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chưa có dịch vụ phí khám DV0001 đang phục vụ."));
        BigDecimal phi = dichVu.getDonGiaNiemYet();
        if (tienKhachDua.compareTo(phi) < 0)
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");

        String ma = maMoi("HD", hoaDon.keySet());
        HoaDon hd = new HoaDon(ma);
        hd.setNgayThu(LocalDateTime.now());
        hd.setHinhThucThanhToan(hinhThucThanhToan);
        hd.setDaThanhToan(true);
        hd.setTienKhachDua(tienKhachDua);
        hd.setBenhNhan(bn);
        hd.setMaNhanVien(maNhanVien);

        ChiTietHoaDon ct = new ChiTietHoaDon(ma, "DV0001", 1, phi);
        ct.setDichVu(dichVu);
        hd.themChiTiet(ct);

        hoaDon.put(ma, hd);
        return hd;
    }

    @Override
    public synchronized PhieuKham lapPhieu(String maBenhNhan, String maPhong, String lyDo) {
        PhongKham p = phong.get(maPhong);
        if (p == null || !p.isDangNhanBenh() || p.getMaBacSi() == null || p.getMaPhuTa() == null)
            throw new IllegalArgumentException("Phòng chưa nhận bệnh hoặc chưa phân công nhân sự.");
        List<NhanVien> nhanSu = khoQuanLy.danhSachNhanVien();
        NhanVien bacSi = nhanSu.stream()
                .filter(n -> n.getMaNhanVien().equals(p.getMaBacSi()) && n.getChucVu() == ChucVu.BAC_SI
                        && n.isConLamViec())
                .findFirst().orElse(null);
        NhanVien phuTa = nhanSu.stream()
                .filter(n -> n.getMaNhanVien().equals(p.getMaPhuTa()) && n.getChucVu() == ChucVu.PHU_TA
                        && n.isConLamViec())
                .findFirst().orElse(null);
        if (bacSi == null || phuTa == null)
            throw new IllegalArgumentException("Bác sĩ hoặc phụ tá không còn làm việc đúng phân công.");
        BenhNhan bn = benhNhan.get(maBenhNhan);
        if (bn == null)
            throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
        HoaDon phi = phiChuaDung(maBenhNhan);
        if (phi == null)
            throw new IllegalArgumentException("Bệnh nhân chưa có phí khám đã thu và chưa dùng.");

        String ma = maMoi("PK", phieu.keySet());
        String khoa = LocalDate.now() + maPhong;
        int stt = soThuTu.merge(khoa, 1, Integer::sum);

        PhieuKham moi = new PhieuKham(ma);
        moi.setNgayKham(LocalDate.now());
        moi.setSoThuTu(stt);
        moi.setLyDoKham(lyDo);
        moi.setTrangThai(TrangThaiPhieuKham.CHO_KHAM);
        moi.setBenhNhan(bn);
        moi.setNhanVien(bacSi);
        moi.setPhong(p);
        moi.setHoaDonPhiKham(phi);
        phieu.put(ma, moi);
        return moi;
    }

    @Override
    public synchronized List<ChiDinhDichVu> dichVuTheoPhieu(String maPhieu) {
        return List.copyOf(dichVuPhieu.getOrDefault(maPhieu, List.of()));
    }

    @Override
    public synchronized HoaDon thanhToanDichVu(String maPhieu, String hinhThucThanhToan,
            BigDecimal tienKhachDua, String maNhanVien) {
        PhieuKham pk = phieu.get(maPhieu);
        if (pk == null || pk.getTrangThai() != TrangThaiPhieuKham.HOAN_TAT)
            throw new IllegalArgumentException("Lượt khám chưa hoàn tất hoặc không tồn tại.");
        if (hoaDon.values().stream().anyMatch(h -> maPhieu.equals(h.getMaPhieuKham())))
            throw new IllegalArgumentException("Lượt khám đã thanh toán.");
        List<ChiDinhDichVu> ds = dichVuTheoPhieu(maPhieu);
        if (ds.isEmpty())
            throw new IllegalArgumentException("Lượt khám chưa có dịch vụ cần thu.");
        BigDecimal tong = ds.stream().map(ChiDinhDichVu::getDonGia).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (tienKhachDua.compareTo(tong) < 0)
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");

        String ma = maMoi("HD", hoaDon.keySet());
        HoaDon hd = new HoaDon(ma);
        hd.setNgayThu(LocalDateTime.now());
        hd.setHinhThucThanhToan(hinhThucThanhToan);
        hd.setDaThanhToan(true);
        hd.setTienKhachDua(tienKhachDua);
        hd.setBenhNhan(pk.getBenhNhan());
        hd.setMaNhanVien(maNhanVien);
        hd.setMaPhieuKham(maPhieu);
        for (ChiDinhDichVu cd : ds) {
            ChiTietHoaDon ct = new ChiTietHoaDon(ma, cd.getMaDichVu(), 1, cd.getDonGia());
            ct.setDichVu(cd.getDichVu());
            hd.themChiTiet(ct);
        }
        hoaDon.put(ma, hd);
        return hd;
    }

    @Override
    public synchronized CaThuNgan caDangMo() {
        return ca.values().stream().filter(CaThuNgan::isDangMo).findFirst().orElse(null);
    }

    @Override
    public synchronized CaThuNgan moCa(String maNhanVien) {
        if (caDangMo() != null)
            throw new IllegalArgumentException("Quầy đã có ca đang mở.");
        String ten = khoQuanLy.danhSachNhanVien().stream()
                .filter(n -> n.getMaNhanVien().equals(maNhanVien))
                .map(NhanVien::getHoTen).findFirst().orElse(maNhanVien);
        String ma = maMoi("CT", ca.keySet());
        CaThuNgan moi = new CaThuNgan(ma);
        NhanVien nv = new NhanVien(maNhanVien);
        nv.setHoTen(ten);
        moi.setNhanVien(nv);
        moi.setThoiGianMo(LocalDateTime.now());
        moi.setTrangThai(CaThuNgan.DANG_MO);
        ca.put(ma, moi);
        return moi;
    }

    @Override
    public synchronized CaThuNgan chotCa(String maCa) {
        CaThuNgan cu = ca.get(maCa);
        if (cu == null || !cu.isDangMo())
            throw new IllegalArgumentException("Ca đã chốt hoặc không tồn tại.");
        LocalDateTime lucChot = LocalDateTime.now();
        List<HoaDon> ds = hoaDonTrongCa(cu);
        BigDecimal dichVu = ds.stream()
                .filter(h -> h.getMaPhieuKham() != null)
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal phi = ds.stream()
                .filter(h -> h.getMaPhieuKham() == null)
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cu.setThoiGianChot(lucChot);
        cu.setDoanhThuDichVu(dichVu);
        cu.setPhiThuHo(phi);
        cu.setTrangThai(CaThuNgan.DA_CHOT);
        return cu;
    }

    @Override
    public synchronized List<HoaDon> hoaDonTrongCa(CaThuNgan ca) {
        if (ca == null || ca.getThoiGianMo() == null)
            return List.of();
        LocalDateTime den = ca.isDangMo() ? LocalDateTime.now() : ca.getThoiGianChot();
        return hoaDon.values().stream()
                .filter(h -> !h.getNgayThu().isBefore(ca.getThoiGianMo()) && !h.getNgayThu().isAfter(den))
                .toList();
    }

    @Override
    public synchronized List<CaThuNgan> lichSuCa() {
        List<CaThuNgan> ds = new ArrayList<>(ca.values());
        Collections.reverse(ds);
        return ds;
    }
}
