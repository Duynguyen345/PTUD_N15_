package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import main.App;
import bus.TaiKhoan_BUS;
import entity.SessionContext;
import util.AlertUtil;

/** Dang nhap — layout theo thiet ke Figma (panel thuong hieu ben trai). */
public class GD_DangNhap_Controller {

    @FXML private TextField txt_taiKhoan;
    @FXML private PasswordField txt_matKhau;
    @FXML private TextField txt_matKhauHien;
    @FXML private Button btn_eye;
    @FXML private ImageView img_eye;
    @FXML private Label lb_thongBao;
    @FXML private VBox loginLeft;

    private boolean dangHien = false;

    @FXML
    private void initialize() {
        // Nen trai = anh brand nguoi dung cap (set bang code de tranh loi @url cua CSS)
        try {
            Image brand = new Image("/pic/login_brand.png");
            if (!brand.isError()) {
                loginLeft.setBackground(new Background(new BackgroundImage(brand,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                                false, false, true, false)))); // cover
            }
        } catch (Exception ignored) { }
        // dong bo 2 truong mat khau khi bat/tat hien thi
        txt_matKhau.textProperty().addListener((o, cu, moi) -> {
            if (!dangHien) {
                txt_matKhauHien.setText(moi);
            }
        });
        txt_matKhauHien.textProperty().addListener((o, cu, moi) -> {
            if (dangHien) {
                txt_matKhau.setText(moi);
            }
        });
    }

    @FXML
    private void anHienMatKhau(ActionEvent event) {
        dangHien = !dangHien;
        txt_matKhau.setVisible(!dangHien);
        txt_matKhau.setManaged(!dangHien);
        txt_matKhauHien.setVisible(dangHien);
        txt_matKhauHien.setManaged(dangHien);
        String icon = dangHien ? "/pic/icon_eye.png" : "/pic/icon_eye.png";
        img_eye.setImage(new Image(icon));
    }

    @FXML
    private void quenMatKhau() {
        AlertUtil.baoThongTin("Vui lòng liên hệ Quản trị viên để được cấp lại mật khẩu.");
    }

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
