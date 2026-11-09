package vn.nhom15.nhakhoa.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import vn.nhom15.nhakhoa.connect.NguonKetNoi;
import vn.nhom15.nhakhoa.entity.*;
import vn.nhom15.nhakhoa.entity.DuLieuBaoCao.DongThongKe;

/** Truy cập dữ liệu phòng khám trực tiếp qua JDBC. */
public class PhongKhamDAO {
    private final NguonKetNoi nguonKetNoi;

    public PhongKhamDAO(NguonKetNoi nguonKetNoi) {
        this.nguonKetNoi = nguonKetNoi;
    }

    @FunctionalInterface
    private interface TacVuCSDL<T> {
        T execute(Connection ketNoi) throws SQLException;
    }

    @FunctionalInterface
    private interface AnhXaDuLieu<T> {
        T map(ResultSet ketQua) throws SQLException;
    }

    private <T> T docDuLieu(TacVuCSDL<T> tacVu) {
        try (Connection ketNoi = nguonKetNoi.moKetNoi()) {
            return tacVu.execute(ketNoi);
        } catch (SQLException loi) {
            throw chuyenLoiSQL(loi);
        }
    }

    private <T> T thucHienGiaoDich(TacVuCSDL<T> tacVu) {
        return docDuLieu(ketNoi -> {
            ketNoi.setAutoCommit(false);
            try {
                T ketQua = tacVu.execute(ketNoi);
                ketNoi.commit();
                return ketQua;
            } catch (SQLException | RuntimeException loi) {
                try {
                    ketNoi.rollback();
                } catch (SQLException loiHoanTac) {
                    loi.addSuppressed(loiHoanTac);
                }
                throw loi;
            }
        });
    }

    private NgoaiLeTruyCapDuLieu chuyenLoiSQL(SQLException loi) {
        String thongBao = switch (loi.getErrorCode()) {
        case 2601, 2627 ->
            "Dữ liệu đã tồn tại: kiểm tra tên dịch vụ, tên hàng, mã tài khoản hoặc tài khoản của nhân viên.";
        case 547 ->
            "Dữ liệu đang được sử dụng hoặc không hợp lệ. Không thể xóa nhân viên đã có tài khoản/phiếu khám.";
        case 208, 207 -> "Database thiếu bảng hoặc cột. Chạy DATA/nhakhoa_sqlserver.sql trước khi sử dụng.";
        default ->
            "Không thực hiện được thao tác database. Kiểm tra db.properties, SQL Server và script cài đặt.";
        };
        return new NgoaiLeTruyCapDuLieu(thongBao, loi);
    }

    private PreparedStatement taoCauLenh(Connection ketNoi, String cauLenhSQL, Object... cacGiaTri)
            throws SQLException {
        PreparedStatement cauLenh = ketNoi.prepareStatement(cauLenhSQL);
        try {
            cauLenh.setQueryTimeout(15);
            for (int i = 0; i < cacGiaTri.length; i++) {
                Object giaTri = cacGiaTri[i];
                if (giaTri instanceof LocalDate ngay)
                    cauLenh.setDate(i + 1, Date.valueOf(ngay));
                else
                    cauLenh.setObject(i + 1, giaTri);
            }
            return cauLenh;
        } catch (SQLException loi) {
            cauLenh.close();
            throw loi;
        }
    }

    private int thucHienCapNhat(Connection ketNoi, String cauLenhSQL, Object... cacGiaTri)
            throws SQLException {
        try (PreparedStatement cauLenh = taoCauLenh(ketNoi, cauLenhSQL, cacGiaTri)) {
            return cauLenh.executeUpdate();
        }
    }

    private <T> List<T> truyVan(Connection ketNoi, String cauLenhSQL, AnhXaDuLieu<T> boAnhXa,
            Object... cacGiaTri) throws SQLException {
        try (PreparedStatement cauLenh = taoCauLenh(ketNoi, cauLenhSQL, cacGiaTri);
                ResultSet ketQua = cauLenh.executeQuery()) {
            List<T> cacDong = new ArrayList<>();
            while (ketQua.next())
                cacDong.add(boAnhXa.map(ketQua));
            return cacDong;
        }
    }

    private void kiemTraSoDongCapNhat(int soLuong) {
        if (soLuong != 1)
            throw new IllegalArgumentException("Bản ghi không còn tồn tại. Vui lòng tải lại.");
    }

