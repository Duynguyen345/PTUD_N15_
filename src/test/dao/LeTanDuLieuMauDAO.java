package dao;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import entity.*;

/** Kho lễ tân trong bộ nhớ cho --demo; không chạm JDBC. */
public final class LeTanDuLieuMauDAO extends LeTanDAO {
    private final BoDAO khoQuanLy;
    private final Map<String, BenhNhan> benhNhan = new LinkedHashMap<>();
    private final Map<String, PhongLeTan> phong = new LinkedHashMap<>();
    private final Map<String, PhieuLeTan> phieu = new LinkedHashMap<>();
    private final Map<String, HoaDonLeTan> hoaDon = new LinkedHashMap<>();
    private final Map<String, List<DichVuThanhToan>> dichVuPhieu = new HashMap<>();
    private final Map<String, CaThuNgan> ca = new LinkedHashMap<>();
    private final Map<String, Integer> boDem = new HashMap<>();
    private final Map<String, Integer> soThuTu = new HashMap<>();

    public LeTanDuLieuMauDAO(BoDAO khoQuanLy) {
        super(() -> { throw new java.sql.SQLException("Chế độ demo không dùng JDBC."); });
        this.khoQuanLy = khoQuanLy;
        benhNhan.put("BN2601010001", new BenhNhan("BN2601010001", "Nguyễn Minh Phúc",
                "0909000001", LocalDate.of(1998, 5, 18), null, "Thủ Đức", null));
        benhNhan.put("BN2601010002", new BenhNhan("BN2601010002", "Trần Gia Linh",
                "0909000002", LocalDate.of(2000, 3, 21), null, "Gò Vấp", "Dị ứng penicillin"));
        phong.put("P001", new PhongLeTan("P001", "Phòng khám 1", false,
                "NV2601010003", "Nguyễn Văn An", "NV2601010004", "Trần Minh Bình", 0, 0));
        phong.put("P002", new PhongLeTan("P002", "Phòng khám 2", false,
                "NV2601010008", "Bùi Đức Tâm", "NV2601010010", "Lê Minh Bình", 0, 0));
        phong.put("P003", new PhongLeTan("P003", "Phòng khám 3", true,
                "NV2601010003", "Nguyễn Văn An", "NV2601010004", "Trần Minh Bình", 0, 0));
        phong.put("P004", new PhongLeTan("P004", "Phòng khám 4", true,
                "NV2601010008", "Bùi Đức Tâm", "NV2601010010", "Lê Minh Bình", 0, 0));
        String ma = "PK2601019999";
        phieu.put(ma, new PhieuLeTan(ma, LocalDate.now(), 1, "P001", "BN2601010002",
                "Trần Gia Linh", "NV2601010003", "Nguyễn Văn An", "Khám và lấy cao răng",
                "HOAN_TAT", null));
        dichVuPhieu.put(ma, List.of(new DichVuThanhToan("DV0002", "Lấy cao răng",
                new BigDecimal("250000"))));
    }

    private String maMoi(String loai, Set<String> daCo) {
        String dau = loai + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int so = boDem.getOrDefault(dau, 0);
        String ma;
        do {
            if (++so > 9999) throw new IllegalArgumentException("Đã hết dải mã " + dau + ".");
            ma = dau + String.format("%04d", so);
        } while (daCo.contains(ma));
        boDem.put(dau, so);
        return ma;
    }

    @Override public synchronized List<BenhNhan> danhSachBenhNhan() {
        return List.copyOf(benhNhan.values());
    }

    @Override public synchronized List<BenhNhan> traCuuBenhNhan(String tieuChi, String tuKhoa) {
        String tim = tuKhoa.toLowerCase(Locale.ROOT);
        return benhNhan.values().stream().filter(b -> switch (tieuChi) {
            case "SĐT" -> b.soDienThoai().contains(tim);
            case "Họ tên" -> b.hoTen().toLowerCase(Locale.ROOT).contains(tim);
            default -> b.maBenhNhan().toLowerCase(Locale.ROOT).contains(tim);
        }).toList();
    }

    @Override public synchronized List<PhieuLeTan> lichSuPhieuKham(String maBenhNhan) {
        return phieu.values().stream().filter(p -> p.maBenhNhan().equals(maBenhNhan))
                .sorted(Comparator.comparing(PhieuLeTan::ngayKham).reversed()).toList();
    }

    @Override public synchronized List<BenhNhan> timBenhNhan(String tuKhoa) {
        String tim = tuKhoa.toLowerCase(Locale.ROOT);
        return benhNhan.values().stream().filter(b -> b.maBenhNhan().toLowerCase(Locale.ROOT).contains(tim)
                || b.hoTen().toLowerCase(Locale.ROOT).contains(tim) || b.soDienThoai().contains(tim)
                || (b.cccd() != null && b.cccd().contains(tim))).toList();
    }

