package vn.nhom15.nhakhoa.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.entity.DuLieuBaoCao.DongThongKe;
import vn.nhom15.nhakhoa.util.BoBamMatKhau;

/** Kho giả lập chỉ dùng trong kiểm thử; ứng dụng chính luôn dùng JDBC. */
public final class PhongKhamDuLieuMauDAO extends PhongKhamDAO {
    private final Map<String, NhanVien> danhSachNhanVien = new LinkedHashMap<>();
    private final Map<String, TaiKhoan> danhSachTaiKhoan = new LinkedHashMap<>();
    private final Map<String, DichVu> danhSachDichVu = new LinkedHashMap<>();
    private final Map<String, HangHoa> danhSachHangHoa = new LinkedHashMap<>();
    private final Set<String> nhanVienDaThamChieu = new HashSet<>();
    private int boDemDichVu;
    private final Map<String, Integer> cacBoDem = new HashMap<>();
    private final List<BoDuLieuMau.LuotKhamMau> danhSachLuotKham;

    public PhongKhamDuLieuMauDAO(BoBamMatKhau boBamMatKhau) {
        super(() -> {
            throw new java.sql.SQLException("Chế độ dữ liệu mẫu không sử dụng kết nối JDBC.");
        });
        BoDuLieuMau boDuLieu = new BoDuLieuMau(LocalDate.now());
        String bamMatKhau = boBamMatKhau.bamMatKhau("123456".toCharArray());
        for (NhanVien nhanVien : boDuLieu.danhSachNhanVien()) {
            danhSachNhanVien.put(nhanVien.getMaNhanVien(), nhanVien);
            ghiNhanMaTheoNgay(nhanVien.getMaNhanVien());
        }
        for (BoDuLieuMau.TaiKhoanMau duLieuKhoiTao : boDuLieu.danhSachTaiKhoan()) {
            String maTaiKhoan = taoMaTaiKhoan(LocalDate.of(2026, 1, 1));
            danhSachTaiKhoan.put(maTaiKhoan, new TaiKhoan(maTaiKhoan, bamMatKhau, duLieuKhoiTao.trangThai(),
                    duLieuKhoiTao.maNhanVien()));
        }
        for (DichVu dichVu : boDuLieu.danhSachDichVu()) {
            danhSachDichVu.put(dichVu.getMaDichVu(), dichVu);
            boDemDichVu = Math.max(boDemDichVu, Integer.parseInt(dichVu.getMaDichVu().substring(2)));
        }
        for (HangHoa hangHoa : boDuLieu.danhSachHangHoa()) {
            danhSachHangHoa.put(hangHoa.ma(), hangHoa);
            ghiNhanMaTheoNgay(hangHoa.ma());
        }
        danhSachLuotKham = boDuLieu.danhSachLuotKham();
        danhSachLuotKham.forEach(luotKham -> nhanVienDaThamChieu.add(luotKham.maNhanVien()));
    }

    private void ghiNhanMaTheoNgay(String ma) {
        cacBoDem.merge(ma.substring(0, 8), Integer.parseInt(ma.substring(8)), Math::max);
    }

    private String taoMaTheoNgay(String loai, LocalDate ngay, java.util.function.Predicate<String> daTonTai) {
        String tienTo = loai + ngay.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        int soThuTu = cacBoDem.getOrDefault(tienTo, 0);
        String ma;
        do {
            if (++soThuTu > 9999)
                throw new IllegalArgumentException("Đã hết dải mã " + tienTo + ".");
            ma = tienTo + String.format("%04d", soThuTu);
        } while (daTonTai.test(ma));
        cacBoDem.put(tienTo, soThuTu);
        return ma;
    }

    private NhanVien saoChep(NhanVien n) {
        return new NhanVien(n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(),
                n.getGioiTinh(), n.getCccd(), n.getChucVu(), n.isConLamViec());
    }

    private TaiKhoan saoChep(TaiKhoan t) {
        return new TaiKhoan(t.getMaTaiKhoan(), t.getMatKhau(), t.getTrangThai(), t.getMaNhanVien());
    }

    private DichVu saoChep(DichVu d) {
        return new DichVu(d.getMaDichVu(), d.getTenDichVu(), d.getDonGiaNiemYet(), d.getTrangThai());
    }

    @Override
    public void kiemTraKetNoi() {
    }

    @Override
    public synchronized List<NhanVien> danhSachNhanVien() {
        return danhSachNhanVien.values().stream().map(this::saoChep).toList();
    }

    @Override
    public synchronized void luuNhanVien(NhanVien nhanVien, boolean themMoi) {
        NhanVien giaTri = saoChep(nhanVien);
        if (themMoi)
            giaTri.setMaNhanVien(taoMaTheoNgay("NV", LocalDate.now(), danhSachNhanVien::containsKey));
        else if (!danhSachNhanVien.containsKey(giaTri.getMaNhanVien()))
            throw new IllegalArgumentException("Nhân viên không tồn tại.");
        danhSachNhanVien.put(giaTri.getMaNhanVien(), giaTri);
    }

    @Override
    public synchronized void xoaNhanVien(String ma) {
        if (danhSachTaiKhoan.values().stream().anyMatch(t -> t.getMaNhanVien().equals(ma))
                || nhanVienDaThamChieu.contains(ma))
            throw new IllegalArgumentException("Nhân viên đang có tài khoản hoặc phiếu khám, không thể xóa.");
        if (danhSachNhanVien.remove(ma) == null)
            throw new IllegalArgumentException("Nhân viên không tồn tại.");
    }