    /** Bộ đếm được khóa trong cùng transaction với INSERT, không dùng MAX+1. */
    private String taoMaTiepTheo(Connection ketNoi, String tienTo, String bang, String cot)
            throws SQLException {
        String ngay = tienTo.equals("DV") ? ""
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String khoa = tienTo + ngay;
        // Range lock giữ cả khóa chưa tồn tại, tránh hai transaction cùng INSERT bộ đếm.
        List<Integer> cacGiaTri = truyVan(ketNoi,
                "SELECT giaTri FROM DuyBoDem WITH (UPDLOCK,HOLDLOCK) WHERE khoa=?", r -> r.getInt(1), khoa);
        int boDem = cacGiaTri.isEmpty() ? 0 : cacGiaTri.getFirst();
        String ma;
        do {
            boDem++;
            if (boDem > 9999)
                throw new IllegalArgumentException("Đã hết dải mã " + khoa + ".");
            ma = khoa + String.format("%04d", boDem);
        } while (!truyVan(ketNoi, "SELECT 1 FROM " + bang + " WHERE " + cot + "=?", r -> r.getInt(1), ma)
                .isEmpty());
        if (cacGiaTri.isEmpty())
            thucHienCapNhat(ketNoi, "INSERT INTO DuyBoDem(khoa,giaTri) VALUES (?,?)", khoa, boDem);
        else
            thucHienCapNhat(ketNoi, "UPDATE DuyBoDem SET giaTri=? WHERE khoa=?", boDem, khoa);
        return ma;
    }

    public void kiemTraKetNoi() {
        docDuLieu(c -> {
            if (!c.isValid(5))
                throw new SQLException("Kết nối không hợp lệ.");
            return null;
        });
    }

