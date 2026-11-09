package gui;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import dao.NgoaiLeTruyCapDuLieu;

/** Chạy IO ở worker; đọc/ghi component chỉ trên EDT. */
public final class BoXuLyTacVuGiaoDien {
    private boolean dangThucHien;

    public boolean dangXuLy() {
        return dangThucHien;
    }

    public <T> void thucHien(Component nguon, Callable<T> tacVu, Consumer<T> khiThanhCong) {
        if (dangThucHien)
            return;
        dangThucHien = true;
        if (nguon instanceof ManHinhCoSo khung)
            khung.trangThai.setText("Đang xử lý...");
        Window cuaSo = SwingUtilities.getWindowAncestor(nguon);
        Component goc = cuaSo instanceof JFrame khungCuaSo ? khungCuaSo.getContentPane() : nguon;
        Map<Component, Boolean> cacTrangThai = new IdentityHashMap<>();
        voHieuHoa(goc, cacTrangThai);
        goc.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tacVu.call();
            }

            @Override
            protected void done() {
                cacTrangThai.forEach(Component::setEnabled);
                goc.setCursor(Cursor.getDefaultCursor());
                dangThucHien = false;
                try {
                    khiThanhCong.accept(get());
                } catch (InterruptedException loi) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException loi) {
                    hienThiLoi(nguon, loi.getCause());
                } catch (RuntimeException loi) {
                    hienThiLoi(nguon, loi);
                }
            }
        }.execute();
    }

    private void voHieuHoa(Component thanhPhan, Map<Component, Boolean> cacTrangThai) {
        cacTrangThai.put(thanhPhan, thanhPhan.isEnabled());
        thanhPhan.setEnabled(false);
        if (thanhPhan instanceof Container vungChua)
            for (Component thanhPhanCon : vungChua.getComponents())
                voHieuHoa(thanhPhanCon, cacTrangThai);
    }

    public void hienThiLoi(Component chuSoHuu, Throwable hienThiLoi) {
        if (chuSoHuu instanceof ManHinhCoSo khung)
            khung.trangThai.setText("Thao tác chưa hoàn tất. Kiểm tra thông báo lỗi và thử lại.");
        String thongBao = hienThiLoi instanceof IllegalArgumentException
                || hienThiLoi instanceof SecurityException || hienThiLoi instanceof NgoaiLeTruyCapDuLieu
                        ? hienThiLoi.getMessage()
                        : "Không thực hiện được thao tác. Kiểm tra cấu hình hoặc quyền ghi file.";
        JOptionPane.showMessageDialog(chuSoHuu, thongBao, "Không thể thực hiện", JOptionPane.ERROR_MESSAGE);
    }
}
