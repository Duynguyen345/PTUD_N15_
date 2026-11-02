package gui;

import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.App;
import entity.SessionContext;

/** Tong quan (dashboard) — duoc nap vao khung noi dung cua man hinh chinh. */
public class GD_TongQuan_Controller {

    @FXML private Label lb_homNay;
    @FXML private Label lb_vaiTro;
    @FXML private Label lb_chaoMung;
    @FXML private Label lb_avatar;
    @FXML private Label lb_chipTen;
    @FXML private Label lb_chipVaiTro;

    @FXML
    private void initialize() {
        SessionContext s = App.session;
        String ten = s == null ? "" : s.getHoTen();
        String vaiTro = s == null || s.getChucVu() == null ? "—" : s.getChucVu().toString();
        lb_homNay.setText(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lb_vaiTro.setText(vaiTro);
        lb_chaoMung.setText("Chào mừng " + ten + " đến với hệ thống quản lý phòng khám!");
        lb_avatar.setText(viettelHoa(ten));
        lb_chipTen.setText(ten);
        lb_chipVaiTro.setText(vaiTro);
    }

    private static String viettelHoa(String hoTen) {
        String[] tu = hoTen.trim().split("\\s+");
        if (tu.length == 0 || tu[0].isEmpty()) return "?";
        if (tu.length == 1) return tu[0].substring(0, 1).toUpperCase();
        return (tu[0].substring(0, 1) + tu[tu.length - 1].substring(0, 1)).toUpperCase();
    }
}
