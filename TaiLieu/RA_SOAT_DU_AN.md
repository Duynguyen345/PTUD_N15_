# Rà soát tính nhất quán — 09/11/2026

Đã đối chiếu mã khởi động, GUI, BUS, DAO, entity, kết nối, tiện ích, cấu hình Maven, script chạy, schema/seed, mã kiểm thử và tài liệu trong repository. Đây là rà soát mã nguồn và kiểm thử cục bộ; chưa phải nghiệm thu trên SQL Server thật.

## Các điểm đã sửa

| Điểm không nhất quán | Cách xử lý |
|---|---|
| DAO ứng dụng chứa cả lớp phục vụ SQL lẫn demo, thêm bộ khởi tạo trung gian | Gỡ chế độ demo; `UngDung` khởi tạo trực tiếp 6 DAO chức năng. DAO ứng dụng còn 8 file, gồm 6 DAO, `JdbcDAO` và ngoại lệ truy cập dữ liệu. |
| Tham số demo đi xuyên khung chính và màn đăng nhập nhưng không có tác dụng hiển thị | Bỏ tham số, trường và nhánh demo; bỏ `-Demo` khỏi script chạy. |
| Test phụ thuộc vào demo trong mã ứng dụng | Đưa bộ dữ liệu, DAO giả lập và bộ khởi tạo vào `src/test`. Chúng không nằm trong JAR phát hành. |
| Màn tài khoản tự quyết định nhân viên đang làm việc/chưa có tài khoản | Chuyển điều kiện này sang `TaiKhoanBUS.timNhanVienChuaCoTaiKhoan`; GUI chỉ lấy từ khóa và hiển thị kết quả. |
| Chặn tự khóa dựa vào mã nhân viên của đối tượng từ GUI | Đọc tài khoản hiện hành qua DAO trước khi kiểm tra, ngăn đối tượng cũ hoặc bị thay mã nhân viên vượt qua điều kiện. |
| Kiểm tra phiên chưa đối chiếu quan hệ tài khoản–nhân viên hiện hành | Đối chiếu tài khoản còn thuộc nhân viên trong phiên, bên cạnh trạng thái hoạt động và chức vụ. |
| Đăng nhập và kiểm tra quyền tải toàn bộ nhân viên để tìm một người | Thay bằng `DangNhapDAO.timNhanVien` có điều kiện mã nhân viên trong SQL. |
| Dữ liệu null ở một số BUS gây lỗi kỹ thuật; CCCD nullable trong SQL nhưng bị gọi trim trực tiếp | Thêm thông báo nghiệp vụ cho dữ liệu thiếu, chuẩn hóa CCCD null thành rỗng, kiểm tra mã khi cập nhật/xóa. |
| Biến báo cáo có tên `DuLieuTaiKhoan`, comment phiên trỏ tới lớp cũ, comment mật khẩu chỉ ghi SHA-256 | Đổi tên biến và sửa comment theo lớp/định dạng đang dùng. |
| Thư mục tài nguyên CSS/FXML còn sót dù ứng dụng dùng Swing | Bỏ các file giữ chỗ không dùng. |
| README và hướng dẫn còn chỉ chạy demo, nguồn TSV không khớp sau khi chuyển | Cập nhật cách chạy SQL Server, vị trí dữ liệu test, công cụ sinh seed và ranh giới kiểm thử. |

## Cấu trúc ứng dụng

| Chức năng | BUS | DAO |
|---|---|---|
| Đăng nhập | DangNhapBUS | DangNhapDAO |
| Nhân viên | NhanVienBUS | NhanVienDAO |
| Tài khoản | TaiKhoanBUS | TaiKhoanDAO |
| Dịch vụ | DichVuBUS | DichVuDAO |
| Hàng hóa | HangHoaBUS | HangHoaDAO |
| Báo cáo | BaoCaoBUS | BaoCaoDAO |

`QuanLyPhienDangNhap` dùng DAO đăng nhập để kiểm tra lại quyền. Tổng quan phối hợp các BUS nhân viên, dịch vụ và hàng hóa. `JdbcDAO` giữ thao tác JDBC dùng chung, không gom lại các luồng nghiệp vụ.

## Kiểm chứng

- Biên dịch toàn bộ 42 file Java của ứng dụng bằng Java 21 vào thư mục kiểm chứng riêng thành công.
- 26 kiểm thử: 25 đạt, 1 kiểm thử tích hợp SQL được bỏ qua vì thiếu `duy.jdbc.config`.
- Các test mới bao phủ đối tượng tài khoản sai thông tin nhân viên, điều kiện tạo tài khoản tại BUS và dữ liệu đầu vào thiếu.
- Đóng gói JAR thành công; kiểm tra không chứa `BoDAO`, DAO giả lập, `PhongKhamDAO` cũ hoặc dataset demo.
- Công cụ sinh seed chạy được với vị trí TSV mới; không chạy seed vào database.
- Kiểm tra diff không có lỗi khoảng trắng.

## Giới hạn và phần chưa triển khai

- Chưa kiểm chứng kết nối, CRUD, giao dịch, khóa đồng thời hoặc migration trên SQL Server thực tế. Cần database kiểm thử riêng `N15_DUY_TEST_*` và cấu hình `-Dduy.jdbc.config=<file>` để chạy test tích hợp.
- Các luồng lễ tân, bác sĩ, hàng chờ và tra cứu hồ sơ trong bảng phân công chưa được triển khai trong mã nguồn hiện tại; tài liệu phân công không đồng nghĩa các luồng đã hoàn thành.
- Tìm kiếm bảng vẫn tải danh sách khi người dùng yêu cầu rồi lọc tại giao diện; chưa có phân trang/truy vấn tìm kiếm SQL.
- Hàng hóa mới quản lý tồn hiện tại/ngưỡng cảnh báo, chưa có sổ nhập xuất. Báo cáo thống kê lượt khám/chỉ định, chưa có doanh thu. Quên mật khẩu vẫn hướng dẫn liên hệ quản trị.

Ứng dụng hiện cần `db.properties` hợp lệ và tài khoản có sẵn trong database. Seed SQL là công cụ kiểm thử có thao tác reset; không tự chạy khi mở ứng dụng.
