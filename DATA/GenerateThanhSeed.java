import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Sinh seed SQL tu cung TSV ma DieuPhoiDuLieuMauDAO doc. Chay tu thu muc du an. */
public class GenerateThanhSeed {
    private static String chuoi(String giaTri) { return "N'" + giaTri.replace("'", "''") + "'"; }
    private static String ngay(String doLech) { return "DATEADD(day," + Integer.parseInt(doLech) + ",CONVERT(date,GETDATE()))"; }

    public static void main(String[] thamSo) throws Exception {
        List<String> cacLenh = new ArrayList<>();
        cacLenh.add("-- Generated from src/test/resources/demo/dieu_phoi.tsv by DATA/GenerateThanhSeed.java");
        cacLenh.add("-- Chay SAU schema 040 va seed 001_duy_demo.sql trong database demo/test.");
        cacLenh.add("-- Khong ghi de du lieu cu. Ngay kham duoc tinh tai lan chay seed dau tien.");
        cacLenh.add("SET XACT_ABORT ON;\nSET QUOTED_IDENTIFIER ON;\nBEGIN TRANSACTION;");
        for (String dong : Files.readAllLines(Path.of("src/test/resources/demo/dieu_phoi.tsv"), StandardCharsets.UTF_8)) {
            if (dong.isBlank() || dong.startsWith("#")) continue;
            String[] f = dong.split("\t", -1);
            switch (f[0]) {
                case "BN" -> cacLenh.add("IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=" + chuoi(f[1]) + ")\n"
                        + "INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES("
                        + String.join(",", java.util.Arrays.stream(f).skip(1).map(GenerateThanhSeed::chuoi).toList()) + ");");
                case "PH" -> cacLenh.add("IF NOT EXISTS(SELECT 1 FROM PhongKham WHERE maPhong=" + chuoi(f[1]) + ")\n"
                        + "INSERT INTO PhongKham(maPhong,tenPhong,maBacSi,dangNhanBenh) VALUES("
                        + chuoi(f[1]) + "," + chuoi(f[2]) + "," + chuoi(f[3]) + "," + (Boolean.parseBoolean(f[5]) ? 1 : 0) + ");");
                case "HC" -> {
                    boolean dangKham = f[7].equals("DANG_KHAM");
                    cacLenh.add("IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=" + chuoi(f[1]) + ")\n"
                            + "INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)\n"
                            + "SELECT " + chuoi(f[1]) + "," + ngay(f[2]) + "," + chuoi(dangKham ? "DANG_KHAM" : "CHO_KHAM")
                            + ",maBacSi," + chuoi(f[5]) + ",maPhong," + Integer.parseInt(f[4]) + ","
                            + (Boolean.parseBoolean(f[6]) ? 1 : 0) + "," + chuoi(f[7]) + ","
                            + (dangKham ? "DATEADD(hour,8,CONVERT(datetime2," + ngay(f[2]) + "))" : "NULL")
                            + " FROM PhongKham WHERE maPhong=" + chuoi(f[3]) + ";");
                }
                case "LS" -> {
                    cacLenh.add("IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=" + chuoi(f[1]) + ")\n"
                            + "INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan) VALUES("
                            + chuoi(f[1]) + "," + ngay(f[2]) + ",N'HOAN_TAT'," + chuoi(f[4]) + "," + chuoi(f[3]) + ");");
                    cacLenh.add("IF NOT EXISTS(SELECT 1 FROM HoSoKham WHERE maPhieuKham=" + chuoi(f[1]) + ")\n"
                            + "INSERT INTO HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu) VALUES("
                            + chuoi(f[1]) + "," + chuoi(f[6]) + "," + chuoi(f[7]) + "," + chuoi(f[8]) + ");");
                }
                default -> throw new IllegalArgumentException("Unknown fixture type " + f[0]);
            }
        }
        cacLenh.add("COMMIT;\nGO\n");
        Files.writeString(Path.of("DATA/seed/040_thanh_demo.sql"), String.join("\n", cacLenh), StandardCharsets.UTF_8);
        System.out.println("Generated DATA/seed/040_thanh_demo.sql");
    }
}
