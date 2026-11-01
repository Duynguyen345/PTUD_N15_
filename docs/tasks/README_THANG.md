# NHIỆM VỤ CODE — HUỲNH QUỐC THẮNG (22693891) — LUỒNG BÁC SĨ

> **Tóm tắt:** Thắng sở hữu luồng chuyên môn: nhận danh sách khám → xem tiền sử bệnh án → ghi chẩn đoán → chỉ định dịch vụ → cấp thuốc → lưu kết quả → hoàn tất lượt khám (UC004), cộng phần xem tiền sử bệnh án điện tử.
> **Bắt buộc đọc trước:** [README_RULES.md](../../README_RULES.md). Thắng dùng lại entity `PhieuKham`/`BenhNhan` của Huy và `DichVu` của Duy qua DAO/BUS của họ — **không tạo bản sao**.

---

## 1. Phạm vi công việc

| Mã | Use case / đầu việc | Màn hình | Điểm nội bộ | Thứ tự làm |
| --- | --- | --- | --- | --- |
| BS-01 | UC004 — Màn hình khám: nhận danh sách phiếu của mình | `GD_KhamBenh` | 3 | 1 |
| BS-02 | UC004 — Xem tiền sử & hồ sơ bệnh án điện tử | (trong `GD_KhamBenh`) | 3 | 2 |
| BS-03 | UC004 — Ghi chẩn đoán + chỉ định dịch vụ | (trong `GD_KhamBenh`) | 4 | 3 |
| BS-04 | UC004 — Cấp thuốc (kê đơn nội bộ) | (trong `GD_KhamBenh`) | 3 | 4 |
| BS-05 | UC004 — Lưu kết quả + hoàn tất lượt khám | (trong `GD_KhamBenh`) | 4 | 5 |
| BS-06 | Kiểm thử, tài liệu, demo | — | 4 | song song |

Tổng 21 + 4 phần tài liệu/demo của các chức năng = 25. Backlog có chủ nhiệm (chốt M0): **BS-08 phẫu thuật/chờ phẫu thuật & kết quả** (cần trạng thái và mô hình mới, phối hợp Thanh về hàng chờ phẫu thuật).

## 2. File sở hữu (quy tắc một-chủ)

| Loại | File | Ghi chú |
| --- | --- | --- |
| Model | `model/HoSoBenhAn.java`, `model/ChiDinhDichVu.java`, `model/Thuoc.java`, `model/ChiTietCapThuoc.java` | |
| DAO | `dao/HoSoBenhAn_DAO.java`, `dao/ChiDinhDichVu_DAO.java`, `dao/Thuoc_DAO.java`, `dao/ChiTietCapThuoc_DAO.java` | |
| BUS | `bus/KhamBenh_BUS.java`, `bus/HoSoBenhAn_BUS.java` | |
| GUI | `gui/GD_KhamBenh_Controller.java` + `fxml/GD_KhamBenh.fxml` | một màn hình làm đủ BS-01→05 |
| SQL | `DATA/schema/004_thuoc.sql`, `006_hosobenhan.sql`, `009_chidinhdichvu.sql`, `010_chitietcapthuoc.sql` + seed | |
| Minh chứng | `docs/evidence/thang/` | |

**File dùng chéo:** `PhieuKham_DAO` (Huy) — Thắng gọi `capNhatTrangThai`, `layDanhSachTheoBacSi`; nếu thiếu hàm → Issue cho Huy. `DichVu_BUS.danhSachDichVuDangPhucVu` (Duy) — nạp ComboBox chỉ định. `BoDem_DAO` (Thanh) — mã `HSyyMMddNNNN` khi tạo hồ sơ bệnh án. **Quyền chuyển `HOAN_TAT` thuộc duy nhất về `KhamBenh_BUS` của Thắng.**

## 3. Chi tiết chức năng — UC004: Ghi nhận khám và chẩn đoán (`GD_KhamBenh`)

**Mục tiêu (đặc tả):** xem tiền sử bệnh án; chọn bệnh nhân trong danh sách; nhập chẩn đoán tình trạng răng miệng; chỉ định dịch vụ đối chiếu danh mục đang kinh doanh; lưu vào bệnh án điện tử; (đặc tả trạng thái) trước HOAN_TAT phải kiểm tra chẩn đoán và kết quả nếu có dịch vụ.

### 3.1. BS-01 — Danh sách khám của mình

**GUI:** `table_danhSach` — phiếu khám có `maNhanVien = session.maNhanVien` (chỉ phiếu của chính mình), cột: STT, mã phiếu, tên BN, lý do khám, trạng thái (badge `CHO_KHAM` mờ / `DANG_KHAM` chọn được), giờ lập. Nút `btn_taiLai`. Click dòng → nạp toàn bộ khung khám bên phải.

**BUS — `KhamBenh_BUS.danhSachKhamCuaToi(session)`:** quyền BAC_SI; đọc qua `PhieuKham_DAO.layDanhSachTheoBacSi(maBacSi, ngay)` (Huy viết, Thắng dùng — cần rõ ràng chữ ký ngay M0).

