# Tích hợp Thanh và Huy — 09/11/2026

## Bản gốc và nguồn

- Bản gốc: toàn bộ working tree tại `D:\PTUD_N15_`, nhánh `Duy_Tao_Moi`, HEAD `ccd2779`, bao gồm thay đổi chưa commit.
- Thanh: `origin/master`, commit `4088c4c91b0ee5ce8ff448223db37e3fc3f2551e`.
- Huy: `origin/dev/letan_dinhhuy`, commit `767ba2ceb991e7d4cf77f1a3e7479068a9d4f39b`.
- Sao lưu: `merge-backup-20261109-164804/working-tree.zip`, kèm `tracked.patch`, `HEAD.txt` và archive hai nhánh nguồn. Thư mục này được bỏ qua bởi Git. Giữ lại để đối chiếu/khôi phục; không giải nén đè khi đang có thay đổi mới.
- Tích hợp nội dung vào working tree, chưa tạo merge commit, chưa push.

## Phần tích hợp

Điều chỉnh theo yêu cầu: vai trò QUAN_LY chỉ có QUAN_TRI (tổng quan, nhân viên/tài khoản, dịch vụ, hàng hóa, báo cáo/CSV). Không hiện hoặc cấp quyền hàng chờ, phòng nhận bệnh, tra cứu hồ sơ cho vai trò này. Module Thanh vẫn dùng cho các vai trò được phân quyền tương ứng; module lễ tân vẫn thuộc LE_TAN.

- Thanh: chính sách quyền theo vai trò, kiểm tra lại tài khoản/nhân viên tại BUS, hàng chờ/gọi lại/vắng mặt/xác nhận vào khám, bật/tắt nhận bệnh, bảng gọi số, tra cứu hồ sơ/lịch sử khám.
- Huy: hồ sơ bệnh nhân, nhận ca/chốt ca, thu phí khám, lập/in phiếu khám, cấp STT và thanh toán dịch vụ. Nối chuyển màn hình hồ sơ → thu tiền → lập phiếu. Menu tra cứu dùng UC006 của Thanh; màn tra cứu riêng của Huy vẫn giữ trong source.
- Giữ cấu trúc `src/main/{bus,dao,entity,gui,...}`, DAO quản lý tách riêng, đăng nhập SQL Server, cấu hình kết nối và script chạy hiện tại. Kho giả lập chỉ nằm trong `src/test`. Các mô tả 6 DAO/9 entity trong tài liệu Duy chỉ áp dụng phần quản lý, không bao gồm module mới.
- Thống nhất BenhNhan, giữ CCCD/ghi chú tiền sử-dị ứng của Huy và giới tính/tiền sử/dị ứng riêng của Thanh. Tra cứu chấp nhận ngày sinh trống và đọc ghi chú lễ tân; cập nhật lễ tân không ghi đè các cột lâm sàng riêng của Thanh.
- Lập phiếu sau thu phí ghi `daThanhToanPhiKham=1` trong cùng transaction. Cấp STT lấy tối đa giữa bộ đếm và phiếu hiện có, dùng khóa phòng chung với điều phối.
- Đăng nhập thất bại hủy phiên cũ. Test dữ liệu thiếu đăng nhập lại trước khi kiểm tra thao tác quản lý cần quyền.
- Không đưa script reset database riêng, tài khoản mẫu, cấu hình SQL Server hoặc layout tổng thể cũ đè lên bản hiện tại. Bản nguồn đầy đủ nằm trong archive sao lưu.

## Chuẩn bị SQL Server

Trên database có schema gốc của Duy, chọn đúng database trong SSMS và chạy:

1. `DATA/schema/040_le_tan.sql`.
2. `DATA/schema/040_thanh_dieu_phoi.sql`.
3. `DATA/schema/050_tich_hop_thanh_huy.sql`.

Script lễ tân bổ sung cột thiếu khi bảng bệnh nhân/phòng có sẵn từ Thanh. Script 050 thống nhất trường và đánh dấu phiếu có hóa đơn phí khám hợp lệ. Không xóa bảng hoặc reset dữ liệu. Unique index điện thoại/CCCD/STT sẽ báo lỗi nếu dữ liệu cũ trùng; cần đối chiếu trước khi chạy tiếp, không tự xóa bản ghi.

Lễ tân cần phí khám `DV0001` đang phục vụ và phòng có bác sĩ, phụ tá còn làm việc đúng vai trò (`PhongKham.maBacSi`, `maPhuTa`), bật nhận bệnh. Migration không tự phân công nhân viên hoặc nạp dữ liệu mẫu. Phòng cũ của Thanh chưa có phụ tá sẽ chưa cho lập phiếu đến khi được phân công.

Chưa áp dụng migration hoặc thao tác trên database thật trong lần tích hợp này. Luồng bác sĩ của Thắng không thuộc hai nhánh được yêu cầu; thanh toán cuối lượt cần phiếu đã `HOAN_TAT` và có chỉ định dịch vụ.

## Kiểm thử

Chạy trong thư mục build riêng để tránh VS Code ghi chung class:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" "-Dbuild.output=target/merge-verify" test
```

Bộ test gồm quản lý cũ, nghiệp vụ lễ tân/điều phối, quyền sau khóa tài khoản/đổi vai trò, gọi số đồng thời và Swing. Ảnh ở `target/thanh-preview` và `target/ui-preview-letan`.

`TichHopThanhHuyJdbcTest` chỉ chạy khi truyền `-Dmerge.jdbc.config=<file>`, yêu cầu database riêng tên bắt đầu `N15_MERGE_TEST_`, đã có schema gốc và ba migration trên. Test thu phí → lập phiếu → tra cứu → gọi số → vào khám trong transaction ngoài cùng rồi rollback. Không dùng database đang làm việc để kiểm thử.

Test SQL nguyên bản của Thanh phụ thuộc bộ reset/seed riêng được giữ trong backup; test JDBC kết hợp thay thế hợp đồng đó. Chưa cung cấp database test nên hai test JDBC được bỏ qua. Kết quả unit/Swing không chứng minh migration chạy thành công trên SQL Server thật.
