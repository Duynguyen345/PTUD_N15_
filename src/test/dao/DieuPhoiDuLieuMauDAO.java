package dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import entity.*;

/** Kho bộ nhớ chỉ dùng kiểm thử; synchronized tương ứng transaction của JDBC. */
public final class DieuPhoiDuLieuMauDAO implements DieuPhoiDAO {
    private final Map<String, BenhNhan> cacBenhNhan = new LinkedHashMap<>();
    private final Map<String, PhongKham> cacPhong = new LinkedHashMap<>();
    private final Map<String, LuotChoKham> cacLuotCho = new LinkedHashMap<>();
    private final Map<String, List<LichSuKham>> cacLichSu = new HashMap<>();

    /** Nạp fixture cùng nguồn với SQL seed, tính ngày từ đồng hồ được truyền vào. */
    public DieuPhoiDuLieuMauDAO(LocalDate homNay) {
        var luong = getClass().getResourceAsStream("/demo/dieu_phoi.tsv");
        if (luong == null) throw new IllegalStateException("Thiếu demo/dieu_phoi.tsv.");
        try (var boDoc = new BufferedReader(new InputStreamReader(luong, StandardCharsets.UTF_8))) {
            String dong;
            while ((dong = boDoc.readLine()) != null) {
                if (dong.isBlank() || dong.startsWith("#")) continue;
                String[] f = dong.split("\t", -1);
                switch (f[0]) {
                    case "BN" -> cacBenhNhan.put(f[1], new BenhNhan(f[1], f[2], LocalDate.parse(f[3]),
                            f[4], f[5], f[6], f[7], f[8]));
                    case "PH" -> cacPhong.put(f[1], new PhongKham(f[1], f[2], f[3], f[4], Boolean.parseBoolean(f[5])));
                    case "HC" -> {
                        LocalDate ngay = homNay.plusDays(Integer.parseInt(f[2]));
                        TrangThaiHangCho trangThai = TrangThaiHangCho.valueOf(f[7]);
                        cacLuotCho.put(f[1], new LuotChoKham(f[1], ngay, f[3], Integer.parseInt(f[4]), f[5],
                                cacBenhNhan.get(f[5]).hoTen(), Boolean.parseBoolean(f[6]), trangThai,
                                trangThai == TrangThaiHangCho.DANG_KHAM ? ngay.atTime(8, 0) : null));
                    }
                    case "LS" -> cacLichSu.computeIfAbsent(f[3], ma -> new ArrayList<>()).add(
                            new LichSuKham(f[1], homNay.plusDays(Integer.parseInt(f[2])), f[5], f[6], f[7], f[8], "Hoàn tất"));
                    default -> throw new IllegalArgumentException("Dòng dữ liệu điều phối không hợp lệ.");
                }
            }
        } catch (Exception loi) {
            throw new IllegalStateException("Không đọc được dữ liệu điều phối mẫu.", loi);
        }
    }

    @Override
    public synchronized List<PhongKham> layDanhSachPhong() { return List.copyOf(cacPhong.values()); }

    @Override
    public synchronized List<LuotChoKham> layHangCho(String maPhong, LocalDate ngay) {
        return cacLuotCho.values().stream().filter(l -> l.maPhong().equals(maPhong) && l.ngayKham().equals(ngay))
                .sorted(Comparator.comparingInt(LuotChoKham::soThuTu)).toList();
    }

