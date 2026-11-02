package vn.nhom15.nhakhoa.gui;

import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.bus.DichVu_BUS;
import vn.nhom15.nhakhoa.bus.HoSoBenhAn_BUS;
import vn.nhom15.nhakhoa.bus.KhamBenh_BUS;
import vn.nhom15.nhakhoa.dao.ChiDinhDichVu_DAO;
import vn.nhom15.nhakhoa.dao.ChiTietCapThuoc_DAO;
import vn.nhom15.nhakhoa.dao.Thuoc_DAO;
import vn.nhom15.nhakhoa.model.ChiDinhDichVu;
import vn.nhom15.nhakhoa.model.ChiTietCapThuoc;
import vn.nhom15.nhakhoa.model.DichVu;
import vn.nhom15.nhakhoa.model.HoSoBenhAn;
import vn.nhom15.nhakhoa.model.PhieuKham;
import vn.nhom15.nhakhoa.model.Thuoc;
import vn.nhom15.nhakhoa.util.AlertUtil;
import vn.nhom15.nhakhoa.util.FormatUtil;
import vn.nhom15.nhakhoa.util.MaThongBao;
import vn.nhom15.nhakhoa.util.ValidateUtil;

/** UC004 - Ghi nhan kham va chan doan (chu file: Thang). */
public class GD_KhamBenh_Controller {

    @FXML private TableView<PhieuKham> table_danhSach;
    @FXML private TableColumn<PhieuKham, String> cl_stt;
    @FXML private TableColumn<PhieuKham, String> cl_ma;
    @FXML private TableColumn<PhieuKham, String> cl_bn;
    @FXML private TableColumn<PhieuKham, String> cl_tt;
    @FXML private Label lb_bn;
    @FXML private Label lb_tienSu;
    @FXML private TextArea ta_chanDoan;
    @FXML private TextField txt_ketQua;
    @FXML private TextField txt_thuocTuMua;
    @FXML private ComboBox<DichVu> cbb_dichVu;
    @FXML private ComboBox<Thuoc> cbb_thuoc;
    @FXML private TextField txt_soLuong;
    @FXML private TextField txt_ghiChuCap;
    @FXML private TableView<ChiDinhDichVu> table_chiDinh;
    @FXML private TableColumn<ChiDinhDichVu, String> cl_dv;
    @FXML private TableColumn<ChiDinhDichVu, String> cl_gia;
    @FXML private TableView<ChiTietCapThuoc> table_thuoc;
    @FXML private TableColumn<ChiTietCapThuoc, String> cl_tenThuoc;
    @FXML private TableColumn<ChiTietCapThuoc, String> cl_soLuongThuoc;
    @FXML private TableColumn<ChiTietCapThuoc, String> cl_ghiChuThuoc;
    @FXML private Button btn_taiLai;
    @FXML private Button btn_luu;
    @FXML private Button btn_hoanTat;

    private ArrayList<ChiDinhDichVu> chiDinhTam = new ArrayList<>();
    private ArrayList<ChiTietCapThuoc> thuocTam = new ArrayList<>();

