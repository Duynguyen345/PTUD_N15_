package vn.nhom15.nhakhoa.bus;

import java.util.List;
import vn.nhom15.nhakhoa.dao.DieuPhoiDAO;
import vn.nhom15.nhakhoa.entity.*;

/** Tra cứu hồ sơ chỉ đọc; luôn kiểm tra quyền trước khi truy xuất dữ liệu. */
public final class TraCuuHoSoBUS {
    private final DieuPhoiDAO dieuPhoiDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    /** Nhận kho dữ liệu và phiên đăng nhập do composition root cung cấp. */
    public TraCuuHoSoBUS(DieuPhoiDAO dieuPhoiDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.dieuPhoiDAO = dieuPhoiDAO;
        this.quanLyPhien = quanLyPhien;
    }

    /** Tìm chuỗi con theo mã, tên không dấu hoặc SĐT; không tự tải tất cả hồ sơ. */
    public List<BenhNhan> timBenhNhan(String tuKhoa) {
        quanLyPhien.kiemTraQuyen(Quyen.TRA_CUU_HO_SO);
        if (tuKhoa == null || tuKhoa.isBlank())
            throw new IllegalArgumentException("Nhập mã bệnh nhân, họ tên hoặc số điện thoại để tìm.");
        if (tuKhoa.trim().length() > 100)
            throw new IllegalArgumentException("Từ khóa tối đa 100 ký tự.");
        return dieuPhoiDAO.timBenhNhan(tuKhoa.trim());
    }

    /** Trả lịch sử đúng bệnh nhân đã chọn, quyền được kiểm tra ở mỗi lần đọc. */
    public List<LichSuKham> layLichSuKham(String maBenhNhan) {
        quanLyPhien.kiemTraQuyen(Quyen.TRA_CUU_HO_SO);
        if (maBenhNhan == null || !maBenhNhan.matches("BN[0-9]{10}"))
            throw new IllegalArgumentException("Mã bệnh nhân phải có dạng BNyyMMddNNNN.");
        return dieuPhoiDAO.layLichSuKham(maBenhNhan);
    }
}