    /** Chuẩn hóa dấu để demo khớp collation CI_AI của SQL Server. */
    private String chuanHoa(String chuoi) {
        return Normalizer.normalize(chuoi, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    @Override
    public synchronized List<BenhNhan> timBenhNhan(String tuKhoa) {
        String mau = chuanHoa(tuKhoa);
        return cacBenhNhan.values().stream().filter(b -> chuanHoa(b.maBenhNhan()).contains(mau)
                || chuanHoa(b.hoTen()).contains(mau) || b.soDienThoai().contains(mau)).toList();
    }

    @Override
    public synchronized List<LichSuKham> layLichSuKham(String maBenhNhan) {
        List<LichSuKham> ketQua = new ArrayList<>(cacLichSu.getOrDefault(maBenhNhan, List.of()));
        for (LuotChoKham luot : cacLuotCho.values()) {
            if (luot.maBenhNhan().equals(maBenhNhan))
                ketQua.add(new LichSuKham(luot.maPhieuKham(), luot.ngayKham(), cacPhong.get(luot.maPhong()).tenBacSi(),
                        "Chưa có kết quả", "Chưa có kết quả", "", luot.trangThai().toString()));
        }
        return ketQua.stream().sorted(Comparator.comparing(LichSuKham::ngayKham).reversed()
                .thenComparing(LichSuKham::maPhieuKham)).toList();
    }

    private PhongKham layPhong(String maPhong) {
        PhongKham phong = cacPhong.get(maPhong);
        if (phong == null) throw new IllegalArgumentException("Không tìm thấy phòng khám.");
        return phong;
    }

    private boolean coLuotDangPhucVu(String maPhong, LocalDate ngay) {
        return layHangCho(maPhong, ngay).stream().anyMatch(l -> l.trangThai() == TrangThaiHangCho.DANG_GOI
                || l.trangThai() == TrangThaiHangCho.DANG_KHAM);
    }

    @Override
    public synchronized void datNhanBenh(String maPhong, boolean nhanBenh, LocalDate ngay) {
        PhongKham phong = layPhong(maPhong);
        if (!nhanBenh && coLuotDangPhucVu(maPhong, ngay))
            throw new IllegalArgumentException("Phòng còn bệnh nhân đang gọi/đang khám; chưa thể tắt nhận bệnh.");
        cacPhong.put(maPhong, new PhongKham(phong.maPhong(), phong.tenPhong(), phong.maBacSi(), phong.tenBacSi(), nhanBenh));
    }

    @Override
    public synchronized LuotChoKham goiTiepTheo(String maPhong, LocalDate ngay, LocalDateTime thoiDiem) {
        if (!layPhong(maPhong).dangNhanBenh()) throw new IllegalArgumentException("Phòng đang tắt nhận bệnh.");
        if (coLuotDangPhucVu(maPhong, ngay))
            throw new IllegalArgumentException("Phòng đang gọi hoặc đang khám. Hoàn tất lượt hiện tại trước.");
        LuotChoKham luot = layHangCho(maPhong, ngay).stream()
                .filter(l -> l.trangThai() == TrangThaiHangCho.CHO_KHAM && l.daThanhToanPhiKham())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Không còn bệnh nhân đã trả phí đang chờ."));
        LuotChoKham ketQua = luot.chuyenTrangThai(TrangThaiHangCho.DANG_GOI, thoiDiem);
        cacLuotCho.put(ketQua.maPhieuKham(), ketQua);
        return ketQua;
    }

    @Override
    public synchronized LuotChoKham chuyenTrangThai(String maPhieuKham, String maPhong, LocalDate ngay,
            TrangThaiHangCho dich, LocalDateTime thoiDiem) {
        if (!layPhong(maPhong).dangNhanBenh()) throw new IllegalArgumentException("Phòng đang tắt nhận bệnh.");
        LuotChoKham luot = cacLuotCho.get(maPhieuKham);
        if (luot == null || !luot.maPhong().equals(maPhong) || !luot.ngayKham().equals(ngay))
            throw new IllegalArgumentException("Lượt khám không thuộc phòng/ngày đang chọn.");
        if (luot.trangThai() != TrangThaiHangCho.DANG_GOI)
            throw new IllegalArgumentException("Chỉ thao tác trên bệnh nhân đang được gọi.");
        LuotChoKham ketQua = luot.chuyenTrangThai(dich, thoiDiem);
        cacLuotCho.put(maPhieuKham, ketQua);
        return ketQua;
    }
}
