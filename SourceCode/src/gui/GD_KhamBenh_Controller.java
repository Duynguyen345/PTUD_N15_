package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.geometry.Orientation;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;
import bus.HoSoBenhAn_BUS;
import bus.KetQuaKham_BUS;
import bus.KhamBenh_BUS;
import bus.PhieuKham_BUS;
import entity.BenhNhan;
import entity.ChiDinhDichVu;
import entity.ChiTietCapThuoc;
import entity.GhiNhanKham;
import entity.HoSoBenhAn;
import entity.KetQuaKham;
import entity.PhieuKham;
import entity.TrangThaiPhieuKham;
import main.App;
import util.AlertUtil;
import util.FormatUtil;
import util.MaThongBao;

public class GD_KhamBenh_Controller {
    @FXML
    private TableView<PhieuKham> table_danhSach, table_lichSu;
    @FXML
    private TableView<DongKham> table_ghiNhan;
    private final List<GhiNhanKham> dsGhiNhan = new ArrayList<>();
    private final javafx.collections.ObservableList<ChiDinhDichVu> dsChiDinh = FXCollections.observableArrayList();
    @FXML
    private TableView<ChiTietCapThuoc> table_thuoc;
    @FXML
    private Label lb_bn, lb_trangThai, lb_daLuu, lb_diUng, lb_tongDichVu;
    @FXML
    private TextArea ta_tienSu, ta_diUng, ta_thuocDangDung, ta_trieuChung, ta_chanDoan, ta_danDo,
            ta_thuocTuMua, ta_lichSu;
    @FXML
    private DatePicker dp_ngay, dp_hen;
    @FXML
    private Button btn_luu, btn_hoanTat;
    @FXML
    private javafx.scene.layout.VBox pn_keThuoc;
    @FXML
    private SplitPane split_donThuoc;
    private boolean thuocDangNhap;
    @FXML
    private BorderPane pn_chiTiet;
    @FXML
    private GD_KeHoachDieuTri_Controller keHoachController;
    private PhieuKham phieu;
    private boolean dangNap, daSua;
    private int trangLichSu;

    private Window owner() {
        return pn_chiTiet.getScene().getWindow();
    }

    private boolean duocSua() {
        return phieu != null && phieu.getTrangThai() == TrangThaiPhieuKham.DANG_KHAM;
    }

