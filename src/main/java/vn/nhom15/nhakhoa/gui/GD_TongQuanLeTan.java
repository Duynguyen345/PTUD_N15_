package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.LeTanBUS;
import vn.nhom15.nhakhoa.entity.PhienDangNhap;

/** Màn Tổng quan của Lễ tân — dùng đúng ảnh /pic/dashboard_clinic.png từ bộ tài nguyên gốc. */
public final class GD_TongQuanLeTan extends ManHinhCoSo {
    private final LeTanBUS nghiepVu;
    private final JLabel lbHomNay = new JLabel();
    private final JLabel lbVaiTro = new JLabel();
    private final JLabel lbChaoMung = new JLabel();

    public GD_TongQuanLeTan(LeTanBUS nghiepVu, BoXuLyTacVuGiaoDien boXuLy) {
        super("Tổng quan", "", boXuLy);
        this.nghiepVu = nghiepVu;

        JPanel hangThe = new JPanel(new GridLayout(1, 3, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(taoThe("HÔM NAY", lbHomNay, null));
        hangThe.add(taoThe("VAI TRÒ", lbVaiTro, null));
        JLabel lbHeThong = new JLabel("DentistClinic");
        hangThe.add(taoThe("HỆ THỐNG", lbHeThong, "Nha khoa Sài Gòn - Nhóm 15"));
        noiDung.add(hangThe, BorderLayout.NORTH);

        JPanel khungChao = new JPanel(new BorderLayout(0, 12));
        khungChao.setBackground(Color.WHITE);
        khungChao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 232)),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        lbChaoMung.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbChaoMung.setForeground(new Color(18, 49, 79));
        khungChao.add(lbChaoMung, BorderLayout.NORTH);
        khungChao.add(new AnhPhongKham("/pic/dashboard_clinic.png"), BorderLayout.CENTER);
        noiDung.add(khungChao, BorderLayout.CENTER);
    }

    private JPanel taoThe(String nhanTren, JLabel giaTriChinh, String phuDe) {
        JPanel the = new JPanel(new BorderLayout(0, 6));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 234, 242)),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));
        JLabel lbTren = new JLabel(nhanTren);
        lbTren.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbTren.setForeground(new Color(122, 140, 163));
        the.add(lbTren, BorderLayout.NORTH);

        giaTriChinh.setFont(new Font("Segoe UI", Font.BOLD, 30));
        giaTriChinh.setForeground(new Color(23, 58, 94));
        the.add(giaTriChinh, BorderLayout.CENTER);

        if (phuDe != null) {
            JLabel lbPhu = new JLabel(phuDe);
            lbPhu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbPhu.setForeground(new Color(122, 140, 163));
            the.add(lbPhu, BorderLayout.SOUTH);
        }
        return the;
    }

    @Override
    public void lamMoi() {
        PhienDangNhap phien = nghiepVu.phienLeTan();
        lbHomNay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lbVaiTro.setText(phien.getChucVu().toString());
        lbChaoMung.setText("Chào mừng " + phien.getHoTen() + " đến với hệ thống quản lý phòng khám!");
        trangThai.setText(" ");
    }

    /** Vẽ ảnh dashboard_clinic.png giữ đúng tỷ lệ (contain & center) như CSS gốc. */
    private static final class AnhPhongKham extends JPanel {
        private final BufferedImage anh;

        AnhPhongKham(String duongDan) {
            setOpaque(false);
            setPreferredSize(new Dimension(600, 340));
            BufferedImage doc = null;
            try (var luong = getClass().getResourceAsStream(duongDan)) {
                if (luong != null)
                    doc = ImageIO.read(luong);
            } catch (Exception boQua) {
            }
            this.anh = doc;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (anh == null || getWidth() <= 0 || getHeight() <= 0)
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                double tiLe = Math.min((double) (getWidth() - 24) / anh.getWidth(),
                        (double) (getHeight() - 12) / anh.getHeight());
                int w = Math.max(1, (int) Math.round(anh.getWidth() * tiLe));
                int h = Math.max(1, (int) Math.round(anh.getHeight() * tiLe));
                g2.drawImage(anh, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, this);
            } finally {
                g2.dispose();
            }
        }
    }
}