    @Override public synchronized BenhNhan luuBenhNhan(BenhNhan bn) {
        for (BenhNhan cu : benhNhan.values()) {
            if (Objects.equals(cu.maBenhNhan(), bn.maBenhNhan())) continue;
            if (cu.soDienThoai().equals(bn.soDienThoai()))
                throw new IllegalArgumentException("Số điện thoại đã có trong hồ sơ khác.");
            if (bn.cccd() != null && bn.cccd().equals(cu.cccd()))
                throw new IllegalArgumentException("CCCD đã có trong hồ sơ khác.");
        }
        if (bn.maBenhNhan() != null && !benhNhan.containsKey(bn.maBenhNhan()))
            throw new IllegalArgumentException("Hồ sơ bệnh nhân không còn tồn tại.");
        String ma = bn.maBenhNhan() == null ? maMoi("BN", benhNhan.keySet()) : bn.maBenhNhan();
        BenhNhan moi = new BenhNhan(ma, bn.hoTen(), bn.soDienThoai(), bn.ngaySinh(),
                bn.cccd(), bn.diaChi(), bn.tienSuDiUng());
        benhNhan.put(ma, moi);
        return moi;
    }

    @Override public synchronized List<PhongLeTan> danhSachPhong() {
        return phong.values().stream().map(p -> new PhongLeTan(p.maPhong(), p.tenPhong(), p.dangNhanBenh(),
                p.maBacSi(), p.tenBacSi(), p.maPhuTa(), p.tenPhuTa(),
                (int) phieu.values().stream().filter(pk -> pk.maPhong().equals(p.maPhong())
                        && pk.ngayKham().equals(LocalDate.now()) && pk.trangThai().equals("CHO_KHAM")).count(),
                (int) phieu.values().stream().filter(pk -> pk.maPhong().equals(p.maPhong())
                        && pk.ngayKham().equals(LocalDate.now()) && pk.trangThai().equals("DANG_KHAM")).count()))
                .toList();
    }

    @Override public synchronized List<PhieuLeTan> danhSachLuot(String maPhong, LocalDate ngay) {
        return phieu.values().stream().filter(p -> p.maPhong().equals(maPhong) && p.ngayKham().equals(ngay)
                && !p.trangThai().equals("HOAN_TAT")).sorted(Comparator.comparingInt(PhieuLeTan::soThuTu)).toList();
    }

    @Override public synchronized List<PhieuLeTan> phieuChoThanhToan() {
        return phieu.values().stream().filter(p -> p.trangThai().equals("HOAN_TAT")
                && dichVuPhieu.containsKey(p.maPhieuKham())
                && hoaDon.values().stream().noneMatch(h -> p.maPhieuKham().equals(h.maPhieuKham()))).toList();
    }

    @Override public synchronized HoaDonLeTan phiChuaDung(String maBenhNhan) {
        return hoaDon.values().stream().filter(h -> h.laPhiKham() && h.maBenhNhan().equals(maBenhNhan)
                && phieu.values().stream().noneMatch(p -> h.maHoaDon().equals(p.maHoaDonPhiKham())))
                .reduce((a, b) -> b).orElse(null);
    }

    @Override public synchronized HoaDonLeTan thuPhi(String maBenhNhan, HinhThucThanhToan hinhThuc,
            BigDecimal tienKhachDua, String maNhanVien) {
        yeuCauCa();
        BenhNhan bn = benhNhan.get(maBenhNhan);
        if (bn == null) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
        if (phiChuaDung(maBenhNhan) != null)
            throw new IllegalArgumentException("Bệnh nhân đã có phí khám chưa dùng.");
        DichVu dichVu = khoQuanLy.dichVu().danhSachDichVu().stream().filter(d -> d.getMaDichVu().equals("DV0001")
                && d.getTrangThai() == TrangThaiDichVu.DANG_PHUC_VU).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chưa có dịch vụ phí khám DV0001 đang phục vụ."));
        BigDecimal phi = dichVu.getDonGiaNiemYet();
        if (tienKhachDua.compareTo(phi) < 0) throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");
        String ma = maMoi("HD", hoaDon.keySet());
        HoaDonLeTan hd = new HoaDonLeTan(ma, maBenhNhan, bn.hoTen(), null,
                LocalDateTime.now(), phi, tienKhachDua, hinhThuc, maNhanVien);
        hoaDon.put(ma, hd);
        return hd;
    }

