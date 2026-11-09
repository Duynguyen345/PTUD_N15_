# Bài hệ thống, điều phối và tra cứu — Phan Chí Thanh

Ứng dụng sử dụng trực tiếp SQL Server cho đăng nhập, phân quyền, quản trị, hàng chờ, gọi số, phòng nhận bệnh và tra cứu hồ sơ.

## Cài đặt và chạy

Trên máy SQL Server Express cục bộ, chạy một lần:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\cau_hinh_sqlserver.ps1
```

Hoặc chạy `DATA/nhakhoa_sqlserver.sql` bằng SSMS, sao chép `src/main/resources/db.properties.example` thành `db.properties` ở thư mục gốc và điền tài khoản kết nối SQL.

Sau đó nhấp đúp **chay_bai_thanh.cmd** hoặc chạy:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

Cần JDK 21+, Maven và SQL Server. Database mới là **N15_NHAKHOA_DB**; database cũ được giữ nguyên. Thay đổi trên giao diện được lưu vào SQL Server, còn lại sau khi thoát/mở lại ứng dụng.

## Tài khoản mẫu trên giao diện

| Mã tài khoản | Mật khẩu | Vai trò | Chức năng |
|---|---|---|---|
| TK2601010001 | 123456 | Quản lý | Tất cả chức năng hiện có |
| TK2601010002 | 123456 | Lễ tân | Tra cứu hồ sơ |
| TK2601010003 | 123456 | Bác sĩ | Xem hàng chờ, tra cứu; bật/tắt P01, P04, P05 phụ trách |
| TK2601010004 | 123456 | Phụ tá | Gọi số, xác nhận vào khám, đánh dấu vắng, bật/tắt nhận bệnh |
| TK2601010005 | 123456 | Bác sĩ | Xem hàng chờ, tra cứu; bật/tắt P02, P03, P06 phụ trách |
| TK2601010006 | 123456 | Lễ tân | Bị khóa để kiểm tra từ chối đăng nhập |

## File cần xem

| Nội dung | File |
|---|---|
| Tạo toàn bộ bảng và dữ liệu mẫu | [DATA/nhakhoa_sqlserver.sql](DATA/nhakhoa_sqlserver.sql) |
| Cài đặt SQL Server và cấu hình | [scripts/cau_hinh_sqlserver.ps1](scripts/cau_hinh_sqlserver.ps1) |
| Kết nối JDBC | [KetNoiCSDL.java](src/main/java/vn/nhom15/nhakhoa/connect/KetNoiCSDL.java) |
| Khởi tạo và nối GUI/BUS/DAO SQL | [UngDung.java](src/main/java/vn/nhom15/nhakhoa/UngDung.java) |
| Đăng nhập | [DangNhapBUS.java](src/main/java/vn/nhom15/nhakhoa/bus/DangNhapBUS.java) |
| Đăng xuất, kiểm tra phiên/quyền | [QuanLyPhienDangNhap.java](src/main/java/vn/nhom15/nhakhoa/bus/QuanLyPhienDangNhap.java) |
| Chính sách vai trò | [PhanQuyenBUS.java](src/main/java/vn/nhom15/nhakhoa/bus/PhanQuyenBUS.java), [Quyen.java](src/main/java/vn/nhom15/nhakhoa/entity/Quyen.java) |
| Menu theo quyền | [GD_Chinh.java](src/main/java/vn/nhom15/nhakhoa/gui/GD_Chinh.java) |
| Gọi số, xác nhận, bật/tắt phòng | [DieuPhoiBUS.java](src/main/java/vn/nhom15/nhakhoa/bus/DieuPhoiBUS.java) |
| Giao diện hàng chờ | [GD_HangCho.java](src/main/java/vn/nhom15/nhakhoa/gui/GD_HangCho.java) |
| Bảng số theo phòng | [GD_BangGoiSo.java](src/main/java/vn/nhom15/nhakhoa/gui/GD_BangGoiSo.java) |
| Phòng nhận bệnh | [GD_PhongKham.java](src/main/java/vn/nhom15/nhakhoa/gui/GD_PhongKham.java) |
| Nghiệp vụ tra cứu | [TraCuuHoSoBUS.java](src/main/java/vn/nhom15/nhakhoa/bus/TraCuuHoSoBUS.java) |
| Giao diện hồ sơ/lịch sử | [GD_TraCuuHoSo.java](src/main/java/vn/nhom15/nhakhoa/gui/GD_TraCuuHoSo.java) |
| Hợp đồng dữ liệu điều phối | [DieuPhoiDAO.java](src/main/java/vn/nhom15/nhakhoa/dao/DieuPhoiDAO.java) |
| Đọc/ghi điều phối, hồ sơ SQL | [DieuPhoiJdbcDAO.java](src/main/java/vn/nhom15/nhakhoa/dao/DieuPhoiJdbcDAO.java) |
| Quản trị/báo cáo SQL | [PhongKhamDAO.java](src/main/java/vn/nhom15/nhakhoa/dao/PhongKhamDAO.java) |
| Kiểm thử SQL mới | [SqlServerToanBaiTest.java](src/test/java/vn/nhom15/nhakhoa/SqlServerToanBaiTest.java) |

Thực thể mới: `BenhNhan`, `PhongKham`, `LuotChoKham`, `LichSuKham`, `TrangThaiHangCho`. Lượt chờ và lịch sử được truy vấn từ các bảng nghiệp vụ, không tạo bảng trùng cho từng lớp Java.

DAO giả lập/TSV chỉ nằm trong `src/test` để chạy kiểm thử. `UngDungDemo` và script đóng gói demo đã bỏ. Không cần chạy schema/seed/generator cũ khi cài database mới.

Số lượng và tình huống dữ liệu: [TaiLieu/DU_LIEU_MAU.md](TaiLieu/DU_LIEU_MAU.md).
