package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import javax.swing.*;
import vn.nhom15.nhakhoa.bus.DangNhapBUS;

public final class GD_DangNhap extends ManHinhCoSo {
    private final JTextField maTaiKhoan = new JTextField();
    private final JPasswordField matKhau = new JPasswordField();

    public GD_DangNhap(DangNhapBUS dangNhapBUS, BoXuLyTacVuGiaoDien boXuLy,
            Runnable khiDangNhapThanhCong) {
        super("", "", boXuLy);
        removeAll();
        setBorder(null);
        setLayout(new GridLayout(1, 2));
        add(new KhungAnhDangNhap());
        JPanel dangNhap = new JPanel(new GridBagLayout());
        dangNhap.setBackground(Color.WHITE);
        dangNhap.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));
        JPanel khungBieuMau = new JPanel(new GridBagLayout());
        khungBieuMau.setOpaque(false);
        GridBagConstraints dong = new GridBagConstraints();
        dong.gridx = 0;
        dong.weightx = 1;
        dong.fill = GridBagConstraints.HORIZONTAL;
        dong.insets = new Insets(0, 0, 16, 0);
        JLabel tieuDe = new JLabel("Đăng nhập");
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD, 30f));
        khungBieuMau.add(tieuDe, dong);
        JLabel nhanTaiKhoan = new JLabel("Mã tài khoản");
        nhanTaiKhoan.setLabelFor(maTaiKhoan);
        dong.gridy = 1;
        khungBieuMau.add(nhanTaiKhoan, dong);
        maTaiKhoan.setPreferredSize(new Dimension(360, 44));
        dong.gridy++;
        khungBieuMau.add(maTaiKhoan, dong);
        JLabel nhanMatKhau = new JLabel("Mật khẩu");
        nhanMatKhau.setLabelFor(matKhau);
        dong.gridy++;
        khungBieuMau.add(nhanMatKhau, dong);
        matKhau.setPreferredSize(new Dimension(360, 44));
        dong.gridy++;
        khungBieuMau.add(matKhau, dong);
        JButton nutDangNhap = taoNut("Đăng nhập", () -> {
            String ten = maTaiKhoan.getText();
            char[] matKhauNhap = matKhau.getPassword();
            matKhau.setText("");
            boXuLy.thucHien(this, () -> dangNhapBUS.dangNhap(ten, matKhauNhap),
                    phien -> khiDangNhapThanhCong.run());
        });
        nutDangNhap.setPreferredSize(new Dimension(360, 44));
        matKhau.addActionListener(suKien -> nutDangNhap.doClick());
        maTaiKhoan.addActionListener(suKien -> matKhau.requestFocusInWindow());
        dong.gridy++;
        khungBieuMau.add(nutDangNhap, dong);
        JButton nutQuenMatKhau = taoNut("Quên mật khẩu?", () -> JOptionPane.showMessageDialog(this,
                "Liên hệ quản trị viên để được hỗ trợ đặt lại mật khẩu."));
        nutQuenMatKhau.setBorderPainted(false);
        nutQuenMatKhau.setContentAreaFilled(false);
        dong.gridy++;
        khungBieuMau.add(nutQuenMatKhau, dong);
        GridBagConstraints viTri = new GridBagConstraints();
        viTri.weightx = 1;
        viTri.fill = GridBagConstraints.HORIZONTAL;
        dangNhap.add(khungBieuMau, viTri);
        add(dangNhap);
    }

    @Override
    public void lamMoi() {
        maTaiKhoan.requestFocusInWindow();
    }
}
