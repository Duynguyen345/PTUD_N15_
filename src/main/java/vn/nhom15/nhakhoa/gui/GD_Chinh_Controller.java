package vn.nhom15.nhakhoa.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.model.ChucVu;
import vn.nhom15.nhakhoa.model.SessionContext;

/** Khung man hinh chinh + menu theo vai tro (chu file: Duy). */
public class GD_Chinh_Controller {

    @FXML private Label lb_xinChao;
    @FXML private Label lb_chaoMung;
    @FXML private VBox vb_menu;
    @FXML private Button btn_dangXuat;

    @FXML
    private void initialize() {
        SessionContext s = App.session;
        String ten = s == null ? "" : s.getHoTen();
        String vaiTro = s == null ? "" : s.getChucVu().toString();
        lb_xinChao.setText("Xin chào, " + ten + " [" + vaiTro + "]");
        lb_chaoMung.setText("Hôm nay: " + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (s == null) {
            return;
        }
        them("Quản lý hồ sơ bệnh nhân (UC002)", "GD_QLBenhNhan", ChucVu.LE_TAN);
        them("Tra cứu hồ sơ bệnh nhân (UC006)", "GD_TraCuuBenhNhan", ChucVu.LE_TAN, ChucVu.QUAN_LY);
        them("Thu tiền phí khám (UC005)", "GD_ThuPhi", ChucVu.LE_TAN);
        them("Lập phiếu khám (UC003)", "GD_LapPhieuKham", ChucVu.LE_TAN);
        them("Theo dõi hàng chờ (UC009)", "GD_HangCho", ChucVu.PHU_TA, ChucVu.QUAN_LY);
        them("Khám và chẩn đoán (UC004)", "GD_KhamBenh", ChucVu.BAC_SI);
        them("Quản lý nhân viên (UC010)", "GD_QLNhanVien", ChucVu.QUAN_LY);
        them("Quản lý dịch vụ (UC007)", "GD_QLDichVu", ChucVu.QUAN_LY);
        them("Báo cáo lượt khám (UC008)", "GD_BaoCao", ChucVu.QUAN_LY);
    }

    private void them(String tieuDe, String fxml, ChucVu... choPhep) {
        SessionContext s = App.session;
        for (ChucVu cv : choPhep) {
            if (s != null && cv == s.getChucVu()) {
                Button b = new Button(tieuDe);
                b.setOnAction(e -> App.openModal(fxml, 980, 640));
                vb_menu.getChildren().add(b);
                return;
            }
        }
    }

    @FXML
    private void dangXuat() {
        App.session = null;
        App.setRoot("GD_DangNhap");
    }
}
