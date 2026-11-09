package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import vn.nhom15.nhakhoa.bus.LeTanBUS;
import vn.nhom15.nhakhoa.entity.BenhNhan;

/** UC002: Hồ sơ bệnh nhân — đúng bố cục giao diện gốc (STT, Mã BN, Họ tên, SĐT + form bên phải + nút Thu tiền). */
public final class GD_HoSoBenhNhan extends ManHinhCoSo {
    private final LeTanBUS nghiepVu;
    private final Consumer<BenhNhan> khiChuyenThuTien;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "STT", "Mã BN", "Họ tên", "SĐT" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);
    private List<BenhNhan> dsHienTai = List.of();
    private final JTextField oTim = new JTextField(20);
    private final JTextField ten = new JTextField();
    private final JTextField sdt = new JTextField();
    private final JTextField cccd = new JTextField();
    private final JTextField sinh = new JTextField();
    private final JTextField diaChi = new JTextField();
    private final JTextArea diUng = new JTextArea(3, 18);
    private final JButton nutThuTien;
    private BenhNhan dangChon;

    public GD_HoSoBenhNhan(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        this(nghiepVu, boXuLy, null);
    }

    public GD_HoSoBenhNhan(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy,
            Consumer<BenhNhan> khiChuyenThuTien) {
        super("Hồ sơ bệnh nhân", "", boXuLy);
        this.nghiepVu = nghiepVu;
        this.khiChuyenThuTien = khiChuyenThuTien;

        JPanel thanhTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        oTim.setToolTipText("Nhập số điện thoại");
        thanhTim.add(oTim);
        thanhTim.add(taoNut("Tìm", this::tim));
        thanhTim.add(taoNut("Hiện tất cả", this::hienTatCa));
        oTim.addActionListener(e -> tim());
        noiDung.add(thanhTim, BorderLayout.NORTH);

        JPanel vungBang = new JPanel(new BorderLayout(0, 6));
        JLabel nhanBang = new JLabel("DANH SÁCH BỆNH NHÂN");
        nhanBang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vungBang.add(nhanBang, BorderLayout.NORTH);
        bang.setRowHeight(32);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        vungBang.add(new JScrollPane(bang), BorderLayout.CENTER);
        noiDung.add(vungBang, BorderLayout.CENTER);

        JPanel benPhai = new JPanel(new BorderLayout(0, 12));
        benPhai.setPreferredSize(new Dimension(390, 0));
        benPhai.setBackground(Color.WHITE);
        benPhai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 232)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel tieuDeForm = new JLabel("THÔNG TIN HỒ SƠ");
        tieuDeForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        benPhai.add(tieuDeForm, BorderLayout.NORTH);

        diUng.setLineWrap(true);
        diUng.setWrapStyleWord(true);
        JScrollPane cuonDiUng = new JScrollPane(diUng);
        cuonDiUng.setPreferredSize(new Dimension(220, 68));
        sinh.setToolTipText("Định dạng dd/MM/yyyy");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        String[] cacNhan = { "Họ tên*", "SĐT*", "CCCD", "Ngày sinh", "Địa chỉ", "Tiền sử dị ứng" };
        JComponent[] cacTruong = { ten, sdt, cccd, sinh, diaChi, cuonDiUng };
        for (int i = 0; i < cacNhan.length; i++) {
            GridBagConstraints gcNhan = new GridBagConstraints();
            gcNhan.gridx = 0;
            gcNhan.gridy = i;
            gcNhan.anchor = GridBagConstraints.WEST;
            gcNhan.insets = new Insets(6, 0, 6, 12);
            form.add(new JLabel(cacNhan[i]), gcNhan);

            GridBagConstraints gcTruong = new GridBagConstraints();
            gcTruong.gridx = 1;
            gcTruong.gridy = i;
            gcTruong.weightx = 1;
            gcTruong.fill = GridBagConstraints.HORIZONTAL;
            gcTruong.insets = new Insets(6, 0, 6, 0);
            if (cacTruong[i] != cuonDiUng)
                cacTruong[i].setPreferredSize(new Dimension(220, 32));
            form.add(cacTruong[i], gcTruong);
        }
        benPhai.add(form, BorderLayout.CENTER);

        JButton nutThem = taoNut("Thêm", () -> luu(true));
        nutThem.setBackground(new Color(19, 62, 102));
        nutThem.setForeground(Color.WHITE);
        JButton nutSua = taoNut("Sửa", () -> luu(false));
        JButton nutLamMoi = taoNut("Làm mới", this::xoaForm);

        JPanel hangNut = new JPanel(new GridLayout(1, 3, 8, 0));
        hangNut.setOpaque(false);
        hangNut.add(nutThem);
        hangNut.add(nutSua);
        hangNut.add(nutLamMoi);

        nutThuTien = taoNut("Thu tiền", this::chuyenSangThuTien);
        nutThuTien.setEnabled(false);
        JPanel dayPhai = new JPanel(new BorderLayout(0, 8));
        dayPhai.setOpaque(false);
        dayPhai.add(hangNut, BorderLayout.NORTH);
        dayPhai.add(nutThuTien, BorderLayout.SOUTH);
        benPhai.add(dayPhai, BorderLayout.SOUTH);

        noiDung.add(benPhai, BorderLayout.EAST);

        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = bang.getSelectedRow();
                if (r >= 0 && r < dsHienTai.size()) {
                    dangChon = dsHienTai.get(r);
                    ten.setText(dangChon.getHoTen());
                    sdt.setText(dangChon.getSoDienThoai());
                    cccd.setText(dangChon.getCccd() == null ? "" : dangChon.getCccd());
                    sinh.setText(ngay(dangChon.getNgaySinh()));
                    diaChi.setText(dangChon.getDiaChi() == null ? "" : dangChon.getDiaChi());
                    diUng.setText(dangChon.getTienSuDiUng() == null ? "" : dangChon.getTienSuDiUng());
                    nutThuTien.setEnabled(true);
                } else {
                    dangChon = null;
                    nutThuTien.setEnabled(false);
                }
            }
        });
    }

    private void capNhatBang(List<BenhNhan> ds) {
        dsHienTai = List.copyOf(ds);
        model.setRowCount(0);
        int stt = 1;
        for (BenhNhan b : dsHienTai)
            model.addRow(new Object[] { stt++, b.getMaBenhNhan(), b.getHoTen(), b.getSoDienThoai() });
        dangChon = null;
        nutThuTien.setEnabled(false);
    }

    private void tim() {
        String tu = oTim.getText().trim();
        if (tu.isEmpty()) {
            capNhatBang(List.of());
            trangThai.setText("Nhập số điện thoại rồi bấm Tìm, hoặc bấm Hiện tất cả.");
            return;
        }
        boXuLy.thucHien(this, () -> nghiepVu.timBenhNhan(tu), ds -> {
            capNhatBang(ds);
            trangThai.setText(ds.size() + " hồ sơ phù hợp.");
        });
    }

    private void hienTatCa() {
        boXuLy.thucHien(this, nghiepVu::danhSachBenhNhan, ds -> {
            capNhatBang(ds);
            trangThai.setText("Đang hiển thị " + ds.size() + " hồ sơ bệnh nhân.");
        });
    }

    private void luu(boolean them) {
        if (!them && dangChon == null)
            throw new IllegalArgumentException("Chọn bệnh nhân trong bảng trước khi sửa.");
        LocalDate ngaySinh = sinh.getText().isBlank() ? null : ngay(sinh.getText());
        BenhNhan b = new BenhNhan(them ? null : dangChon.getMaBenhNhan(), ten.getText(), sdt.getText(),
                cccd.getText(), ngaySinh, diaChi.getText(), diUng.getText());
        boXuLy.thucHien(this, () -> nghiepVu.luuBenhNhan(b), daLuu -> {
            oTim.setText(daLuu.getSoDienThoai());
            capNhatBang(List.of(daLuu));
            bang.setRowSelectionInterval(0, 0);
            trangThai.setText("Đã lưu hồ sơ " + daLuu.getMaBenhNhan() + ".");
        });
    }

    private void chuyenSangThuTien() {
        if (dangChon == null)
            throw new IllegalArgumentException("Chọn hoặc thêm bệnh nhân trước khi thu tiền.");
        if (khiChuyenThuTien != null)
            khiChuyenThuTien.accept(dangChon);
    }

    private void xoaForm() {
        ten.setText("");
        sdt.setText("");
        sinh.setText("");
        cccd.setText("");
        diaChi.setText("");
        diUng.setText("");
        bang.clearSelection();
        dangChon = null;
        nutThuTien.setEnabled(false);
    }

    @Override
    public void lamMoi() {
        oTim.setText("");
        capNhatBang(List.of());
        xoaForm();
        trangThai.setText("No content in table — nhập số điện thoại để tìm hoặc bấm Hiện tất cả.");
    }
}
