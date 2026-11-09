package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.*;
import javax.swing.*;

public abstract class ManHinhCoSo extends JPanel {
    protected final BoXuLyTacVuGiaoDien boXuLy;
    protected final JPanel noiDung = new JPanel(new BorderLayout(12, 12));
    protected final JLabel trangThai = new JLabel(" ");

    protected ManHinhCoSo(String tieuDe, String moTa, BoXuLyTacVuGiaoDien boXuLy) {
        super(new BorderLayout(14, 18));
        this.boXuLy = boXuLy;
        setBorder(BorderFactory.createEmptyBorder(24, 28, 18, 28));
        JPanel phanTieuDe = new JPanel(new GridLayout(0, 1, 0, 6));
        JLabel nhan = new JLabel(tieuDe);
        nhan.setFont(nhan.getFont().deriveFont(Font.BOLD, 26f));
        phanTieuDe.add(nhan);
        if (moTa != null && !moTa.isBlank())
            phanTieuDe.add(new JLabel(moTa));
        add(phanTieuDe, BorderLayout.NORTH);
        add(noiDung, BorderLayout.CENTER);
        add(trangThai, BorderLayout.SOUTH);
    }

    protected JButton taoNut(String nhan, Runnable thaoTac) {
        JButton taoNut = new JButton(nhan);
        taoNut.addActionListener(suKien -> {
            try {
                thaoTac.run();
            } catch (RuntimeException loi) {
                boXuLy.hienThiLoi(this, loi);
            }
        });
        return taoNut;
    }

    protected JPanel taoThanhThaoTac(JButton... cacNut) {
        JPanel khung = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        for (JButton taoNut : cacNut)
            khung.add(taoNut);
        return khung;
    }

    protected JPanel taoBieuMau(String[] cacNhan, JComponent... cacTruong) {
        JPanel khung = new JPanel(new GridBagLayout());
        khung.setBorder(BorderFactory.createTitledBorder("Thông tin"));
        for (int i = 0; i < cacNhan.length; i++) {
            GridBagConstraints nhan = new GridBagConstraints();
            nhan.gridx = 0;
            nhan.gridy = i;
            nhan.anchor = GridBagConstraints.WEST;
            nhan.insets = new Insets(5, 8, 5, 10);
            khung.add(new JLabel(cacNhan[i]), nhan);
            GridBagConstraints field = new GridBagConstraints();
            field.gridx = 1;
            field.gridy = i;
            field.weightx = 1;
            field.fill = GridBagConstraints.HORIZONTAL;
            field.insets = new Insets(5, 0, 5, 8);
            cacTruong[i].setPreferredSize(new Dimension(220, 30));
            khung.add(cacTruong[i], field);
        }
        return khung;
    }

    protected LocalDate ngay(String vanBan) {
        try {
            return LocalDate.parse(vanBan.trim(),
                    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException loi) {
            throw new IllegalArgumentException("Ngày cần đúng định dạng dd/MM/yyyy và là ngày có thật.");
        }
    }

    protected String ngay(LocalDate giaTri) {
        return giaTri == null ? "" : giaTri.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    protected boolean xacNhan(String thongBao) {
        return JOptionPane.showConfirmDialog(this, thongBao, "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public abstract void lamMoi();
}
