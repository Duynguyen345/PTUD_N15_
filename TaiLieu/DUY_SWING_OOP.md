# Phần Duy — Swing, OOP và JDBC

Phân công cập nhật ngày 08/11/2026: Duy phụ trách xác thực đăng nhập, đăng xuất và quản lý tài khoản; **Thanh phụ trách phân quyền** tại BUS và phối hợp menu theo vai trò. Bỏ nhiệm vụ SQL schema/migration khỏi phân công cả nhóm. Bảng dưới mô tả chức năng hiện có, không phải giao toàn bộ phần phân quyền cho Duy. Xem [phân công 4 thành viên](../README.md#phân-công-nhiệm-vụ-4-thành-viên).

## Phạm vi màn hình

| Màn hình | Luồng đã triển khai |
|---|---|
| Đăng nhập | Kiểm tra rỗng/sai mật khẩu/khóa/nghỉ việc, xác định vai trò từ DB, kiểm tra kết nối, hướng dẫn quên mật khẩu |
| Khung điều hướng | Một JFrame, sidebar theo quyền, họ tên/vai trò, đăng xuất hủy phiên và màn cũ |
| Tổng quan | Số nhân viên, số dịch vụ, hàng cần bổ sung |
| Nhân viên | Tìm kiếm, thêm/sửa, kiểm 18 tuổi/SĐT/CCCD, chức vụ, trạng thái làm việc, xóa có ràng buộc |
| Tài khoản | Chọn nhân viên chưa có tài khoản, nhập mật khẩu, nhận mã TK tự sinh, khóa/mở; cấm tự khóa |
| Dịch vụ | Tìm kiếm, thêm/sửa tên/giá, trạng thái phục vụ; không xóa cứng |
| Hàng hóa | Tên/đơn vị/tồn/ngưỡng, cảnh báo tồn <= ngưỡng |
| Báo cáo | Khoảng ngày, hai biểu đồ/bảng, CSV UTF-8 BOM, xác nhận ghi đè |

Vai trò Lễ tân/Bác sĩ/Phụ tá đăng nhập được nhưng chỉ thấy thông báo phạm vi; các màn nghiệp vụ của Huy/Thắng/Thanh không nằm trong lần triển khai này. Hàng hóa là QT-07 bổ sung của Duy, chưa có sổ nhập/xuất hoặc AI. Báo cáo không có doanh thu. Quên mật khẩu chỉ hướng dẫn liên hệ quản trị viên, chưa có luồng đặt lại tự động.

## Cách áp dụng OOP

`UngDung` là nơi khởi tạo và truyền dependency qua constructor:

```text
GUI đăng nhập → DangNhapBUS → DangNhapDAO
GUI nhân viên → NhanVienBUS → NhanVienDAO
GUI tài khoản → TaiKhoanBUS → TaiKhoanDAO
GUI dịch vụ   → DichVuBUS   → DichVuDAO
GUI hàng hóa  → HangHoaBUS  → HangHoaDAO
GUI báo cáo   → BaoCaoBUS   → BaoCaoDAO
DAO từng luồng → JdbcDAO → NguonKetNoi → KetNoiCSDL → SQL Server
```

`UngDung` khởi tạo trực tiếp từng DAO với cùng nguồn kết nối SQL Server. Ứng dụng không có chế độ demo; dữ liệu giả lập và `BoDAO` chỉ nằm trong `src/test` để kiểm thử. Mỗi BUS chỉ nhận DAO của luồng mình; `QuanLyPhienDangNhap` nhận `DangNhapDAO`. SQL nghiệp vụ nằm trong từng DAO, còn `JdbcDAO` chỉ giữ các hàm hỗ trợ kết nối, truy vấn, transaction và sinh mã.

- Không có session/connection static.
- `QuanLyPhienDangNhap` quản lý phiên của một cửa sổ. BUS kiểm tra quyền quản lý và trạng thái tài khoản/nhân viên hiện hành trong kho dữ liệu.
- Chỉ giữ entity cần cho phần Duy; `TaiKhoan` dùng thuộc tính `maTaiKhoan`. `HangHoa`, `DuLieuBaoCao` là record bất biến.
- `ManHinhCoSo` chia sẻ phần trình bày và form; `BangDuLieu<T>` tái sử dụng bảng có tìm kiếm/sắp xếp; mỗi màn giữ trách nhiệm riêng.
- GUI không gọi DAO hoặc viết SQL. GUI đọc form trên EDT, gọi service trong `SwingWorker`, cập nhật component lại trên EDT.
- Kho dữ liệu chỉ xử lý lưu trữ. JDBC dùng PreparedStatement, try-with-resources, transaction và rollback.
- Bộ đếm `DuyBoDem` khóa `UPDLOCK,HOLDLOCK` trong cùng transaction với INSERT. Có kiểm tra mã đã có từ seed, không dùng MAX+1. Các trình ghi khác phải dùng cùng cơ chế hoặc phối hợp cấp mã khi tích hợp.
- Mật khẩu mới: PBKDF2-HMAC-SHA256, salt ngẫu nhiên riêng từng tài khoản. Hash SHA-256 cũ được chấp nhận cho seed; không tự đổi hash cũ.

## Ý nghĩa báo cáo

- SQL `COUNT_BIG(*) GROUP BY ngayKham`: số phiếu đã lập, gồm CHO_KHAM/DANG_KHAM/HOAN_TAT; không phải số bệnh nhân duy nhất.
- Tần suất dịch vụ đếm bản ghi ChiDinhDichVu theo ngày phiếu; không phải doanh thu.
- Khoảng ngày gồm cả hai đầu. Mặc định hôm nay và 6 ngày trước = 7 ngày lịch.
- Ngày không có phiếu không hiện một dòng 0. Không có dữ liệu vẫn xem và xuất CSV được.
- CSV xuất snapshot của lần Xem thành công, ghi khoảng ngày trong file; thay ngày trên form không tự chạy truy vấn mới.
- CSV có BOM, dấu `;`, ngày dd/MM/yyyy, escape dấu nháy/xuống dòng và chặn tên dịch vụ được Excel hiểu thành công thức.

## Kịch bản nghiệm thu

1. Cấu hình `db.properties`, chạy `run.ps1` và đăng nhập tài khoản quản lý trong database. Nếu đã nạp seed SQL vào database thử nghiệm riêng, dùng `TK2601010001 / 123456`. Dữ liệu thay đổi được lưu trong SQL Server.
2. Nhân viên: thử SĐT sai, dưới 18 tuổi; thêm hợp lệ, chọn dòng sửa; thử xóa nhân viên có tài khoản.
3. Tài khoản: tạo cho nhân viên vừa thêm; thử tạo lần nữa cho nhân viên đã có tài khoản; khóa, đăng xuất và thử đăng nhập tài khoản đó.
4. Với database đã nạp seed kiểm thử, đăng nhập `TK2601010002 / 123456`: không có menu quản lý. Các kiểm thử gọi trực tiếp BUS cũng phải bị từ chối.
5. Dịch vụ: thử giá 0/âm/chữ, tên trùng; thêm hợp lệ rồi đổi trạng thái ngừng phục vụ.
6. Hàng hóa: nhập tồn = ngưỡng, phải hiện Cần bổ sung; tăng tồn lớn hơn ngưỡng, phải hiện Đủ hàng.
7. Báo cáo: xem 7 ngày; chọn từ > đến; chọn ngày năm 1900; xuất CSV và kiểm tra tiếng Việt/cột/ngày.
8. Đăng xuất và đăng nhập lại: không quay về màn cũ bằng điều hướng.

## Kiểm thử tự động

- `NghiepVuQuanLyTest`: xác thực, phân quyền, validation, CRUD, khóa/mở theo dữ liệu mới nhất, tìm nhân viên đủ điều kiện tại BUS, tham chiếu và CSV.
- `ManHinhSwingTest`: nạp/render 7 màn hình và chạy tác vụ nền; ảnh ở `target/ui-preview`.
- `LuongGiaoDienTest`: đăng nhập qua sự kiện Swing, điều hướng và thêm dịch vụ từ form; ảnh khung đầy đủ ở `target/workspace-preview.png`.
- `TichHopJdbcTest`: chạy khi có `-Dduy.jdbc.config=<file>`; chỉ chấp nhận database tên `N15_DUY_TEST_*`. Bao gồm CRUD SQL thật, khóa/mở, PBKDF2, chống xóa tham chiếu, hai luồng tạo dịch vụ đồng thời, báo cáo từ seed. Dùng database kiểm thử riêng và dọn sau khi chạy.

Lệnh thông thường: `mvn test`. Không có cấu hình riêng thì test JDBC được bỏ qua có thông báo; không tự ghi vào database phòng khám.

## Thay đổi mã tài khoản

Mã đăng nhập là TKyyMMddNNNN do DAO cấp trong transaction bằng DuyBoDem; BUS không nhận tham số tên đăng nhập tự đặt. GUI chỉ hiển thị mã và thông báo mã sau khi tạo thành công. DangNhapBUS từ chối tên dạng quanly/bacsi1. Database cũ dùng script 030_account_ids.sql; không giữ alias đăng nhập cũ. Seed SQL kiểm thử dùng bộ đếm ngày mô phỏng 01/01/2026, tài khoản quản lý đầu tiên là TK2601010001.

## Trạng thái danh sách trước khi tìm kiếm

- Các màn Nhân viên, Tài khoản, Dịch vụ, Hàng hóa mở ra với bảng trống; không gọi tải danh sách khi mở màn hình.
- Nhập từ khóa rồi bấm Tìm kiếm hoặc Enter. Từ khóa rỗng không tải dữ liệu.
- Đổi/xóa từ khóa hoặc bấm Xóa tìm kiếm sẽ xóa kết quả cũ. Không có kết quả thì bảng vẫn trống.
- Sau thao tác lưu/xóa/đổi trạng thái, bảng trở về trạng thái chờ tìm kiếm.
- Chọn nhân viên tạo tài khoản cũng bắt đầu trống: nhập mã/tên và bấm Tìm nhân viên để lấy những người đủ điều kiện.
- Tổng quan vẫn hiển thị số liệu tổng hợp; báo cáo chỉ nạp khi bấm Xem báo cáo.
- Thay đổi này xử lý trạng thái giao diện. Tìm kiếm hiện lọc trên danh sách được tải theo yêu cầu; phân trang và truy vấn tìm kiếm trực tiếp bằng SQL chưa được triển khai.
