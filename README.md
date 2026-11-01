# HỆ THỐNG QUẢN LÝ PHÒNG KHÁM NHA KHOA SÀI GÒN

Đồ án môn học **Phát triển Ứng dụng (Application Development)** — Nhóm 15
Trường Đại học Công nghiệp TP. Hồ Chí Minh (IUH) — Khoa Công nghệ Thông tin — Học kỳ I, năm học 2026–2027.

> 📚 **Trang tài liệu nhóm:** [README_RULES.md](README_RULES.md) (quy tắc code chung) · [README_THANH.md](docs/tasks/README_THANH.md) · [README_DUY.md](docs/tasks/README_DUY.md) · [README_THANG.md](docs/tasks/README_THANG.md) · [README_HUY.md](docs/tasks/README_HUY.md) (nhiệm vụ từng thành viên) · [README_KHOI_NGHIEP.md](README_KHOI_NGHIEP.md) (đề xuất khởi nghiệp — Chương 5 báo cáo) · [KICHBAN_THUC_NGHIEM.md](KICHBAN_THUC_NGHIEM.md) (kịch bản kiểm thử S0–S10).

---

## 1. Thành viên nhóm

| STT | Họ tên | MSSV | Vai trò nhóm | Luồng phụ trách | Tài liệu nhiệm vụ |
| --- | --- | --- | --- | --- | --- |
| 1 | Phan Chí Thanh | 24738801 | Facilitator | **Phụ tá + hạ tầng CSDL**: hàng chờ/gọi số, xác nhận vào khám + ConnectDB/BoDem/schema dùng chung | [README_THANH.md](docs/tasks/README_THANH.md) |
| 2 | Nguyễn Tuấn Duy | 24726131 | Reporter | **Quản lý**: đăng nhập/phân quyền, nhân viên, dịch vụ, báo cáo + khung ứng dụng | [README_DUY.md](docs/tasks/README_DUY.md) |
| 3 | Huỳnh Quốc Thắng | 22693891 | Recorder | **Bác sĩ**: khám & chẩn đoán, chỉ định dịch vụ, cấp thuốc, hoàn tất | [README_THANG.md](docs/tasks/README_THANG.md) |
| 4 | Hoàng Đình Huy | 22669751 | Time Keeper | **Lễ tân**: hồ sơ bệnh nhân, thu phí, lập phiếu khám, tra cứu | [README_HUY.md](docs/tasks/README_HUY.md) |

---

## 2. Giới thiệu đề tài

Phòng khám Nha khoa Sài Gòn hiện quản lý bệnh nhân, phiếu khám và thanh toán bằng sổ tay và bảng tính, dẫn đến tra cứu chậm, dữ liệu trùng lặp, cấp số thứ tự dễ trùng và không có báo cáo tổng hợp. Đồ án xây dựng ứng dụng desktop bằng **Java + JavaFX kết nối SQL Server qua JDBC** để số hóa toàn bộ quy trình: tiếp nhận → thu phí khám → lập phiếu & cấp số thứ tự → gọi số vào khám → khám & chỉ định → hoàn tất → thống kê.

**Phạm vi (trích nguyên văn báo cáo):**

> "Đề tài tập trung xây dựng ứng dụng Java phục vụ công tác quản lý nội bộ tại Phòng khám Nha khoa Sài Gòn. Hệ thống hỗ trợ hình thức đăng ký khám trực tiếp, sử dụng hồ sơ điện tử và phiếu khám để quản lý thông tin bệnh nhân, từng lượt khám và số thứ tự theo phòng… Hệ thống không triển khai đăng ký khám trực tuyến, đặt lịch khám trước hoặc tài khoản tự phục vụ dành cho bệnh nhân."

Việc đặt lịch hẹn qua điện thoại/quét QR và tích hợp AI **không thuộc phạm vi đồ án**, được phát triển riêng trong phần khởi nghiệp: [README_KHOI_NGHIEP.md](README_KHOI_NGHIEP.md).

