package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.*;
import bus.DieuPhoiBUS;
import entity.*;

/** Bảng số dành cho khu vực chờ: chỉ hiển thị phòng và số, không đưa bệnh án lên bảng. */
public final class GD_BangGoiSo extends JFrame {
    private final DieuPhoiBUS dieuPhoiBUS;
    private final JPanel pnlCacPhong = new JPanel(new GridLayout(0, 3, 18, 18));
    private final JLabel lblCapNhat = new JLabel("Đang tải...", SwingConstants.CENTER);
    private final Timer tmrLamMoi;
    private boolean dangTai;
    private long theHeTai;

    /** Làm mới mỗi 5 giây; hủy timer khi đóng hoặc đăng xuất khỏi cửa sổ chính. */
    public GD_BangGoiSo(DieuPhoiBUS dieuPhoiBUS) {
        super("Nha khoa Sài Gòn — Bảng gọi số");
        this.dieuPhoiBUS = dieuPhoiBUS;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 460);
        setLocationRelativeTo(null);
        JPanel pnlGoc = new JPanel(new BorderLayout(20, 24));
        pnlGoc.setBackground(new Color(19, 43, 65));
        pnlGoc.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel lblTieuDe = new JLabel("KÍNH MỜI BỆNH NHÂN VÀO PHÒNG KHÁM", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(Color.WHITE);
        pnlCacPhong.setOpaque(false);
        lblCapNhat.setForeground(Color.WHITE);
        pnlGoc.add(lblTieuDe, BorderLayout.NORTH);
        pnlGoc.add(pnlCacPhong, BorderLayout.CENTER);
        pnlGoc.add(lblCapNhat, BorderLayout.SOUTH);
        setContentPane(pnlGoc);
        tmrLamMoi = new Timer(5000, e -> lamMoi());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { tmrLamMoi.stop(); theHeTai++; }
        });
        tmrLamMoi.start();
        lamMoi();
    }

    private record DongBang(PhongKham phong, LuotChoKham hienTai) { }

    private void lamMoi() {
        if (dangTai) return;
        dangTai = true;
        long theHe = theHeTai;
        new SwingWorker<List<DongBang>, Void>() {
            @Override protected List<DongBang> doInBackground() {
                return dieuPhoiBUS.layDanhSachPhong().stream().map(p -> new DongBang(p,
                        dieuPhoiBUS.layHangCho(p.maPhong()).stream().filter(l -> l.trangThai() == TrangThaiHangCho.DANG_GOI
                                || l.trangThai() == TrangThaiHangCho.DANG_KHAM).findFirst().orElse(null))).toList();
            }
            @Override protected void done() {
                dangTai = false;
                if (theHe != theHeTai) return;
                try {
                    List<DongBang> cacDong = get();
                    pnlCacPhong.removeAll();
                    for (DongBang dong : cacDong) {
                        JPanel pnlPhong = new JPanel(new GridLayout(3, 1));
                        pnlPhong.setBackground(Color.WHITE);
                        pnlPhong.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
                        JLabel lblPhong = new JLabel(dong.phong().tenPhong(), SwingConstants.CENTER);
                        JLabel lblSo = new JLabel(dong.hienTai() == null ? "—" : String.format("%03d", dong.hienTai().soThuTu()), SwingConstants.CENTER);
                        lblPhong.setFont(new Font("Segoe UI", Font.BOLD, 21));
                        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 64));
                        lblSo.setForeground(new Color(18, 116, 96));
                        String trangThaiPhong = !dong.phong().dangNhanBenh() ? "Tạm ngưng nhận bệnh"
                                : dong.hienTai() == null ? "Vui lòng chờ gọi số" : dong.hienTai().trangThai().toString();
                        pnlPhong.add(lblPhong);
                        pnlPhong.add(lblSo);
                        pnlPhong.add(new JLabel(trangThaiPhong, SwingConstants.CENTER));
                        pnlCacPhong.add(pnlPhong);
                    }
                    lblCapNhat.setText("Tự cập nhật mỗi 5 giây • Cập nhật lúc " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                    revalidate(); repaint();
                } catch (Exception loi) {
                    pnlCacPhong.removeAll();
                    lblCapNhat.setText("Không thể cập nhật. Đóng bảng và kiểm tra phiên đăng nhập/kết nối.");
                    tmrLamMoi.stop();
                    revalidate(); repaint();
                }
            }
        }.execute();
    }
}
