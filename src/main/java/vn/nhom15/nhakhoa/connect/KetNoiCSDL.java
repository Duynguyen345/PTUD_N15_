package vn.nhom15.nhakhoa.connect;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Mỗi lần gọi mở một kết nối JDBC riêng; nơi gọi đóng bằng try-with-resources. */
public final class KetNoiCSDL implements NguonKetNoi {
    private final Path duongDanCauHinh;

    public KetNoiCSDL(Path duongDanCauHinh) {
        this.duongDanCauHinh = duongDanCauHinh;
    }

    @Override
    public Connection moKetNoi() throws SQLException {
        try {
            return docCauHinhKetNoi();
        } catch (IOException loi) {
            throw new SQLException("Không đọc được db.properties. Kiểm tra đường dẫn và cấu hình.", loi);
        }
    }

    private Connection docCauHinhKetNoi() throws IOException, SQLException {
        if (!Files.isRegularFile(duongDanCauHinh)) {
            throw new IOException("Chưa có cấu hình. Sao chép src/main/resources/db.properties.example "
                    + "thành db.properties tại thư mục chạy ứng dụng.");
        }
        Properties cauHinh = new Properties();
        try (Reader boDoc = Files.newBufferedReader(duongDanCauHinh, StandardCharsets.UTF_8)) {
            cauHinh.load(boDoc);
        }
        String url = cauHinh.getProperty("db.url", "").trim();
        String nguoiDung = cauHinh.getProperty("db.user", "").trim();
        String matKhau = cauHinh.getProperty("db.password");
        if (!url.startsWith("jdbc:sqlserver://") || nguoiDung.isEmpty() || matKhau == null) {
            throw new IOException("Cấu hình cần db.url dạng jdbc:sqlserver://, db.user và db.password.");
        }
        Properties thongTinKetNoi = new Properties();
        thongTinKetNoi.setProperty("user", nguoiDung);
        thongTinKetNoi.setProperty("password", matKhau);
        thongTinKetNoi.setProperty("loginTimeout", "5");
        thongTinKetNoi.setProperty("socketTimeout", "10000");
        return DriverManager.getConnection(url, thongTinKetNoi);
    }

}