### 2.1. Tác nhân và danh sách use case (theo báo cáo Chương 3)

| Mã | Tên use case | Tác nhân | Người hiện thực |
| --- | --- | --- | --- |
| UC001 | Đăng nhập hệ thống | Cả 4 tác nhân | Duy |
| UC002 | Quản lý hồ sơ bệnh nhân | Lễ tân | Huy |
| UC003 | Tiếp nhận bệnh nhân — lập phiếu khám bệnh | Lễ tân | Huy |
| UC004 | Ghi nhận khám và chẩn đoán | Bác sĩ | Thắng |
| UC005 | Thu tiền phí khám | Lễ tân | Huy |
| UC006 | Tra cứu hồ sơ bệnh nhân | Lễ tân | Huy |
| UC007 | Quản lý danh mục dịch vụ | Quản lý | Duy |
| UC008 | Xem báo cáo lượt khám (kèm xuất CSV, không có doanh thu) | Quản lý | Duy |
| UC009 | Theo dõi hàng chờ (gọi số, xác nhận vào khám) | Phụ tá | Thanh |
| UC010 | Quản lý nhân viên & tài khoản (nêu trong bảng tác nhân, đặc tả do nhóm chốt tối thiểu) | Quản lý | Duy |

### 2.2. Luồng nghiệp vụ lõi (trạng thái phiếu khám)

```
CHO_KHAM ──(Phụ tá gọi số + xác nhận vào khám)──► DANG_KHAM ──(Bác sĩ hoàn tất)──► HOAN_TAT
```

- Huy tạo phiếu với trạng thái `CHO_KHAM`; **chỉ** Thanh chuyển sang `DANG_KHAM`; **chỉ** Thắng chuyển sang `HOAN_TAT` (đã kiểm tra chẩn đoán/kết quả dịch vụ).
- Thanh toán không làm đổi trạng thái khám. Không chuyển ngược trạng thái.
- Chi tiết chuyển trạng thái và ràng buộc UPDATE: xem [README_RULES.md](README_RULES.md).

---

## 3. Công nghệ chốt (bắt buộc cả nhóm cùng dùng)

| Thành phần | Chốt | Ghi chú |
| --- | --- | --- |
| JDK | **21** | khóa trong `pom.xml` (`maven.compiler.release=21`) |
| Build | **Maven** | một `pom.xml` duy nhất, không ai thêm dependency riêng |
| UI | **JavaFX 21 + FXML (Gluon Scene Builder)** | khớp báo cáo; người dùng tương tác qua FXML + Controller |
| CSDL | **Microsoft SQL Server** (Express 2019/2022) + **SSMS** | tên DB: `N15_PTUD_DB` |
| Driver JDBC | **mssql-jdbc 12.x.jre11** | khai báo trong `pom.xml`, không copy jar vào `Library/` |
| IDE | **VS Code** (Extension Pack for Java) + Scene Builder; Eclipse/IntelliJ dùng lại pom vẫn chạy được | |
| Thiết kế | UML — Visual Paradigm; mockup — Figma | đồng bộ với báo cáo |
| Encoding | **UTF-8** cho toàn bộ file source/SQL/MD | font UI: Segoe UI |

## 4. Kiến trúc ứng dụng (CHỐT — mọi người code theo đúng mô hình này)

Kiến trúc 3 lớp theo sơ đồ nhóm, hiện thực theo chuẩn mẫu của nhóm 11 (project JavaFX tham khảo `PTUD_N11_2025fx`):

