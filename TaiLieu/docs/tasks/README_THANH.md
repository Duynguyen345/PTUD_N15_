# NHIỆM VỤ CODE — PHAN CHÍ THANH (24738801) — LUỒNG PHỤ TÁ + HẠ TẦNG CSDL DÙNG CHUNG

> **Tóm tắt:** Thanh gánh 2 phần: (1) luồng phụ tá — theo dõi hàng chờ theo phòng, gọi số, cập nhật bảng số, xác nhận vào khám (UC009); (2) **hạ tầng dùng chung** + **tra cứu hồ sơ bệnh nhân (UC006)**; phần còn lại của lễ tân vẫn giữ nguyên cho Huy.
> **Bắt buộc đọc trước:** [README_RULES.md](../../README_RULES.md). Phần hạ tầng của Thanh là nền móng: làm trước, làm chuẩn, cả nhóm phụ thuộc.

---

## 1. Phạm vi công việc

| Mã | Use case / đầu việc | Màn hình | Điểm nội bộ | Thứ tự làm |
| --- | --- | --- | --- | --- |
| PT-01 | **Hạ tầng DB dùng chung**: `ConnectDB`, `BoDem_DAO`, schema 000 + 001 + seed phòng, hướng dẫn migration & cấu hình máy | — | 6 | 1 (cả nhóm chờ) |
| PT-02 | UC009 — Theo dõi hàng chờ theo phòng | `GD_HangCho` | 4 | 2 |
| PT-03 | UC009 — Gọi số + cập nhật bảng số đang gọi | (trong `GD_HangCho`) | 3 | 3 |
| PT-04 | UC009 — Xác nhận vào khám (chuyển `DANG_KHAM`) | (trong `GD_HangCho`) | 4 | 4 |
| PT-05 | Quản lý phòng khám: bật/tắt nhận bệnh | (trong `GD_HangCho` + menu Quản lý) | 4 | 5 |
| PT-06 | UC006 — Tra cứu hồ sơ bệnh nhân | `GD_TraCuuBenhNhan` | 3 | 6 |
| PT-07 | Kiểm thử, tài liệu, demo | — | 4 | song song |

Tổng 28. Backlog có chủ nhiệm (chốt M0): **PT-08 ghế/thiết bị, vệ sinh phòng, hàng chờ phẫu thuật, hỗ trợ vật tư** — không dùng cờ `dangNhanBenh` giả làm tình trạng thực tế.

## 2. File sở hữu (quy tắc một-chủ)

| Loại | File | Ghi chú |
| --- | --- | --- |
| Hạ tầng | `connect/ConnectDB.java`, `dao/BoDem_DAO.java`, `src/db.properties.example` | cả nhóm dùng; đổi API phải qua PR + review |
| SQL hạ tầng | `SourceCode/DATA/schema/000_hatang.sql` (CREATE DATABASE `N15_PTUD_DB` + collation + bảng `BoDem`), `001_phongkham.sql`, `011_banggoiso.sql` (tùy chọn chốt M0) + seed phòng | |
| Model | `model/PhongKham.java` | |
| DAO | `dao/PhongKham_DAO.java` | Hàng chờ **không tạo DAO riêng** — dùng `PhieuKham_DAO` của Huy |
| BUS | `bus/HangCho_BUS.java`, `bus/PhongKham_BUS.java` | |
| GUI | `gui/GD_HangCho_Controller.java` + `fxml/GD_HangCho.fxml` | |
| Chức năng tra cứu | `gui/GD_TraCuuBenhNhan_Controller.java` + `fxml/GD_TraCuuBenhNhan.fxml` | là phần Thanh phụ trách; Huy giữ phần quản lý hồ sơ, thu phí, lập phiếu |
| Minh chứng | `TaiLieu/docs/evidence/thanh/` | |

