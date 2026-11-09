# Phòng khám nha khoa — Nhóm 15

Nhánh làm việc: **Duy_Tao_Moi**. **Java 21 · Swing · OOP**.

Phiên bản hiện tại đã có đăng nhập, kiểm tra quyền, khung điều hướng, tổng quan, nhân viên, tài khoản, dịch vụ, hàng hóa và báo cáo/CSV. Phân công dưới đây xác định người phụ trách tiếp tục phát triển; không có nghĩa tất cả chức năng của cả nhóm đã được triển khai.

## Tích hợp Thanh và Huy — 09/11/2026

Đã tích hợp điều phối/tra cứu/phân quyền của Thanh (`master`) và lễ tân của Huy (`dev/letan_dinhhuy`) vào cấu trúc hiện tại. Giữ package ngắn, các DAO riêng của Duy và chế độ SQL Server.

Database hiện tại cần chạy lần lượt `DATA/schema/040_le_tan.sql`, `DATA/schema/040_thanh_dieu_phoi.sql`, `DATA/schema/050_tich_hop_thanh_huy.sql` trước khi dùng chức năng mới. Các migration chưa được chạy tự động. Xem [biên bản tích hợp và kiểm thử](TaiLieu/MERGE_THANH_HUY.md).

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

## Chạy với SQL Server

Ứng dụng chỉ sử dụng database thật, không còn chế độ `--demo`.

1. Chuẩn bị database có schema tương thích. Với database mới riêng cho phần Duy, chạy `DATA/schema/001_fresh_database.sql` trong SSMS. Script này tạo/chọn `N15_DUY_DB`; không chạy lại trên database đã có bảng.
2. Sao chép `src/main/resources/db.properties.example` thành `db.properties` tại thư mục gốc, điền địa chỉ SQL Server và tài khoản kết nối.
3. Chạy `./run.ps1`, đăng nhập bằng tài khoản đã có trong database.

Database cũ: xem `DATA/schema/020_duy_upgrade.sql` và `030_account_ids.sql` trước khi nâng cấp. Dữ liệu mẫu SQL chỉ dành cho database thử nghiệm; hướng dẫn nạp và cảnh báo reset ở [TaiLieu/DU_LIEU_MAU.md](TaiLieu/DU_LIEU_MAU.md).

`run.ps1` dùng cache Maven tại `$env:USERPROFILE\.m2\repository`. Có thể chạy bằng Maven:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" compile exec:java
```

## Kiến trúc OOP

Mã nguồn đặt trực tiếp dưới `src/main`, dùng package ngắn theo tầng:

```text
src/main/
├── UngDung.java
├── gui/
├── bus/
├── dao/
├── entity/
├── connect/
├── util/
└── resources/
```

Ví dụ: `package dao;`, `import bus.DangNhapBUS;`. Lớp chạy chính là `UngDung`; cấu trúc kiểm thử tương ứng nằm dưới `src/test`. Maven đã cấu hình hai thư mục nguồn này, tách `resources` khỏi phần biên dịch Java.

```text
UngDung: khởi tạo từng DAO bằng KetNoiCSDL, truyền vào BUS tương ứng
GUI đăng nhập → DangNhapBUS → DangNhapDAO
GUI nhân viên → NhanVienBUS → NhanVienDAO
GUI tài khoản → TaiKhoanBUS → TaiKhoanDAO
GUI dịch vụ   → DichVuBUS   → DichVuDAO
GUI hàng hóa  → HangHoaBUS  → HangHoaDAO
GUI báo cáo   → BaoCaoBUS   → BaoCaoDAO
```

Mỗi DAO chứa SQL của luồng mình; `JdbcDAO` chỉ chia sẻ hạ tầng JDBC. `QuanLyPhienDangNhap` dùng `DangNhapDAO` để kiểm tra lại tài khoản/nhân viên. Thư mục DAO của ứng dụng chỉ có 6 DAO chức năng, `JdbcDAO` và `NgoaiLeTruyCapDuLieu`. Các lớp giả lập và bộ khởi tạo kiểm thử chỉ nằm trong `src/test`, không được đóng gói vào ứng dụng.

Chỉ giữ 9 entity/enum/record cần cho Duy: NhanVien, TaiKhoan, ChucVu, PhienDangNhap, DichVu, TrangThaiTaiKhoan, TrangThaiDichVu, HangHoa, DuLieuBaoCao. Đã bỏ 10 entity ngoài phạm vi. Phiên đăng nhập không dùng biến static. GUI không chứa SQL. Mỗi tác vụ IO chạy trên SwingWorker; BUS kiểm tra dữ liệu/quyền; DAO dùng PreparedStatement, transaction và đóng tài nguyên.

## Kiểm thử

```powershell
mvn test
```

Nếu Maven đang trỏ sai cache:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

Bộ test nghiệp vụ và Swing dùng dữ liệu giả lập trong `src/test`. Test SQL thật chỉ chạy khi truyền `-Dduy.jdbc.config=<file>` và chỉ chấp nhận database `N15_DUY_TEST_*`; khi chưa có cấu hình, test này được bỏ qua.

Chi tiết luồng, cách áp dụng OOP và kịch bản nghiệm thu: [TaiLieu/DUY_SWING_OOP.md](TaiLieu/DUY_SWING_OOP.md).

## Mã tài khoản tự sinh

Khi tạo tài khoản, chỉ chọn nhân viên và nhập mật khẩu. Hệ thống cấp mã theo dạng TKyyMMddNNNN trong cùng transaction với INSERT, kiểm tra trùng và trả mã về giao diện. Ô mã chỉ đọc; không có tên đăng nhập tự đặt. Đăng nhập bằng mã TK được cấp. Seed SQL kiểm thử cấp mã từ bộ đếm với ngày mô phỏng 01/01/2026.

Bộ dữ liệu kiểm thử và seed SQL dùng chung nguồn `src/test/resources/demo/dataset.tsv`. Xem [danh sách tài khoản và kết quả tìm kiếm mẫu](TaiLieu/DU_LIEU_MAU.md).
