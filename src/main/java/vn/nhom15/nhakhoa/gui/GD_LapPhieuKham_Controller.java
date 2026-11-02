package vn.nhom15.nhakhoa.gui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.bus.BenhNhan_BUS;
import vn.nhom15.nhakhoa.bus.NhanVien_BUS;
import vn.nhom15.nhakhoa.bus.PhieuKham_BUS;
import vn.nhom15.nhakhoa.bus.PhongKham_BUS;
import vn.nhom15.nhakhoa.model.BenhNhan;
import vn.nhom15.nhakhoa.model.HoaDon;
import vn.nhom15.nhakhoa.model.NhanVien;
import vn.nhom15.nhakhoa.model.PhieuKham;
import vn.nhom15.nhakhoa.model.PhongKham;
import vn.nhom15.nhakhoa.util.AlertUtil;
import vn.nhom15.nhakhoa.util.FormatUtil;
import vn.nhom15.nhakhoa.util.MaThongBao;

/** UC003 - Lap phieu kham + cap STT (chu file: Huy). */
public class GD_LapPhieuKham_Controller {

    @FXML private TextField txt_timBN;
    @FXML private Label lb_bn;
    @FXML private Label lb_hoaDon;
    @FXML private ComboBox<NhanVien> cbb_bacSi;
    @FXML private ComboBox<PhongKham> cbb_phong;
    @FXML private TextField txt_lyDoKham;
    @FXML private Label lb_sttDuKien;

    private BenhNhan benhNhan;
    private HoaDon hoaDonPhi;
    private PhieuKham phieuVuaLap;

    @FXML
    private void initialize() {
        cbb_bacSi.setItems(javafx.collections.FXCollections
                .observableArrayList(NhanVien_BUS.danhSachBacSiDangLamViec(App.session)));
        cbb_bacSi.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(NhanVien nv, boolean empty) {
                super.updateItem(nv, empty);
                setText(empty || nv == null ? null : nv.getMaNhanVien() + " - " + nv.getHoTen());
            }
        });
        cbb_bacSi.setButtonCell(cbb_bacSi.getCellFactory().call(null));
        cbb_phong.setItems(javafx.collections.FXCollections
                .observableArrayList(PhongKham_BUS.danhSachPhongDangNhanBenh(App.session)));
        cbb_phong.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(PhongKham p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getMaPhong() + " - " + p.getTenPhong());
            }
        });
        cbb_phong.setButtonCell(cbb_phong.getCellFactory().call(null));
    }

    @FXML
    private void tim() {
        try {
            String tu = txt_timBN.getText().trim();
            benhNhan = null;
            hoaDonPhi = null;
            if (tu.matches("BN\\d+")) {
                benhNhan = BenhNhan_BUS.layBenhNhanTheoMa(tu);
            } else {
                var ds = BenhNhan_BUS.layBenhNhanTheoSDT(tu);
                if (ds.size() == 1) {
                    benhNhan = ds.get(0);
                }
            }
            if (benhNhan == null) {
                lb_bn.setText("(không tìm thấy)");
                AlertUtil.baoLoi(MaThongBao.TB05);
                return;
            }
            lb_bn.setText(benhNhan.getMaBenhNhan() + " - " + benhNhan.getHoTen());
            hoaDonPhi = vn.nhom15.nhakhoa.bus.HoaDon_BUS.layHoaDonPhiChuaDung(App.session,
                    benhNhan.getMaBenhNhan());
            lb_hoaDon.setText(hoaDonPhi == null ? "(chưa có - hãy dùng màn hình Thu phí)"
                    : hoaDonPhi.getMaHoaDon() + " - " + FormatUtil.tien(hoaDonPhi.getTongTien()));
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    @FXML
    private void lapPhieu() {
        try {
            NhanVien bs = cbb_bacSi.getValue();
            PhongKham pk = cbb_phong.getValue();
            PhieuKham phieu = PhieuKham_BUS.lapPhieuKham(App.session, benhNhan,
                    bs == null ? null : bs.getMaNhanVien(),
                    pk == null ? null : pk.getMaPhong(),
                    txt_lyDoKham.getText(), hoaDonPhi);
            phieuVuaLap = phieu;
            lb_sttDuKien.setText(String.valueOf(phieu.getSoThuTu()));
            AlertUtil.baoThongTin("Đã lập phiếu " + phieu.getMaPhieuKham() + " - STT " + phieu.getSoThuTu()
                    + " - phòng " + phieu.getMaPhong() + " (chờ khám)");
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    @FXML
    private void inPhieu() {
        if (phieuVuaLap == null) {
            AlertUtil.baoLoi("Chưa có phiếu vừa lập để xem.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("====== PHIẾU KHÁM - NHA KHOA SÀI GÒN ======\n\n");
        sb.append("Mã phiếu: ").append(phieuVuaLap.getMaPhieuKham()).append("\n");
        sb.append("Ngày: ").append(FormatUtil.ngay(phieuVuaLap.getNgayKham())).append("\n");
        sb.append("STT: ").append(phieuVuaLap.getSoThuTu()).append(" - Phòng: ")
                .append(phieuVuaLap.getMaPhong()).append("\n");
        sb.append("Bệnh nhân: ").append(phieuVuaLap.getTenBenhNhan()).append("\n");
        sb.append("Bác sĩ: ").append(phieuVuaLap.getTenBacSi()).append("\n");
        sb.append("Lý do khám: ").append(phieuVuaLap.getLyDoKham()).append("\n");
        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(sb.toString());
        ta.setEditable(false);
        ta.setPrefSize(420, 300);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initOwner(txt_timBN.getScene().getWindow());
        stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        stage.setTitle("Xem phiếu khám");
        stage.setScene(new javafx.scene.Scene(ta));
        stage.showAndWait();
    }
}
