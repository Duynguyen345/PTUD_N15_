package gui;

import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import bus.LeTanBUS;
import entity.*;
import util.DinhDangLeTan;

/**
 * UC007: Quản lý ca thu ngân — đúng bố cục giao diện gốc:
 * - Hàng 4 thẻ trạng thái: CA HIỆN TẠI, THU NGÂN, MỞ LÚC, TRẠNG THÁI
 * - BẢNG KÊ THU TRONG CA (Hóa đơn, Giờ thu, Bệnh nhân, Loại, Hình thức, Tiền)
 *   + Số hóa đơn | DOANH THU CA (dịch vụ) | Phí khám thu hộ
 * - Hàng nút: NHẬN CA (mở ca làm việc) | CHỐT CA — XÁC NHẬN BÀN GIAO | Lưu PDF bảng kê ca
 * - LỊCH SỬ CA ĐÃ CHỐT (Mã ca, Thu ngân mở ca, Mở lúc, Chốt lúc, Doanh thu dịch vụ, Phí thu hộ, Trạng thái).
 */
public final class GD_ChotCa extends ManHinhCoSo {
    private static final DateTimeFormatter HF = DateTimeFormatter.ofPattern("HH:mm");

    private final LeTanBUS nghiepVu;
    private final JLabel lbMaCa = new JLabel("(chưa có)");
    private final JLabel lbThuNgan = new JLabel("(chưa có)");
    private final JLabel lbMoLuc = new JLabel("(chưa có)");
    private final JLabel lbTrangThai = new JLabel("CHƯA MỞ CA");