    @Override
    public synchronized List<TaiKhoan> danhSachTaiKhoan() {
        return danhSachTaiKhoan.values().stream().map(this::saoChep).toList();
    }

    @Override
    public synchronized TaiKhoan timTaiKhoan(String maTaiKhoan) {
        return danhSachTaiKhoan.values().stream().filter(t -> t.getMaTaiKhoan().equalsIgnoreCase(maTaiKhoan))
                .findFirst().map(this::saoChep).orElse(null);
    }

    private String taoMaTaiKhoan(LocalDate ngay) {
        return taoMaTheoNgay("TK", ngay, danhSachTaiKhoan::containsKey);
    }

    @Override
    public synchronized String taoTaiKhoan(String maNhanVien, String matKhauDaBam) {
        if (danhSachTaiKhoan.values().stream().anyMatch(t -> t.getMaNhanVien().equals(maNhanVien)))
            throw new IllegalArgumentException("Nhân viên đã có tài khoản.");
        if (!danhSachNhanVien.containsKey(maNhanVien) || !danhSachNhanVien.get(maNhanVien).isConLamViec())
            throw new IllegalArgumentException("Nhân viên không còn làm việc.");
        String ma = taoMaTaiKhoan(LocalDate.now());
        danhSachTaiKhoan.put(ma, new TaiKhoan(ma, matKhauDaBam, TrangThaiTaiKhoan.HOAT_DONG, maNhanVien));
        return ma;
    }

    @Override
    public synchronized void capNhatTrangThaiTaiKhoan(String maTaiKhoan, TrangThaiTaiKhoan trangThai) {
        TaiKhoan taiKhoan = danhSachTaiKhoan.get(maTaiKhoan);
        if (taiKhoan == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        taiKhoan.setTrangThai(trangThai);
    }

    @Override
    public synchronized List<DichVu> danhSachDichVu() {
        return danhSachDichVu.values().stream().map(this::saoChep).toList();
    }

    @Override
    public synchronized void luuDichVu(DichVu dichVu, boolean themMoi) {
        if (danhSachDichVu.values().stream()
                .anyMatch(d -> d.getTenDichVu().equalsIgnoreCase(dichVu.getTenDichVu())
                        && (themMoi || !d.getMaDichVu().equals(dichVu.getMaDichVu()))))
            throw new IllegalArgumentException("Tên dịch vụ đã tồn tại.");
        DichVu giaTri = saoChep(dichVu);
        if (themMoi)
            giaTri.setMaDichVu("DV" + String.format("%04d", ++boDemDichVu));
        else if (!danhSachDichVu.containsKey(giaTri.getMaDichVu()))
            throw new IllegalArgumentException("Dịch vụ không tồn tại.");
        danhSachDichVu.put(giaTri.getMaDichVu(), giaTri);
    }

    @Override
    public synchronized List<HangHoa> danhSachHangHoa() {
        return List.copyOf(danhSachHangHoa.values());
    }

    @Override
    public synchronized void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        if (danhSachHangHoa.values().stream().anyMatch(
                h -> h.ten().equalsIgnoreCase(hangHoa.ten()) && (themMoi || !h.ma().equals(hangHoa.ma()))))
            throw new IllegalArgumentException("Tên hàng đã tồn tại.");
        String ma = themMoi ? taoMaTheoNgay("HH", LocalDate.now(), danhSachHangHoa::containsKey)
                : hangHoa.ma();
        if (!themMoi && !danhSachHangHoa.containsKey(ma))
            throw new IllegalArgumentException("Hàng hóa không tồn tại.");
        danhSachHangHoa.put(ma,
                new HangHoa(ma, hangHoa.ten(), hangHoa.donVi(), hangHoa.ton(), hangHoa.toiThieu()));
    }

    @Override
    public synchronized DuLieuBaoCao lapBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        Map<LocalDate, Long> soLuotKhamTheoNgay = new TreeMap<>();
        Map<String, Long> soLanChiDinh = new HashMap<>();
        for (BoDuLieuMau.LuotKhamMau luotKham : danhSachLuotKham) {
            if (luotKham.ngay().isBefore(tuNgay) || luotKham.ngay().isAfter(denNgay))
                continue;
            soLuotKhamTheoNgay.merge(luotKham.ngay(), 1L, Long::sum);
            for (String ma : luotKham.danhSachMaDichVu())
                soLanChiDinh.merge(ma, 1L, Long::sum);
        }
        List<DongThongKe> thongKeTheoNgay = soLuotKhamTheoNgay.entrySet().stream()
                .map(muc -> new DongThongKe(muc.getKey().toString(), muc.getValue())).toList();
        List<DongThongKe> tanSuat = soLanChiDinh.entrySet().stream()
                .map(muc -> new DongThongKe(danhSachDichVu.get(muc.getKey()).getTenDichVu(), muc.getValue()))
                .sorted(Comparator.comparingLong(DongThongKe::soLuong).reversed()
                        .thenComparing(DongThongKe::nhan))
                .toList();
        return new DuLieuBaoCao(tuNgay, denNgay, thongKeTheoNgay, tanSuat);
    }
}
