package gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import entity.DuLieuBaoCao.DongThongKe;

public final class BieuDoCot extends JPanel {
    private List<DongThongKe> cacDong = List.of();

    public BieuDoCot() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 260));
    }

    public void capNhatDuLieu(List<DongThongKe> cacDong) {
        this.cacDong = List.copyOf(cacDong);
        setPreferredSize(new Dimension(600, Math.max(260, cacDong.size() * 34 + 30)));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics doHoa) {
        super.paintComponent(doHoa);
        Graphics2D g = (Graphics2D) doHoa.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (cacDong.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("Không có dữ liệu trong khoảng đã chọn.", 24, 40);
                return;
            }
            long lonNhat = cacDong.stream().mapToLong(DongThongKe::soLuong).max().orElse(1);
            int doRongNhan = Math.min(220, getWidth() / 3),
                    doRongCot = Math.max(1, getWidth() - doRongNhan - 85);
            int dau = Math.max(0, (g.getClipBounds().y - 20) / 34), cuoi = Math.min(cacDong.size(),
                    (g.getClipBounds().y + g.getClipBounds().height) / 34 + 1);
            for (int i = dau; i < cuoi; i++) {
                DongThongKe dong = cacDong.get(i);
                int y = 20 + i * 34;
                String nhan = dong.nhan();
                while (nhan.length() > 1 && g.getFontMetrics().stringWidth(nhan) > doRongNhan - 22)
                    nhan = nhan.substring(0, nhan.length() - 2) + "…";
                g.setColor(new Color(50, 65, 80));
                g.drawString(nhan, 12, y + 17);
                int chieuRong = (int) Math.round((double) dong.soLuong() / Math.max(1, lonNhat) * doRongCot);
                g.setColor(new Color(16, 145, 137));
                g.fillRoundRect(doRongNhan, y, Math.max(1, chieuRong), 23, 6, 6);
                g.setColor(new Color(50, 65, 80));
                g.drawString(Long.toString(dong.soLuong()), doRongNhan + chieuRong + 8, y + 17);
            }
        } finally {
            g.dispose();
        }
    }
}
