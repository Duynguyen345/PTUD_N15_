package gui;

import java.awt.*;
import javax.swing.*;
import entity.PhienDangNhap;

/** Thông tin người dùng của phiên đăng nhập hiện tại. */
public final class KhungThongTinNguoiDung extends JPanel {
    public KhungThongTinNguoiDung(PhienDangNhap phien) {
        super(new BorderLayout(10, 0));
        setOpaque(false);
        JLabel anhDaiDien = new JLabel(new BieuTuongNguoiDung());
        anhDaiDien.getAccessibleContext().setAccessibleName("Avatar người dùng");
        add(anhDaiDien, BorderLayout.WEST);

        JPanel chiTiet = new JPanel(new GridLayout(2, 1, 0, 2));
        chiTiet.setOpaque(false);
        JLabel ten = new JLabel(phien.getHoTen());
        ten.setFont(ten.getFont().deriveFont(Font.BOLD, 14f));
        JLabel chucVu = new JLabel(phien.getChucVu().toString());
        chucVu.setForeground(new Color(90, 105, 120));
        chiTiet.add(ten);
        chiTiet.add(chucVu);
        add(chiTiet, BorderLayout.CENTER);
    }

    private final class BieuTuongNguoiDung implements Icon {
        @Override
        public int getIconWidth() {
            return 38;
        }

        @Override
        public int getIconHeight() {
            return 38;
        }

        @Override
        public void paintIcon(Component thanhPhan, Graphics doHoa, int x, int y) {
            Graphics2D g = (Graphics2D) doHoa.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(227, 240, 239));
                g.fillOval(x, y, 38, 38);
                g.setColor(new Color(13, 132, 124));
                g.fillOval(x + 14, y + 7, 10, 10);
                g.fillRoundRect(x + 8, y + 21, 22, 12, 12, 12);
            } finally {
                g.dispose();
            }
        }
    }
}
