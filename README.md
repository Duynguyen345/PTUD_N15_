# Phòng khám nha khoa — Nhóm 15

Nhánh làm việc: **Duy_Tao_Moi**. **Java 21 · Swing · OOP**.

Phiên bản hiện tại đã có đăng nhập, kiểm tra quyền, khung điều hướng, tổng quan, nhân viên, tài khoản, dịch vụ, hàng hóa và báo cáo/CSV. Phân công dưới đây xác định người phụ trách tiếp tục phát triển; không có nghĩa tất cả chức năng của cả nhóm đã được triển khai.

## Phân công nhiệm vụ 4 thành viên

Cập nhật ngày **08/11/2026**: chuyển phần **phân quyền từ Duy sang Thanh**. Duy tiếp tục phụ trách đăng nhập. **Bỏ nhiệm vụ thiết kế, viết và quản lý SQL schema/migration khỏi phân công cả nhóm.**

| Thành viên | Phạm vi phụ trách |
|---|---|
| **Nguyễn Tuấn Duy** | Khung ứng dụng, màn hình chính/tổng quan, đăng nhập và đăng xuất (phần xác thực UC001); quản lý nhân viên và tài khoản (UC010); danh mục dịch vụ (UC007); hàng hóa/tồn kho; báo cáo lượt khám và xuất CSV (UC008). |
| **Hoàng Đình Huy** | Luồng lễ tân: quản lý hồ sơ bệnh nhân (UC002), thu phí khám (UC005), tiếp nhận/lập phiếu khám, cấp số thứ tự và in phiếu (UC003); thanh toán dịch vụ cuối lượt. |
| **Huỳnh Quốc Thắng** | Luồng bác sĩ (UC004): nhận danh sách khám, xem tiền sử/hồ sơ bệnh án, ghi chẩn đoán, chỉ định dịch vụ, cấp thuốc, lưu kết quả, hoàn tất lượt khám và xem lịch sử khám. |
| **Phan Chí Thanh** | **Phân quyền**: quy định quyền theo vai trò, kiểm tra quyền tại BUS, phối hợp hiển thị menu/chức năng theo quyền; hàng chờ, gọi số và xác nhận vào khám (UC009); bật/tắt phòng nhận bệnh; tra cứu hồ sơ bệnh nhân (UC006); bộ đếm cấp mã/STT dùng chung. |

### Ranh giới đăng nhập, tài khoản và phân quyền

- **Duy:** xác thực mã tài khoản/mật khẩu, kiểm tra tài khoản bị khóa hoặc nhân viên nghỉ việc, tạo/hủy phiên đăng nhập; thêm/cập nhật nhân viên, tạo và khóa/mở tài khoản.
- **Thanh:** xác định mỗi vai trò được thực hiện chức năng nào, chặn thao tác không đủ quyền tại BUS và phối hợp với khung giao diện của Duy để hiển thị menu tương ứng.
- **UC006 tra cứu hồ sơ thuộc Thanh**, không thuộc Huy. Thanh sử dụng dữ liệu bệnh nhân và phiếu khám do luồng của Huy cung cấp.

Mỗi thành viên phụ trách GUI → BUS → DAO, kiểm thử và tài liệu cho phần việc của mình. Không giao đầu việc SQL schema/migration cho thành viên nào.

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

## Kiến trúc OOP

```text
UngDung: khởi tạo, truyền dependency
GUI Swing → BUS → DAO
                    └─ PhongKhamDuLieuMauDAO → bộ nhớ (chế độ demo)
```

Chỉ giữ 9 entity/enum/record cần cho Duy: NhanVien, TaiKhoan, ChucVu, PhienDangNhap, DichVu, TrangThaiTaiKhoan, TrangThaiDichVu, HangHoa, DuLieuBaoCao. Đã bỏ 10 entity ngoài phạm vi. Phiên đăng nhập không dùng biến static. GUI không chứa SQL. Mỗi tác vụ IO chạy trên SwingWorker; BUS kiểm tra dữ liệu/quyền; DAO dùng PreparedStatement, transaction và đóng tài nguyên.

## Kiểm thử

```powershell
mvn test
```

Nếu Maven đang trỏ sai cache:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

Bộ test kiểm tra nghiệp vụ, render màn hình và thao tác Swing.

Chi tiết luồng, cách áp dụng OOP và kịch bản demo: [TaiLieu/DUY_SWING_OOP.md](TaiLieu/DUY_SWING_OOP.md).

## Mã tài khoản tự sinh

Khi tạo tài khoản, chỉ chọn nhân viên và nhập mật khẩu. Hệ thống cấp mã theo dạng TKyyMMddNNNN trong cùng transaction với INSERT, kiểm tra trùng và trả mã về giao diện. Ô mã chỉ đọc; không có tên đăng nhập tự đặt. Đăng nhập bằng mã TK được cấp. Seed demo cũng cấp mã từ bộ đếm, với ngày mô phỏng 01/01/2026.

Bộ dữ liệu mới dùng chung một nguồn cho DEMO và SQL. Xem [danh sách tài khoản và kết quả tìm kiếm mẫu](TaiLieu/DU_LIEU_MAU.md).
