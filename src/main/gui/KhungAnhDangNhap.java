package gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Hiển thị ảnh thương hiệu đúng tỷ lệ khi thay đổi kích thước cửa sổ. */
public final class KhungAnhDangNhap extends JPanel {
    private final BufferedImage anh;

    public KhungAnhDangNhap() {
        try (var luong = getClass().getResourceAsStream("/images/login-brand.png")) {
            if (luong == null)
                throw new IllegalStateException("Không tìm thấy ảnh đăng nhập.");
            anh = ImageIO.read(luong);
            if (anh == null)
                throw new IllegalStateException("Ảnh đăng nhập không hợp lệ.");
        } catch (IOException loi) {
            throw new IllegalStateException("Không thể đọc ảnh đăng nhập.", loi);
        }
        setBackground(new Color(anh.getRGB(0, 0)));
        getAccessibleContext().setAccessibleName("Nha khoa Sài Gòn");
    }

    @Override
    protected void paintComponent(Graphics doHoa) {
        super.paintComponent(doHoa);
        Graphics2D g = (Graphics2D) doHoa.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double tiLe = Math.min((double) getWidth() / anh.getWidth(),
                    (double) getHeight() / anh.getHeight());
            int chieuRong = (int) Math.round(anh.getWidth() * tiLe);
            int chieuCao = (int) Math.round(anh.getHeight() * tiLe);
            g.drawImage(anh, (getWidth() - chieuRong) / 2, (getHeight() - chieuCao) / 2, chieuRong, chieuCao,
                    this);
        } finally {
            g.dispose();
        }
    }
}
