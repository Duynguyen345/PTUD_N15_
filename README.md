# Phòng khám nha khoa — Nhóm 15

Nhánh làm việc: **Duy_Tao_Moi**. **Java 21 · Swing · OOP · JDBC SQL Server**.

Phiên bản hiện tại đã có đăng nhập, kiểm tra quyền, khung điều hướng, tổng quan, nhân viên, tài khoản, dịch vụ, hàng hóa và báo cáo/CSV. Phân công dưới đây xác định người phụ trách tiếp tục phát triển; không có nghĩa tất cả chức năng của cả nhóm đã được triển khai.

## Phân công nhiệm vụ 4 thành viên

Cập nhật ngày **08/11/2026**: chuyển phần **phân quyền từ Duy sang Thanh**. Duy tiếp tục phụ trách đăng nhập. **Bỏ nhiệm vụ thiết kế, viết và quản lý SQL schema/migration khỏi phân công cả nhóm.**

| Thành viên | Phạm vi phụ trách |
|---|---|
| **Nguyễn Tuấn Duy** | Khung ứng dụng, màn hình chính/tổng quan, đăng nhập và đăng xuất (phần xác thực UC001); quản lý nhân viên và tài khoản (UC010); danh mục dịch vụ (UC007); hàng hóa/tồn kho; báo cáo lượt khám và xuất CSV (UC008). |
| **Hoàng Đình Huy** | Luồng lễ tân: quản lý hồ sơ bệnh nhân (UC002), thu phí khám (UC005), tiếp nhận/lập phiếu khám, cấp số thứ tự và in phiếu (UC003); thanh toán dịch vụ cuối lượt. |
| **Huỳnh Quốc Thắng** | Luồng bác sĩ (UC004): nhận danh sách khám, xem tiền sử/hồ sơ bệnh án, ghi chẩn đoán, chỉ định dịch vụ, cấp thuốc, lưu kết quả, hoàn tất lượt khám và xem lịch sử khám. |
| **Phan Chí Thanh** | **Phân quyền**: quy định quyền theo vai trò, kiểm tra quyền tại BUS, phối hợp hiển thị menu/chức năng theo quyền; hàng chờ, gọi số và xác nhận vào khám (UC009); bật/tắt phòng nhận bệnh; tra cứu hồ sơ bệnh nhân (UC006); kết nối JDBC, cấu hình kết nối và bộ đếm cấp mã/STT dùng chung. |

### Ranh giới đăng nhập, tài khoản và phân quyền

- **Duy:** xác thực mã tài khoản/mật khẩu, kiểm tra tài khoản bị khóa hoặc nhân viên nghỉ việc, tạo/hủy phiên đăng nhập; thêm/cập nhật nhân viên, tạo và khóa/mở tài khoản.
- **Thanh:** xác định mỗi vai trò được thực hiện chức năng nào, chặn thao tác không đủ quyền tại BUS và phối hợp với khung giao diện của Duy để hiển thị menu tương ứng.
- **UC006 tra cứu hồ sơ thuộc Thanh**, không thuộc Huy. Thanh sử dụng dữ liệu bệnh nhân và phiếu khám do luồng của Huy cung cấp.

Mỗi thành viên phụ trách GUI → BUS → DAO, kiểm thử và tài liệu cho phần việc của mình. Không giao đầu việc SQL schema/migration cho thành viên nào. Các script hiện có trong `DATA/` được giữ làm tài nguyên cài đặt và dữ liệu thử; hướng dẫn chạy chúng bên dưới không phải nhiệm vụ phân công.

## Chạy thử ngay, không cần database

```powershell
.\run.ps1 -Demo
```

Tài khoản quản lý demo: **TK2601010001 / 123456**. Các mã TK2601010002, TK2601010003, TK2601010004 dùng để thử phân quyền; cùng mật khẩu 123456. Dữ liệu demo chỉ giữ trong bộ nhớ và mất khi thoát.

Hoặc dùng Maven:

```powershell
mvn compile exec:java '-Dexec.args=--demo'
```

`run.ps1` dùng cache Maven tại `$env:USERPROFILE\.m2\repository`, tránh cấu hình mặc định trỏ nhầm `C:\.m2`. Nếu PowerShell chặn script, dùng `powershell -ExecutionPolicy Bypass -File .\run.ps1 -Demo` cho lần chạy đó.

## Chạy với SQL Server thật