```
┌────────────────────────────────────────────────────────┐
│  LỚP 1 — GUI (vn.nhom15.nhakhoa.gui)                    │
│  JavaFX FXML + Controller: nhận dữ liệu, validate nhẹ,  │
│  bắt sự kiện, hiển thị, điều hướng. KHÔNG có SQL.       │
└───────────────▲────────────────────────────────────────┘
                │ gọi hàm BUS (trả entity/List/boolean, ném lỗi message chuẩn)
┌───────────────┴────────────────────────────────────────┐
│  LỚP 2 — BUS (vn.nhom15.nhakhoa.bus)                    │
│  Kiểm tra dữ liệu nghiệp vụ, phân quyền, điều phối      │
│  transaction nhiều bước. KHÔNG có câu lệnh SQL.         │
└───────────────▲────────────────────────────────────────┘
                │ gọi hàm DAO (PreparedStatement)
┌───────────────┴────────────────────────────────────────┐
│  LỚP 3 — DAO (vn.nhom15.nhakhoa.dao)                    │
│  JDBC — đọc/ghi SQL Server, ánh xạ ResultSet → Model.   │
│  KHÔNG Alert, KHÔNG kiểm tra quyền.                     │
└───────────────▲────────────────────────────────────────┘
                │ jdbc:sqlserver://localhost:1433;databaseName=N15_PTUD_DB
        ┌───────┴────────┐
        │  SQL SERVER    │   N15_PTUD_DB (DATA/schema/*.sql)
        └────────────────┘
  🔷 Model (vn.nhom15.nhakhoa.model): POJO + enum dùng chung mọi lớp — không chứa control JavaFX, không chứa Connection.
  🔷 connect (vn.nhom15.nhakhoa.connect): ConnectDB singleton — nơi DUY NHẤT cấp Connection.
  🔷 util (vn.nhom15.nhakhoa.util): FormatUtil, ValidateUtil, QuyenUtil, AlertUtil dùng chung.
```

**Quy tắc vàng:** GUI chỉ gọi BUS; BUS chỉ gọi DAO; DAO chỉ nói chuyện với SQL Server. Cấm "đi tắt" (GUI gọi thẳng DAO, DAO mở Alert…) — chi tiết từng tầng tại [README_RULES.md](README_RULES.md).

### 4.1. Cây thư mục project (nhánh này tài liệu trước; code chuyển dần sang cấu trúc sau)

```
PTUD_N15_/                          ← project Maven "dentist-clinic"
├── pom.xml                         ← JDK 21, JavaFX 21, mssql-jdbc; Duy giữ quyền sửa
├── DATA/
│   ├── schema/                     ← 000_hatang.sql … 011_*.sql (mỗi file một chủ)
│   ├── seed/                       ← dữ liệu mẫu đi kèm từng schema
│   └── queries/                    ← truy vấn báo cáo/ad-hoc
├── docs/
│   ├── tasks/                      ← 4 README nhiệm vụ thành viên
│   ├── evidence/thanh|duy|thang|huy/  ← design.md, test-cases.md, ảnh/video minh chứng
│   └── …tài liệu thiết kế
├── src/main/java/vn/nhom15/nhakhoa/
│   ├── App.java                    ← entry point + điều hướng + session (Duy)
│   ├── connect/ConnectDB.java      ← singleton kết nối (Thanh)
│   ├── model/                      ← 16 entity + enum đã có + SessionContext
│   ├── dao/                        ← Xxx_DAO + BoDem_DAO (cấp mã tự động)
│   ├── bus/                        ← Xxx_BUS
│   ├── gui/                        ← GD_*_Controller (cùng tên FXML)
│   └── util/                       ← FormatUtil, ValidateUtil, QuyenUtil, AlertUtil
├── src/main/resources/
│   ├── fxml/GD_*.fxml              ← Scene Builder
│   ├── css/, pic/                  ← style + icon
│   ├── db.properties.example       ← mẫu cấu hình; db.properties thật KHÔNG commit
│   └── …
└── src/test/java/                  ← kiểm thử DAO/BUS
```

### 4.2. Màn hình ứng dụng (đặt tên chuẩn — cấm đặt lại)

