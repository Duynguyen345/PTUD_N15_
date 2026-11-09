# Bộ dữ liệu mẫu Duy — phiên bản đồng bộ

Đây là dữ liệu giả lập. Nguồn duy nhất: `src/test/resources/demo/dataset.tsv`.
Bộ kiểm thử nạp trực tiếp file này; `java DATA/GenerateDemoSeed.java` sinh script SQL từ cùng nguồn. Ứng dụng chỉ đọc/ghi SQL Server, không có chế độ dữ liệu trong bộ nhớ.

## Số lượng

| Dữ liệu | Số lượng |
|---|---:|
| Nhân viên | 10 |
| Tài khoản | 6 |
| Dịch vụ | 8 (6 phục vụ, 2 ngừng phục vụ) |
| Hàng hóa | 8 (4 cần bổ sung) |
| Phiếu khám | 14, mỗi ngày 2 phiếu trong 7 ngày gần nhất |
| Chỉ định dịch vụ | 28 |

Bộ kiểm thử chốt ngày tham chiếu khi khởi tạo; SQL chốt ngày khi chạy seed. Test tích hợp đối chiếu báo cáo cần seed được nạp trong cùng ngày chạy test.

## Tài khoản đăng nhập

Mật khẩu thử nghiệm: **123456**. Mã được cấp tuần tự từ bộ đếm với ngày mô phỏng 01/01/2026.

| Mã tài khoản | Nhân viên | Vai trò | Kết quả đăng nhập |
|---|---|---|---|
| TK2601010001 | Lê Hoàng Minh | Quản lý | Thành công, đủ menu quản lý |
| TK2601010002 | Nguyễn Thị Anh | Lễ tân | Thành công, không có menu quản lý |
| TK2601010003 | Nguyễn Văn An | Bác sĩ | Thành công, không có menu quản lý |
| TK2601010004 | Trần Minh Bình | Phụ tá | Thành công, không có menu quản lý |
| TK2601010005 | Phạm Thu Hà | Bác sĩ | Bị khóa |
| TK2601010006 | Võ Quốc Huy | Lễ tân | Đã nghỉ việc, bị từ chối |

Đặng Ngọc Mai, Bùi Đức Tâm, Lê Minh Bình đang làm việc và chưa có tài khoản: dùng để thử chức năng tạo tài khoản. Hoàng Gia Bảo đã nghỉ và chưa có tài khoản, không được chọn để tạo tài khoản.

## Tìm kiếm nhân viên

| Từ khóa | Kết quả ban đầu |
|---|---|
| Bình | Trần Minh Bình, Lê Minh Bình |
| An | Nguyễn Văn An, Nguyễn Thị Anh (tìm chứa chuỗi) |
| Nguyễn Văn An | 1 kết quả |
| 0901234567 | Nguyễn Văn An |
| 0987654321 | Trần Minh Bình |
| 079000000003 | Nguyễn Văn An qua CCCD |
| Đang làm | 0, không tìm trên trạng thái |
| Phụ tá | 0, không tìm trên chức vụ |

Các bảng vẫn trống trước khi nhập từ khóa và bấm Tìm kiếm. Tìm không dấu chưa được hỗ trợ.

## Hàng hóa và báo cáo

Găng tay y tế (8/10), Gạc vô trùng (0/5), Vật liệu trám (5/5) và Dung dịch sát khuẩn (4/6) cần bổ sung. Bốn hàng còn lại trên ngưỡng.

Báo cáo 7 ngày có 14 lượt khám và 28 chỉ định: Khám ban đầu 14, Lấy cao răng 5, Trám răng thẩm mỹ 3, Nhổ răng thường 2, Chụp X-quang răng 3, Tẩy trắng răng 1. Hôm nay có 2 lượt/4 chỉ định. Không tính doanh thu.

Báo cáo SQL dùng JOIN/GROUP BY với tên dịch vụ hiện hành; sửa tên danh mục sẽ đổi tên hiển thị trong báo cáo, không đổi số lần chỉ định.

## Nạp lại từ đầu

- SQL: chọn database demo `N15_DUY_DB` đã có schema hiện tại, chạy `DATA/seed/001_duy_demo.sql`. **Script này xóa dữ liệu cũ trong 7 bảng của Duy và nạp lại toàn bộ bộ mẫu**, trong một transaction. Chạy lại không cộng dồn bản ghi.
- Script từ chối database khác ngoài `N15_DUY_DB` và `N15_DUY_TEST_*`. Không dùng trên database nghiệp vụ đầy đủ của nhóm; các ràng buộc ngoài phạm vi có thể khiến reset bị rollback.
- Nếu sửa TSV: chạy lại `java DATA/GenerateDemoSeed.java`, sau đó chạy test và nạp lại seed vào database kiểm thử riêng để dữ liệu khớp nhau.

Dữ liệu giả lập trong `src/test` không được đóng gói vào ứng dụng. Không tự nạp seed khi khởi động.
