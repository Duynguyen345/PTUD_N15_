package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import vn.nhom15.nhakhoa.bus.TraCuuHoSoBUS;
import vn.nhom15.nhakhoa.entity.*;

/** Boundary UC006: tìm hồ sơ, xem thông tin và lịch sử; không sửa dữ liệu khám. */
public final class GD_TraCuuHoSo extends ManHinhCoSo {
    private final TraCuuHoSoBUS traCuuBUS;
    private final JTextField txtTuKhoa = new JTextField(24);
    private final JTextArea txaHoSo = new JTextArea();
    private final JTextArea txaLanKham = new JTextArea();
    private final BangDuLieu<BenhNhan> tblBenhNhan;
    private final BangDuLieu<LichSuKham> tblLichSu;
    private long phienTimKiem;

    /** Một màn hình tra cứu gồm danh sách kết quả, hồ sơ và lịch sử lần khám. */
    public GD_TraCuuHoSo(TraCuuHoSoBUS traCuuBUS, BoXuLyTacVuGiaoDien boXuLy) {
        super("Tra cứu hồ sơ bệnh nhân", "Tìm bằng mã bệnh nhân, họ tên có/không dấu hoặc số điện thoại.", boXuLy);
        this.traCuuBUS = traCuuBUS;
        tblBenhNhan = new BangDuLieu<>(new String[] { "Mã bệnh nhân", "Họ tên", "Ngày sinh", "Điện thoại" },
                b -> new Object[] { b.maBenhNhan(), b.hoTen(), ngay(b.ngaySinh()), b.soDienThoai() });
        tblLichSu = new BangDuLieu<>(new String[] { "Mã phiếu", "Ngày khám", "Bác sĩ", "Trạng thái" },
                l -> new Object[] { l.maPhieuKham(), ngay(l.ngayKham()), l.tenBacSi(), l.trangThai() });
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JLabel lblTuKhoa = new JLabel("Từ khóa");
        lblTuKhoa.setLabelFor(txtTuKhoa);
        pnlTim.add(lblTuKhoa);
        pnlTim.add(txtTuKhoa);
        JButton btnTim = taoNut("Tìm hồ sơ", this::timKiem);
        btnTim.setMnemonic('T');
        pnlTim.add(btnTim);
        pnlTim.add(taoNut("Xóa tìm kiếm", () -> { txtTuKhoa.setText(""); xoaKetQua(); }));
        txtTuKhoa.addActionListener(e -> btnTim.doClick());
        noiDung.add(pnlTim, BorderLayout.NORTH);
        JPanel pnlChiTiet = new JPanel(new GridLayout(2, 1, 8, 8));
        cauHinhVanBan(txaHoSo, "Thông tin bệnh nhân");
        cauHinhVanBan(txaLanKham, "Chi tiết lần khám đã chọn");
        pnlChiTiet.add(new JScrollPane(txaHoSo));
        pnlChiTiet.add(new JScrollPane(txaLanKham));
        JPanel pnlDanhSach = new JPanel(new GridLayout(2, 1, 8, 12));
        tblBenhNhan.setBorder(BorderFactory.createTitledBorder("Kết quả tìm kiếm — chọn bệnh nhân"));
        tblLichSu.setBorder(BorderFactory.createTitledBorder("Lịch sử khám — chọn phiếu"));
        pnlDanhSach.add(tblBenhNhan);
        pnlDanhSach.add(tblLichSu);
        JSplitPane splHoSo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlDanhSach, pnlChiTiet);
        splHoSo.setResizeWeight(0.62);
        splHoSo.setBorder(null);
        pnlChiTiet.setMinimumSize(new Dimension(300, 200));
        noiDung.add(splHoSo, BorderLayout.CENTER);
        tblBenhNhan.khiChonDong(this::xemHoSo);
        tblLichSu.khiChonDong(l -> txaLanKham.setText("PHIẾU KHÁM " + l.maPhieuKham() + "\n\nNgày khám: "
                + ngay(l.ngayKham()) + "\nBác sĩ: " + l.tenBacSi() + "\nTrạng thái: " + l.trangThai()
                + "\n\nChẩn đoán: " + l.chanDoan() + "\n\nĐiều trị: " + l.dieuTri() + "\n\nGhi chú: " + l.ghiChu()));
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { xoaKetQua(); }
            @Override public void removeUpdate(DocumentEvent e) { xoaKetQua(); }
            @Override public void changedUpdate(DocumentEvent e) { xoaKetQua(); }
        });
    }

    private void cauHinhVanBan(JTextArea txa, String tieuDe) {
        txa.setEditable(false);
        txa.setLineWrap(true);
        txa.setWrapStyleWord(true);
        txa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txa.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(tieuDe),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    private void xoaKetQua() {
        phienTimKiem++;
        tblBenhNhan.capNhatDuLieu(List.of());
        tblLichSu.capNhatDuLieu(List.of());
        txaHoSo.setText("");
        txaLanKham.setText("");
        trangThai.setText("Nhập từ khóa rồi nhấn Enter hoặc Tìm hồ sơ.");
    }

    private void timKiem() {
        String tuKhoa = txtTuKhoa.getText();
        xoaKetQua();
        long phien = phienTimKiem;
        boXuLy.thucHien(this, () -> traCuuBUS.timBenhNhan(tuKhoa), cacBenhNhan -> {
            if (phien != phienTimKiem) return;
            tblBenhNhan.capNhatDuLieu(cacBenhNhan);
            trangThai.setText(cacBenhNhan.isEmpty() ? "Không tìm thấy hồ sơ phù hợp." : "Tìm thấy " + cacBenhNhan.size() + " hồ sơ. Chọn một bệnh nhân để xem.");
        });
    }

    private void xemHoSo(BenhNhan benhNhan) {
        if (boXuLy.dangXuLy()) return;
        long phien = ++phienTimKiem;
        tblLichSu.capNhatDuLieu(List.of());
        txaHoSo.setText("");
        txaLanKham.setText("");
        boXuLy.thucHien(this, () -> traCuuBUS.layLichSuKham(benhNhan.maBenhNhan()), lichSu -> {
            if (phien != phienTimKiem) return;
            txaHoSo.setText(benhNhan.hoTen().toUpperCase(java.util.Locale.ROOT) + "\n" + benhNhan.maBenhNhan()
                    + "\n\nNgày sinh: " + ngay(benhNhan.ngaySinh()) + "\nGiới tính: " + benhNhan.gioiTinh()
                    + "\nĐiện thoại: " + benhNhan.soDienThoai() + "\nĐịa chỉ: " + benhNhan.diaChi()
                    + "\n\nTiền sử: " + benhNhan.tienSu() + "\n\nDị ứng: " + benhNhan.diUng());
            tblLichSu.capNhatDuLieu(lichSu);
            trangThai.setText(benhNhan.hoTen() + " — " + lichSu.size() + " lần khám. Chọn phiếu để xem kết quả.");
        });
    }

    @Override
    public void lamMoi() { xoaKetQua(); txtTuKhoa.requestFocusInWindow(); }
}