**File dùng chéo:** `PhieuKham_DAO` (Huy) — Thanh gọi `layDanhSachTheoNgayVaPhong` và `capNhatTrangThai(maPhieuKham, trangThaiMoi, trangThaiCu)`; thiếu hàm → Issue cho Huy. Huy review PR của Thanh. 
**File dùng chung cho tra cứu:** `model/BenhNhan.java`, `dao/BenhNhan_DAO.java` — Huy chủ sở hữu dữ liệu gốc; Thanh đọc/chạy chức năng tra cứu theo API đã thống nhất, không sửa phần nghiệp vụ khác.

## 3. Chi tiết phần hạ tầng (PT-01 — làm trước mọi người)

### 3.1. `ConnectDB` — nơi duy nhất cấp Connection

```java
public class ConnectDB {
    private static Connection con;
    private static final ConnectDB instance = new ConnectDB();
    public static ConnectDB getInstance() { return instance; }
    public void connect() { /* đọc db.properties → DriverManager.getConnection */ }
    public Connection getConnection() { /* trả con; nếu null/đóng → tự nối lại */ }
}
```

- `db.properties` (đặt `src/`, **không commit** — commit `db.properties.example`):

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=N15_PTUD_DB;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=<máy của bạn>
```

- `connect()` đọc từ properties; cấm hardcode user/password trong code (RULES mục 12.1).
- Viết `main()` test kết nối in 1 dòng "Kết nối N15_PTUD_DB thành công" — mọi thành viên chạy được main này trước khi code.

### 3.2. `BoDem_DAO` — cấp mã tự động nguyên tử (cấm MAX+1/COUNT+1)

```java
public static String layMaMoi(Connection con, String loaiMa, LocalDate ngay)   // con do BUS truyền khi trong transaction
public static int laySoThuTuTiepTheo(Connection con, LocalDate ngay, String maPhong)
```

Cách hiện thực bắt buộc: bảng `BoDem(loaiMa varchar, ngay date, giaTri int, PK(loaiMa, ngay))` — cấp bằng `UPDATE BoDem SET giaTri = giaTri + 1 OUTPUT inserted.giaTri WHERE loaiMa=? AND ngay=?` và dùng `OUTPUT` của SQL Server để tránh race condition.

- Format mã theo RULES mục 5: `NVyyMMddNNNN`, `BNyyMMddNNNN`, `HSyyMMddNNNN`, `PKyyMMddNNNN`, `HDyyMMddNNNN`, `DVNNNN`, `THNNNN`, `PNNN` — `layMaMoi` trả chuỗi hoàn chỉnh.
- Viết `main()` test: gọi 2 lần liên tiếp tăng đúng; gọi từ 2 thread → không trùng.

### 3.3. SQL hạ tầng + quy trình migration

- `000_hatang.sql`: `CREATE DATABASE N15_PTUD_DB` (collation hỗ trợ Unicode tiếng Việt), bảng `BoDem`, (tùy chọn) bảng `BangGoiSo` — **chốt M0**: nếu gọi số cần lưu lại lịch sử gọi.
- `001_phongkham.sql`: `PhongKham(maPhong PK PNNN, tenPhong UNIQUE, dangNhanBenh bit DEFAULT 0)`; seed ≥ 4 phòng ("P001 Phòng khám 1"…, 2 phòng bật nhận bệnh).
- Quy trình migration (Thanh soạn + kiểm duyệt): file đánh số theo phụ thuộc (`000`→`011`, bảng trong từng README thành viên); **file đã chạy chung cấm sửa nội dung**. Cập nhật README của từng thành viên khi thêm schema mới.
- Hướng dẫn cấu hình từng máy (đưa vào `SourceCode/DATA/README.md`): bật TCP/IP 1433 trong SQL Server Configuration Manager + restart; bật SQL auth; lỗi `encrypt=true` thêm `trustServerCertificate=true`.

## 4. Chi tiết chức năng — UC009: Theo dõi hàng chờ (`GD_HangCho`)

**Mục tiêu (đặc tả):** truy xuất danh sách chờ theo phòng khám và STT; danh sách rỗng → "Hiện không có bệnh nhân đang chờ khám"; gọi số; xác nhận vào khám.

**GUI — `GD_HangCho.fxml`:** `cbb_phong` ← `PhongKham_BUS.danhSachPhongDangNhanBenh(session)`; `table_hangCho` (STT, mã phiếu, tên BN, lý do khám, trạng thái); `table_dangKham` (phiếu đang ở phòng khám).

**BUS — `HangCho_BUS`:**

```java
public List<PhieuKham> danhSachCho(SessionContext session, LocalDate ngay, String maPhong)   // CHO_KHAM
public PhieuKham goiSo(SessionContext session, String maPhieuKham)                            // hiển thị bảng số
public void tiepNhanVaoKham(SessionContext session, String maPhieuKham)                       // CHO_KHAM → DANG_KHAM
```

Quy tắc:
- `danhSachCho`: quyền PHU_TA; rỗng → GUI Alert **TB07** (không phải lỗi, hiển thị dịu dàng).
- `goiSo`: quyền PHU_TA; chỉ phiếu `CHO_KHAM` của phòng mình phụ trách; **không đổi trạng thái phiếu** (gọi ≠ vào khám) — cập nhật hiển thị `lb_soDangGoi` (+ ghi log ra bảng số).
- `tiepNhanVaoKham`: điều kiện `AND trangThai='CHO_KHAM'` + phòng khớp; `UPDATE` qua `PhieuKham_DAO.capNhatTrangThai(maPhieuKham, DANG_KHAM, CHO_KHAM)` — **0 dòng ảnh hưởng → TB10** (không phải lỗi mơ hồ).
- Chỉ luồng phụ tá chuyển `DANG_KHAM`; bác sĩ (Thắng) chỉ đọc danh sách theo trạng thái.

**DAO:** `PhongKham_DAO` (`getAllPhongKham`, `layPhongDangNhanBenh`, `capNhatNhanBenh`); hàng chờ đọc qua `PhieuKham_DAO.layDanhSachTheoNgayVaPhong(ngay, maPhong, trangThai)`.

**Test case bắt buộc (ghi `TaiLieu/docs/evidence/thanh/test-cases.md`):**

| Mã | Tình huống | Kết quả mong đợi |
| --- | --- | --- |
| TC-PT01 | Hàng chờ rỗng | TB07, màn hình không lỗi |
| TC-PT02 | Lễ tân lập phiếu → hàng chờ hiện trong 10 giây (Timeline) | Đúng |
| TC-PT03 | Gọi số → bảng số đúng STT + phòng, trạng thái phiếu **vẫn** CHO_KHAM | Đúng |
| TC-PT04 | Xác nhận vào khám → DANG_KHAM, rời hàng chờ | Đúng |
| TC-PT05 | **2 máy đồng thời xác nhận vào khám cùng phiếu** | 1 máy thành công, máy kia TB10 |
| TC-PT06 | Bấm vào khám phiếu HOAN_TAT | TB10 |
| TC-PT07 | Phụ tá phòng khác xác nhận phiếu phòng mình | TB10/TB08 |
| TC-PT08 | `BoDem` gọi 2 lần liên tiếp → mã/STT tăng đúng, 2 thread không trùng | Đúng |
| TC-PT09 | Ngắt DB giữa transaction vào khám | Rollback, phiếu vẫn CHO_KHAM |
| TC-PT10 | Tắt/bật phòng nhận bệnh → danh sách phòng của lễ tân thay đổi theo | Đúng |

## 5. Chi tiết chức năng — UC006: Tra cứu hồ sơ bệnh nhân (`GD_TraCuuBenhNhan`)

**Mục tiêu (đặc tả):** tìm theo mã BN hoặc SĐT (mở rộng: họ tên), hiển thị hồ sơ; không thấy → "Không tìm thấy hồ sơ bệnh nhân".

**GUI — `GD_TraCuuBenhNhan.fxml`:** `cbb_tieuChi` (Mã/SĐT/Họ tên) + `txt_tuKhoa` + `btn_tim`; `table_ketQua`; double-click dòng → modal chi tiết: thông tin hành chính + tiền sử + dữ liệu liên quan.

**BUS — `BenhNhan_BUS.traCuu(session, tieuChi, tuKhoa)`:** quyền LE_TAN (Quản lý cũng được xem); không thấy → trả `List` rỗng, GUI Alert **TB05**. Mã/SĐT khớp chính xác qua `trim()`, còn họ tên dùng tìm gần đúng không dấu.

**DAO:** `BenhNhan_DAO` đọc thông tin qua `layBenhNhanTheoMa`, `layBenhNhanTheoSDT`, `traCuuTheoTen` (nếu có), xử lý trước khi hiển thị.

**Test case bắt buộc (ghi `TaiLieu/docs/evidence/thanh/test-cases.md`):**

| Mã | Tình huống | Kết quả mong đợi |
| --- | --- | --- |
| TC-PT11 | Tìm theo mã đúng | Trả ra đúng bệnh nhân |
| TC-PT12 | Tìm SĐT đúng | Trả ra đúng bệnh nhân |
| TC-PT13 | Tìm tên không dấu | Vẫn tìm được |
| TC-PT14 | Không thấy kết quả | TB05 |
| TC-PT15 | Tra cứu rỗng khóa mở chi tiết | Không lỗi, không crash |

## 6. Thứ tự làm & phụ thuộc

```
PT-01 hạ tầng ──► (cả nhóm bắt đầu M1) ──► PT-02 hàng chờ ──► PT-03 gọi số ──► PT-04 vào khám ──► PT-05 phòng ──► PT-06 tra cứu
PT-02/04 chờ Huy có `PhieuKham_DAO.layDanhSachTheoNgayVaPhong` + `capNhatTrangThai` — chốt chữ ký M0; trước đó dùng seed phiếu CHO_KHAM.
PT-06 chờ Huy hoàn thiện `BenhNhan_DAO`/`BenhNhan_BUS` và giao API rõ ràng; nếu chưa có, dùng fake data để chạy UI.
```

## 7. Git workflow cá nhân

- Issue prefix `PT-xx`; nhánh `feat/thanh-pt-0x-<ten>`; commit `feat(uc009): goi so va xac nhan vao kham...` và `feat(uc006): tra cuu ho so benh nhan...`.
- **Reviewer của Thanh: Huy.** PR của Thanh đụng `ConnectDB`/`BoDem_DAO`/schema 000/001 = file toàn nhóm → cần 2 review (Huy + Duy).
- Thanh duyệt thứ tự chạy `SourceCode/DATA/` khi bất kỳ ai thêm schema mới.
- Nếu sửa API tra cứu dùng chung (`BenhNhan_DAO`/`BenhNhan_BUS`), cần xin Huy review trước khi merge.

## 8. Mốc cá nhân

| Mốc | Thanh phải có |
| --- | --- |
| M0 | `ConnectDB` + `BoDem_DAO` + schema 000/001 + seed phòng + `SourceCode/DATA/README.md`; cả nhóm kết nối được DB trên máy mình |
| M1 | `GD_HangCho` + `GD_TraCuuBenhNhan` chạy với phiếu seed CHO_KHAM và dữ liệu fake |
| M2 | `goiSo`/`tiepNhanVaoKham` + `traCuu` chạy DB local, chặn xung đột đồng thời + test-cases.md |
| M3 | Ghép với Huy (phiếu mới xuất hiện trong hàng chờ) và Thắng (phiếu DANG_KHAM hiện bên bác sĩ) |
| M4 | design.md (có sơ đồ 2 phụ tá đồng thời + luồng tra cứu) + demo.md |

## 9. Chuẩn bị bảo vệ

Lần theo: **bấm "Xác nhận vào khám" → `HangCho_BUS.tiepNhanVaoKham` (quyền → WHERE `trangThai='CHO_KHAM'` + phòng khớp) → `PhieuKham_DAO.capNhatTrangThai` → đếm số dòng → update thành công**; riêng phần tra cứu: **bấm tìm bệnh nhân → `BenhNhan_BUS.traCuu` → DAO trả `List` -> GUI hiển thị / modal chi tiết**.

