package gui;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import bus.BaoCaoBUS;
import entity.DuLieuBaoCao;
import entity.DuLieuBaoCao.DongThongKe;

public final class GD_BaoCao extends ManHinhCoSo {
    private final BaoCaoBUS nghiepVu;
    private final JTextField tuNgay = new JTextField(10);
    private final JTextField denNgay = new JTextField(10);
    private final BangDuLieu<DongThongKe> danhSachLuotKham = new BangDuLieu<>(
            new String[] { "Ngày", "Số lượt khám" },
            r -> new Object[] { ngay(LocalDate.parse(r.nhan())), r.soLuong() });
    private final BangDuLieu<DongThongKe> danhSachDichVu = new BangDuLieu<>(
            new String[] { "Dịch vụ", "Số lần chỉ định" }, r -> new Object[] { r.nhan(), r.soLuong() });
    private final BieuDoCot bieuDoLuotKham = new BieuDoCot();
    private final BieuDoCot bieuDoDichVu = new BieuDoCot();
    private DuLieuBaoCao duLieuDaTai;

    public GD_BaoCao(BaoCaoBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Báo cáo lượt khám",
                "Đếm phiếu đã lập ở mọi trạng thái và số lần chỉ định dịch vụ. Không gồm doanh thu.", boXuLy);
        this.nghiepVu = nghiepVu;
        tuNgay.setText(ngay(LocalDate.now().minusDays(6)));
        denNgay.setText(ngay(LocalDate.now()));
        JPanel thanhDieuKhien = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        thanhDieuKhien.add(new JLabel("Từ ngày"));
        thanhDieuKhien.add(tuNgay);
        thanhDieuKhien.add(new JLabel("Đến ngày"));
        thanhDieuKhien.add(denNgay);
        thanhDieuKhien.add(new JLabel("dd/MM/yyyy"));
        thanhDieuKhien.add(taoNut("Xem báo cáo", this::lamMoi));
        thanhDieuKhien.add(taoNut("Xuất CSV", this::xuatBaoCao));
        noiDung.add(thanhDieuKhien, BorderLayout.NORTH);
        JTabbedPane cacTab = new JTabbedPane();
        cacTab.addTab("Lượt khám theo ngày", taoTabBaoCao(danhSachLuotKham, bieuDoLuotKham));
        cacTab.addTab("Tần suất dịch vụ", taoTabBaoCao(danhSachDichVu, bieuDoDichVu));
        noiDung.add(cacTab, BorderLayout.CENTER);
        trangThai.setText("Chọn khoảng thời gian rồi bấm Xem báo cáo.");
    }

    private JPanel taoTabBaoCao(JPanel bang, BieuDoCot bieuDo) {
        JPanel khung = new JPanel(new BorderLayout());
        JSplitPane khungChia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(bieuDo), bang);
        khungChia.setResizeWeight(0.55);
        khungChia.setDividerLocation(280);
        khung.add(khungChia);
        return khung;
    }

    @Override
    public void lamMoi() {
        LocalDate dau = ngay(tuNgay.getText()), cuoi = ngay(denNgay.getText());
        duLieuDaTai = null;
        danhSachLuotKham.capNhatDuLieu(java.util.List.of());
        danhSachDichVu.capNhatDuLieu(java.util.List.of());
        bieuDoLuotKham.capNhatDuLieu(java.util.List.of());
        bieuDoDichVu.capNhatDuLieu(java.util.List.of());
        trangThai.setText("Đang tổng hợp...");
        boXuLy.thucHien(this, () -> nghiepVu.layBaoCao(dau, cuoi), duLieuBaoCao -> {
            duLieuDaTai = duLieuBaoCao;
            danhSachLuotKham.capNhatDuLieu(duLieuBaoCao.danhSachLuotKham());
            danhSachDichVu.capNhatDuLieu(duLieuBaoCao.danhSachDichVu());
            bieuDoLuotKham.capNhatDuLieu(duLieuBaoCao.danhSachLuotKham());
            bieuDoDichVu.capNhatDuLieu(duLieuBaoCao.danhSachDichVu());
            trangThai.setText(
                    "Đã tải " + ngay(duLieuBaoCao.tuNgay()) + " – " + ngay(duLieuBaoCao.denNgay()) + " · "
                            + duLieuBaoCao.danhSachLuotKham().stream().mapToLong(DongThongKe::soLuong).sum()
                            + " lượt khám · CSV xuất đúng lần tải này.");
        });
    }

    private void xuatBaoCao() {
        if (duLieuDaTai == null)
            throw new IllegalArgumentException("Bấm Xem báo cáo thành công trước khi xuất CSV.");
        JFileChooser hopChonTep = new JFileChooser();
        hopChonTep.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        hopChonTep.setSelectedFile(new java.io.File("baocao_luotkham_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));
        if (hopChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        Path tepDaChon = hopChonTep.getSelectedFile().toPath();
        if (!tepDaChon.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".csv"))
            tepDaChon = Path.of(tepDaChon + ".csv");
        if (Files.exists(tepDaChon) && !xacNhan("File đã tồn tại. Ghi đè file này?"))
            return;
        Path tepDich = tepDaChon;
        DuLieuBaoCao banChupDuLieu = duLieuDaTai;
        boXuLy.thucHien(this, () -> {
            nghiepVu.xuatCSV(banChupDuLieu, tepDich);
            return tepDich;
        }, tep -> trangThai.setText("Đã xuất CSV UTF-8 BOM: " + tep.toAbsolutePath()));
    }
}
