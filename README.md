# Phòng khám nha khoa — Nhóm 15

**Java 21 · Swing · JDBC · SQL Server.** Ứng dụng đọc/ghi trực tiếp SQL Server; không có chế độ demo trong bộ nhớ.

Bộ SQL cần để cài và chạy các chức năng hiện có: [DATA/nhakhoa_sqlserver.sql](DATA/nhakhoa_sqlserver.sql).

## Cài đặt và chạy

Yêu cầu JDK 21+, Maven và SQL Server bật TCP/IP. Trên máy SQL Server Express cục bộ tại cổng 1433, chạy một lần bằng Windows Authentication (tài khoản Windows cần quyền tạo database/login):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\cau_hinh_sqlserver.ps1
```

Script tạo database **N15_NHAKHOA_DB**, nạp dữ liệu và tạo `db.properties` với tài khoản SQL riêng có quyền đọc/ghi. SQL Server cần bật Mixed Mode. Script không thay đổi tài khoản `sa` hoặc database cũ. Máy chủ khác có thể truyền `-MayChu`, `-DiaChiJdbc`, `-Cong`. Nếu đã có `db.properties`, script dừng và giữ nguyên cấu hình.

Hoặc cài thủ công bằng SSMS:

1. Mở và chạy toàn bộ [DATA/nhakhoa_sqlserver.sql](DATA/nhakhoa_sqlserver.sql) bằng Execute/F5; không cần chạy thêm các schema/seed cũ.
2. Sao chép `src/main/resources/db.properties.example` thành `db.properties` ở thư mục gốc; điền địa chỉ/cổng SQL Server, tài khoản SQL và mật khẩu của máy bạn.
3. Chạy ứng dụng:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

Có thể nhấp đúp **chay_bai_thanh.cmd**, chạy `mvn compile exec:java`, hoặc chạy lớp `vn.nhom15.nhakhoa.UngDung` trong IDE có nạp dependency Maven. `run.ps1` dùng cache Maven tại `$env:USERPROFILE\.m2\repository` và tìm cả Maven trong bộ Java Extension của VS Code.

Tài khoản quản lý: **TK2601010001 / 123456**. Các mã **TK2601010002** (lễ tân), **TK2601010003** (bác sĩ), **TK2601010004** (phụ tá), **TK2601010005** (bác sĩ) cùng mật khẩu **123456**. **TK2601010006** bị khóa để thử từ chối đăng nhập. Tài khoản trên giao diện khác với tài khoản SQL trong `db.properties`.

Mỗi bảng có 6 bản ghi mẫu; riêng `PhieuKham` có 12: 6 lượt hôm nay và 6 lượt cũ đã hoàn tất. Chạy lại SQL chỉ thêm mã còn thiếu, không xóa/ghi đè trạng thái hoặc đổi ngày dữ liệu cũ. Các thay đổi trên giao diện được lưu trong SQL Server và giữ lại sau khi đóng ứng dụng. Ngày khám mẫu chốt tại lần nạp đầu; sau khi qua ngày mới cần tiếp nhận lượt khám mới.

## Chức năng hiện có

Đăng nhập/đăng xuất, phân quyền, tổng quan, nhân viên/tài khoản, danh mục dịch vụ, hàng hóa/tồn kho, báo cáo lượt khám/dịch vụ và xuất CSV, hàng chờ/gọi số/xác nhận vào khám, bật/tắt nhận bệnh, tra cứu hồ sơ và lịch sử khám.

Luồng tiếp nhận/thu phí và ghi kết quả/hoàn tất khám của các thành viên khác cần tiếp tục tích hợp. Bộ SQL này phục vụ tất cả màn hình đã có trong mã nguồn, không tự triển khai các chức năng còn thiếu.

## Phân công nhiệm vụ 4 thành viên

Phân công ngày 08/11/2026: Duy phụ trách xác thực; Thanh phụ trách phân quyền. Bộ SQL dùng chung phục vụ chạy/tích hợp, không đổi phân công nghiệp vụ.

| Thành viên | Phạm vi phụ trách |
|---|---|
| Nguyễn Tuấn Duy | Khung ứng dụng, tổng quan, đăng nhập/đăng xuất (UC001), nhân viên/tài khoản (UC010), dịch vụ (UC007), hàng hóa/tồn kho, báo cáo/CSV (UC008). |
| Hoàng Đình Huy | Hồ sơ bệnh nhân (UC002), thu phí khám (UC005), tiếp nhận/lập phiếu/cấp số/in phiếu (UC003), thanh toán cuối lượt. |
| Huỳnh Quốc Thắng | Luồng bác sĩ (UC004): nhận khám, tiền sử/bệnh án, chẩn đoán, chỉ định dịch vụ/cấp thuốc, lưu kết quả/hoàn tất khám và lịch sử. |
| Phan Chí Thanh | Phân quyền tại BUS/menu, hàng chờ/gọi số/xác nhận (UC009), bật/tắt nhận bệnh, tra cứu hồ sơ (UC006), bộ đếm mã/STT dùng chung. |

Duy xác thực tài khoản và tạo/hủy phiên; Thanh xác định quyền theo vai trò và chặn thao tác không đủ quyền. UC006 thuộc Thanh, đọc dữ liệu do luồng Huy/Thắng cung cấp. Mỗi thành viên phụ trách GUI → BUS → DAO và kiểm thử cho phần của mình.

## Kiến trúc

```text
UngDung: khởi tạo và truyền dependency
GUI Swing → BUS → PhongKhamDAO → KetNoiCSDL → SQL Server
              └→ DieuPhoiJdbcDAO → KetNoiCSDL → SQL Server
```

GUI không viết SQL hoặc gọi trực tiếp DAO. Tác vụ IO dùng SwingWorker; BUS kiểm tra dữ liệu/quyền; DAO dùng PreparedStatement, transaction, rollback và đóng tài nguyên. Mỗi worker mở kết nối riêng.

`LuotChoKham`, `LichSuKham` là kết quả truy vấn các bảng nghiệp vụ; `DuLieuBaoCao` là dữ liệu tổng hợp. Enum vai trò/quyền/trạng thái và phiên đăng nhập không cần bảng mẫu riêng.

Tạo mã NV/TK/HH theo yyMMddNNNN, dịch vụ theo DVNNNN; bộ đếm `DuyBoDem` được khóa trong cùng transaction với INSERT. Các mã mẫu dùng ngày 01/01/2026; mã mới dùng ngày hiện tại. Mật khẩu tạo từ giao diện dùng PBKDF2; hash SHA-256 của tài khoản mẫu vẫn được hỗ trợ.

DAO giả lập và TSV của bộ kiểm thử có sẵn nằm trong `src/test`, không thuộc chương trình chính. Chỉ cần chạy `DATA/nhakhoa_sqlserver.sql` khi cài database mới; không chạy thêm schema/seed/generator cũ.

## Kiểm thử

```powershell
mvn test
```

Nếu Maven trỏ sai cache:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

Các test nghiệp vụ/Swing có sẵn dùng dữ liệu cố định trong `src/test`. Test `TichHopJdbcTest` cũ dùng bộ dữ liệu cũ và cấu hình `duy.jdbc.config` riêng; được bỏ qua nếu chưa cung cấp cấu hình database kiểm thử.