**Quy tắc cốt lõi:** chỉ được **sửa** phiếu đang `DANG_KHAM` (Phụ tá đã xác nhận vào khám). Phiếu `CHO_KHAM` chỉ xem; phiếu `HOAN_TAT` chỉ đọc — mọi cố tình sửa → BUS ném **TB10**. Điều kiện UPDATE luôn kèm `AND trangThai = 'DANG_KHAM'` **và** `AND maNhanVien = session.maNhanVien` (chặn cả vai trò đúng nhưng sai phiếu).

### 3.2. BS-02 — Tiền sử & bệnh án điện tử

**GUI:** panel trái — thông tin BN + `txt_tienSuDiUng` (readonly, từ `BenhNhan` của Huy), `txt_tienSuBenhLy`, `txt_ghiChuChuyenMon` (từ `HoSoBenhAn`); nếu BN chưa có hồ sơ bệnh án → hiển thị "Chưa có hồ sơ bệnh án" và `KhamBenh_BUS` sẽ tự tạo khi lưu lần đầu (1 BN — 1 hồ sơ, `maBenhNhan` UNIQUE).

**BUS — `HoSoBenhAn_BUS`:** `layTheoBenhNhan(session, maBenhNhan)`; `taoHoSoNeuChuaCo(con, maBenhNhan, tienSuBenhLy)` — chạy trong transaction của `luuKham`, mã `HSyyMMddNNNN` qua `BoDem_DAO`; đã có → không tạo (DB UNIQUE chặn, bóc lỗi → **TB16** `Bệnh nhân này đã có hồ sơ bệnh án.`).

**SQL:** `006_hosobenhan.sql` — `HoSoBenhAn(maHoSo PK HSyyMMddNNNN, tienSuBenhLy, ghiChuChuyenMon, maBenhNhan FK UNIQUE NOT NULL)`; seed hồ sơ cho ~1/2 bệnh nhân seed để có ca "đã có" và "chưa có".

### 3.3. BS-03 — Chẩn đoán + chỉ định dịch vụ

**GUI:** `txt_chanDoan` (TextArea, bắt buộc — VL09); `cbb_dichVu` ← `DichVu_BUS.danhSachDichVuDangPhucVu` (chỉ `DANG_PHUC_VU`); `btn_themChiDinh` → dòng mới trong `table_chiDinh` (tên, đơn giá readonly, ghi chú); `btn_xoaChiDinh`; chọn dòng rỗng/đóng → **VL11**; dịch vụ trùng trong bảng → cảnh báo, không thêm dòng trùng.

**BUS — `KhamBenh_BUS.luuKham`:**

```java
public void luuKham(SessionContext session, String maPhieuKham, String chanDoan,
                    String ketQuaDieuTri, List<ChiDinhDichVu> dsChiDinh,
                    List<ChiTietCapThuoc> dsThuocCap, String ghiChuThuocTuMua)
```

Transaction (1 commit/rollback, đúng thứ tự):
1. `UPDATE PhieuKham SET chanDoan, ketQuaDieuTri, ghiChuThuocTuMua WHERE maPhieuKham=? AND trangThai='DANG_KHAM' AND maNhanVien=?` — 0 dòng → TB10.
2. `HoSoBenhAn_BUS.taoHoSoNeuChuaCo(con, maBenhNhan, ...)` + gắn `maHoSo` vào phiếu (cột `maHoSo` nullable của Huy).
3. Chỉ định: **xóa chỉ định cũ của phiếu rồi INSERT lại toàn bộ** trong cùng transaction (cách chốt để sửa chỉ định đơn giản, tránh UPDATE từng dòng) — mỗi dòng kiểm `DichVu.trangThai = DANG_PHUC_VU` tại thời điểm lưu, vi phạm → rollback + **TB15**; không có dòng nào mà GUI vẫn gửi → **TB06**.
4. Cấp thuốc (BS-04 bên dưới) cùng transaction.

### 3.4. BS-04 — Cấp thuốc

**GUI:** `cbb_thuoc` ← `Thuoc_DAO.getAllThuoc` (mã, tên, đơn vị tính); `txt_soLuong` (VL12); `txt_ghiChuCap`; `btn_themThuoc` → `table_thuoc`; `btn_xoaThuoc`.

**BUS/DAO:** kiểm số lượng nguyên dương; `ChiTietCapThuoc_DAO.themChiTiet(con, ...)` (PK ghép maPhieuKham+maThuoc, trùng thuốc → cập nhật số lượng dòng đó thay vì thêm dòng trùng). **Lưu ý nghiệp vụ (đặc tả):** thuốc cấp **không** tự cộng tiền vào hóa đơn; ghi chú thuốc tự mua lưu cột `ghiChuThuocTuMua` của phiếu.

**SQL:** `004_thuoc.sql` — `Thuoc(maThuoc PK THNNNN, tenThuoc, donViTinh)`; `010_chitietcapthuoc.sql` — `ChiTietCapThuoc(PK ghép maPhieuKham+maThuoc, soLuong CHECK>0, ghiChuCapThuoc)`; seed ≥ 10 loại thuốc.

