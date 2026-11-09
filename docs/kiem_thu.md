# Kết quả kiểm thử SQL Server

Ngày thực hiện: **09/11/2026**, múi giờ Việt Nam. JDK 21; Maven biên dịch theo `--release 21`.

Lệnh đã chạy:

```powershell
mvn test "-Dnhakhoa.jdbc.config=target/sqlserver-test.properties"
```

Kết quả: **BUILD SUCCESS — 40 kiểm thử, 39 đạt, 0 thất bại, 0 lỗi, 1 bỏ qua**.

| Bộ kiểm thử | Số lượng | Kết quả |
|---|---:|---|
| DieuPhoiNghiepVuTest | 14 | Đạt |
| DieuPhoiGiaoDienTest | 2 | Đạt |
| NghiepVuQuanLyTest | 18 | Đạt |
| BoDuLieuMauTest | 1 | Đạt |
| LuongGiaoDienTest | 1 | Đạt |
| ManHinhSwingTest | 1 | Đạt |
| TimKiemNhanVienTest | 1 | Đạt |
| SqlServerToanBaiTest | 1 | Đạt trên SQL Server Express thật |
| TichHopJdbcTest | 1 | Bỏ qua: bộ SQL cũ, không cung cấp duy.jdbc.config |

## Đã xác minh trên SQL Server thật

- Bộ [nhakhoa_sqlserver.sql](../DATA/nhakhoa_sqlserver.sql) tạo đủ 10 bảng; 9 bảng có 6 dòng, PhieuKham có 12 dòng.
- Database kiểm thử riêng `N15_NHAKHOA_TEST_20261109122419`; test không ghi vào database ứng dụng.
- Kết nối JDBC bằng cùng login SQL riêng mà ứng dụng sử dụng.
- Đăng nhập quản lý, lễ tân, bác sĩ, phụ tá; từ chối tài khoản bị khóa.
- Đọc nhân viên, dịch vụ, hàng hóa và báo cáo đúng dữ liệu SQL.
- Tìm không dấu `nguyen minh chau`, `dang gia huy`, tìm SĐT và xử lý wildcard như văn bản.
- Lấy đúng lịch sử, chẩn đoán và điều trị của bệnh nhân.
- Kiểm tra quyền: lễ tân không gọi số; phụ tá không đọc bệnh án; bác sĩ không đổi phòng người khác.
- Bật/tắt phòng được lưu và đọc lại bằng DAO/kết nối mới.
- Hai yêu cầu JDBC cùng gọi một phòng: chỉ một yêu cầu thành công.
- Gọi lại giữ số, đánh dấu vắng cho phép gọi tiếp, xác nhận vào khám được commit vào SQL.
- Chặn gọi tại phòng tạm ngưng/đang khám và chặn tắt phòng đang gọi.
- Chạy lại toàn bộ file SQL bằng login ứng dụng không tạo trùng mẫu, không reset lượt đang gọi.
- Dữ liệu hai lượt kiểm thử được trả về trạng thái ban đầu sau test.
- Bản biên dịch chính không chứa DAO bộ nhớ hoặc tài nguyên TSV; fixture chỉ ở `src/test`.
- Cú pháp hai script PowerShell hợp lệ; cấu hình có mật khẩu được Git bỏ qua.

Các test nghiệp vụ và Swing cũ tiếp tục dùng fixture riêng trong `src/test`. Ảnh kiểm thử được sinh trong `target`, không phải tài nguyên cần để chạy ứng dụng.

## Chạy lại

```powershell
mvn test
```

Lệnh trên chạy test nghiệp vụ/Swing; các test SQL bỏ qua nếu không có cấu hình riêng. Để chạy SQL mới, tạo database kiểm thử tên bắt đầu bằng `N15_NHAKHOA_TEST_` từ bản sao file SQL, thay tên database trong bản sao, cấu hình kết nối tới database đó rồi truyền `nhakhoa.jdbc.config` như lệnh đầu tài liệu.

Ứng dụng thật dùng `db.properties` và database **N15_NHAKHOA_DB**. Không dùng database đang có dữ liệu làm việc để chạy kiểm thử ghi.

Luồng tiếp nhận/thu phí và hoàn tất khám của các thành viên khác chưa được triển khai thêm trong thay đổi này.
