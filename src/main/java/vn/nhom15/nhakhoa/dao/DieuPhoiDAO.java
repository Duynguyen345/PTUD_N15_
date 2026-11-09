package vn.nhom15.nhakhoa.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import vn.nhom15.nhakhoa.entity.*;

/** Hợp đồng lưu trữ; bản JDBC và bản bộ nhớ thực hiện cùng nghiệp vụ nguyên tử. */
public interface DieuPhoiDAO {
    /** Lấy danh sách phòng cùng bác sĩ phụ trách. */
    List<PhongKham> layDanhSachPhong();
    /** Lấy hàng chờ đúng ngày/phòng, tăng dần theo số thứ tự. */
    List<LuotChoKham> layHangCho(String maPhong, LocalDate ngay);
    /** Tìm theo mã, tên hoặc số điện thoại; từ khóa đã được BUS kiểm tra. */
    List<BenhNhan> timBenhNhan(String tuKhoa);
    /** Lấy lịch sử khám theo định danh bệnh nhân. */
    List<LichSuKham> layLichSuKham(String maBenhNhan);
    /** Bật/tắt nhận bệnh trong giao dịch có khóa phòng. */
    void datNhanBenh(String maPhong, boolean nhanBenh, LocalDate ngay);
    /** Gọi lượt đã trả phí nhỏ nhất; khóa phòng để tránh hai máy cùng gọi. */
    LuotChoKham goiTiepTheo(String maPhong, LocalDate ngay, LocalDateTime thoiDiem);
    /** Gọi lại, xác nhận hoặc đánh dấu vắng, có kiểm tra trạng thái hiện tại. */
    LuotChoKham chuyenTrangThai(String maPhieuKham, String maPhong, LocalDate ngay,
            TrangThaiHangCho dich, LocalDateTime thoiDiem);
}
