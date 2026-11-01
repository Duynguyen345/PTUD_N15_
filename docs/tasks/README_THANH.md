# NHIỆM VỤ CODE — PHAN CHÍ THANH (24738801) — LUỒNG PHỤ TÁ + HẠ TẦNG CSDL DÙNG CHUNG

> **Tóm tắt:** Thanh gánh 2 phần: (1) luồng phụ tá — theo dõi hàng chờ theo phòng, gọi số, cập nhật bảng số, xác nhận vào khám (UC009); (2) **hạ tầng dùng chung của cả nhóm** — `ConnectDB`, `BoDem_DAO` (cấp mã/STT), file `DATA/schema/000`+`001`, quy trình migration, hướng dẫn cấu hình SQL Server từng máy.
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
| PT-06 | Kiểm thử, tài liệu, demo | — | 4 | song song |

Tổng 25. Backlog có chủ nhiệm (chốt M0): **PT-07 ghế/thiết bị, vệ sinh phòng, hàng chờ phẫu thuật, hỗ trợ vật tư** — không dùng cờ `dangNhanBenh` giả làm tình trạng ghế; cần mô hình bổ sung.

## 2. File sở hữu (quy tắc một-chủ)

| Loại | File | Ghi chú |
| --- | --- | --- |
| Hạ tầng | `connect/ConnectDB.java`, `dao/BoDem_DAO.java`, `src/main/resources/db.properties.example` | cả nhóm dùng; đổi API phải qua PR + review |
| SQL hạ tầng | `DATA/schema/000_hatang.sql` (CREATE DATABASE `N15_PTUD_DB` + collation + bảng `BoDem`), `001_phongkham.sql`, `011_banggoiso.sql` (tùy chọn chốt M0) + seed phòng | Thanh kiểm duyệt thứ tự chạy toàn bộ `DATA/` |
| Model | `model/PhongKham.java` | |
| DAO | `dao/PhongKham_DAO.java` | Hàng chờ **không tạo DAO riêng** — dùng `PhieuKham_DAO` của Huy |
| BUS | `bus/HangCho_BUS.java`, `bus/PhongKham_BUS.java` | |
| GUI | `gui/GD_HangCho_Controller.java` + `fxml/GD_HangCho.fxml` | |
| Minh chứng | `docs/evidence/thanh/` | |

**File dùng chéo:** `PhieuKham_DAO` (Huy) — Thanh gọi `layDanhSachTheoNgayVaPhong` và `capNhatTrangThai(maPhieuKham, trangThaiMoi, trangThaiCu)`; thiếu hàm → Issue cho Huy. Huy review PR của Thanh (vòng review), đảm bảo `BoDem`/schema không bị phá.

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

- `db.properties` (đặt `src/main/resources/`, **không commit** — commit `db.properties.example`):

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

Cách hiện thực bắt buộc: bảng `BoDem(loaiMa varchar, ngay date, giaTri int, PK(loaiMa, ngay))` — cấp bằng `UPDATE BoDem SET giaTri = giaTri + 1 OUTPUT inserted.giaTri WHERE loaiMa=? AND ngay=?` (khóa dòng trong transaction, 2 máy chạy đồng thời không trùng); chưa có dòng → INSERT giá trị 1. Hết dải 9999 → ném RuntimeException mô tả rõ (GUI báo TB09).

- Format mã theo RULES mục 5: `NVyyMMddNNNN`, `BNyyMMddNNNN`, `HSyyMMddNNNN`, `PKyyMMddNNNN`, `HDyyMMddNNNN`, `DVNNNN`, `THNNNN`, `PNNN` — `layMaMoi` trả chuỗi hoàn chỉnh.
- Viết `main()` test: gọi 2 lần liên tiếp tăng đúng; gọi từ 2 thread → không trùng.

### 3.3. SQL hạ tầng + quy trình migration

- `000_hatang.sql`: `CREATE DATABASE N15_PTUD_DB` (collation hỗ trợ Unicode tiếng Việt), bảng `BoDem`, (tùy chọn) bảng `BangGoiSo` — **chốt M0**: nếu gọi số cần lưu lại (số đang gọi, phòng, thời điểm, người gọi) thì thêm `011_banggoiso.sql`; nếu chỉ hiển thị tạm trên màn hình thì không cần bảng — **hai người demo 2 máy vẫn an toàn vì trạng thái phiếu nằm ở `PhieuKham.trangThai`**.
- `001_phongkham.sql`: `PhongKham(maPhong PK PNNN, tenPhong UNIQUE, dangNhanBenh bit DEFAULT 0)`; seed ≥ 4 phòng ("P001 Phòng khám 1"…, 2 phòng bật nhận bệnh).
- Quy trình migration (Thanh soạn + kiểm duyệt): file đánh số theo phụ thuộc (`000`→`011`, bảng trong từng README thành viên); **file đã chạy chung cấm sửa nội dung** — cần đổi → tạo file số mới `012_*.sql` kèm ghi chú; mỗi schema có seed riêng cùng số trong `DATA/seed/`; Thanh viết `DATA/README.md` hướng dẫn thứ tự chạy.
- Hướng dẫn cấu hình từng máy (đưa vào `DATA/README.md`): bật TCP/IP 1433 trong SQL Server Configuration Manager + restart; bật SQL auth; lỗi `encrypt=true` thêm `trustServerCertificate=true`; test bằng main() `ConnectDB`.

