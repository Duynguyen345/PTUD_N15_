package vn.nhom15.nhakhoa.gui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import vn.nhom15.nhakhoa.App;
import vn.nhom15.nhakhoa.bus.BenhNhan_BUS;
import vn.nhom15.nhakhoa.bus.DichVu_BUS;
import vn.nhom15.nhakhoa.bus.HoaDon_BUS;
import vn.nhom15.nhakhoa.model.BenhNhan;
import vn.nhom15.nhakhoa.model.DichVu;
import vn.nhom15.nhakhoa.model.HoaDon;
import vn.nhom15.nhakhoa.util.AlertUtil;
import vn.nhom15.nhakhoa.util.FormatUtil;
import vn.nhom15.nhakhoa.util.MaThongBao;

/** UC005 - Thu tien phi kham (chu file: Huy). */
public class GD_ThuPhi_Controller {

    @FXML private TextField txt_timBN;
    @FXML private Label lb_thongTinBN;
    @FXML private Label lb_mucPhi;
    @FXML private ComboBox<String> cbb_hinhThuc;
    @FXML private TextField txt_tienKhachDua;
    @FXML private Label lb_tienThua;
    @FXML private Label lb_hoaDon;

    private BenhNhan benhNhan;
    private DichVu phiKham;
    private HoaDon hoaDonDaThu;

    @FXML
    private void initialize() {
        cbb_hinhThuc.getItems().addAll("TIEN_MAT", "CHUYEN_KHOAN");
        cbb_hinhThuc.setValue("TIEN_MAT");
        phiKham = DichVu_BUS.layDichVuTheoMa(HoaDon_BUS.MA_DICH_VU_PHI_KHAM);
        lb_mucPhi.setText(phiKham == null ? "Thiếu DV0001 - chạy 003_seed_dichvu.sql"
                : FormatUtil.tien(phiKham.getDonGiaNiemYet()));
        txt_tienKhachDua.textProperty().addListener((o, cu, moi) -> capNhatTienThua());
    }

    @FXML
    private void tim() {
        try {
            String tu = txt_timBN.getText().trim();
            benhNhan = null;
            if (tu.matches("BN\\d+")) {
                benhNhan = BenhNhan_BUS.layBenhNhanTheoMa(tu);
            } else {
                var ds = BenhNhan_BUS.layBenhNhanTheoSDT(tu);
                if (ds.size() == 1) {
                    benhNhan = ds.get(0);
                }
            }
            if (benhNhan == null) {
                lb_thongTinBN.setText("(không tìm thấy)");
                AlertUtil.baoLoi(MaThongBao.TB05);
                return;
            }
            lb_thongTinBN.setText(benhNhan.getMaBenhNhan() + " - " + benhNhan.getHoTen());
            hoaDonDaThu = null;
            lb_hoaDon.setText("");
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    private void capNhatTienThua() {
        if (phiKham == null) {
            return;
        }
        try {
            var dua = new java.math.BigDecimal(txt_tienKhachDua.getText().trim());
            lb_tienThua.setText(FormatUtil.tien(dua.subtract(phiKham.getDonGiaNiemYet())));
        } catch (Exception ex) {
            lb_tienThua.setText("");
        }
    }

    @FXML
    private void thuPhi() {
        try {
            if (benhNhan == null) {
                AlertUtil.baoLoi(MaThongBao.VL05);
                return;
            }
            HoaDon hd = HoaDon_BUS.thuPhiKham(App.session, benhNhan.getMaBenhNhan(),
                    cbb_hinhThuc.getValue(), txt_tienKhachDua.getText());
            hoaDonDaThu = hd;
            lb_hoaDon.setText("Đã thu: " + hd.getMaHoaDon() + " - " + FormatUtil.tien(hd.getTongTien()));
            AlertUtil.baoThongTin("Thu phí thành công. Hóa đơn " + hd.getMaHoaDon()
                    + " sẽ được gắn khi lập phiếu khám.");
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    @FXML
    private void dungHoaDonDaThu() {
        try {
            if (benhNhan == null) {
                AlertUtil.baoLoi(MaThongBao.VL05);
                return;
            }
            HoaDon hd = HoaDon_BUS.layHoaDonPhiChuaDung(App.session, benhNhan.getMaBenhNhan());
            if (hd == null) {
                AlertUtil.baoLoi(MaThongBao.TB05);
                return;
            }
            hoaDonDaThu = hd;
            lb_hoaDon.setText("Dùng hóa đơn đã thu: " + hd.getMaHoaDon() + " - " + FormatUtil.tien(hd.getTongTien()));
        } catch (RuntimeException ex) {
            AlertUtil.baoLoi(ex.getMessage());
        }
    }

    public HoaDon getHoaDonDaThu() {
        return hoaDonDaThu;
    }

    public BenhNhan getBenhNhan() {
        return benhNhan;
    }
}
