package dao;

import connect.NguonKetNoi;
import util.BoBamMatKhau;

/** Chỉ khởi tạo các DAO theo luồng, không thực hiện truy vấn hay nghiệp vụ. */
public record BoDAO(DangNhapDAO dangNhap, NhanVienDAO nhanVien, TaiKhoanDAO taiKhoan, DichVuDAO dichVu, HangHoaDAO hangHoa, BaoCaoDAO baoCao) {
    public static BoDAO jdbc(NguonKetNoi nguonKetNoi) {
        return new BoDAO(new DangNhapDAO(nguonKetNoi), new NhanVienDAO(nguonKetNoi), new TaiKhoanDAO(nguonKetNoi),
                new DichVuDAO(nguonKetNoi), new HangHoaDAO(nguonKetNoi), new BaoCaoDAO(nguonKetNoi));
    }

    public static BoDAO duLieuMau(BoBamMatKhau boBamMatKhau) {
        DuLieuMauDungChung duLieu = new DuLieuMauDungChung(boBamMatKhau);
        return new BoDAO(new DangNhapDuLieuMauDAO(duLieu), new NhanVienDuLieuMauDAO(duLieu), new TaiKhoanDuLieuMauDAO(duLieu),
                new DichVuDuLieuMauDAO(duLieu), new HangHoaDuLieuMauDAO(duLieu), new BaoCaoDuLieuMauDAO(duLieu));
    }
}