    @FXML
    private void initialize() {
        cl_stt.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getSoThuTu())));
        cl_ma.setCellValueFactory(new PropertyValueFactory<>("maPhieuKham"));
        cl_bn.setCellValueFactory(new PropertyValueFactory<>("tenBenhNhan"));
        cl_tt.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTrangThai().toString()));
        table_danhSach.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                chonPhieu();
            }
        });
        cl_dv.setCellValueFactory(new PropertyValueFactory<>("tenDichVu"));
        cl_gia.setCellValueFactory(cd -> new SimpleStringProperty(FormatUtil.tien(cd.getValue().getDonGia())));
        cl_tenThuoc.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTenThuoc() == null ? "" : cd.getValue().getTenThuoc()));
        cl_soLuongThuoc.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getSoLuong())));
        cl_ghiChuThuoc.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGhiChuCapThuoc() == null ? "" : cd.getValue().getGhiChuCapThuoc()));
        cbb_dichVu.setItems(FXCollections.observableArrayList(DichVu_BUS.danhSachDichVuDangPhucVu(App.session)));
        cbb_thuoc.setItems(FXCollections.observableArrayList(Thuoc_DAO.getAllThuoc()));
        taiLai();
    }

    @FXML
    private void taiLai() {
        try {
            var ds = KhamBenh_BUS.danhSachKhamCuaToi(App.session);
            table_danhSach.getItems().clear();
            table_danhSach.setItems(FXCollections.observableArrayList(ds));
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    private void chonPhieu() {
        PhieuKham pk = table_danhSach.getSelectionModel().getSelectedItem();
        if (pk == null) {
            return;
        }
        lb_bn.setText(pk.getMaPhieuKham() + " - " + pk.getTenBenhNhan() + " [" + pk.getTrangThai() + "]");
        HoSoBenhAn hs = HoSoBenhAn_BUS.layTheoBenhNhan(App.session, pk.getMaBenhNhan());
        lb_tienSu.setText(hs == null ? "Chưa có hồ sơ bệnh án (sẽ tự tạo khi lưu lần đầu)."
                : "Hồ sơ " + hs.getMaHoSo() + " — tiền sử: "
                + (hs.getTienSuBenhLy() == null ? "" : hs.getTienSuBenhLy()));
        ta_chanDoan.setText(pk.getChanDoan() == null ? "" : pk.getChanDoan());
        txt_ketQua.setText(pk.getKetQuaDieuTri() == null ? "" : pk.getKetQuaDieuTri());
        txt_thuocTuMua.setText(pk.getGhiChuThuocTuMua() == null ? "" : pk.getGhiChuThuocTuMua());
        chiDinhTam = new ArrayList<>(ChiDinhDichVu_DAO.layTheoPhieu(pk.getMaPhieuKham()));
        thuocTam = new ArrayList<>(ChiTietCapThuoc_DAO.layTheoPhieu(pk.getMaPhieuKham()));
        napBangTam();
        boolean choSua = pk.getTrangThai() == vn.nhom15.nhakhoa.model.TrangThaiPhieuKham.DANG_KHAM;
        btn_luu.setDisable(!choSua);
        btn_hoanTat.setDisable(!choSua);
    }

    private void napBangTam() {
        table_chiDinh.getItems().clear();
        table_chiDinh.setItems(FXCollections.observableArrayList(chiDinhTam));
        table_thuoc.getItems().clear();
        table_thuoc.setItems(FXCollections.observableArrayList(thuocTam));
    }

    @FXML
    private void themChiDinh() {
        DichVu dv = cbb_dichVu.getValue();
        PhieuKham pk = table_danhSach.getSelectionModel().getSelectedItem();
        if (pk == null) {
            AlertUtil.baoLoi(MaThongBao.VL05);
            return;
        }
        if (dv == null) {
            AlertUtil.baoLoi(MaThongBao.VL11);
            return;
        }
        boolean trung = chiDinhTam.stream().anyMatch(c -> c.getMaDichVu().equals(dv.getMaDichVu()));
        if (trung) {
            AlertUtil.baoLoi("Dịch vụ đã có trong danh sách chỉ định.");
            return;
        }
        ChiDinhDichVu cd = new ChiDinhDichVu(pk.getMaPhieuKham(), dv.getMaDichVu());
        cd.setTenDichVu(dv.getTenDichVu());
        cd.setDonGia(dv.getDonGiaNiemYet());
        chiDinhTam.add(cd);
        napBangTam();
    }

    @FXML
    private void xoaChiDinh() {
        ChiDinhDichVu cd = table_chiDinh.getSelectionModel().getSelectedItem();
        if (cd != null) {
            chiDinhTam.remove(cd);
            napBangTam();
        }
    }

    @FXML
    private void themThuoc() {
        Thuoc th = cbb_thuoc.getValue();
        PhieuKham pk = table_danhSach.getSelectionModel().getSelectedItem();
        if (pk == null) {
            AlertUtil.baoLoi(MaThongBao.VL05);
            return;
        }
        if (th == null) {
            AlertUtil.baoLoi("Vui lòng chọn thuốc.");
            return;
        }
        int sl = ValidateUtil.soLuong(txt_soLuong.getText());
        ChiTietCapThuoc ct = new ChiTietCapThuoc(pk.getMaPhieuKham(), th.getMaThuoc(), sl,
                txt_ghiChuCap.getText() == null ? null : txt_ghiChuCap.getText().trim());
        ChiTietCapThuoc daCo = thuocTam.stream().filter(t -> t.getMaThuoc().equals(th.getMaThuoc()))
                .findFirst().orElse(null);
        if (daCo != null) {
            daCo.setSoLuong(sl);
            daCo.setGhiChuCapThuoc(ct.getGhiChuCapThuoc());
        } else {
            ct.setTenThuoc(th.getTenThuoc());
            ct.setDonViTinh(th.getDonViTinh());
            thuocTam.add(ct);
        }
        napBangTam();
    }

    @FXML
    private void xoaThuoc() {
        ChiTietCapThuoc ct = table_thuoc.getSelectionModel().getSelectedItem();
        if (ct != null) {
            thuocTam.remove(ct);
            napBangTam();
        }
    }

    @FXML
    private void luu() {
        PhieuKham pk = table_danhSach.getSelectionModel().getSelectedItem();
        if (pk == null) {
            AlertUtil.baoLoi(MaThongBao.VL05);
            return;
        }
        try {
            KhamBenh_BUS.luuKham(App.session, pk.getMaPhieuKham(), ta_chanDoan.getText(),
                    txt_ketQua.getText(), txt_thuocTuMua.getText(), chiDinhTam, thuocTam);
            AlertUtil.baoThongTin("Đã lưu thông tin khám (phiếu vẫn đang khám).");
            taiLai();
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    @FXML
    private void hoanTat() {
        PhieuKham pk = table_danhSach.getSelectionModel().getSelectedItem();
        if (pk == null) {
            AlertUtil.baoLoi(MaThongBao.VL05);
            return;
        }
        try {
            if (!AlertUtil.xacNhan("Hoàn tất lượt khám " + pk.getMaPhieuKham() + "?")) {
                return;
            }
            KhamBenh_BUS.hoanTat(App.session, pk.getMaPhieuKham());
            AlertUtil.baoThongTin("Lượt khám đã HOAN_TAT.");
            taiLai();
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }
}