### 3.5. BS-05 — Hoàn tất lượt khám

**GUI:** `btn_luu` (lưu nháp giữa chừng, phiếu vẫn `DANG_KHAM`) và `btn_hoanTat`.

**BUS — `KhamBenh_BUS.hoanTat(session, maPhieuKham)`:** quyền BAC_SI + phiếu của mình; điều kiện hoàn tất (đúng đặc tả): `chanDoan` không rỗng (VL09) **và** nếu có chỉ định thì `ChiDinhDichVu` đã lưu ít nhất 1 dòng hợp lệ (TB06) → `UPDATE PhieuKham SET trangThai='HOAN_TAT' WHERE maPhieuKham=? AND trangThai='DANG_KHAM' AND maNhanVien=?` — 0 dòng → **TB10**. Không chuyển ngược; Thanh toán không đổi trạng thái này (hóa đơn cuối lượt là backlog LT-06).

**Test case bắt buộc (ghi `docs/evidence/thang/test-cases.md`):**

| Mã | Tình huống | Kết quả mong đợi |
| --- | --- | --- |
| TC-BS01 | Danh sách chỉ hiện phiếu của bác sĩ đang đăng nhập | Đúng, không thấy phiếu bác sĩ khác |
| TC-BS02 | Bệnh nhân có/chưa có hồ sơ bệnh án | Hiển thị đúng 2 trường hợp; lưu lần đầu tự tạo hồ sơ |
| TC-BS03 | Lưu đủ chẩn đoán + 2 chỉ định + 2 thuốc | 1 transaction: phiếu + hồ sơ + chỉ định + thuốc cùng xuất hiện |
| TC-BS04 | Chẩn đoán rỗng | VL09, không lưu gì |
| TC-BS05 | Chỉ định rỗng | TB06 |
| TC-BS06 | Chỉ định dịch vụ `NGUNG_PHUC_VU` | TB15, rollback toàn bộ |
| TC-BS07 | Hoàn tất khi chưa lưu chẩn đoán | VL09 |
| TC-BS08 | Hoàn tất 2 lần / phiếu đã HOAN_TAT | TB10 lần 2 |
| TC-BS09 | Sửa phiếu của bác sĩ khác (đổi mã bằng tay trong DB) | TB10/TB08 |
| TC-BS10 | Ngắt DB giữa lưu (chỉ định xong, thuốc lỗi) | Rollback hết — không có chỉ định mồ côi |
| TC-BS11 | Số lượng thuốc 0/chữ | VL12 |
| TC-BS12 | Sửa chỉ định rồi lưu lại | Xóa cũ + insert mới, không nhân đôi dòng |

## 4. Thứ tự làm & phụ thuộc

```
M0: chốt chữ ký PhieuKham_DAO với Huy + schema 004/006/009/010 ──► BS-01 ──► BS-02 ──► BS-03 ──► BS-04 ──► BS-05
BS-01 chờ Thanh chuyển phiếu DANG_KHAM (M3 ghép) — trước đó tự tạo phiếu seed DANG_KHAM trong seed riêng của mình.
```

## 5. Git workflow cá nhân

- Issue prefix `BS-xx`; nhánh `feat/thang-bs-0x-<ten>`; commit `feat(uc004): luu kham va chi dinh dich vu...`.
- **Reviewer của Thắng: Duy.** PR đụng `PhieuKham_DAO`/schema 007 mời thêm Huy; đụng `DichVu` mời Duy.

## 6. Mốc cá nhân

| Mốc | Thắng phải có |
| --- | --- |
| M0 | Chốt chữ ký dùng chéo với Thanh/Duy/Huy; entity + schema 004/006/009/010 + seed thuốc |
| M1 | `GD_KhamBenh` chạy FAKE (danh sách phiếu seed DANG_KHAM) |
| M2 | luuKham + hoanTat chạy DB local, transaction thật + test-cases.md |
| M3 | Ghép với Thanh (vào khám) và Huy (phiếu) chạy trơn |
| M4 | design.md/demo.md + sơ đồ sequence cập nhật khớp code cuối |

## 7. Chuẩn bị bảo vệ

Lần theo: **chọn phiếu DANG_KHAM → `KhamBenh_BUS.luuKham` (quyền → điều kiện WHERE trạng thái+chủ phiếu → transaction 4 bước: phiếu → hồ sơ → chỉ định → thuốc → commit/rollback) → `ChiDinhDichVu_DAO`/`ChiTietCapThuoc_DAO` → 4 bảng**. Sẵn sàng giải thích: vì sao xóa-then-insert cho chỉ định trong cùng transaction là an toàn; vì sao chỉ Thắng được chuyển `HOAN_TAT` và chặn bằng WHERE thay vì chỉ ẩn nút; snapshot đơn giá dịch vụ nằm ở đâu; một test case khó nhất (TC-BS10 rollback).
