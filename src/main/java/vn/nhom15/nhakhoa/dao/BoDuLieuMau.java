package vn.nhom15.nhakhoa.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import vn.nhom15.nhakhoa.entity.*;

/** Nạp cùng dữ liệu nguồn mà DATA/GenerateDemoSeed.java dùng để sinh SQL. */
public final class BoDuLieuMau {
    public record TaiKhoanMau(String maNhanVien, TrangThaiTaiKhoan trangThai) {
    }

    public record LuotKhamMau(String ma, LocalDate ngay, String trangThai, String maNhanVien,
            List<String> danhSachMaDichVu) {
        public LuotKhamMau {
            danhSachMaDichVu = List.copyOf(danhSachMaDichVu);
        }
    }

    private final List<NhanVien> danhSachNhanVien = new ArrayList<>();
    private final List<TaiKhoanMau> danhSachTaiKhoan = new ArrayList<>();
    private final List<DichVu> danhSachDichVu = new ArrayList<>();
    private final List<HangHoa> danhSachHangHoa = new ArrayList<>();
    private final List<LuotKhamMau> danhSachLuotKham = new ArrayList<>();

    public BoDuLieuMau(LocalDate homNay) {
        InputStream luongVao = getClass().getResourceAsStream("/demo/dataset.tsv");
        if (luongVao == null)
            throw new IllegalStateException("Thiếu dữ liệu demo/dataset.tsv.");
        try (BufferedReader boDoc = new BufferedReader(
                new InputStreamReader(luongVao, StandardCharsets.UTF_8))) {
            String dongVanBan;
            while ((dongVanBan = boDoc.readLine()) != null) {
                if (dongVanBan.isBlank() || dongVanBan.startsWith("#"))
                    continue;
                String[] cacTruong = dongVanBan.split("\t", -1);
                switch (cacTruong[0]) {
                case "NV" -> danhSachNhanVien.add(new NhanVien(cacTruong[1], cacTruong[2], cacTruong[3],
                        LocalDate.parse(cacTruong[4]), Boolean.valueOf(cacTruong[5]), cacTruong[6],
                        ChucVu.valueOf(cacTruong[7]), Boolean.parseBoolean(cacTruong[8])));
                case "TK" -> danhSachTaiKhoan
                        .add(new TaiKhoanMau(cacTruong[1], TrangThaiTaiKhoan.valueOf(cacTruong[2])));
                case "DV" -> danhSachDichVu.add(new DichVu(cacTruong[1], cacTruong[2],
                        new BigDecimal(cacTruong[3]), TrangThaiDichVu.valueOf(cacTruong[4])));
                case "HH" -> danhSachHangHoa.add(new HangHoa(cacTruong[1], cacTruong[2], cacTruong[3],
                        Integer.parseInt(cacTruong[4]), Integer.parseInt(cacTruong[5])));
                case "PK" -> danhSachLuotKham
                        .add(new LuotKhamMau(cacTruong[1], homNay.minusDays(Integer.parseInt(cacTruong[2])),
                                cacTruong[3], cacTruong[4], List.of(cacTruong[5].split(","))));
                default ->
                    throw new IllegalArgumentException("Loại dữ liệu demo không hợp lệ: " + cacTruong[0]);
                }
            }
        } catch (Exception loi) {
            throw new IllegalStateException("Không đọc được bộ dữ liệu demo.", loi);
        }
    }

    public List<NhanVien> danhSachNhanVien() {
        return List.copyOf(danhSachNhanVien);
    }

    public List<TaiKhoanMau> danhSachTaiKhoan() {
        return List.copyOf(danhSachTaiKhoan);
    }

    public List<DichVu> danhSachDichVu() {
        return List.copyOf(danhSachDichVu);
    }

    public List<HangHoa> danhSachHangHoa() {
        return List.copyOf(danhSachHangHoa);
    }

    public List<LuotKhamMau> danhSachLuotKham() {
        return List.copyOf(danhSachLuotKham);
    }
}
