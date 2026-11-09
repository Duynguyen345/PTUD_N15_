package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import bus.LeTanBUS;
import entity.*;

/**
 * UC004: Lập phiếu khám — đúng bố cục giao diện gốc:
 * - Thanh lọc (Tất cả phòng / Phòng đang khám / Phòng có hàng chờ / Phòng trống / Phòng tắt nhận bệnh) + Tìm phòng + Tất cả + Làm mới
 * - Các tile phòng tô màu (Xám: tắt nhận / chưa phân công, Xanh lá: trống — lập phiếu được, Vàng: có hàng chờ, Đỏ: đang khám)
 * - Dòng thông tin phòng + Bác sĩ / Phụ tá + Chờ / Đang khám
 * - Bảng lượt khám của phòng (STT, Bệnh nhân, Trạng thái)
 * - Nút "Lập phiếu khám tại phòng này" mở dialog chọn bệnh nhân & nhập lý do khám.
 */
public final class GD_LapPhieuKham extends ManHinhCoSo {
    private final LeTanBUS nghiepVu;
    private final JComboBox<String> cbbLoc = new JComboBox<>(new String[] {
            "Tất cả phòng", "Phòng đang khám", "Phòng có hàng chờ", "Phòng trống", "Phòng tắt nhận bệnh" });
    private final JTextField txtTimPhong = new JTextField(18);
    private final JPanel paneTiles = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
    private final JLabel lbPhongDaChon = new JLabel("Chọn phòng");
    private final JLabel lbNhanSuPhong = new JLabel(" ");
    private final JLabel lbSoLuot = new JLabel(" ");
    private final DefaultTableModel modelLuot = new DefaultTableModel(
            new Object[] { "STT", "Bệnh nhân", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tableLuot = new JTable(modelLuot);
    private final JButton btnLapPhieu;

    private List<PhongLeTan> dsPhong = List.of();
    private PhongLeTan phongDaChon;
    private BenhNhan bnChoLapPhieu;

    public GD_LapPhieuKham(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        this(nghiepVu, boXuLy, null);
    }

    public GD_LapPhieuKham(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy, BenhNhan bnBanDau) {
        super("Lập phiếu khám", "", boXuLy);
        this.nghiepVu = nghiepVu;
        this.bnChoLapPhieu = bnBanDau;

        JPanel thanhLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        thanhLoc.setOpaque(false);
        cbbLoc.setPreferredSize(new Dimension(200, 34));
        txtTimPhong.setToolTipText("Nhập mã/tên phòng cần tìm");
        txtTimPhong.addActionListener(e -> veDanhSachTile());
        thanhLoc.add(cbbLoc);
        thanhLoc.add(txtTimPhong);
        thanhLoc.add(taoNut("Tìm phòng", this::veDanhSachTile));
        thanhLoc.add(taoNut("Tất cả", () -> {
            txtTimPhong.setText("");
            cbbLoc.setSelectedIndex(0);
            veDanhSachTile();
        }));
        thanhLoc.add(taoNut("Làm mới", this::lamMoi));
        cbbLoc.addActionListener(e -> veDanhSachTile());
        noiDung.add(thanhLoc, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 14));
        giua.setOpaque(false);

        paneTiles.setOpaque(false);
        giua.add(paneTiles, BorderLayout.NORTH);

        JPanel khungDuoi = new JPanel(new BorderLayout(0, 8));
        khungDuoi.setOpaque(false);

        JPanel dongThongTin = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        dongThongTin.setOpaque(false);
        lbPhongDaChon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbNhanSuPhong.setForeground(new Color(18, 88, 168));
        lbNhanSuPhong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbSoLuot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dongThongTin.add(lbPhongDaChon);
        dongThongTin.add(lbNhanSuPhong);
        dongThongTin.add(lbSoLuot);
        khungDuoi.add(dongThongTin, BorderLayout.NORTH);

        tableLuot.setRowHeight(32);
        tableLuot.setFillsViewportHeight(true);
        tableLuot.getTableHeader().setReorderingAllowed(false);
        khungDuoi.add(new JScrollPane(tableLuot), BorderLayout.CENTER);

        btnLapPhieu = taoNut("Lập phiếu khám tại phòng này", this::moDialogLapPhieu);
        btnLapPhieu.setEnabled(false);
        JPanel dayNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        dayNut.setOpaque(false);
        dayNut.add(btnLapPhieu);
        khungDuoi.add(dayNut, BorderLayout.SOUTH);

        giua.add(khungDuoi, BorderLayout.CENTER);
        noiDung.add(giua, BorderLayout.CENTER);
    }

    private void veDanhSachTile() {
        paneTiles.removeAll();
        String loc = (String) cbbLoc.getSelectedItem();
        String tu = txtTimPhong.getText() == null ? "" : txtTimPhong.getText().trim().toLowerCase(Locale.ROOT);
        for (PhongLeTan p : dsPhong) {
            if (!khopBoLoc(p, loc))
                continue;
            if (!tu.isEmpty()
                    && !p.maPhong().toLowerCase(Locale.ROOT).contains(tu)
                    && !p.tenPhong().toLowerCase(Locale.ROOT).contains(tu))
                continue;
            paneTiles.add(taoTile(p));
        }
        paneTiles.revalidate();
        paneTiles.repaint();
    }

    private boolean khopBoLoc(PhongLeTan p, String loc) {
        if (loc == null || "Tất cả phòng".equals(loc))
            return true;
        return switch (loc) {
            case "Phòng đang khám" -> p.dangNhanBenh() && p.soDangKham() > 0;
            case "Phòng có hàng chờ" -> p.dangNhanBenh() && p.soCho() > 0;
            case "Phòng trống" -> p.dangNhanBenh() && p.soCho() == 0 && p.soDangKham() == 0;
            case "Phòng tắt nhận bệnh" -> !p.dangNhanBenh();
            default -> true;
        };
    }

    private JPanel taoTile(PhongLeTan p) {
        Color mau;
        String chuTrangThai;
        if (!p.dangNhanBenh()) {
            mau = new Color(0x9EA3AA);
            chuTrangThai = "KHÔNG TIẾP NHẬN";
        } else if (!p.sanSang()) {
            mau = new Color(0x9EA3AA);
            chuTrangThai = "CHƯA PHÂN CÔNG";
        } else if (p.soDangKham() > 0) {
            mau = new Color(0xE05252);
            chuTrangThai = "ĐANG KHÁM";
        } else if (p.soCho() > 0) {
            mau = new Color(0xE5A13D);
            chuTrangThai = "CÓ HÀNG CHỜ";
        } else {
            mau = new Color(0x28B463);
            chuTrangThai = "TRỐNG — LẬP PHIẾU ĐƯỢC";
        }

        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(mau);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                } finally {
                    g2.dispose();
                }
            }
        };
        tile.setOpaque(false);
        tile.setLayout(new GridLayout(4, 1, 0, 2));
        tile.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        tile.setPreferredSize(new Dimension(245, 130));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbMa = new JLabel(p.maPhong());
        lbMa.setForeground(Color.WHITE);
        lbMa.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String moTaTen = p.tenPhong() + (p.dangNhanBenh() ? "" : " — TẮT NHẬN BỆNH NHÂN");
        JLabel lbTen = new JLabel(moTaTen + " ");
        lbTen.setForeground(Color.WHITE);
        lbTen.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lbSo = new JLabel("Chờ " + p.soCho() + " · Đang " + p.soDangKham() + " ");
        lbSo.setForeground(Color.WHITE);
        lbSo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lbTT = new JLabel(chuTrangThai + " ");
        lbTT.setForeground(Color.WHITE);
        lbTT.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tile.add(lbMa);
        tile.add(lbTen);
        tile.add(lbSo);
        tile.add(lbTT);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chonPhong(p);
            }
        });
        return tile;
    }

    private void chonPhong(PhongLeTan p) {
        phongDaChon = p;
        lbPhongDaChon.setText(p.maPhong() + " · " + p.tenPhong() + "   ");
        lbNhanSuPhong.setText("Bác sĩ: " + (p.tenBacSi() == null ? "chưa phân công" : p.tenBacSi())
                + " · Phụ tá: " + (p.tenPhuTa() == null ? "chưa phân công" : p.tenPhuTa()) + "    ");
        lbSoLuot.setText("Chờ " + p.soCho() + " · Đang khám " + p.soDangKham() + "   ");
        btnLapPhieu.setEnabled(p.sanSang());

        boXuLy.thucHien(this, () -> nghiepVu.danhSachLuot(p.maPhong()), ds -> {
            modelLuot.setRowCount(0);
            for (PhieuLeTan l : ds) {
                String tt = switch (l.trangThai()) {
                    case "CHO_KHAM" -> "Chờ khám";
                    case "DANG_KHAM" -> "Đang khám";
                    default -> l.trangThai();
                };
                modelLuot.addRow(new Object[] { l.soThuTu(), l.maBenhNhan() + " - " + l.tenBenhNhan(), tt });
            }
            trangThai.setText(ds.isEmpty() ? "Chưa có lượt khám"
                    : "Phòng " + p.maPhong() + " có " + ds.size() + " lượt khám.");
        });
    }

    private void moDialogLapPhieu() {
        if (phongDaChon == null || !phongDaChon.sanSang())
            throw new IllegalArgumentException("Chọn một phòng màu xanh (đang nhận bệnh) trước khi lập phiếu.");

        PhongLeTan p = phongDaChon;
        JTextField txtBn = new JTextField(22);
        JButton btnTimBn = new JButton("Tìm / Nhận diện");
        JLabel lbBn = new JLabel("(chưa chọn bệnh nhân)");
        lbBn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        BenhNhan[] bnChon = { bnChoLapPhieu };
        if (bnChon[0] != null) {
            txtBn.setText(bnChon[0].soDienThoai());
            lbBn.setText("Đã chọn: " + bnChon[0].maBenhNhan() + " - " + bnChon[0].hoTen());
            bnChoLapPhieu = null;
        }

        btnTimBn.addActionListener(e -> {
            String tu = txtBn.getText().trim();
            if (tu.isEmpty())
                return;
            try {
                List<BenhNhan> kq = nghiepVu.timBenhNhan(tu);
                if (kq.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân phù hợp.",
                            "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BenhNhan b = kq.getFirst();
                if (kq.size() > 1) {
                    BenhNhan lua = (BenhNhan) JOptionPane.showInputDialog(this,
                            "Chọn bệnh nhân:", "Kết quả tìm kiếm",
                            JOptionPane.PLAIN_MESSAGE, null, kq.toArray(), kq.getFirst());
                    if (lua != null)
                        b = lua;
                }
                bnChon[0] = b;
                lbBn.setText("Đã chọn: " + b.maBenhNhan() + " - " + b.hoTen());
            } catch (RuntimeException loi) {
                boXuLy.hienThiLoi(this, loi);
            }
        });

        JTextField txtLyDo = new JTextField(26);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 10);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Phòng khám:"), g);
        g.gridx = 1;
        form.add(new JLabel(p.maPhong() + " · " + p.tenPhong()), g);

        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("Nhân sự phòng:"), g);
        g.gridx = 1;
        form.add(new JLabel("Bác sĩ: " + p.tenBacSi() + " · Phụ tá: " + p.tenPhuTa()), g);

        g.gridx = 0;
        g.gridy = 2;
        form.add(new JLabel("Bệnh nhân (SĐT/CCCD/mã):"), g);
        JPanel hangTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangTim.add(txtBn);
        hangTim.add(btnTimBn);
        g.gridx = 1;
        form.add(hangTim, g);

        g.gridx = 1;
        g.gridy = 3;
        form.add(lbBn, g);

        g.gridx = 0;
        g.gridy = 4;
        form.add(new JLabel("Lý do khám*:"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtLyDo, g);

        int chon = JOptionPane.showConfirmDialog(this, form,
                "Lập phiếu khám — " + p.maPhong() + " · " + p.tenPhong(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (chon != JOptionPane.OK_OPTION)
            return;
        if (bnChon[0] == null)
            throw new IllegalArgumentException("Chưa chọn bệnh nhân — nhập SĐT/CCCD/mã BN rồi bấm Tìm / Nhận diện.");

        String maBN = bnChon[0].maBenhNhan();
        String maPhong = p.maPhong();
        String lyDo = txtLyDo.getText();
        boXuLy.thucHien(this, () -> nghiepVu.lapPhieu(maBN, maPhong, lyDo), pk -> {
            InChungTuLeTan.xemVaIn(this, "Phiếu tiếp nhận " + pk.maPhieuKham(), """
                    NHA KHOA SÀI GÒN — DentistClinic
                    PHIẾU TIẾP NHẬN KHÁM
                    — — — — — — — — — — — — — — — — — — —
                    Mã phiếu:   %s
                    Ngày khám:  %s
                    Số thứ tự:  %d
                    Phòng:      %s
                    Bác sĩ:     %s
                    Bệnh nhân:  %s · %s
                    Lý do khám: %s
                    HĐ phí:     %s
                    """.formatted(pk.maPhieuKham(), ngay(pk.ngayKham()), pk.soThuTu(), pk.maPhong(),
                    pk.tenBacSi(), pk.maBenhNhan(), pk.tenBenhNhan(), pk.lyDoKham(), pk.maHoaDonPhiKham()));
            lamMoi();
            trangThai.setText("Đã lập phiếu " + pk.maPhieuKham() + " — STT " + pk.soThuTu() + ".");
        });
    }

    @Override
    public void lamMoi() {
        boXuLy.thucHien(this, nghiepVu::danhSachPhong, ds -> {
            dsPhong = List.copyOf(ds);
            veDanhSachTile();
            if (!dsPhong.isEmpty()) {
                PhongLeTan chon = phongDaChon == null ? dsPhong.getFirst()
                        : dsPhong.stream().filter(p -> p.maPhong().equals(phongDaChon.maPhong()))
                                .findFirst().orElse(dsPhong.getFirst());
                chonPhong(chon);
            } else {
                phongDaChon = null;
                lbPhongDaChon.setText("Chọn phòng");
                lbNhanSuPhong.setText(" ");
                lbSoLuot.setText(" ");
                modelLuot.setRowCount(0);
                btnLapPhieu.setEnabled(false);
            }
        });
    }
}