| FXML (`src/main/resources/fxml/`) | Controller (`gui/`) | Use case | Người |
| --- | --- | --- | --- |
| `GD_DangNhap.fxml` | `GD_DangNhap_Controller` | UC001 | Duy |
| `GD_Chinh.fxml` | `GD_Chinh_Controller` | khung menu theo vai trò | Duy |
| `GD_QLBenhNhan.fxml` | `GD_QLBenhNhan_Controller` | UC002 | Huy |
| `GD_TraCuuBenhNhan.fxml` | `GD_TraCuuBenhNhan_Controller` | UC006 | Huy |
| `GD_ThuPhi.fxml` | `GD_ThuPhi_Controller` | UC005 | Huy |
| `GD_LapPhieuKham.fxml` | `GD_LapPhieuKham_Controller` | UC003 | Huy |
| `GD_HangCho.fxml` | `GD_HangCho_Controller` | UC009 | Thanh |
| `GD_KhamBenh.fxml` | `GD_KhamBenh_Controller` | UC004 | Thắng |
| `GD_QLNhanVien.fxml` | `GD_QLNhanVien_Controller` | UC010 | Duy |
| `GD_QLDichVu.fxml` | `GD_QLDichVu_Controller` | UC007 | Duy |
| `GD_BaoCao.fxml` | `GD_BaoCao_Controller` | UC008 | Duy |

## 5. Cơ sở dữ liệu (theo báo cáo Chương 4 — 12 bảng)

`NhanVien`, `TaiKhoan`, `PhongKham`, `BenhNhan`, `HoSoBenhAn`, `DichVu`, `Thuoc`, `PhieuKham`, `HoaDon`, `ChiTietHoaDon`, `ChiDinhDichVu`, `ChiTietCapThuoc` — cộng bảng hạ tầng `BoDem` (bộ đếm mã) do Thanh chủ trì.

Quy ước chính (chi tiết đầy đủ trong [README_RULES.md](README_RULES.md)):

- Mã tự động theo ngày, cấp nguyên tử qua bảng `BoDem` trong transaction: `NVyyMMddNNNN`, `BNyyMMddNNNN`, `HSyyMMddNNNN`, `PKyyMMddNNNN`, `HDyyMMddNNNN`, `DVNNNN`, `THNNNN`, `PNNN`.
- `PhieuKham.soThuTu`: UNIQUE theo cặp `(ngayKham, maPhong)` — không trùng STT cùng phòng cùng ngày.
- Tiền: `decimal(18,2)` ↔ `BigDecimal`; chuỗi tiếng Việt: `nvarchar`; trạng thái: `nvarchar` lưu đúng tên enum (`CHO_KHAM`, `HOAT_DONG`, `DANG_PHUC_VU`…).
- Script SQL đặt tại `DATA/schema/` (tạo cấu trúc) và `DATA/seed/` (dữ liệu mẫu), đánh số theo thứ tự phụ thuộc — ai tạo bảng tự viết script bảng mình, Thanh kiểm duyệt thứ tự chạy.

## 6. Hướng dẫn cài đặt & chạy (làm đúng thứ tự)

1. **JDK 21** — cài Temurin JDK 21, `java -version` phải báo 21.
2. **SQL Server + SSMS** — cài SQL Server Express; bật **TCP/IP 1433** trong SQL Server Configuration Manager và restart dịch vụ; bật chế độ **SQL Server + Windows Authentication**; nhớ mật khẩu user `sa`.
3. **VS Code** — cài Extension Pack for Java + Extension for Maven; **Scene Builder** (Gluon) để mở file FXML.
4. **Tạo DB** — mở SSMS, chạy lần lượt `DATA/schema/000_hatang.sql` → `011_*.sql` rồi `DATA/seed/*.sql`.
5. **Cấu hình kết nối** — copy `src/main/resources/db.properties.example` thành `db.properties`, sửa user/password của máy mình (file thật không commit).
6. **Chạy app** — tại thư mục gốc: `mvn clean compile` rồi `mvn javafx:run` (hoặc chạy class `vn.nhom15.nhakhoa.App` từ IDE).
7. **Đăng nhập thử** — tài khoản seed trong `DATA/seed/` (xem mật khẩu trong file seed tương ứng).

> ⚠️ Lỗi hay gặp (chưa kết nối được DB): port 1433 chưa bật, `encrypt=true` thiếu `trustServerCertificate=true`, sai driver — bẫy chi tiết ở [README_RULES.md](README_RULES.md).