    @FXML
    private void initialize() {
        BacSiDialog.cot(table_danhSach, "STT", p -> String.valueOf(p.getSoThuTu()), 48);
        table_danhSach.getColumns().getFirst().setMinWidth(40);
        table_danhSach.getColumns().getFirst().setMaxWidth(40);
        TableColumn<PhieuKham, String> cotBenhNhan = new TableColumn<>("Bệnh nhân");
        cotBenhNhan.setPrefWidth(150);
        cotBenhNhan.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getTenBenhNhan()));
        cotBenhNhan.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String ten, boolean trong) {
                super.updateItem(ten, trong);
                setText(trong ? null : ten);
                setTooltip(trong || ten == null ? null : new Tooltip(ten));
            }
        });
        table_danhSach.getColumns().add(cotBenhNhan);
        BacSiDialog.cotTrangThaiPhieu(table_danhSach);
        TableColumn<PhieuKham, ?> cotTrangThai = table_danhSach.getColumns().getLast();
        cotTrangThai.setMinWidth(110);
        cotTrangThai.setMaxWidth(110);
        table_danhSach.setFixedCellSize(48);
        table_danhSach.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table_danhSach.setPlaceholder(new Label("Chưa có phiếu được phân công trong ngày"));
        table_danhSach.getSelectionModel().selectedItemProperty().addListener((o, cu, moi) -> {
            if (dangNap || moi == null)
                return;
            if (!choPhepRoi()) {
                dangNap = true;
                table_danhSach.getSelectionModel().select(cu);
                dangNap = false;
                return;
            }
            thucHien(() -> napPhieu(moi.getMaPhieuKham()));
        });
        BacSiDialog.cot(table_ghiNhan, "Răng / vùng",
                d -> d.ghiNhan == null ? Objects.toString(d.dichVu.getFirst().getSoRang(), "")
                        : d.ghiNhan.getViTriString(),
                105);
        BacSiDialog.cot(table_ghiNhan, "Chẩn đoán", d -> d.ghiNhan == null ? "" : d.ghiNhan.getChanDoan(), 155);
        BacSiDialog.cot(table_ghiNhan, "Điều trị / thực hiện",
                d -> d.ghiNhan == null ? "" : d.ghiNhan.getNoiDung(), 190);
        cotDichVu("Dịch vụ", ChiDinhDichVu::getTenDichVu, 230);
        cotDichVu("SL", d -> d.getSoLuong() + " " + Objects.toString(d.getDonViTinh(), ""), 85);
        cotDichVu("Đơn giá", d -> FormatUtil.tien(d.getDonGia()), 115);
        cotDichVu("Thành tiền", d -> FormatUtil.tien(d.getThanhTien()), 130);
        // Giữ cột đủ đọc; cửa sổ hẹp cuộn ngang thay vì ép số tiền và đơn vị.
        table_ghiNhan.getColumns().forEach(c -> c.setMinWidth(c.getPrefWidth()));
        table_ghiNhan.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table_ghiNhan.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                suaGhiNhan();
        });
        dsChiDinh.addListener((javafx.collections.ListChangeListener<ChiDinhDichVu>) c -> hienTongDichVu());
        table_thuoc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        BacSiDialog.cot(table_thuoc, "Thuốc", ChiTietCapThuoc::getTenThuoc, 145);
        BacSiDialog.cot(table_thuoc, "Số lượng", t -> t.getSoLuong() + " " + t.getDonViTinh(), 100);
        BacSiDialog.cot(table_thuoc, "Cách dùng", ChiTietCapThuoc::getHuongDanString, 270);
        BacSiDialog.cot(table_lichSu, "Ngày", p -> FormatUtil.ngay(p.getNgayKham()), 100);
        BacSiDialog.cot(table_lichSu, "Bác sĩ", PhieuKham::getTenBacSi, 160);
        BacSiDialog.cot(table_lichSu, "Nhận định", PhieuKham::getChanDoan, 260);
        table_lichSu.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null)
                thucHien(() -> ta_lichSu.setText(chiTiet(b)));
        });
        for (TextArea a : List.of(ta_tienSu, ta_diUng, ta_thuocDangDung, ta_trieuChung, ta_chanDoan, ta_danDo,
                ta_thuocTuMua))
            a.textProperty().addListener((o, x, y) -> danhDauSua());
        ta_diUng.textProperty().addListener((o, a, b) -> {
            if (!dangNap)
                lb_diUng.setText("Dị ứng: " + (b == null || b.isBlank() ? "Chưa khai thác" : b));
        });
        dp_hen.valueProperty().addListener((o, a, b) -> danhDauSua());
        BacSiDialog.dinhDangNgay(dp_ngay);
        BacSiDialog.dinhDangNgay(dp_hen);
        dp_ngay.setValue(LocalDate.now());
        pn_chiTiet.setDisable(true);
        pn_chiTiet.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.S) {
                luu();
                e.consume();
            }
        });
        split_donThuoc.widthProperty().addListener((o, cu, moi) -> {
            Orientation huong = moi.doubleValue() < 760 ? Orientation.VERTICAL : Orientation.HORIZONTAL;
            if (split_donThuoc.getOrientation() != huong) {
                split_donThuoc.setOrientation(huong);
                // Khi xếp dọc, cuộn cả trang để form không bị cắt thành nửa khung.
                split_donThuoc.setMinHeight(huong == Orientation.VERTICAL ? 940 : 0);
                split_donThuoc.setDividerPositions(huong == Orientation.VERTICAL ? 0.60 : 0.52);
            }
        });
        taiLai();
    }

    private void danhDauSua() {
        if (!dangNap && duocSua()) {
            daSua = true;
            lb_daLuu.setText("");
        }
    }

    public boolean choPhepRoi() {
        return !(daSua || thuocDangNhap) || AlertUtil.xacNhan(MaThongBao.BS09);
    }

    @FXML
    private void taiLai() {
        if (!choPhepRoi())
            return;
        thucHien(() -> {
            dangNap = true;
            table_danhSach.setItems(FXCollections.observableArrayList(KhamBenh_BUS.danhSachKhamCuaToi(App.session,
                    dp_ngay.getValue() == null ? LocalDate.now() : dp_ngay.getValue())));
            phieu = null;
            thuocDangNhap = false;
            pn_keThuoc.getChildren().clear();
            daSua = false;
            pn_chiTiet.setDisable(true);
            lb_bn.setText("Chọn phiếu được phân công");
            lb_trangThai.setText("");
            lb_daLuu.setText("");
            dangNap = false;
        });
    }

    /** Chỉ là hàng hiển thị, không lưu xuống DB và không tạo chẩn đoán cho dữ liệu cũ. */
    private static final class DongKham {
        private final GhiNhanKham ghiNhan;
        private final List<ChiDinhDichVu> dichVu;

        private DongKham(GhiNhanKham ghiNhan, List<ChiDinhDichVu> dichVu) {
            this.ghiNhan = ghiNhan;
            this.dichVu = dichVu;
        }
    }

    private void hienBangKham() {
        List<DongKham> cacDong = new ArrayList<>();
        for (GhiNhanKham g : dsGhiNhan)
            cacDong.add(new DongKham(g, dsChiDinh.stream().filter(d -> d.getDsGhiNhan().contains(g)).toList()));
        for (ChiDinhDichVu d : dsChiDinh)
            if (d.getDsGhiNhan().isEmpty())
                cacDong.add(new DongKham(null, List.of(d)));
        table_ghiNhan.setItems(FXCollections.observableArrayList(cacDong));
        hienTongDichVu();
    }

    // Các cột dịch vụ cùng hàng khám; model nghiệp vụ vẫn dùng ChiDinhDichVu của nhóm.
    private void cotDichVu(String ten, java.util.function.Function<ChiDinhDichVu, String> hienThi, double rong) {
        TableColumn<DongKham, DongKham> cot = new TableColumn<>(ten);
        cot.setPrefWidth(rong);
        cot.setCellValueFactory(d -> new javafx.beans.property.ReadOnlyObjectWrapper<>(d.getValue()));
        cot.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(DongKham g, boolean trong) {
                super.updateItem(g, trong);
                setText(null);
                setGraphic(null);
                if (trong || g == null)
                    return;
                javafx.scene.layout.VBox noiDung = new javafx.scene.layout.VBox(2);
                for (ChiDinhDichVu d : g.dichVu) {
                    Label muc = new Label(hienThi.apply(d));
                    muc.setWrapText(true);
                    muc.setMinHeight(64);
                    muc.setPrefHeight(64);
                    muc.maxWidthProperty().bind(widthProperty().subtract(16));
                    muc.setTooltip(new Tooltip(d.getTenDichVu() + " · " + d.getSoLuong() + " "
                            + Objects.toString(d.getDonViTinh(), "") + " × " + FormatUtil.tien(d.getDonGia())));
                    noiDung.getChildren().add(muc);
                }
                setGraphic(noiDung);
            }
        });
        table_ghiNhan.getColumns().add(cot);
    }

    private void hienTongDichVu() {
        java.math.BigDecimal tong = dsChiDinh.stream().map(ChiDinhDichVu::getThanhTien)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        lb_tongDichVu.setText("Tổng dịch vụ: " + FormatUtil.tien(tong));
        if (dsChiDinh.stream().anyMatch(d -> d.getDsGhiNhan().size() > 1))
            lb_tongDichVu.setText(lb_tongDichVu.getText() + " · Dịch vụ dùng chung giữa các dòng được tính một lần.");
    }

    private void napPhieu(String ma) {
        dangNap = true;
        try {
            phieu = KhamBenh_BUS.layPhieuCuaToi(App.session, ma);
            pn_chiTiet.setDisable(false);
            BenhNhan bn = KhamBenh_BUS.layBenhNhan(App.session, phieu.getMaBenhNhan());
            HoSoBenhAn hs = HoSoBenhAn_BUS.layTheoBenhNhan(App.session, phieu.getMaBenhNhan());
            lb_bn.setText(phieu.getMaBenhNhan() + " · " + phieu.getTenBenhNhan() + " · " + phieu.getMaPhieuKham()
                    + " · " + phieu.getTenPhong() + "\nBác sĩ: " + phieu.getTenBacSi() + " · Lý do: "
                    + phieu.getLyDoKham());
            lb_trangThai.setText(phieu.getTrangThai().toString());
            lb_trangThai.getStyleClass().removeAll("badge-cho", "badge-dang", "badge-hoan");
            lb_trangThai.getStyleClass().add(phieu.getTrangThai() == TrangThaiPhieuKham.HOAN_TAT ? "badge-hoan"
                    : duocSua() ? "badge-dang" : "badge-cho");
            lb_diUng.setText("Dị ứng: "
                    + Objects.toString(phieu.getDiUngTaiLanKham(),
                            Objects.toString(bn.getTienSuDiUng(), "Chưa khai thác"))
                    + " · Thuốc đang dùng: " + Objects.toString(phieu.getThuocDangDung(), "Chưa khai thác"));
            ta_tienSu.setText(phieu.getTienSuTaiLanKham() != null ? phieu.getTienSuTaiLanKham()
                    : hs == null ? null : hs.getTienSuBenhLy());
            ta_diUng.setText(phieu.getDiUngTaiLanKham() != null ? phieu.getDiUngTaiLanKham() : bn.getTienSuDiUng());
            ta_thuocDangDung.setText(phieu.getThuocDangDung());
            ta_trieuChung.setText(phieu.getTrieuChung());
            ta_chanDoan.setText(phieu.getChanDoan());
            ta_danDo.setText(phieu.getDanDo());
            ta_thuocTuMua.setText(phieu.getGhiChuThuocTuMua());
            dp_hen.setValue(phieu.getNgayHenTiep());
            dsGhiNhan.clear();
            dsGhiNhan.addAll(KhamBenh_BUS.layGhiNhan(App.session, ma));
            dsChiDinh.setAll(KhamBenh_BUS.layChiDinhCuaToi(App.session, ma));
            table_thuoc.setItems(FXCollections.observableArrayList(KhamBenh_BUS.layThuocCapCuaToi(App.session, ma)));
            for (ChiDinhDichVu cd : dsChiDinh)
                cd.setDsGhiNhan(cd.getDsGhiNhan().stream().map(ref -> dsGhiNhan.stream()
                        .filter(g -> Objects.equals(g.getMaGhiNhan(), ref.getMaGhiNhan())).findFirst().orElse(ref))
                        .toList());
            for (TextArea a : List.of(ta_tienSu, ta_diUng, ta_thuocDangDung, ta_trieuChung, ta_chanDoan,
                    ta_danDo, ta_thuocTuMua))
                a.setEditable(duocSua());
            dp_hen.setDisable(!duocSua());
            btn_luu.setDisable(!duocSua());
            btn_hoanTat.setDisable(!duocSua());
            keHoachController.hienThi(phieu);
            trangLichSu = 0;
            taiLichSu();
            moFormThuoc(null);
            hienBangKham();
            daSua = false;
            lb_daLuu.setText("");
        } finally {
            dangNap = false;
        }
    }

    @FXML
    private void themGhiNhan() {
        suaDongKham(null);
    }

    @FXML
    private void suaGhiNhan() {
        DongKham dong = table_ghiNhan.getSelectionModel().getSelectedItem();
        if (dong == null || !duocSua())
            return;
        if (dong.ghiNhan != null) {
            suaDongKham(dong.ghiNhan);
        } else {
            ChiDinhDichVu d = dong.dichVu.getFirst();
            Spinner<Integer> sl = new Spinner<>(1, 9999, d.getSoLuong());
            Dialog<Integer> hopThoai = BacSiDialog.tao(owner(), "Sửa dịch vụ",
                    BacSiDialog.form(new Label(d.getTenDichVu()), new Label("Số lượng"), sl));
            hopThoai.setResultConverter(b -> b == ButtonType.OK ? sl.getValue() : null);
            hopThoai.showAndWait().ifPresent(n -> {
                d.setSoLuong(n);
                hienBangKham();
                danhDauSua();
            });
        }
    }

    private void suaDongKham(GhiNhanKham cu) {
        if (!duocSua())
            return;
        thucHien(() -> BacSiDialog.ghiNhan(owner(), cu, new ArrayList<>(dsGhiNhan), phieu.getMaPhieuKham(),
                new ArrayList<>(dsChiDinh), dsChiDinh::setAll).ifPresent(g -> {
                    if (cu == null)
                        dsGhiNhan.add(g);
                    else
                        dsGhiNhan.set(dsGhiNhan.indexOf(cu), g);
                    hienBangKham();
                    table_ghiNhan.getSelectionModel().select(
                            table_ghiNhan.getItems().stream().filter(d -> d.ghiNhan == g).findFirst().orElse(null));
                    danhDauSua();
                }));
    }

    @FXML
    private void xoaGhiNhan() {
        DongKham dong = table_ghiNhan.getSelectionModel().getSelectedItem();
        if (!duocSua() || dong == null || !AlertUtil.xacNhan(MaThongBao.BS28))
            return;
        GhiNhanKham g = dong.ghiNhan;
        if (g == null) {
            dsChiDinh.removeAll(dong.dichVu);
            hienBangKham();
            danhDauSua();
            return;
        }
        for (ChiDinhDichVu d : new ArrayList<>(dsChiDinh)) {
            if (!d.getDsGhiNhan().contains(g))
                continue;
            d.setDsGhiNhan(d.getDsGhiNhan().stream().filter(x -> x != g).toList());
            if (d.getDsGhiNhan().isEmpty())
                dsChiDinh.remove(d);
            else
                d.setSoRang(String.join(",",
                        d.getDsGhiNhan().stream().flatMap(x -> x.getDsRang().stream()).distinct().sorted().toList()));
        }
        dsGhiNhan.remove(g);
        hienBangKham();
        danhDauSua();
    }

    @FXML
    private void themThuoc() {
        if (!duocSua() || thuocDangNhap && !AlertUtil.xacNhan(MaThongBao.BS09))
            return;
        moFormThuoc(null);
    }

    @FXML
    private void suaThuoc() {
        ChiTietCapThuoc t = table_thuoc.getSelectionModel().getSelectedItem();
        if (t != null && duocSua() && (!thuocDangNhap || AlertUtil.xacNhan(MaThongBao.BS09)))
            moFormThuoc(t);
    }

    private void moFormThuoc(ChiTietCapThuoc cu) {
        thuocDangNhap = false;
        javafx.scene.layout.VBox box = BacSiDialog.formThuoc(owner(), phieu.getMaPhieuKham(), cu, t -> {
            if (table_thuoc.getItems().stream().anyMatch(x -> x != cu && x.getMaThuoc().equals(t.getMaThuoc()))) {
                AlertUtil.baoLoi(MaThongBao.BS06);
                return;
            }
            if (cu == null)
                table_thuoc.getItems().add(t);
            else
                table_thuoc.getItems().set(table_thuoc.getItems().indexOf(cu), t);
            danhDauSua();
            moFormThuoc(null);
        }, this::themThuoc);
        box.setDisable(!duocSua());
        pn_keThuoc.getChildren().setAll(box);
        theoDoiThuoc(box);
    }

    private void theoDoiThuoc(javafx.scene.Node node) {
        Runnable sua = () -> {
            if (!dangNap && duocSua()) {
                thuocDangNhap = true;
                lb_daLuu.setText("");
            }
        };
        if (node instanceof TextInputControl t)
            t.textProperty().addListener((o, a, b) -> sua.run());
        if (node instanceof ComboBox<?> c) {
            c.valueProperty().addListener((o, a, b) -> sua.run());
            if (c.isEditable())
                c.getEditor().textProperty().addListener((o, a, b) -> sua.run());
        }
        if (node instanceof Spinner<?> s)
            s.valueProperty().addListener((o, a, b) -> sua.run());
        if (node instanceof CheckBox c)
            c.selectedProperty().addListener((o, a, b) -> sua.run());
        if (node instanceof javafx.scene.Parent p)
            for (javafx.scene.Node con : p.getChildrenUnmodifiable())
                theoDoiThuoc(con);
        if (node instanceof TitledPane t && t.getContent() != null)
            theoDoiThuoc(t.getContent());
    }

    @FXML
    private void xoaThuoc() {
        if (duocSua() && table_thuoc.getSelectionModel().getSelectedItem() != null) {
            if (thuocDangNhap && !AlertUtil.xacNhan(MaThongBao.BS09))
                return;
            table_thuoc.getItems().remove(table_thuoc.getSelectionModel().getSelectedItem());
            moFormThuoc(null);
            danhDauSua();
        }
    }

    @FXML
    private void luu() {
        luu(false);
    }

    @FXML
    private void hoanTat() {
        if (duocSua() && AlertUtil.xacNhan(MaThongBao.BS29))
            luu(true);
    }

    private void luu(boolean hoanTat) {
        if (!duocSua())
            return;
        if (thuocDangNhap) {
            AlertUtil.baoLoi(MaThongBao.BS26);
            return;
        }
        thucHien(() -> {
            dp_hen.commitValue();
            phieu.setChanDoan(ta_chanDoan.getText());
            phieu.setTrieuChung(ta_trieuChung.getText());
            phieu.setTienSuTaiLanKham(ta_tienSu.getText());
            phieu.setDiUngTaiLanKham(ta_diUng.getText());
            phieu.setThuocDangDung(ta_thuocDangDung.getText());
            phieu.setDanDo(ta_danDo.getText());
            phieu.setGhiChuThuocTuMua(ta_thuocTuMua.getText());
            phieu.setNgayHenTiep(dp_hen.getValue());
            KhamBenh_BUS.luuKham(App.session, phieu, new ArrayList<>(dsGhiNhan), new ArrayList<>(dsChiDinh),
                    new ArrayList<>(table_thuoc.getItems()), hoanTat);
            daSua = false;
            String ma = phieu.getMaPhieuKham();
            if (hoanTat)
                taiLai();
            else
                napPhieu(ma);
            lb_daLuu.setText(hoanTat ? "Đã hoàn tất lượt khám" : "Đã lưu");
        });
    }

    private void taiLichSu() {
        if (phieu == null)
            return;
        table_lichSu.setItems(FXCollections
                .observableArrayList(KhamBenh_BUS.lichSu(App.session, phieu.getMaBenhNhan(), null, null, trangLichSu)));
        ta_lichSu.clear();
    }

    @FXML
    private void lichSuTruoc() {
        if (trangLichSu > 0) {
            trangLichSu--;
            thucHien(this::taiLichSu);
        }
    }

    @FXML
    private void lichSuSau() {
        if (table_lichSu.getItems().size() == 30) {
            trangLichSu++;
            thucHien(this::taiLichSu);
        }
    }

    static String chiTiet(PhieuKham p) {
        StringBuilder s = new StringBuilder(p.getMaPhieuKham() + " · " + FormatUtil.ngay(p.getNgayKham()) + " · "
                + p.getTenBacSi() + "\n" + p.getTrangThai() + "\n");
        themThongTin(s, "Lý do khám", p.getLyDoKham());
        themThongTin(s, "Tiền sử", p.getTienSuTaiLanKham());
        themThongTin(s, "Dị ứng", p.getDiUngTaiLanKham());
        themThongTin(s, "Thuốc đang dùng", p.getThuocDangDung());
        themThongTin(s, "Triệu chứng", p.getTrieuChung());
        themThongTin(s, "Khám lâm sàng", p.getKhamLamSang());
        themThongTin(s, "Nhận định chung", p.getChanDoan());
        themThongTin(s, "Kết quả", p.getKetQuaDieuTri());
        themThongTin(s, "Phác đồ", p.getPhacDieuTri());
        themThongTin(s, "Thuốc tự mua", p.getGhiChuThuocTuMua());
        for (GhiNhanKham g : KhamBenh_BUS.layGhiNhan(App.session, p.getMaPhieuKham()))
            s.append(g.getViTriString()).append(" | ").append(Objects.toString(g.getChanDoan(), "")).append(" | ")
                    .append(Objects.toString(g.getNoiDung(), "")).append(" | ").append(g.getTrangThai()).append(" | ")
                    .append(Objects.toString(g.getKetQua(), "")).append("\n");
        for (ChiDinhDichVu d : PhieuKham_BUS.layChiDinhTheoPhieu(App.session, p.getMaPhieuKham()))
            s.append("Dịch vụ: ").append(d.getTenDichVu()).append(" × ").append(d.getSoLuong()).append(" · ")
                    .append(FormatUtil.tien(d.getThanhTien()))
                    .append(d.isGiaThamKhao() ? " (giá danh mục hiện tại; dữ liệu cũ chưa lưu đơn giá)" : "")
                    .append("\n");
        for (ChiTietCapThuoc t : KhamBenh_BUS.layThuocLichSu(App.session, p.getMaPhieuKham()))
            s.append("Thuốc: ").append(t.getTenThuoc()).append(" × ").append(t.getSoLuong()).append(" ")
                    .append(t.getDonViTinh()).append(" · ").append(t.getHuongDanString()).append("\n");
        for (KetQuaKham k : KetQuaKham_BUS.danhSach(App.session, p.getMaPhieuKham()))
            s.append("Kết quả khảo sát: ").append(k.getNgayThucHien()).append(" · ").append(k.getNguoiGhi().getHoTen())
                    .append(" · ").append(k.getNhanXet()).append(" · ")
                    .append(Objects.toString(k.getTenTep(), "Không có tệp")).append("\n");
        s.append("Dặn dò: ").append(Objects.toString(p.getDanDo(), "")).append("\nNgày tái khám: ")
                .append(p.getNgayHenTiep() == null ? "Không ghi" : FormatUtil.ngay(p.getNgayHenTiep()));
        return s.toString();
    }

    private static void themThongTin(StringBuilder s, String nhan, String noiDung) {
        if (noiDung != null && !noiDung.isBlank())
            s.append(nhan).append(": ").append(noiDung).append("\n");
    }

    private void thucHien(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }
}