1. Trên instance SQL cần dùng, chạy `DATA/schema/001_fresh_database.sql` **một lần cho database mới N15_DUY_DB**. Không chạy file này trên database đã có các bảng tương ứng.
2. Chọn database N15_DUY_DB trong SSMS, chạy `DATA/schema/020_duy_upgrade.sql`.
3. Nếu cần dữ liệu thử, chạy `DATA/seed/001_duy_demo.sql` trong database mới. Seed xóa dữ liệu cũ trong các bảng Duy rồi nạp 10 nhân viên, 6 tài khoản, 8 dịch vụ, 8 hàng hóa, 14 phiếu khám và 28 chỉ định. Ngày báo cáo gắn với ngày chạy script.
4. Tạo cấu hình riêng:

```powershell
Copy-Item src/main/resources/db.properties.example db.properties
```

5. Sửa URL, SQL username và password theo máy. Database mặc định là `N15_DUY_DB`. Dùng SQL Authentication; cấu hình mẫu không dùng Windows integrated authentication. File thật đã được Git ignore.
6. Chạy:

```powershell
.\run.ps1
```

Trên máy hiện tại, cổng `localhost:1433` thuộc **SQLEXPRESS**. Kết nối `localhost` mặc định trong SSMS/sqlcmd có thể trỏ instance khác; cần chọn đúng instance. Ví dụ sqlcmd: `-S tcp:localhost,1433`.

Nếu dùng database cũ có schema tương thích, chạy lần lượt script 020 rồi 030_account_ids.sql và chỉnh `databaseName` trong cấu hình. Script 030 đổi cột tenDangNhap thành maTaiKhoan, cấp mã TK cho tài khoản tên cũ và trả bảng đối chiếu; hãy lưu mã mới để đăng nhập. Mật khẩu, nhân viên và trạng thái giữ nguyên. Không chạy file khởi tạo 001 hoặc seed lên dữ liệu thật. Ứng dụng không tự tạo/sửa schema khi mở.

## Kiến trúc OOP

```text
UngDung: khởi tạo, truyền dependency
GUI Swing → BUS → PhongKhamDAO (lớp xử lý JDBC)
                    ├─ NguonKetNoi → SQL Server
                    └─ PhongKhamDuLieuMauDAO kế thừa → bộ nhớ (chế độ demo)
```

Chỉ giữ 9 entity/enum/record cần cho Duy: NhanVien, TaiKhoan, ChucVu, PhienDangNhap, DichVu, TrangThaiTaiKhoan, TrangThaiDichVu, HangHoa, DuLieuBaoCao. Đã bỏ 10 entity ngoài phạm vi. Session và Connection không dùng biến static. GUI không chứa SQL. Mỗi tác vụ IO chạy trên SwingWorker; BUS kiểm tra dữ liệu/quyền; DAO dùng PreparedStatement, transaction và đóng tài nguyên.

## Kiểm thử

```powershell
mvn test
```

Nếu Maven đang trỏ sai cache:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

Bộ test kiểm tra nghiệp vụ, render màn hình và thao tác Swing. Test JDBC chỉ chạy khi truyền `-Dduy.jdbc.config=<file cấu hình kiểm thử>` và bắt buộc database tên `N15_DUY_TEST_*` đã có schema/seed. Không dùng database thật để chạy test ghi dữ liệu.

Chi tiết luồng, cách áp dụng OOP và kịch bản demo: [TaiLieu/DUY_SWING_OOP.md](TaiLieu/DUY_SWING_OOP.md).

## Mã tài khoản tự sinh

Khi tạo tài khoản, chỉ chọn nhân viên và nhập mật khẩu. Hệ thống cấp mã theo dạng TKyyMMddNNNN trong cùng transaction với INSERT, kiểm tra trùng và trả mã về giao diện. Ô mã chỉ đọc; không có tên đăng nhập tự đặt. Đăng nhập bằng mã TK được cấp. Seed demo cũng cấp mã từ bộ đếm, với ngày mô phỏng 01/01/2026.

Schema mới chỉ chứa phần Duy và hai bảng dữ liệu nguồn PhieuKham/ChiDinhDichVu cho báo cáo. Không có luồng tiếp nhận, khám bệnh, cấp thuốc hay thanh toán. Việc dọn áp dụng repo gốc; hai repo con và database hiện có không bị xóa.

Bộ dữ liệu mới dùng chung một nguồn cho DEMO và SQL. Xem [danh sách tài khoản và kết quả tìm kiếm mẫu](TaiLieu/DU_LIEU_MAU.md).
