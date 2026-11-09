package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.DieuPhoiBUS;
import vn.nhom15.nhakhoa.entity.*;

/** Boundary UC009: hàng chờ theo phòng; mọi lệnh ghi đi qua BUS có kiểm tra quyền. */
public final class GD_HangCho extends ManHinhCoSo {
    private final DieuPhoiBUS dieuPhoiBUS;
    private final boolean duocDieuPhoi;
    private final Runnable moBangGoiSo;
    private final JComboBox<PhongKham> cmbPhong = new JComboBox<>();
    private final JLabel lblThongKe = new JLabel(" ");
    private final JLabel lblDangGoi = new JLabel("Chưa có số đang gọi", SwingConstants.CENTER);
    private final JLabel lblBacSi = new JLabel(" ", SwingConstants.CENTER);
    private final BangDuLieu<LuotChoKham> tblHangCho;
    private final JButton btnGoiTiep;
    private final JButton btnGoiLai;
    private final JButton btnXacNhan;
    private final JButton btnVangMat;
    private boolean dangNap;
    private boolean coTheGoiTiep;

    /** Nhận quyền hiển thị từ phiên; BUS vẫn tự xác thực ở từng thao tác. */
    public GD_HangCho(DieuPhoiBUS dieuPhoiBUS, boolean duocDieuPhoi,
            BoXuLyTacVuGiaoDien boXuLy, Runnable moBangGoiSo) {
        super("Hàng chờ theo phòng", "Gọi số → xác nhận vào khám. Danh sách chỉ gồm các lượt trong hôm nay.", boXuLy);
        this.dieuPhoiBUS = dieuPhoiBUS;
        this.duocDieuPhoi = duocDieuPhoi;
        this.moBangGoiSo = moBangGoiSo;
        JPanel pnlDau = new JPanel(new BorderLayout(10, 10));
        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlLoc.add(new JLabel("Phòng khám"));
        cmbPhong.setPreferredSize(new Dimension(320, 34));
        pnlLoc.add(cmbPhong);
        pnlLoc.add(taoNut("Tải lại", this::lamMoi));
        pnlLoc.add(taoNut("Bảng hiển thị số", () -> this.moBangGoiSo.run()));
        pnlDau.add(pnlLoc, BorderLayout.NORTH);
        JPanel pnlSoGoi = new JPanel(new GridLayout(0, 1, 4, 8));
        pnlSoGoi.setBackground(new Color(225, 244, 239));
        pnlSoGoi.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        lblDangGoi.setFont(lblDangGoi.getFont().deriveFont(Font.BOLD, 28f));
        lblDangGoi.setForeground(new Color(18, 96, 81));
        pnlSoGoi.add(lblDangGoi);
        pnlSoGoi.add(lblBacSi);
        pnlSoGoi.add(lblThongKe);
        pnlDau.add(pnlSoGoi, BorderLayout.CENTER);
        noiDung.add(pnlDau, BorderLayout.NORTH);
        tblHangCho = new BangDuLieu<>(new String[] { "STT", "Mã phiếu khám", "Mã bệnh nhân", "Họ tên", "Phí khám", "Trạng thái", "Lần gọi gần nhất" },
                l -> new Object[] { l.soThuTu(), l.maPhieuKham(), l.maBenhNhan(), l.hoTen(),
                        l.daThanhToanPhiKham() ? "Đã thanh toán" : "Chưa thanh toán", l.trangThai(),
                        l.thoiDiemGoi() == null ? "" : l.thoiDiemGoi().format(DateTimeFormatter.ofPattern("HH:mm:ss")) });
        noiDung.add(tblHangCho, BorderLayout.CENTER);
        btnGoiTiep = taoNut("Gọi số tiếp theo", () -> thucHien(0));
        btnGoiLai = taoNut("Gọi lại", () -> thucHien(1));
        btnXacNhan = taoNut("Xác nhận vào khám", () -> thucHien(2));
        btnVangMat = taoNut("Đánh dấu vắng", () -> thucHien(3));
        if (duocDieuPhoi) noiDung.add(taoThanhThaoTac(btnGoiTiep, btnGoiLai, btnXacNhan, btnVangMat), BorderLayout.SOUTH);
        tblHangCho.khiChonDong(l -> datQuyenNut(l.trangThai() == TrangThaiHangCho.DANG_GOI));
        cmbPhong.addActionListener(e -> { if (!dangNap && !boXuLy.dangXuLy()) lamMoi(); });
        datQuyenNut(false);
    }

