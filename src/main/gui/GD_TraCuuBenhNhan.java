package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import bus.LeTanBUS;
import entity.*;

/** UC006: Tra cứu hồ sơ bệnh nhân + lịch sử phiếu khám (double-click dòng để xem chi tiết). */
public final class GD_TraCuuBenhNhan extends ManHinhCoSo {
    private final LeTanBUS nghiepVu;
    private final JComboBox<String> cbbTieuChi = new JComboBox<>(new String[] { "Mã", "SĐT", "Họ tên" });
    private final JTextField txtTuKhoa = new JTextField(26);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "STT", "Mã BN", "Họ tên", "SĐT", "CCCD", "Địa chỉ", "Tiền sử dị ứng" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);
    private List<BenhNhan> dsHienTai = List.of();

    public GD_TraCuuBenhNhan(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Tra cứu hồ sơ bệnh nhân", "", boXuLy);
        this.nghiepVu = nghiepVu;

        JPanel thanh = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        thanh.add(cbbTieuChi);
        thanh.add(txtTuKhoa);
        thanh.add(taoNut("Tìm", this::tim));
        thanh.add(taoNut("Hiện tất cả", this::hienTatCa));
        txtTuKhoa.addActionListener(e -> tim());
        noiDung.add(thanh, BorderLayout.NORTH);

        bang.setRowHeight(32);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    xemChiTiet();
            }
        });
        noiDung.add(new JScrollPane(bang), BorderLayout.CENTER);
    }

    private void capNhatBang(List<BenhNhan> ds) {
        dsHienTai = List.copyOf(ds);
        model.setRowCount(0);
        int stt = 1;
        for (BenhNhan b : dsHienTai) {
            model.addRow(new Object[] { stt++, b.maBenhNhan(), b.hoTen(), b.soDienThoai(),
                    b.cccd() == null ? "" : b.cccd(),
                    b.diaChi() == null ? "" : b.diaChi(),
                    b.tienSuDiUng() == null ? "" : b.tienSuDiUng() });
        }
    }

    private void tim() {
        String tu = txtTuKhoa.getText().trim();
        String tieuChi = (String) cbbTieuChi.getSelectedItem();
        if (tu.isEmpty()) {
            capNhatBang(List.of());
            trangThai.setText("Nhập từ khóa rồi bấm Tìm, hoặc bấm Hiện tất cả.");
            return;
        }
        boXuLy.thucHien(this, () -> nghiepVu.traCuuBenhNhan(tieuChi, tu), ds -> {
            capNhatBang(ds);
            trangThai.setText(ds.size() + " kết quả. Double-click vào bệnh nhân để xem chi tiết & lịch sử khám.");
        });
    }

    private void hienTatCa() {
        boXuLy.thucHien(this, nghiepVu::danhSachBenhNhan, ds -> {
            capNhatBang(ds);
            trangThai.setText("Đang hiển thị " + ds.size() + " hồ sơ. Double-click dòng để xem lịch sử phiếu khám.");
        });
    }

    private void xemChiTiet() {
        int r = bang.getSelectedRow();
        if (r < 0 || r >= dsHienTai.size())
            return;
        BenhNhan bn = dsHienTai.get(r);
        boXuLy.thucHien(this, () -> nghiepVu.lichSuPhieuKham(bn.maBenhNhan()), ls -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Mã BN: ").append(bn.maBenhNhan()).append("\n");
            sb.append("Họ tên: ").append(bn.hoTen()).append("\n");
            sb.append("SĐT: ").append(bn.soDienThoai()).append("\n");
            sb.append("CCCD: ").append(bn.cccd() == null ? "(không)" : bn.cccd()).append("\n");
            sb.append("Ngày sinh: ").append(bn.ngaySinh() == null ? "(không)" : ngay(bn.ngaySinh())).append("\n");
            sb.append("Địa chỉ: ").append(bn.diaChi() == null ? "(không)" : bn.diaChi()).append("\n");
            sb.append("Tiền sử dị ứng: ").append(bn.tienSuDiUng() == null ? "(không)" : bn.tienSuDiUng()).append("\n");
            sb.append("\n----- LỊCH SỬ PHIẾU KHÁM -----\n");
            if (ls.isEmpty()) {
                sb.append("(chưa có lượt khám nào)\n");
            } else {
                int lan = 0;
                for (PhieuLeTan pk : ls) {
                    lan++;
                    sb.append("Lần ").append(lan).append(": ").append(pk.maPhieuKham())
                            .append(" — ").append(ngay(pk.ngayKham()))
                            .append(" — Phòng ").append(pk.maPhong())
                            .append(" — BS: ").append(pk.tenBacSi())
                            .append(" — ").append(pk.trangThai())
                            .append(" — ").append(pk.lyDoKham()).append("\n");
                }
            }
            JTextArea ta = new JTextArea(sb.toString(), 18, 52);
            ta.setEditable(false);
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                    "Chi tiết hồ sơ " + bn.maBenhNhan() + " — " + bn.hoTen(),
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void lamMoi() {
        txtTuKhoa.setText("");
        cbbTieuChi.setSelectedIndex(0);
        capNhatBang(List.of());
        trangThai.setText("Nhập từ khóa rồi bấm Tìm, hoặc bấm Hiện tất cả.");
    }
}

