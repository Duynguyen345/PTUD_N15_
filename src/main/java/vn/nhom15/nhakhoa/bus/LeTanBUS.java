package vn.nhom15.nhakhoa.bus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import vn.nhom15.nhakhoa.dao.LeTanDAO;
import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.*;

/** Validation, quyền và điều phối lễ tân; dùng đúng các Entity OOP chuẩn của dự án. */
public final class LeTanBUS {
    private final LeTanDAO kho;
    private final PhongKhamDAO khoQuanLy;
    private final QuanLyPhienDangNhap phien;

    public LeTanBUS(LeTanDAO kho, PhongKhamDAO khoQuanLy, QuanLyPhienDangNhap phien) {
        this.kho = kho;
        this.khoQuanLy = khoQuanLy;
        this.phien = phien;
    }

    public PhienDangNhap phienLeTan() {
        return phien.kiemTraQuyen(ChucVu.LE_TAN);
    }

    private String maLeTan() {
        return phienLeTan().getMaNhanVien();
    }

    public List<BenhNhan> danhSachBenhNhan() {
        maLeTan();
        return kho.danhSachBenhNhan();
    }

    public List<BenhNhan> traCuuBenhNhan(String tieuChi, String tuKhoa) {
        maLeTan();
        if (tuKhoa == null || tuKhoa.isBlank())
            return List.of();
        return kho.traCuuBenhNhan(tieuChi == null ? "Mã" : tieuChi, tuKhoa.trim());
    }

    public List<PhieuKham> lichSuPhieuKham(String maBenhNhan) {
        maLeTan();
        if (maBenhNhan == null || maBenhNhan.isBlank())
            return List.of();
        return kho.lichSuPhieuKham(maBenhNhan.trim());
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) {
        maLeTan();
        if (tuKhoa == null || tuKhoa.isBlank())
            return List.of();
        return kho.timBenhNhan(tuKhoa.trim());
    }

    public BenhNhan luuBenhNhan(BenhNhan bn) {
        maLeTan();
        if (bn == null)
            throw new IllegalArgumentException("Chưa nhập hồ sơ bệnh nhân.");
        String ten = bn.getHoTen() == null ? "" : bn.getHoTen().trim();
        String sdt = bn.getSoDienThoai() == null ? "" : bn.getSoDienThoai().trim();
        String cccd = chuanHoa(bn.getCccd());
        if (ten.isEmpty() || ten.length() > 100)
            throw new IllegalArgumentException("Họ tên cần từ 1 đến 100 ký tự.");
        if (!sdt.matches("0\\d{9}"))
            throw new IllegalArgumentException("Số điện thoại gồm 10 số, bắt đầu bằng 0.");
        if (cccd != null && !cccd.matches("\\d{12}"))
            throw new IllegalArgumentException("CCCD phải gồm 12 số hoặc để trống.");
        if (bn.getNgaySinh() != null && bn.getNgaySinh().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        return kho.luuBenhNhan(new BenhNhan(bn.getMaBenhNhan(), ten, sdt, cccd,
                bn.getNgaySinh(), chuanHoa(bn.getDiaChi()), chuanHoa(bn.getTienSuDiUng())));
    }

    private String chuanHoa(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public List<PhongKham> danhSachPhong() {
        maLeTan();
        return kho.danhSachPhong();
    }

    public NhanVien timNhanVienTheoMa(String maNhanVien) {
        maLeTan();
        if (maNhanVien == null || maNhanVien.isBlank())
            return null;
        return khoQuanLy.danhSachNhanVien().stream()
                .filter(nv -> nv.getMaNhanVien().equals(maNhanVien))
                .findFirst().orElse(null);
    }

    public List<PhieuKham> danhSachLuot(String maPhong) {
        maLeTan();
        return kho.danhSachLuot(maPhong, LocalDate.now());
    }

    public HoaDon phiChuaDung(String maBenhNhan) {
        maLeTan();
        return kho.phiChuaDung(maBenhNhan);
    }

    public BigDecimal mucPhiKham() {
        maLeTan();
        return khoQuanLy.danhSachDichVu().stream()
                .filter(d -> d.getMaDichVu().equals("DV0001") && d.getTrangThai() == TrangThaiDichVu.DANG_PHUC_VU)
                .map(DichVu::getDonGiaNiemYet).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chưa có phí khám DV0001 đang phục vụ."));
    }

    private BigDecimal tien(String vanBan) {
        try {
            BigDecimal so = new BigDecimal(vanBan == null ? "" : vanBan.trim());
            if (so.signum() <= 0 || so.scale() > 2)
                throw new NumberFormatException();
            return so;
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Tiền khách đưa phải là số dương, tối đa 2 chữ số thập phân.");
        }
    }

    public HoaDon thuPhi(String maBenhNhan, String hinhThucThanhToan, String tienKhachDua) {
        String nguoiThu = maLeTan();
        if (maBenhNhan == null || maBenhNhan.isBlank())
            throw new IllegalArgumentException("Chọn bệnh nhân trước khi thu phí.");
        if (hinhThucThanhToan == null || hinhThucThanhToan.isBlank())
            throw new IllegalArgumentException("Chọn hình thức thanh toán.");
        return kho.thuPhi(maBenhNhan, hinhThucThanhToan, tien(tienKhachDua), nguoiThu);
    }

    public PhieuKham lapPhieu(String maBenhNhan, String maPhong, String lyDoKham) {
        maLeTan();
        if (maBenhNhan == null || maBenhNhan.isBlank() || maPhong == null || maPhong.isBlank())
            throw new IllegalArgumentException("Chọn bệnh nhân và phòng khám.");
        String lyDo = lyDoKham == null ? "" : lyDoKham.trim();
        if (lyDo.isEmpty() || lyDo.length() > 300)
            throw new IllegalArgumentException("Lý do khám cần từ 1 đến 300 ký tự.");
        return kho.lapPhieu(maBenhNhan, maPhong, lyDo);
    }

    public List<PhieuKham> phieuChoThanhToan() {
        maLeTan();
        return kho.phieuChoThanhToan();
    }

    public List<ChiDinhDichVu> dichVuTheoPhieu(String maPhieu) {
        maLeTan();
        return kho.dichVuTheoPhieu(maPhieu);
    }

    public HoaDon thanhToanDichVu(String maPhieu, String hinhThucThanhToan, String tienKhachDua) {
        String nguoiThu = maLeTan();
        if (maPhieu == null || maPhieu.isBlank())
            throw new IllegalArgumentException("Chọn lượt khám đã hoàn tất.");
        if (hinhThucThanhToan == null || hinhThucThanhToan.isBlank())
            throw new IllegalArgumentException("Chọn hình thức thanh toán.");
        return kho.thanhToanDichVu(maPhieu, hinhThucThanhToan, tien(tienKhachDua), nguoiThu);
    }

    public CaThuNgan caDangMo() {
        maLeTan();
        return kho.caDangMo();
    }

    public CaThuNgan moCa() {
        return kho.moCa(maLeTan());
    }

    public CaThuNgan chotCa(String maCa) {
        maLeTan();
        if (maCa == null || maCa.isBlank())
            throw new IllegalArgumentException("Chưa có ca đang mở.");
        return kho.chotCa(maCa);
    }

    public List<HoaDon> hoaDonTrongCa(CaThuNgan ca) {
        maLeTan();
        return ca == null ? List.of() : kho.hoaDonTrongCa(ca);
    }

    public List<CaThuNgan> lichSuCa() {
        maLeTan();
        return kho.lichSuCa();
    }
}
