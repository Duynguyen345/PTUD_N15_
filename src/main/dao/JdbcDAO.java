package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import connect.NguonKetNoi;

/** Hạ tầng JDBC dùng chung: kết nối, truy vấn, giao dịch và sinh mã. */
public abstract class JdbcDAO {
    private final NguonKetNoi nguonKetNoi;

    protected JdbcDAO(NguonKetNoi nguonKetNoi) {
        this.nguonKetNoi = nguonKetNoi;
    }

    @FunctionalInterface
    protected interface TacVuCSDL<T> {
        T execute(Connection ketNoi) throws SQLException;
    }

    @FunctionalInterface
    protected interface AnhXaDuLieu<T> {
        T map(ResultSet ketQua) throws SQLException;
    }

    protected <T> T docDuLieu(TacVuCSDL<T> tacVu) {
        try (Connection ketNoi = nguonKetNoi.moKetNoi()) {
            return tacVu.execute(ketNoi);
        } catch (SQLException loi) {
            throw chuyenLoiSQL(loi);
        }
    }

    protected <T> T thucHienGiaoDich(TacVuCSDL<T> tacVu) {
        return docDuLieu(ketNoi -> {
            ketNoi.setAutoCommit(false);
            try {
                T ketQua = tacVu.execute(ketNoi);
                ketNoi.commit();
                return ketQua;
            } catch (SQLException | RuntimeException loi) {
                try {
                    ketNoi.rollback();
                } catch (SQLException loiHoanTac) {
                    loi.addSuppressed(loiHoanTac);
                }
                throw loi;
            }
        });
    }

    private NgoaiLeTruyCapDuLieu chuyenLoiSQL(SQLException loi) {
        String thongBao = switch (loi.getErrorCode()) {
        case 2601, 2627 ->
            "Dữ liệu đã tồn tại: kiểm tra tên dịch vụ, tên hàng, mã tài khoản hoặc tài khoản của nhân viên.";
        case 547 ->
            "Dữ liệu đang được sử dụng hoặc không hợp lệ. Không thể xóa nhân viên đã có tài khoản/phiếu khám.";
        case 208, 207 -> "Database thiếu bảng hoặc cột. Chạy script nâng cấp Duy trước khi sử dụng.";
        default ->
            "Không thực hiện được thao tác database. Kiểm tra db.properties, SQL Server và script cài đặt.";
        };
        return new NgoaiLeTruyCapDuLieu(thongBao, loi);
    }

    private PreparedStatement taoCauLenh(Connection ketNoi, String cauLenhSQL, Object... cacGiaTri)
            throws SQLException {
        PreparedStatement cauLenh = ketNoi.prepareStatement(cauLenhSQL);
        try {
            cauLenh.setQueryTimeout(15);
            for (int i = 0; i < cacGiaTri.length; i++) {
                Object giaTri = cacGiaTri[i];
                if (giaTri instanceof LocalDate ngay)
                    cauLenh.setDate(i + 1, Date.valueOf(ngay));
                else
                    cauLenh.setObject(i + 1, giaTri);
            }
            return cauLenh;
        } catch (SQLException loi) {
            cauLenh.close();
            throw loi;
        }
    }

    protected int thucHienCapNhat(Connection ketNoi, String cauLenhSQL, Object... cacGiaTri)
            throws SQLException {
        try (PreparedStatement cauLenh = taoCauLenh(ketNoi, cauLenhSQL, cacGiaTri)) {
            return cauLenh.executeUpdate();
        }
    }

    protected <T> List<T> truyVan(Connection ketNoi, String cauLenhSQL, AnhXaDuLieu<T> boAnhXa,
            Object... cacGiaTri) throws SQLException {
        try (PreparedStatement cauLenh = taoCauLenh(ketNoi, cauLenhSQL, cacGiaTri);
                ResultSet ketQua = cauLenh.executeQuery()) {
            List<T> cacDong = new ArrayList<>();
            while (ketQua.next())
                cacDong.add(boAnhXa.map(ketQua));
            return cacDong;
        }
    }

    protected void kiemTraSoDongCapNhat(int soLuong) {
        if (soLuong != 1)
            throw new IllegalArgumentException("Bản ghi không còn tồn tại. Vui lòng tải lại.");
    }

    /** Bộ đếm được khóa trong cùng transaction với INSERT, không dùng MAX+1. */
    protected String taoMaTiepTheo(Connection ketNoi, String tienTo, String bang, String cot)
            throws SQLException {
        String ngay = tienTo.equals("DV") ? ""
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String khoa = tienTo + ngay;
        // Range lock giữ cả khóa chưa tồn tại, tránh hai transaction cùng INSERT bộ đếm.
        List<Integer> cacGiaTri = truyVan(ketNoi,
                "SELECT giaTri FROM DuyBoDem WITH (UPDLOCK,HOLDLOCK) WHERE khoa=?", r -> r.getInt(1), khoa);
        int boDem = cacGiaTri.isEmpty() ? 0 : cacGiaTri.getFirst();
        String ma;
        do {
            boDem++;
            if (boDem > 9999)
                throw new IllegalArgumentException("Đã hết dải mã " + khoa + ".");
            ma = khoa + String.format("%04d", boDem);
        } while (!truyVan(ketNoi, "SELECT 1 FROM " + bang + " WHERE " + cot + "=?", r -> r.getInt(1), ma)
                .isEmpty());
        if (cacGiaTri.isEmpty())
            thucHienCapNhat(ketNoi, "INSERT INTO DuyBoDem(khoa,giaTri) VALUES (?,?)", khoa, boDem);
        else
            thucHienCapNhat(ketNoi, "UPDATE DuyBoDem SET giaTri=? WHERE khoa=?", boDem, khoa);
        return ma;
    }

}
