package vn.nhom15.nhakhoa.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.bus.TaiKhoan_BUS;
import vn.nhom15.nhakhoa.model.SessionContext;
import vn.nhom15.nhakhoa.util.AlertUtil;

/** UC001 - Dang nhap (chu file: Duy). */
public class GD_DangNhap_Controller {

    @FXML private TextField txt_taiKhoan;
    @FXML private PasswordField txt_matKhau;
    @FXML private Label lb_thongBao;

    @FXML
    private void dangNhap(ActionEvent event) {
        try {
            SessionContext ctx = TaiKhoan_BUS.dangNhap(txt_taiKhoan.getText(), txt_matKhau.getText());
            App.session = ctx;
            App.setRoot("GD_Chinh");
        } catch (RuntimeException ex) {
            lb_thongBao.setText(ex.getMessage());
            AlertUtil.baoLoi(ex.getMessage());
        }
    }
}