    private void datQuyenNut(boolean dangGoi) {
        btnGoiTiep.setEnabled(duocDieuPhoi && coTheGoiTiep);
        btnGoiLai.setEnabled(duocDieuPhoi && dangGoi);
        btnXacNhan.setEnabled(duocDieuPhoi && dangGoi);
        btnVangMat.setEnabled(duocDieuPhoi && dangGoi);
    }

    private PhongKham layPhongChon() {
        if (!(cmbPhong.getSelectedItem() instanceof PhongKham phong))
            throw new IllegalArgumentException("Chọn phòng khám trước.");
        return phong;
    }

    private void thucHien(int thaoTac) {
        String maPhong = layPhongChon().maPhong();
        String maPhieu = thaoTac == 0 ? null : tblHangCho.layDongDangChon().maPhieuKham();
        if (thaoTac == 2 && !xacNhan("Xác nhận bệnh nhân đã có mặt và vào phòng khám?")) return;
        if (thaoTac == 3 && !xacNhan("Bệnh nhân không có mặt? Chuyển lượt này sang Vắng mặt?")) return;
        boXuLy.thucHien(this, () -> {
            LuotChoKham ketQua = switch (thaoTac) {
                case 0 -> dieuPhoiBUS.goiTiepTheo(maPhong);
                case 1 -> dieuPhoiBUS.goiLai(maPhieu, maPhong);
                case 2 -> dieuPhoiBUS.xacNhanVaoKham(maPhieu, maPhong);
                case 3 -> dieuPhoiBUS.danhDauVang(maPhieu, maPhong);
                default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
            };
            return new KetQua(dieuPhoiBUS.layDanhSachPhong(), maPhong, dieuPhoiBUS.layHangCho(maPhong),
                    "Số " + String.format("%03d", ketQua.soThuTu()) + " — " + ketQua.trangThai());
        }, this::hienThi);
    }

    private record KetQua(List<PhongKham> cacPhong, String maPhong, List<LuotChoKham> cacLuot, String thongBao) { }

    private void hienThi(KetQua duLieu) {
        dangNap = true;
        cmbPhong.removeAllItems();
        for (PhongKham phong : duLieu.cacPhong()) {
            cmbPhong.addItem(phong);
            if (phong.maPhong().equals(duLieu.maPhong())) cmbPhong.setSelectedItem(phong);
        }
        dangNap = false;
        tblHangCho.capNhatDuLieu(duLieu.cacLuot());
        PhongKham phong = (PhongKham) cmbPhong.getSelectedItem();
        lblBacSi.setText(phong == null ? "Không có phòng khám" : phong.tenPhong() + " • BS. " + phong.tenBacSi());
        LuotChoKham hienTai = duLieu.cacLuot().stream().filter(l -> l.trangThai() == TrangThaiHangCho.DANG_GOI
                || l.trangThai() == TrangThaiHangCho.DANG_KHAM).findFirst().orElse(null);
        lblDangGoi.setText(hienTai == null ? "Chưa có số đang gọi" : hienTai.trangThai() + " • SỐ "
                + String.format("%03d", hienTai.soThuTu()));
        long cho = duLieu.cacLuot().stream().filter(l -> l.trangThai() == TrangThaiHangCho.CHO_KHAM && l.daThanhToanPhiKham()).count();
        long chuaPhi = duLieu.cacLuot().stream().filter(l -> !l.daThanhToanPhiKham()).count();
        lblThongKe.setHorizontalAlignment(SwingConstants.CENTER);
        lblThongKe.setText("Đủ điều kiện chờ: " + cho + "     •     Chưa thanh toán: " + chuaPhi + "     •     Tổng lượt: " + duLieu.cacLuot().size());
        coTheGoiTiep = phong != null && phong.dangNhanBenh() && hienTai == null && cho > 0;
        datQuyenNut(false);
        trangThai.setText(duLieu.thongBao());
    }

    @Override
    public void lamMoi() {
        String ma = cmbPhong.getSelectedItem() instanceof PhongKham p ? p.maPhong() : null;
        boXuLy.thucHien(this, () -> {
            List<PhongKham> cacPhong = dieuPhoiBUS.layDanhSachPhong();
            String chon = ma == null && !cacPhong.isEmpty() ? cacPhong.getFirst().maPhong() : ma;
            return new KetQua(cacPhong, chon, chon == null ? List.of() : dieuPhoiBUS.layHangCho(chon),
                    "Chọn dòng đang gọi để gọi lại, xác nhận hoặc đánh dấu vắng.");
        }, this::hienThi);
    }
}
