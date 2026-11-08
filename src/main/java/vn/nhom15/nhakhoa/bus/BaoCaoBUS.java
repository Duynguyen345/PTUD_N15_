package vn.nhom15.nhakhoa.bus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import vn.nhom15.nhakhoa.dao.PhongKhamDAO;
import vn.nhom15.nhakhoa.entity.DuLieuBaoCao;

public final class BaoCaoBUS {
    private final PhongKhamDAO phongKhamDAO;
    private final QuanLyPhienDangNhap quanLyPhien;

    public BaoCaoBUS(PhongKhamDAO phongKhamDAO, QuanLyPhienDangNhap quanLyPhien) {
        this.phongKhamDAO = phongKhamDAO;
        this.quanLyPhien = quanLyPhien;
    }

    public DuLieuBaoCao layBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay))
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng đến ngày.");
        return phongKhamDAO.lapBaoCao(tuNgay, denNgay);
    }

    public void xuatCSV(DuLieuBaoCao DuLieuTaiKhoan, Path tep) throws IOException {
        quanLyPhien.kiemTraQuyenQuanLy();
        if (DuLieuTaiKhoan == null)
            throw new IllegalArgumentException("Bấm Xem báo cáo trước khi xuất CSV.");
        Path tepDich = tep.toAbsolutePath();
        Path tepTam = Files.createTempFile(tepDich.getParent(), "baocao-", ".tmp");
        try {
            try (BufferedWriter boGhi = Files.newBufferedWriter(tepTam, StandardCharsets.UTF_8)) {
                boGhi.write('\ufeff');
                boGhi.write("Tu ngay;Den ngay\r\n" + ngay(DuLieuTaiKhoan.tuNgay()) + ";"
                        + ngay(DuLieuTaiKhoan.denNgay()) + "\r\n\r\nNgay;So luot kham\r\n");
                for (var dong : DuLieuTaiKhoan.danhSachLuotKham())
                    boGhi.write(ngay(LocalDate.parse(dong.nhan())) + ";" + dong.soLuong() + "\r\n");
                boGhi.write("\r\nDich vu;So lan chi dinh\r\n");
                for (var dong : DuLieuTaiKhoan.danhSachDichVu())
                    boGhi.write(dinhDangO(dong.nhan()) + ";" + dong.soLuong() + "\r\n");
            }
            try {
                Files.move(tepTam, tepDich, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException loi) {
                Files.move(tepTam, tepDich, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tepTam);
        }
    }

    private String ngay(LocalDate ngay) {
        return ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String dinhDangO(String vanBan) {
        String giaTri = vanBan;
        if (!giaTri.stripLeading().isEmpty() && "=+-@".indexOf(giaTri.stripLeading().charAt(0)) >= 0)
            giaTri = "'" + giaTri;
        return "\"" + giaTri.replace("\"", "\"\"") + "\"";
    }
}