    @Override public synchronized PhieuLeTan lapPhieu(String maBenhNhan, String maPhong, String lyDo) {
        PhongLeTan p = phong.get(maPhong);
        if (p == null || !p.sanSang())
            throw new IllegalArgumentException("Phòng chưa nhận bệnh hoặc chưa phân công nhân sự.");
        List<NhanVien> nhanSu = khoQuanLy.nhanVien().danhSachNhanVien();
        boolean bacSi = nhanSu.stream().anyMatch(n -> n.getMaNhanVien().equals(p.maBacSi())
                && n.getChucVu() == ChucVu.BAC_SI && n.isConLamViec());
        boolean phuTa = nhanSu.stream().anyMatch(n -> n.getMaNhanVien().equals(p.maPhuTa())
                && n.getChucVu() == ChucVu.PHU_TA && n.isConLamViec());
        if (!bacSi || !phuTa)
            throw new IllegalArgumentException("Bác sĩ hoặc phụ tá không còn làm việc đúng phân công.");
        BenhNhan bn = benhNhan.get(maBenhNhan);
        if (bn == null) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
        HoaDonLeTan phi = phiChuaDung(maBenhNhan);
        if (phi == null) throw new IllegalArgumentException("Bệnh nhân chưa có phí khám đã thu và chưa dùng.");
        String ma = maMoi("PK", phieu.keySet());
        String khoa = LocalDate.now() + maPhong;
        int stt = soThuTu.merge(khoa, 1, Integer::sum);
        PhieuLeTan moi = new PhieuLeTan(ma, LocalDate.now(), stt, maPhong, maBenhNhan,
                bn.hoTen(), p.maBacSi(), p.tenBacSi(), lyDo, "CHO_KHAM", phi.maHoaDon());
        phieu.put(ma, moi);
        return moi;
    }

    @Override public synchronized List<DichVuThanhToan> dichVuTheoPhieu(String maPhieu) {
        return List.copyOf(dichVuPhieu.getOrDefault(maPhieu, List.of()));
    }

    @Override public synchronized HoaDonLeTan thanhToanDichVu(String maPhieu,
            HinhThucThanhToan hinhThuc, BigDecimal tienKhachDua, String maNhanVien) {
        yeuCauCa();
        PhieuLeTan pk = phieu.get(maPhieu);
        if (pk == null || !pk.trangThai().equals("HOAN_TAT"))
            throw new IllegalArgumentException("Lượt khám chưa hoàn tất hoặc không tồn tại.");
        if (hoaDon.values().stream().anyMatch(h -> maPhieu.equals(h.maPhieuKham())))
            throw new IllegalArgumentException("Lượt khám đã thanh toán.");
        List<DichVuThanhToan> ds = dichVuTheoPhieu(maPhieu);
        if (ds.isEmpty()) throw new IllegalArgumentException("Lượt khám chưa có dịch vụ cần thu.");
        BigDecimal tong = ds.stream().map(DichVuThanhToan::donGia).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (tienKhachDua.compareTo(tong) < 0) throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");
        String ma = maMoi("HD", hoaDon.keySet());
        HoaDonLeTan hd = new HoaDonLeTan(ma, pk.maBenhNhan(), pk.tenBenhNhan(), maPhieu,
                LocalDateTime.now(), tong, tienKhachDua, hinhThuc, maNhanVien);
        hoaDon.put(ma, hd);
        return hd;
    }

    private void yeuCauCa() {
        if (ca.values().stream().noneMatch(CaThuNgan::dangMo))
            throw new IllegalArgumentException("Chưa nhận ca thu ngân. Mở ca trước khi thu tiền.");
    }

    @Override public synchronized CaThuNgan caDangMo() {
        return ca.values().stream().filter(CaThuNgan::dangMo).findFirst().orElse(null);
    }

    @Override public synchronized CaThuNgan moCa(String maNhanVien) {
        if (caDangMo() != null) throw new IllegalArgumentException("Quầy đã có ca đang mở.");
        String ten = khoQuanLy.nhanVien().danhSachNhanVien().stream().filter(n -> n.getMaNhanVien().equals(maNhanVien))
                .map(NhanVien::getHoTen).findFirst().orElse(maNhanVien);
        String ma = maMoi("CT", ca.keySet());
        CaThuNgan moi = new CaThuNgan(ma, maNhanVien, ten, LocalDateTime.now(), null, null, null);
        ca.put(ma, moi);
        return moi;
    }

    @Override public synchronized CaThuNgan chotCa(String maCa) {
        CaThuNgan cu = ca.get(maCa);
        if (cu == null || !cu.dangMo()) throw new IllegalArgumentException("Ca đã chốt hoặc không tồn tại.");
        LocalDateTime lucChot = LocalDateTime.now();
        List<HoaDonLeTan> ds = hoaDonTrongCa(cu);
        BigDecimal dichVu = ds.stream().filter(h -> !h.laPhiKham()).map(HoaDonLeTan::tongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal phi = ds.stream().filter(HoaDonLeTan::laPhiKham).map(HoaDonLeTan::tongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CaThuNgan moi = new CaThuNgan(maCa, cu.maNhanVien(), cu.tenNhanVien(), cu.moLuc(),
                lucChot, dichVu, phi);
        ca.put(maCa, moi);
        return moi;
    }

    @Override public synchronized List<HoaDonLeTan> hoaDonTrongCa(CaThuNgan ca) {
        LocalDateTime den = ca.dangMo() ? LocalDateTime.now() : ca.chotLuc();
        return hoaDon.values().stream().filter(h -> !h.ngayThu().isBefore(ca.moLuc())
                && !h.ngayThu().isAfter(den)).toList();
    }

    @Override public synchronized List<CaThuNgan> lichSuCa() {
        List<CaThuNgan> ds = new ArrayList<>(ca.values());
        Collections.reverse(ds);
        return ds;
    }
}
