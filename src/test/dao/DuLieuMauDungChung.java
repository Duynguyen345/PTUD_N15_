package dao;

import java.time.LocalDate;
import java.util.*;
import entity.*;
import util.BoBamMatKhau;

/** Trạng thái dùng chung giữa các luồng trong một phiên DEMO. */
final class DuLieuMauDungChung {
    final Map<String, NhanVien> danhSachNhanVien = new LinkedHashMap<>();
    final Map<String, TaiKhoan> danhSachTaiKhoan = new LinkedHashMap<>();
    final Map<String, DichVu> danhSachDichVu = new LinkedHashMap<>();
    final Map<String, HangHoa> danhSachHangHoa = new LinkedHashMap<>();
    final Set<String> nhanVienDaThamChieu = new HashSet<>();
    int boDemDichVu;
    final Map<String, Integer> cacBoDem = new HashMap<>();
    final List<BoDuLieuMau.LuotKhamMau> danhSachLuotKham;

    DuLieuMauDungChung(BoBamMatKhau boBamMatKhau) {
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

    void ghiNhanMaTheoNgay(String ma) {
        cacBoDem.merge(ma.substring(0, 8), Integer.parseInt(ma.substring(8)), Math::max);
    }

    String taoMaTheoNgay(String loai, LocalDate ngay, java.util.function.Predicate<String> daTonTai) {
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

    NhanVien saoChep(NhanVien n) {
        return new NhanVien(n.getMaNhanVien(), n.getHoTen(), n.getSoDienThoai(), n.getNgaySinh(),
                n.getGioiTinh(), n.getCccd(), n.getChucVu(), n.isConLamViec());
    }

    TaiKhoan saoChep(TaiKhoan t) {
        return new TaiKhoan(t.getMaTaiKhoan(), t.getMatKhau(), t.getTrangThai(), t.getMaNhanVien());
    }

    DichVu saoChep(DichVu d) {
        return new DichVu(d.getMaDichVu(), d.getTenDichVu(), d.getDonGiaNiemYet(), d.getTrangThai());
    }


    String taoMaTaiKhoan(LocalDate ngay) {
        return taoMaTheoNgay("TK", ngay, danhSachTaiKhoan::containsKey);
    }
}