## 7. Quy trình làm việc nhóm (Git/GitHub)

- Nhánh ổn định: `master`. Nhánh làm việc cá nhân: `feat/<ban>-<mã>-<tên>` ví dụ `feat/huy-lt-02-thu-phi`. Nhánh tổng hợp tài liệu: `prj15/main`.
- Mỗi chức năng = 1 Issue (prefix `LT-`/`QT-`/`BS-`/`PT-` theo thành viên) = 1 PR. Commit theo conventional: `feat(uc003): lap phieu kham va cap so thu tu`.
- Review xoay vòng bắt buộc trước merge: **Thanh review Duy → Duy review Thắng → Thắng review Huy → Huy review Thanh**. PR đụng file dùng chung phải thêm chủ file vào review.
- Board: `Backlog → Ready → In progress → Review → Done`; Done chỉ sau khi PR merge và chạy được với DB.
- Cấm: force push, commit trực tiếp lên `master`, commit `db.properties` thật/dữ liệu bệnh nhân thật, nhờ người khác đứng tên commit.

### Mốc tiến độ (áp cho cả nhóm)

| Mốc | Kết quả | Hạn chính |
| --- | --- | --- |
| **M0 — Chốt nền** | `pom.xml`, khung package, `App`, `ConnectDB`, `BoDem_DAO`, `util`, schema 000–003 + seed; mọi máy build chạy được | tuần 1 |
| **M1 — GUI từng luồng** | mỗi người có màn hình chạy được với dữ liệu giả (ghi rõ FAKE) | tuần 2–3 |
| **M2 — BUS/DAO với DB** | chức năng của mình chạy với DB local, có test case | tuần 4–5 |
| **M3 — Ghép luồng** | Lễ tân tạo phiếu → Phụ tá gọi/vào khám → Bác sĩ khám/hoàn tất → Quản lý báo cáo | tuần 6 |
| **M4 — Nghiệm thu** | kiểm tra chéo, cập nhật sơ đồ/Word/PPT, tập demo | tuần 7–8 |

## 8. Trình bày & bảo vệ

Luồng demo chuẩn sau đăng nhập (mỗi người trình bày chặng của mình):

1. **Duy** — giới thiệu hệ thống, đăng nhập phân quyền, mở khung ứng dụng theo vai trò.
2. **Huy** — tìm/tạo hồ sơ bệnh nhân → thu phí khám → lập phiếu, cấp STT, in phiếu.
3. **Thanh** — theo dõi hàng chờ theo phòng → gọi số → cập nhật bảng số → xác nhận vào khám.
4. **Thắng** — nhận danh sách khám → xem tiền sử → ghi chẩn đoán, chỉ định dịch vụ, cấp thuốc → hoàn tất.
5. **Duy** — quản lý nhân viên/dịch vụ → xem báo cáo lượt khám → xuất CSV.

Mỗi thành viên phải lần theo được một thao tác từ GUI xuống DB (GUI → BUS → DAO → SQL) và giải thích: lỗi bắt ở đâu, transaction gồm bước nào, quyền chặn thế nào — thang điểm chi tiết trong từng README nhiệm vụ. Minh chứng lưu `docs/evidence/<tên>/`.

---

## 9. Tham khảo

- Báo cáo môn học `Nhom15_REPORT_ApplicationDevelopment_v3.docx` (nguồn nghiệp vụ chính thức — không sửa trực tiếp).
- Source mẫu khóa trước: `PTUD_2025_Nhom11_DHKHMT18A/SourceCode/…/PTUD_N11_2025fx` (chuẩn GUI→DAO, ConnectDB, DAO, entity, controller/FXML).
- [OpenJFX — JavaFX + Maven](https://openjfx.io/openjfx-docs/#maven) · [Microsoft JDBC for SQL Server](https://learn.microsoft.com/sql/connect/jdbc/microsoft-jdbc-driver-for-sql-server) · [GitHub Flow](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/github-flow).
