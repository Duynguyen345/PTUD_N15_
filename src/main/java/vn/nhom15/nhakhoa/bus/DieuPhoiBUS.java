package vn.nhom15.nhakhoa.bus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import vn.nhom15.nhakhoa.dao.DieuPhoiDAO;
import vn.nhom15.nhakhoa.entity.*;

/** Điều phối hàng chờ; GUI không được truy cập trực tiếp DAO. */
public final class DieuPhoiBUS {
    private final DieuPhoiDAO dieuPhoiDAO;
    private final QuanLyPhienDangNhap quanLyPhien;
    private final Clock dongHo;

    /** Nhận repository và phiên qua constructor để có thể thay thế khi kiểm thử. */
    public DieuPhoiBUS(DieuPhoiDAO dieuPhoiDAO, QuanLyPhienDangNhap quanLyPhien, Clock dongHo) {
        this.dieuPhoiDAO = dieuPhoiDAO;
        this.quanLyPhien = quanLyPhien;
        this.dongHo = dongHo;
    }

    /** Trả các phòng cho người có quyền xem hàng chờ hoặc bật/tắt phòng. */
    public List<PhongKham> layDanhSachPhong() {
        quanLyPhien.kiemTraQuyen(Quyen.XEM_HANG_CHO);
        return dieuPhoiDAO.layDanhSachPhong();
    }

    /** Chỉ thao tác hàng chờ trong ngày hiện tại. */
    public List<LuotChoKham> layHangCho(String maPhong) {
        quanLyPhien.kiemTraQuyen(Quyen.XEM_HANG_CHO);
        return dieuPhoiDAO.layHangCho(kiemTraMa(maPhong), LocalDate.now(dongHo));
    }

    /** Gọi bệnh nhân tiếp theo đã trả phí, theo FIFO trong phòng. */
    public LuotChoKham goiTiepTheo(String maPhong) {
        quanLyPhien.kiemTraQuyen(Quyen.DIEU_PHOI_HANG_CHO);
        return dieuPhoiDAO.goiTiepTheo(kiemTraMa(maPhong), LocalDate.now(dongHo), LocalDateTime.now(dongHo));
    }

    /** Gọi lại đúng lượt đang gọi, không tạo số thứ tự mới. */
    public LuotChoKham goiLai(String maPhieuKham, String maPhong) {
        return chuyenTrangThai(maPhieuKham, maPhong, TrangThaiHangCho.DANG_GOI);
    }

    /** Xác nhận bệnh nhân đã đến phòng; không thể xác nhận lượt chưa được gọi. */
    public LuotChoKham xacNhanVaoKham(String maPhieuKham, String maPhong) {
        return chuyenTrangThai(maPhieuKham, maPhong, TrangThaiHangCho.DANG_KHAM);
    }

    /** Đánh dấu vắng để giải phóng lượt đang gọi và tiếp tục hàng chờ. */
    public LuotChoKham danhDauVang(String maPhieuKham, String maPhong) {
        return chuyenTrangThai(maPhieuKham, maPhong, TrangThaiHangCho.VANG_MAT);
    }

    private LuotChoKham chuyenTrangThai(String maPhieuKham, String maPhong, TrangThaiHangCho dich) {
        quanLyPhien.kiemTraQuyen(Quyen.DIEU_PHOI_HANG_CHO);
        return dieuPhoiDAO.chuyenTrangThai(kiemTraMa(maPhieuKham), kiemTraMa(maPhong),
                LocalDate.now(dongHo), dich, LocalDateTime.now(dongHo));
    }

    /** Bác sĩ chỉ đổi phòng mình phụ trách; phụ tá/quản lý điều phối các phòng. */
    public void datNhanBenh(String maPhong, boolean nhanBenh) {
        quanLyPhien.kiemTraQuyen(Quyen.BAT_TAT_PHONG);
        String ma = kiemTraMa(maPhong);
        PhongKham phong = dieuPhoiDAO.layDanhSachPhong().stream().filter(p -> p.maPhong().equals(ma))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng khám."));
        PhienDangNhap phien = quanLyPhien.phienHienTai();
        if (phien == null) throw new SecurityException("Phiên đã kết thúc. Vui lòng đăng nhập lại.");
        if (phien.getChucVu() == ChucVu.BAC_SI && !phien.getMaNhanVien().equals(phong.maBacSi()))
            throw new SecurityException("Bác sĩ chỉ được bật/tắt phòng mình phụ trách.");
        dieuPhoiDAO.datNhanBenh(ma, nhanBenh, LocalDate.now(dongHo));
    }

    private String kiemTraMa(String ma) {
        if (ma == null || ma.isBlank()) throw new IllegalArgumentException("Vui lòng chọn phòng hoặc lượt khám.");
        return ma.trim();
    }
}
