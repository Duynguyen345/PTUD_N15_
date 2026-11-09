package vn.nhom15.nhakhoa.gui;

import java.awt.BorderLayout;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.DieuPhoiBUS;
import vn.nhom15.nhakhoa.entity.PhongKham;

/** Boundary bật/tắt nhận bệnh; không xóa phòng hoặc thay đổi hồ sơ bệnh nhân. */
public final class GD_PhongKham extends ManHinhCoSo {
    private final DieuPhoiBUS dieuPhoiBUS;
    private final BangDuLieu<PhongKham> tblPhong;

    /** Các thao tác đều yêu cầu xác nhận và được BUS kiểm tra lại quyền. */
    public GD_PhongKham(DieuPhoiBUS dieuPhoiBUS, BoXuLyTacVuGiaoDien boXuLy) {
        super("Phòng nhận bệnh", "Tạm ngưng phòng sẽ chặn gọi số; các lượt đang chờ vẫn được giữ nguyên.", boXuLy);
        this.dieuPhoiBUS = dieuPhoiBUS;
        tblPhong = new BangDuLieu<>(new String[] { "Mã phòng", "Tên phòng", "Bác sĩ phụ trách", "Nhận bệnh" },
                p -> new Object[] { p.maPhong(), p.tenPhong(), p.tenBacSi(), p.dangNhanBenh() ? "Đang nhận bệnh" : "Tạm ngưng" });
        noiDung.add(tblPhong, BorderLayout.CENTER);
        JButton btnBat = taoNut("Bật nhận bệnh", () -> doiTrangThai(true));
        JButton btnTat = taoNut("Tắt nhận bệnh", () -> doiTrangThai(false));
        noiDung.add(taoThanhThaoTac(btnBat, btnTat, taoNut("Tải lại", this::lamMoi)), BorderLayout.NORTH);
    }

    private void doiTrangThai(boolean nhanBenh) {
        PhongKham phong = tblPhong.layDongDangChon();
        if (!xacNhan((nhanBenh ? "Bật" : "Tắt") + " nhận bệnh tại " + phong.tenPhong() + "?")) return;
        boXuLy.thucHien(this, () -> {
            dieuPhoiBUS.datNhanBenh(phong.maPhong(), nhanBenh);
            return dieuPhoiBUS.layDanhSachPhong();
        }, cacPhong -> {
            tblPhong.capNhatDuLieu(cacPhong);
            trangThai.setText("Đã " + (nhanBenh ? "bật" : "tắt") + " nhận bệnh tại " + phong.tenPhong() + ".");
        });
    }

    @Override
    public void lamMoi() {
        boXuLy.thucHien(this, dieuPhoiBUS::layDanhSachPhong, cacPhong -> {
            tblPhong.capNhatDuLieu(cacPhong);
            trangThai.setText("Bác sĩ chỉ bật/tắt phòng phụ trách. Không thể tắt phòng đang gọi hoặc đang khám.");
        });
    }
}