    public List<NhanVien> danhSachNhanVien() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec FROM NhanVien ORDER BY maNhanVien",
                r -> {
                    Date ngaySinh = r.getDate("ngaySinh");
                    Boolean gioiTinh = r.getObject("gioiTinh") == null ? null : r.getBoolean("gioiTinh");
                    return new NhanVien(r.getString("maNhanVien"), r.getString("hoTen"),
                            r.getString("soDienThoai"), ngaySinh == null ? null : ngaySinh.toLocalDate(),
                            gioiTinh, r.getString("cccd"), ChucVu.valueOf(r.getString("chucVu")),
                            r.getBoolean("conLamViec"));
                }));
    }

    public void luuNhanVien(NhanVien n, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi) {
                String ma = taoMaTiepTheo(c, "NV", "NhanVien", "maNhanVien");
                thucHienCapNhat(c,
                        "INSERT INTO NhanVien(maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec) VALUES (?,?,?,?,?,?,?,?)",
                        ma, n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(), n.getGioiTinh(), n.getCccd(),
                        n.getChucVu().name(), n.isConLamViec());
            } else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE NhanVien SET hoTen=?,soDienThoai=?,ngaySinh=?,gioiTinh=?,cccd=?,chucVu=?,conLamViec=? WHERE maNhanVien=?",
                        n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(), n.getGioiTinh(), n.getCccd(),
                        n.getChucVu().name(), n.isConLamViec(), n.getMaNhanVien()));
            return null;
        });
    }

    public void xoaNhanVien(String ma) {
        thucHienGiaoDich(c -> {
            kiemTraSoDongCapNhat(thucHienCapNhat(c, "DELETE FROM NhanVien WHERE maNhanVien=?", ma));
            return null;
        });
    }

    private TaiKhoan docTaiKhoan(ResultSet r) throws SQLException {
        return new TaiKhoan(r.getString("maTaiKhoan"), r.getString("matKhau"),
                TrangThaiTaiKhoan.valueOf(r.getString("trangThai")), r.getString("maNhanVien"));
    }

    public List<TaiKhoan> danhSachTaiKhoan() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maTaiKhoan,matKhau,trangThai,maNhanVien FROM TaiKhoan ORDER BY maTaiKhoan",
                this::docTaiKhoan));
    }

    public TaiKhoan timTaiKhoan(String maTaiKhoan) {
        return docDuLieu(c -> truyVan(c,
                "SELECT maTaiKhoan,matKhau,trangThai,maNhanVien FROM TaiKhoan WHERE maTaiKhoan=?",
                this::docTaiKhoan, maTaiKhoan).stream().findFirst().orElse(null));
    }

    public String taoTaiKhoan(String maNhanVien, String matKhauDaBam) {
        return thucHienGiaoDich(c -> {
            String maTaiKhoan = taoMaTiepTheo(c, "TK", "TaiKhoan", "maTaiKhoan");
            List<Integer> dangLamViec = truyVan(c,
                    "SELECT 1 FROM NhanVien WITH (UPDLOCK,HOLDLOCK) WHERE maNhanVien=? AND conLamViec=1",
                    r -> r.getInt(1), maNhanVien);
            if (dangLamViec.isEmpty())
                throw new IllegalArgumentException("Nhân viên không còn làm việc.");
            thucHienCapNhat(c,
                    "INSERT INTO TaiKhoan(maTaiKhoan,matKhau,trangThai,maNhanVien) VALUES (?,?,?,?)",
                    maTaiKhoan, matKhauDaBam, TrangThaiTaiKhoan.HOAT_DONG.name(), maNhanVien);
            return maTaiKhoan;
        });
    }

    public void capNhatTrangThaiTaiKhoan(String ten, TrangThaiTaiKhoan trangThai) {
        thucHienGiaoDich(c -> {
            kiemTraSoDongCapNhat(thucHienCapNhat(c, "UPDATE TaiKhoan SET trangThai=? WHERE maTaiKhoan=?",
                    trangThai.name(), ten));
            return null;
        });
    }

    public List<DichVu> danhSachDichVu() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maDichVu,tenDichVu,donGiaNiemYet,trangThai FROM DichVu ORDER BY maDichVu",
                r -> new DichVu(r.getString(1), r.getString(2), r.getBigDecimal(3),
                        TrangThaiDichVu.valueOf(r.getString(4)))));
    }

    public void luuDichVu(DichVu dichVu, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi)
                thucHienCapNhat(c,
                        "INSERT INTO DichVu(maDichVu,tenDichVu,donGiaNiemYet,trangThai) VALUES (?,?,?,?)",
                        taoMaTiepTheo(c, "DV", "DichVu", "maDichVu"), dichVu.getTenDichVu(),
                        dichVu.getDonGiaNiemYet(), dichVu.getTrangThai().name());
            else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE DichVu SET tenDichVu=?,donGiaNiemYet=?,trangThai=? WHERE maDichVu=?",
                        dichVu.getTenDichVu(), dichVu.getDonGiaNiemYet(), dichVu.getTrangThai().name(),
                        dichVu.getMaDichVu()));
            return null;
        });
    }

    public List<HangHoa> danhSachHangHoa() {
        return docDuLieu(c -> truyVan(c,
                "SELECT maHangHoa,tenHangHoa,donViTinh,soLuongTon,mucTonToiThieu FROM HangHoa ORDER BY maHangHoa",
                r -> new HangHoa(r.getString(1), r.getString(2), r.getString(3), r.getInt(4), r.getInt(5))));
    }

    public void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        thucHienGiaoDich(c -> {
            if (themMoi)
                thucHienCapNhat(c,
                        "INSERT INTO HangHoa(maHangHoa,tenHangHoa,donViTinh,soLuongTon,mucTonToiThieu) VALUES (?,?,?,?,?)",
                        taoMaTiepTheo(c, "HH", "HangHoa", "maHangHoa"), hangHoa.ten(), hangHoa.donVi(),
                        hangHoa.ton(), hangHoa.toiThieu());
            else
                kiemTraSoDongCapNhat(thucHienCapNhat(c,
                        "UPDATE HangHoa SET tenHangHoa=?,donViTinh=?,soLuongTon=?,mucTonToiThieu=? WHERE maHangHoa=?",
                        hangHoa.ten(), hangHoa.donVi(), hangHoa.ton(), hangHoa.toiThieu(), hangHoa.ma()));
            return null;
        });
    }

    public DuLieuBaoCao lapBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        return thucHienGiaoDich(c -> {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            List<DongThongKe> danhSachLuotKham = truyVan(c,
                    "SELECT ngayKham,COUNT_BIG(*) FROM PhieuKham WHERE ngayKham>=? AND ngayKham<=? GROUP BY ngayKham ORDER BY ngayKham",
                    r -> new DongThongKe(r.getDate(1).toLocalDate().toString(), r.getLong(2)), tuNgay,
                    denNgay);
            List<DongThongKe> danhSachDichVu = truyVan(c,
                    "SELECT dv.tenDichVu,COUNT_BIG(*) FROM ChiDinhDichVu cd JOIN DichVu dv ON dv.maDichVu=cd.maDichVu JOIN PhieuKham pk ON pk.maPhieuKham=cd.maPhieuKham WHERE pk.ngayKham>=? AND pk.ngayKham<=? GROUP BY dv.maDichVu,dv.tenDichVu ORDER BY COUNT_BIG(*) DESC,dv.tenDichVu",
                    r -> new DongThongKe(r.getString(1), r.getLong(2)), tuNgay, denNgay);
            return new DuLieuBaoCao(tuNgay, denNgay, danhSachLuotKham, danhSachDichVu);
        });
    }
}
