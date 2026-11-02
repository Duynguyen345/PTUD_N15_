package vn.nhom15.nhakhoa.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.bus.BenhNhan_BUS;
import vn.nhom15.nhakhoa.model.BenhNhan;
import vn.nhom15.nhakhoa.model.PhieuKham;
import vn.nhom15.nhakhoa.util.AlertUtil;
import vn.nhom15.nhakhoa.util.FormatUtil;
import vn.nhom15.nhakhoa.util.MaThongBao;

/** UC006 - Tra cuu ho so benh nhan (chu file: Huy). */
public class GD_TraCuuBenhNhan_Controller {

    @FXML private ComboBox<String> cbb_tieuChi;
    @FXML private TextField txt_tuKhoa;
    @FXML private TableView<BenhNhan> table_ketQua;
    @FXML private TableColumn<BenhNhan, String> cl_stt;
    @FXML private TableColumn<BenhNhan, String> cl_ma;
    @FXML private TableColumn<BenhNhan, String> cl_hoTen;
    @FXML private TableColumn<BenhNhan, String> cl_sdt;
    @FXML private TableColumn<BenhNhan, String> cl_tienSu;

    @FXML
    private void initialize() {
        cbb_tieuChi.getItems().addAll("Mã", "SĐT", "Họ tên");
        cbb_tieuChi.setValue("Mã");
        cl_stt.setCellValueFactory(cd -> new SimpleStringProperty(
                String.valueOf(table_ketQua.getItems().indexOf(cd.getValue()) + 1)));
        cl_ma.setCellValueFactory(new PropertyValueFactory<>("maBenhNhan"));
        cl_hoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        cl_sdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        cl_tienSu.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTienSuDiUng() == null ? "" : cd.getValue().getTienSuDiUng()));
        table_ketQua.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                xemChiTiet();
            }
        });
    }

    @FXML
    private void tim() {
        try {
            var ds = BenhNhan_BUS.traCuu(App.session, cbb_tieuChi.getValue(), txt_tuKhoa.getText());
            table_ketQua.getItems().clear();
            table_ketQua.setItems(FXCollections.observableArrayList(ds));
            if (ds.isEmpty()) {
                AlertUtil.baoLoi(MaThongBao.TB05);
            }
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    private void xemChiTiet() {
        BenhNhan bn = table_ketQua.getSelectionModel().getSelectedItem();
        if (bn == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Họ tên: ").append(bn.getHoTen()).append("\n");
        sb.append("SĐT: ").append(bn.getSoDienThoai()).append("\n");
        sb.append("CCCD: ").append(bn.getCccd() == null ? "(không có)" : bn.getCccd()).append("\n");
        sb.append("Ngày sinh: ").append(FormatUtil.ngay(bn.getNgaySinh())).append("\n");
        sb.append("Địa chỉ: ").append(bn.getDiaChi() == null ? "" : bn.getDiaChi()).append("\n");
        sb.append("Tiền sử dị ứng: ").append(bn.getTienSuDiUng() == null ? "" : bn.getTienSuDiUng()).append("\n\n");
        sb.append("LỊCH SỬ PHIẾU KHÁM:\n");
        var dsPhieu = BenhNhan_BUS.layLichSuPhieuKham(App.session, bn.getMaBenhNhan());
        if (dsPhieu.isEmpty()) {
            sb.append("(chưa có phiếu khám nào)");
        } else {
            for (PhieuKham pk : dsPhieu) {
                sb.append(String.format("%s | %s | STT %d | %s | %s%n", pk.getMaPhieuKham(),
                        FormatUtil.ngay(pk.getNgayKham()), pk.getSoThuTu(), pk.getLyDoKham(), pk.getTrangThai()));
            }
        }
        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(560, 420);
        Stage stage = new Stage();
        stage.initOwner(table_ketQua.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Chi tiết hồ sơ " + bn.getMaBenhNhan());
        stage.setScene(new javafx.scene.Scene(ta));
        stage.showAndWait();
    }
}
