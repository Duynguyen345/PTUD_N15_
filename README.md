# Dentist Clinic

Ứng dụng desktop quản lý phòng khám nha khoa.

## Công nghệ
- Java 21
- Java Swing/AWT
- JDBC (Microsoft SQL Server JDBC Driver)
- Microsoft SQL Server
- Eclipse IDE

## Cấu trúc
- `src/giaodien_UI`: giao diện và xử lý sự kiện Swing.
- `src/dAO`: truy cập dữ liệu bằng JDBC (`PreparedStatement`, `ResultSet`).
- `src/model`: các lớp dữ liệu như `BenhNhan`, `PhieuKham`, `HoaDon`.
- `src/connectDB`: cấu hình và tạo kết nối JDBC.
- `src/resources`: biểu tượng, hình ảnh và tài nguyên giao diện.
- `DATA`: lược đồ, dữ liệu mẫu và truy vấn SQL Server.
- `test`: kiểm thử các lớp dữ liệu và DAO.
- `Library`: thư viện JAR cục bộ; thêm Microsoft SQL Server JDBC Driver tại đây.
- `docs`: tài liệu phân tích và thiết kế.

## Luồng phụ thuộc dự kiến
`giaodien_UI` → `dAO` → JDBC → SQL Server. Các lớp trong `model` được GUI/DAO dùng để biểu diễn và truyền dữ liệu. `connectDB` cung cấp `Connection` cho DAO.

## Mở bằng Eclipse
Import thư mục này dưới dạng **Existing Projects into Workspace**. Thêm Microsoft SQL Server JDBC Driver vào Build Path (ví dụ `Library/mssql-jdbc.jar`).