    private final DefaultTableModel modelBk = new DefaultTableModel(
            new Object[] { "Hóa đơn", "Giờ thu", "Bệnh nhân", "Loại", "Hình thức", "Tiền" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tableBk = new JTable(modelBk);

    private final JLabel lbSoHd = new JLabel("Số hóa đơn: 0");
    private final JLabel lbTongCa = new JLabel("DOANH THU CA (dịch vụ): 0 ₫");
    private final JLabel lbPhiHo = new JLabel("Phí khám thu hộ: 0 ₫");

    private final JButton btnMoCa;
    private final JButton btnChotCa;
    private final JButton btnLuuBangKe;

    private final DefaultTableModel modelLs = new DefaultTableModel(
            new Object[] { "Mã ca", "Thu ngân mở ca", "Mở lúc", "Chốt lúc",
                    "Doanh thu dịch vụ", "Phí thu hộ", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tableLs = new JTable(modelLs);

    private CaThuNgan caHienTai;
    private List<HoaDonLeTan> dsHoaDonCa = List.of();

    public GD_ChotCa(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Quản lý ca thu ngân", "", boXuLy);
        this.nghiepVu = nghiepVu;

        // 1. Hàng 4 thẻ trạng thái ca
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 14, 0));
        hangThe.setBackground(Color.WHITE);
        hangThe.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 232)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        hangThe.add(taoOThongTin("CA HIỆN TẠI", lbMaCa));
        hangThe.add(taoOThongTin("THU NGÂN", lbThuNgan));
        hangThe.add(taoOThongTin("MỞ LÚC", lbMoLuc));
        hangThe.add(taoOThongTin("TRẠNG THÁI", lbTrangThai));
        noiDung.add(hangThe, BorderLayout.NORTH);

        // 2. Phần giữa: Bảng kê thu trong ca + các nút thao tác
        JPanel vungGiua = new JPanel(new BorderLayout(0, 10));
        vungGiua.setOpaque(false);

        JPanel khungBangKe = new JPanel(new BorderLayout(0, 8));
        khungBangKe.setBackground(Color.WHITE);
        khungBangKe.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 232)),
                BorderFactory.createEmptyBorder(14, 16, 12, 16)));
        JLabel tieuDeBk = new JLabel("BẢNG KÊ THU TRONG CA");
        tieuDeBk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        khungBangKe.add(tieuDeBk, BorderLayout.NORTH);

        tableBk.setRowHeight(30);
        tableBk.setFillsViewportHeight(true);
        tableBk.getTableHeader().setReorderingAllowed(false);
        khungBangKe.add(new JScrollPane(tableBk), BorderLayout.CENTER);

        JPanel dongTong = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 2));
        dongTong.setOpaque(false);
        lbSoHd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbTongCa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbTongCa.setForeground(new Color(21, 128, 61));
        lbPhiHo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dongTong.add(lbSoHd);
        dongTong.add(lbTongCa);
        dongTong.add(lbPhiHo);
        khungBangKe.add(dongTong, BorderLayout.SOUTH);

        vungGiua.add(khungBangKe, BorderLayout.CENTER);

        btnMoCa = taoNut("NHẬN CA (mở ca làm việc)", this::moCa);
        btnChotCa = taoNut("CHỐT CA — XÁC NHẬN BÀN GIAO", this::chotCa);
        btnLuuBangKe = taoNut("Lưu PDF bảng kê ca", this::xemBangKe);
        btnMoCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChotCa.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        hangNut.setOpaque(false);
        hangNut.add(btnMoCa);
        hangNut.add(btnChotCa);
        hangNut.add(btnLuuBangKe);
        vungGiua.add(hangNut, BorderLayout.SOUTH);

        noiDung.add(vungGiua, BorderLayout.CENTER);

        // 3. Phần dưới: Lịch sử ca đã chốt
        JPanel khungLichSu = new JPanel(new BorderLayout(0, 6));
        khungLichSu.setOpaque(false);
        khungLichSu.setPreferredSize(new Dimension(0, 210));
        JLabel tieuDeLs = new JLabel("LỊCH SỬ CA ĐÃ CHỐT");
        tieuDeLs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        khungLichSu.add(tieuDeLs, BorderLayout.NORTH);

        tableLs.setRowHeight(30);
        tableLs.setFillsViewportHeight(true);
        tableLs.getTableHeader().setReorderingAllowed(false);
        khungLichSu.add(new JScrollPane(tableLs), BorderLayout.CENTER);
        noiDung.add(khungLichSu, BorderLayout.SOUTH);
    }

    private JPanel taoOThongTin(String tieuDe, JLabel giaTri) {
        JPanel o = new JPanel(new BorderLayout(0, 6));
        o.setBackground(new Color(248, 250, 252));
        o.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 233, 240)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lb = new JLabel(tieuDe);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lb.setForeground(new Color(120, 134, 148));
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 15));
        giaTri.setForeground(new Color(19, 43, 65));
        o.add(lb, BorderLayout.NORTH);
        o.add(giaTri, BorderLayout.CENTER);
        return o;
    }

    private void moCa() {
        boXuLy.thucHien(this, nghiepVu::moCa, ca -> {
            JOptionPane.showMessageDialog(this,
                    "Đã nhận ca " + ca.maCa() + " lúc " + DinhDangLeTan.gio(ca.moLuc()) + ".",
                    "Mở ca làm việc", JOptionPane.INFORMATION_MESSAGE);
            lamMoi();
        });
    }

    private void chotCa() {
        if (caHienTai == null)
            throw new IllegalArgumentException("Chưa có ca đang mở.");
        if (!xacNhan("Chốt ca " + caHienTai.maCa()
                + "?\nBảng kê sẽ được ghi nhận và nhân viên tiếp theo phải NHẬN CA mới thu được."))
            return;
        String ma = caHienTai.maCa();
        boXuLy.thucHien(this, () -> nghiepVu.chotCa(ma), daChot -> {
            InChungTuLeTan.xemVaIn(this, "Bảng kê chốt ca " + daChot.maCa(), noiDungBangKe(daChot));
            lamMoi();
            trangThai.setText("Đã chốt ca " + daChot.maCa() + ".");
        });
    }

    private void xemBangKe() {
        if (caHienTai == null)
            throw new IllegalArgumentException("Chưa có ca đang mở.");
        InChungTuLeTan.xemVaIn(this, "Bảng kê ca " + caHienTai.maCa(), noiDungBangKe(caHienTai));
    }

    private String noiDungBangKe(CaThuNgan ca) {
        StringBuilder sb = new StringBuilder();
        sb.append("NHA KHOA SÀI GÒN — DentistClinic\n");
        sb.append("BẢNG KÊ CA THU NGÂN ").append(ca.maCa()).append("\n");
        sb.append("Thu ngân: ").append(ca.maNhanVien()).append(" - ").append(ca.tenNhanVien()).append("\n");
        sb.append("Mở lúc:   ").append(DinhDangLeTan.gio(ca.moLuc())).append("\n");
        if (!ca.dangMo())
            sb.append("Chốt lúc: ").append(DinhDangLeTan.gio(ca.chotLuc())).append("\n");
        sb.append("— — — — — — — — — — — — — — — — — — —\n");
        for (HoaDonLeTan h : dsHoaDonCa) {
            sb.append(h.maHoaDon()).append(" | ").append(h.ngayThu().format(HF)).append(" | ")
                    .append(h.tenBenhNhan()).append(" | ")
                    .append(h.laPhiKham() ? "Phí khám" : "Dịch vụ").append(" | ")
                    .append(DinhDangLeTan.tien(h.tongTien())).append("\n");
        }
        sb.append("— — — — — — — — — — — — — — — — — — —\n");
        sb.append(lbSoHd.getText()).append("\n");
        sb.append(lbTongCa.getText()).append("\n");
        sb.append(lbPhiHo.getText()).append("\n");
        return sb.toString();
    }

    private record DuLieuCa(CaThuNgan ca, List<HoaDonLeTan> hoaDon, List<CaThuNgan> lichSu) {
    }

    @Override
    public void lamMoi() {
        boXuLy.thucHien(this, () -> {
            CaThuNgan ca = nghiepVu.caDangMo();
            return new DuLieuCa(ca, nghiepVu.hoaDonTrongCa(ca), nghiepVu.lichSuCa());
        }, d -> {
            caHienTai = d.ca();
            dsHoaDonCa = List.copyOf(d.hoaDon());
            modelBk.setRowCount(0);
            BigDecimal dtDv = BigDecimal.ZERO;
            BigDecimal phiHo = BigDecimal.ZERO;

            if (caHienTai == null) {
                lbMaCa.setText("(chưa có)");
                lbThuNgan.setText("(chưa có)");
                lbMoLuc.setText("(chưa có)");
                lbTrangThai.setText("CHƯA MỞ CA");
                lbTrangThai.setForeground(new Color(180, 83, 9));
                btnMoCa.setEnabled(true);
                btnChotCa.setEnabled(false);
                btnLuuBangKe.setEnabled(false);
            } else {
                lbMaCa.setText(caHienTai.maCa());
                lbThuNgan.setText(caHienTai.maNhanVien() + " - " + caHienTai.tenNhanVien());
                lbMoLuc.setText(DinhDangLeTan.gio(caHienTai.moLuc()));
                lbTrangThai.setText("ĐANG MỞ");
                lbTrangThai.setForeground(new Color(21, 128, 61));
                btnMoCa.setEnabled(false);
                btnChotCa.setEnabled(true);
                btnLuuBangKe.setEnabled(true);

                for (HoaDonLeTan h : dsHoaDonCa) {
                    modelBk.addRow(new Object[] {
                            h.maHoaDon(),
                            h.ngayThu().format(HF),
                            h.tenBenhNhan(),
                            h.laPhiKham() ? "Phí khám (thu hộ)" : "Dịch vụ cuối lượt",
                            h.hinhThuc().name(),
                            DinhDangLeTan.tien(h.tongTien())
                    });
                    if (h.laPhiKham())
                        phiHo = phiHo.add(h.tongTien());
                    else
                        dtDv = dtDv.add(h.tongTien());
                }
            }
            lbSoHd.setText("Số hóa đơn: " + dsHoaDonCa.size() + "   ");
            lbTongCa.setText("DOANH THU CA (dịch vụ): " + DinhDangLeTan.tien(dtDv) + "   ");
            lbPhiHo.setText("Phí khám thu hộ: " + DinhDangLeTan.tien(phiHo) + "   ");

            modelLs.setRowCount(0);
            for (CaThuNgan c : d.lichSu()) {
                modelLs.addRow(new Object[] {
                        c.maCa(),
                        c.maNhanVien() + " - " + c.tenNhanVien(),
                        DinhDangLeTan.gio(c.moLuc()),
                        c.dangMo() ? "(đang mở)" : DinhDangLeTan.gio(c.chotLuc()),
                        c.doanhThuDichVu() == null ? "—" : DinhDangLeTan.tien(c.doanhThuDichVu()),
                        c.phiKhamThuHo() == null ? "—" : DinhDangLeTan.tien(c.phiKhamThuHo()),
                        c.dangMo() ? "ĐANG MỞ" : "ĐÃ CHỐT"
                });
            }
            trangThai.setText(" ");
        });
    }
}
