# Dữ liệu mẫu SQL Server cho toàn bộ chức năng hiện có

Chạy duy nhất [DATA/nhakhoa_sqlserver.sql](../DATA/nhakhoa_sqlserver.sql) để tạo database **N15_NHAKHOA_DB**, các bảng/ràng buộc và dữ liệu. Script không xóa dữ liệu cũ; chạy lại chỉ bổ sung mã còn thiếu, không đặt lại trạng thái hay ngày khám.

| Bảng | Số mẫu khi tạo mới | Công dụng |
|---|---:|---|
| NhanVien | 6 | Quản lý, lễ tân, bác sĩ, phụ tá |
| TaiKhoan | 6 | Đăng nhập và kiểm tra tài khoản bị khóa |
| DichVu | 6 | Danh mục và thống kê dịch vụ |
| HangHoa | 6 | Tồn kho, cảnh báo cần bổ sung |
| DuyBoDem | 6 | Bộ đếm NV, TK, DV, HH, BN, PK |
| BenhNhan | 6 | Thông tin, tiền sử và dị ứng |
| PhongKham | 6 | Bác sĩ phụ trách và trạng thái nhận bệnh |
| PhieuKham | 12 | 6 lượt hôm nay và 6 lượt cũ đã hoàn tất |
| HoSoKham | 6 | Chẩn đoán, điều trị, ghi chú của 6 lượt khám cũ |
| ChiDinhDichVu | 6 | Dịch vụ được chỉ định trong các lượt khám cũ |

Không phải mỗi lớp Java có một bảng riêng. `LuotChoKham` và `LichSuKham` là kết quả truy vấn các bảng trên; `DuLieuBaoCao` là dữ liệu tổng hợp. Vai trò/quyền/trạng thái là enum cố định, không cần 5–6 mẫu.

## Tài khoản giao diện

Mật khẩu: **123456**. Tài khoản kết nối SQL trong `db.properties` là tài khoản riêng, không dùng các mã TK này để đăng nhập SQL Server.

| Mã tài khoản | Họ tên | Vai trò | Trạng thái |
|---|---|---|---|
| TK2601010001 | Lê Hoàng Minh | Quản lý | Hoạt động |
| TK2601010002 | Nguyễn Thị Anh | Lễ tân | Hoạt động |
| TK2601010003 | Nguyễn Văn An | Bác sĩ | Hoạt động |
| TK2601010004 | Trần Minh Bình | Phụ tá | Hoạt động |
| TK2601010005 | Phạm Thu Hà | Bác sĩ | Hoạt động |
| TK2601010006 | Đặng Ngọc Mai | Lễ tân | Bị khóa |

## Tình huống thử

- Quản lý: xem tổng quan, nhân viên, tài khoản, 6 dịch vụ, 6 hàng hóa và báo cáo 7 ngày.
- Phụ tá: P01 có số 001/002 đủ điều kiện gọi, số 003 chưa trả phí, số 004 vắng mặt. P02 có bệnh nhân đang khám; P03 tạm ngưng. P04/P05 trống, đang nhận bệnh; P06 trống, tạm ngưng.
- Bác sĩ TK2601010003 phụ trách P01/P04/P05; TK2601010005 phụ trách P02/P03/P06. Không được bật/tắt phòng của bác sĩ khác.
- Lễ tân/bác sĩ: tìm `nguyen minh chau`, `BN2601010001` hoặc `0911110001`; chọn phiếu cũ để xem chẩn đoán/điều trị. Cả 6 bệnh nhân đều có lịch sử hoàn tất.
- Lưu dữ liệu: gọi số/đổi trạng thái phòng, đóng/mở lại ứng dụng rồi kiểm tra trạng thái còn nguyên trong SQL Server.

Ngày mẫu là ngày Việt Nam khi nạp lần đầu. Hàng chờ chỉ hiển thị lượt của hôm nay; sau khi qua ngày mới cần tiếp nhận lượt khám mới trong SQL Server, không tự chuyển lượt cũ sang ngày mới.

Các TSV trong `src/test/resources/demo` chỉ dành cho kiểm thử cũ, không được nạp khi chạy ứng dụng.
