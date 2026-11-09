package bus;

import java.util.List;
import dao.HangHoaDAO;
import entity.*;

public final class HangHoaBUS {
    private final HangHoaDAO hangHoaDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    public HangHoaBUS(HangHoaDAO hangHoaDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.hangHoaDAO = hangHoaDAO;
        this.quanLyPhien = quanLyPhien;
    }

    public List<HangHoa> danhSachHangHoa() {
        quanLyPhien.kiemTraQuyenQuanLy();
        return hangHoaDAO.danhSachHangHoa();
    }

    public void luuHangHoa(HangHoa hangHoa, boolean themMoi) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (hangHoa == null || hangHoa.ten() == null || hangHoa.donVi() == null)
            throw new IllegalArgumentException("Nhập tên hàng và đơn vị tính.");
        if (!themMoi && (hangHoa.ma() == null || hangHoa.ma().isBlank()))
            throw new IllegalArgumentException("Chọn hàng hóa cần cập nhật.");
        if (hangHoa.ten().isBlank() || hangHoa.ten().trim().length() > 100 || hangHoa.donVi().isBlank()
                || hangHoa.donVi().trim().length() > 30)
            throw new IllegalArgumentException(
                    "Tên hàng tối đa 100 và đơn vị tối đa 30 ký tự, không để trống.");
        if (hangHoa.ton() < 0 || hangHoa.toiThieu() < 0)
            throw new IllegalArgumentException("Tồn kho và ngưỡng tối thiểu phải là số nguyên không âm.");
        hangHoaDAO.luuHangHoa(new HangHoa(hangHoa.ma(), hangHoa.ten().trim(), hangHoa.donVi().trim(),
                hangHoa.ton(), hangHoa.toiThieu()), themMoi);
    }
}
