package vn.nhom15.nhakhoa.gui;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;

/** Xem trước và in phiếu/hóa đơn bằng dịch vụ in sẵn có của Java. */
final class InChungTuLeTan {
    private InChungTuLeTan() { }

    static void xemVaIn(Component cha, String tieuDe, String noiDung) {
        JTextArea vanBan = new JTextArea(noiDung, 18, 42);
        vanBan.setEditable(false);
        vanBan.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int chon = JOptionPane.showConfirmDialog(cha, new JScrollPane(vanBan), tieuDe,
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) return;
        PrinterJob congViec = PrinterJob.getPrinterJob();
        congViec.setJobName(tieuDe);
        String[] dong = noiDung.split("\\R", -1);
        congViec.setPrintable((g, trang, soTrang) -> {
            int moiTrang = Math.max(1, (int) (trang.getImageableHeight() / 17));
            if (soTrang * moiTrang >= dong.length) return Printable.NO_SUCH_PAGE;
            Graphics2D ve = (Graphics2D) g;
            ve.translate(trang.getImageableX(), trang.getImageableY());
            ve.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            for (int i = 0; i < moiTrang && soTrang * moiTrang + i < dong.length; i++)
                ve.drawString(dong[soTrang * moiTrang + i], 0, (i + 1) * 17);
            return Printable.PAGE_EXISTS;
        });
        if (!congViec.printDialog()) return;
        try { congViec.print(); }
        catch (PrinterException loi) {
            JOptionPane.showMessageDialog(cha, "Không in được: " + loi.getMessage(),
                    "Lỗi in", JOptionPane.ERROR_MESSAGE);
        }
    }
}