## 4. Chi tiết chức năng — UC009: Theo dõi hàng chờ (`GD_HangCho`)

**Mục tiêu (đặc tả):** truy xuất danh sách chờ theo phòng khám và STT; danh sách rỗng → "Hiện không có bệnh nhân đang chờ khám"; gọi số; xác nhận vào khám.

**GUI — `GD_HangCho.fxml`:** `cbb_phong` ← `PhongKham_BUS.danhSachPhongDangNhanBenh(session)`; `table_hangCho` (STT, mã phiếu, tên BN, lý do khám, trạng thái); `table_dangKham` (phiếu đã vào khám của phòng hôm nay); **bảng số hiển thị**: `lb_soDangGoi` (label chữ lớn) + `lb_phongDangGoi`; `btn_goSo`, `btn_xacNhanVaoKham`, `btn_taiLai` (refresh định kỳ 10 giây bằng `Timeline` — hàng chờ phải thấy phiếu mới của lễ tân không cần bấm tay).

**BUS — `HangCho_BUS`:**

```java
public List<PhieuKham> danhSachCho(SessionContext session, LocalDate ngay, String maPhong)   // CHO_KHAM
public PhieuKham goiSo(SessionContext session, String maPhieuKham)                            // hiển thị bảng số
public void tiepNhanVaoKham(SessionContext session, String maPhieuKham)                       // CHO_KHAM → DANG_KHAM
```

Quy tắc:
- `danhSachCho`: quyền PHU_TA; rỗng → GUI Alert **TB07** (không phải lỗi, hiển thị dịu dàng).
- `goiSo`: quyền PHU_TA; chỉ phiếu `CHO_KHAM` của phòng mình phụ trách; **không đổi trạng thái phiếu** (gọi ≠ vào khám) — cập nhật hiển thị `lb_soDangGoi` (+ ghi `BangGoiSo` nếu M0 chốt có bảng). Người khác bấm gọi phiếu đang gọi → TB10.
- `tiepNhanVaoKham`: điều kiện `AND trangThai='CHO_KHAM'` + phòng khớp; `UPDATE` qua `PhieuKham_DAO.capNhatTrangThai(maPhieuKham, DANG_KHAM, CHO_KHAM)` — **0 dòng ảnh hưởng → TB10** ("đã thay đổi, tải lại") — đây là lớp chặn khi 2 người cùng bấm 1 phiếu.
- Chỉ luồng phụ tá chuyển `DANG_KHAM`; bác sĩ (Thắng) chỉ đọc danh sách theo trạng thái.

**DAO:** `PhongKham_DAO` (`getAllPhongKham`, `layPhongDangNhanBenh`, `capNhatNhanBenh`); hàng chờ đọc qua `PhieuKham_DAO.layDanhSachTheoNgayVaPhong(ngay, maPhong, trangThai)`.

**Test case bắt buộc (ghi `docs/evidence/thanh/test-cases.md`):**

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

## 5. Thứ tự làm & phụ thuộc

```
PT-01 hạ tầng ──► (cả nhóm bắt đầu M1) ──► PT-02 hàng chờ ──► PT-03 gọi số ──► PT-04 vào khám ──► PT-05 phòng
PT-02/04 chờ Huy có `PhieuKham_DAO.layDanhSachTheoNgayVaPhong` + `capNhatTrangThai` — chốt chữ ký M0; trước đó dùng seed phiếu CHO_KHAM.
```

## 6. Git workflow cá nhân

- Issue prefix `PT-xx`; nhánh `feat/thanh-pt-0x-<ten>`; commit `feat(uc009): goi so va xac nhan vao kham...`.
- **Reviewer của Thanh: Huy.** PR của Thanh đụng `ConnectDB`/`BoDem_DAO`/schema 000/001 = file toàn nhóm → cần 2 review (Huy + Duy).
- Thanh duyệt thứ tự chạy `DATA/` khi bất kỳ ai thêm schema mới.

## 7. Mốc cá nhân

| Mốc | Thanh phải có |
| --- | --- |
| M0 | `ConnectDB` + `BoDem_DAO` + schema 000/001 + seed phòng + `DATA/README.md`; cả nhóm kết nối được DB trên máy mình |
| M1 | `GD_HangCho` chạy với phiếu seed CHO_KHAM |
| M2 | goiSo/tiepNhanVaoKham chạy DB local, chặn xung đột đồng thời + test-cases.md |
| M3 | Ghép với Huy (phiếu mới xuất hiện trong hàng chờ) và Thắng (phiếu DANG_KHAM hiện bên bác sĩ) |
| M4 | design.md (có sơ đồ 2 phụ tá đồng thời) + demo.md |

## 8. Chuẩn bị bảo vệ

Lần theo: **bấm "Xác nhận vào khám" → `HangCho_BUS.tiepNhanVaoKham` (quyền → WHERE `trangThai='CHO_KHAM'` + phòng khớp) → `PhieuKham_DAO.capNhatTrangThai` → đếm số dòng → 0 dòng = TB10**. Sẵn sàng giải thích: vì sao cấp mã qua `BoDem` + OUTPUT thay vì `MAX+1` (2 máy đồng thời); vì sao gọi số không đổi trạng thái; cơ chế nào bảo đảm 2 người không cùng đưa 1 bệnh nhân vào khám (conditional UPDATE + kiểm số dòng); hướng dẫn một máy mới kết nối DB từng bước.
