# README_KHOI_NGHIEP — DỰ ÁN KHỞI NGHIỆP: **DentCare AI**

> **Nền tảng quản lý & chăm sóc bệnh nhân nha khoa thông minh cho phòng khám tư nhân Việt Nam**
> Nội dung này dùng để điền **Chương 5: KHỞI NGHIỆP** của báo cáo môn học và dùng khi trình bày bài tập khởi nghiệp.

**Đề bài của giảng viên (trích nguyên văn):** *"Em là sinh viên khoa CNTT, nhận thấy yêu cầu về ứng dụng CNTT trong xã hội rất lớn và em (hoặc kết hợp với nhóm bạn) thành lập công ty để phát triển dự án khởi nghiệp. Vậy các em trả lời các câu hỏi: 1. Giải thích kế hoạch phát triển dự án khởi nghiệp. 2. Dự án của các em là gì? trình bày kế hoạch cho dự án. 3. Dự án của các em có gì khác biệt? (có những ý tưởng mà em cho là hay, sáng tạo)."*

Khung lập kế hoạch theo tài liệu Đề án 1665 dành cho sinh viên khởi nghiệp: lập kế hoạch kinh doanh (tầm nhìn–sứ mệnh, sản phẩm, thị trường, marketing, nhân sự, tài chính), **Business Model Canvas 9 khối** và **Lean Startup (Build–Measure–Learn)**, **Design Thinking**.

---

## 0. Ý tưởng bắt đầu từ chính đồ án

Đồ án "Hệ thống quản lý Phòng khám Nha khoa Sài Gòn" giải quyết bài toán **quản lý nội bộ** của một phòng khám (hồ sơ, phiếu khám, hàng chờ, hóa đơn, báo cáo). Phạm vi đồ án loại trừ đặt lịch trực tuyến và tài khoản bệnh nhân — đó chính là **khoảng trống thị trường** nhóm chọn để khởi nghiệp:

> Đa số phòng khám nha khoa tư nhân nhỏ ở Việt Nam vẫn quản lý bằng sổ tay hoặc bảng tính; phần mềm hiện có hoặc hướng đến bệnh viện lớn, hoặc thiếu AI và kênh bệnh nhân. DentCare AI = đồ án (app quản lý) **+ kênh bệnh nhân quét QR + AI** — dựng trên chính code base `GUI → BUS → DAO → SQL Server` của nhóm.

---

## 1. CÂU 1 — Giải thích kế hoạch phát triển dự án khởi nghiệp

Tư duy phát triển: **Lean Startup** — mỗi vòng Build–Measure–Learn 3–6 tháng, ra sản phẩm nhỏ chạy thật rồi mới mở rộng; sản phẩm khởi đầu là chính sản phẩm đồ án đã có (không bắt đầu từ con số 0).

| Giai đoạn | Thời gian | Xây gì | Đo gì (Measure) | Điều kiện đi tiếp (Learn) |
| --- | --- | --- | --- | --- |
| **G0 — MVP nội bộ** | Tháng 1–3 | Hoàn thiện app quản lý từ đồ án (desktop JavaFX + SQL Server local) | Cài thử **miễn phí** cho 2–3 phòng khám thân quen; đếm số lượt khám/phiếu xử lý/tháng | ≥ 2 phòng dùng ≥ 4 tuần + 5 phản hồi cải tiến cụ thể |
| **G1 — QR đặt lịch** | Tháng 4–9 | Thêm **DentCare Booking**: phòng khám có mã QR (đặt ở quầy/bảng hiệu); bệnh nhân quét bằng điện thoại → web đặt/đổi lịch theo khung giờ trống thật, không cần cài app | Tỷ lệ bệnh nhân tự đặt lịch; giờ lễ tân tiết kiệm được | ≥ 30% lượt hẹn tự đặt qua QR tại ≥ 5 phòng khám |
| **G2 — Tính năng AI** | Tháng 10–15 | Chatbot Zalo OA phân loại triệu chứng răng; nhắc tái khám thông minh; trợ lý báo cáo tiếng Việt cho chủ phòng khám | Tỷ lệ khách quay lại sau nhắc; thời gian làm báo cáo giảm | Tỷ lệ tái khám tăng ≥ 15% tại phòng dùng AI |
| **G3 — SaaS mở rộng** | Tháng 16+ | Gói thuê bao đa phòng khám, backup cloud đồng bộ, cổng thanh toán, marketplace vật tư | MRR (doanh thu định kỳ hàng tháng), tỷ lệ rời bỏ | MRR đủ nuôi đội ≥ 4 người |

**Mô hình doanh thu:** freemium (bản quản lý cơ bản miễn phí như Medpro đang làm) → thuê bao **299.000–599.000đ/tháng/phòng** theo gói (có module AI/QR) + phí triển khai cài đặt lần đầu 2.000.000–5.000.000đ.

**Nguồn lực & đội hình:** 4 founder khoa CNTT (vai trò tại mục 2.6), vốn tự có ít + hỗ trợ hệ sinh thái khởi nghiệp sinh viên (Đề án 1665, vườn ươm IUH, các cuộc thi SV khởi nghiệp) — lợi thế: chi phí phát triển gần bằng 0, sản phẩm đã có sẵn.

**Chỉ tiêu năm đầu:** 30 phòng khám trả phí, MRR ≈ 12–15 triệu/tháng, hòa vốn chi phí vận hành trong tháng 12–14.

---

## 2. CÂU 2 — Dự án là gì? Kế hoạch cho dự án

### 2.1. Tầm nhìn – sứ mệnh

- **Tầm nhìn:** trở thành nền tảng số hóa mặc định cho phòng khám nha khoa tư nhân nhỏ tại Việt Nam.
- **Sứ mệnh:** để chủ phòng khám chuyên tâm điều trị — hệ thống lo phần còn lại: tiếp nhận, lịch hẹn, nhắc bệnh nhân, thống kê.

### 2.2. Sản phẩm (3 thành phần)

1. **DentCare Manager** — app quản lý nội bộ (ngay từ đồ án): hồ sơ bệnh nhân, thu phí, phiếu khám + STT, hàng chờ, chỉ định, hóa đơn, báo cáo. Điểm đặc thù: **chạy offline-first** (CSDL SQL Server tại phòng khám) — mất mạng vẫn khám bệnh được, dữ liệu đồng bộ khi có mạng (G3).
2. **DentCare Booking (QR)** — mỗi phòng khám nhận mã QR riêng; bệnh nhân quét bằng camera điện thoại → mở web đặt lịch (không cài app): chọn dịch vụ → xem khung giờ trống thật (đọc từ DB lịch hẹn của Manager) → xác nhận → nhận mã hẹn. Lễ tân thấy lịch hẹn ngay trên app quản lý.
3. **Bộ tính năng AI** (G2, gọi API mô hình AI — không tự train): chatbot tư vấn/định hướng triệu chứng răng miệng trước khi khám; nhắc tái khám cá nhân hóa theo dịch vụ đã làm; trợ lý báo cáo hỏi-đáp tiếng Việt.

### 2.3. Phân tích thị trường (số liệu kèm nguồn — xem mục 4)

- Thị trường dịch vụ nha khoa Việt Nam: **~4,21 tỷ USD năm 2024**, dự báo ~**5,5 tỷ USD năm 2030** (CAGR ~4,5%) theo Research and Markets; các ước tính khác (IMARC, Ken Research) cho quy mô 2,35–2,36 tỷ USD 2024–2025 với CAGR 6,3% — **mọi phương pháp đều cho tăng trưởng đều 4,5–6,3%/năm**.
- Cung: **hơn 3.000 phòng khám nha khoa** trên cả nước (Tổng cục Thống kê 2024 qua Brands Vietnam), riêng TP.HCM có khoảng **2.000 cơ sở** đăng ký hoạt động (Sở Y tế, 11/2025) — và con số thực tế có thể lớn hơn nhiều nếu tính các cơ sở nhỏ.
- Khách hàng mục tiêu đầu tiên: **phòng khám tư 1–5 ghế** chưa có phần mềm, tập trung TP.HCM — nơi cơ hội cải thiện lớn nhất và nhóm dễ tiếp cận trực tiếp.

### 2.4. Phân tích cạnh tranh

| Đối thủ | Thế mạnh | Kẽ hở DentCare AI khai thác |
| --- | --- | --- |
| ISOFH (HIS/EMR) | Bệnh viện lớn, quản trị chuẩn | Quá "nặng" và đắt cho phòng khám 1–5 ghế |
| Medpro | Freemium, đặt lịch online | Cloud-thuần — chết khi mất mạng; AI chưa sâu vào nha khoa |
| Bambufit, VTTech Dental, NaNo Dental, TMT SOFT, NhaKhoaSoPlus | Chuyên sâu nha khoa, có bản offline | Thiếu kênh bệnh nhân QR + AI; giao diện/xếp lịch ít thông minh |
| CareSoft, MediCRM/Base.vn | CRM/marketing chung nhiều ngành | Không hiểu nghiệp vụ nha khoa (phiếu khám, STT, chỉ định) |

**Định vị:** "phần mềm nha khoa nhỏ gọn, offline-first, có sẵn kênh bệnh nhân QR và AI tiếng Việt — giá thuê bao bằng 1 cạo vôi/tháng của phòng khám".

### 2.5. Kế hoạch marketing & nhân sự

- **Marketing:** bản dùng thử 60 ngày tại 5–10 phòng khám khu vực Q. Thủ Đức/GV (mạng lưới quen biết); video demo ngắn TikTok/Facebook nhóm nha sĩ; Zalo OA làm kênh chatbot + nhắc lịch (người VN dùng Zalo nhiều hơn app riêng); hợp tác tổ chức hội thảo nha khoa địa phương; chương trình giới thiệu: phòng khám giới thiệu được 1 phòng mới giảm 1 tháng phí.
- **Nhân sự (4 founder — chính là nhóm 15):** CEO/BA (Thanh — ngược dòng quy trình nghiệp vụ), CTO/kiến trúc (Duy — khung app, hạ tầng), Backend/DB (Huy — CSDL, transaction), Frontend/AI (Thắng — UI, tích hợp AI API). Sau G2 tuyển 1 sales/kỹ thuật triển khai part-time.

### 2.6. Kế hoạch tài chính dự phóng (tháng đi ổn định, G2)

| Khoản | Chi phí/tháng | Ghi chú |
| --- | --- | --- |
| VPS + tên miền + backup | ~1.500.000đ | đồng bộ lịch hẹn + web QR |
| API AI (chatbot, trợ lý báo cáo) | ~2.000.000đ | theo mức dùng, giảm dần khi cache |
| Marketing + đi triển khai | ~3.000.000đ | xăng xe, quà tặng phòng khám mới |
| Dự phòng + công cụ | ~1.000.000đ | |
| **Tổng chi** | **~7.500.000đ** | chưa tính lương founder (giai đoạn nuôi sản phẩm) |
| Doanh thu cần hòa vốn | ~15–20 phòng gói 299k hoặc 10–12 phòng gói 599k | khả thi với chỉ tiêu 30 phòng/năm 1 |

### 2.7. Business Model Canvas (9 khối — theo tài liệu 1665)

| Khối | Nội dung |
| --- | --- |
| 1. Phân khúc khách hàng | Phòng khám nha khoa tư 1–5 ghế VN (chưa số hóa / đang dùng sổ tay) |
| 2. Giá trị đề xuất | Quản lý trọn gói + bệnh nhân tự đặt lịch qua QR + AI tiếng Việt — offline-first, giá "nội địa" |
| 3. Kênh | Đi trực tiếp phòng khám, Zalo OA, cộng đồng nha sĩ, hội thảo, giới thiệu chéo |
| 4. Quan hệ khách hàng | Kỹ thuật cài tại nhà, hỗ trợ Zalo/call, vòng phản hồi 2 tuần/lần |
| 5. Dòng doanh thu | Thuê bao 299k–599k/tháng + phí cài đặt + (G3) % cổng thanh toán |
| 6. Tài nguyên then chốt | Code base đồ án, đội 4 founder CNTT, dữ liệu quy trình khám thật từ phòng thử nghiệm |
| 7. Hoạt động then chốt | Phát triển tính năng, triển khai/cài đặt, CSKH, thu thập phản hồi (Build–Measure–Learn) |
| 8. Đối tác then chốt | Vườn ươm IUH/Đề án 1665, nhà cung cấp vật tư nha khoa (G3), nhà thiết kế bảng hiệu QR |
| 9. Cơ cấu chi phí | Hosting/AI API, marketing, đi triển khai (rất thấp nhờ đội sinh viên + sản phẩm có sẵn) |

### 2.8. Rủi ro & phương án

| Rủi ro | Phương án |
| --- | --- |
| Phòng khám ngại thay đổi sổ tay | 60 ngày miễn phí + nhập liệu giúp + đo lường giờ tiết kiệm cho lễ tân |
| Đối thủ lớn thêm tính năng AI | Bám sát kênh offline-first + giá nhỏ; chuyển hướng nhanh nhờ đội nhỏ (Lean) |
| API AI tăng giá | Chatbot có chế độ kịch bản (không AI) vẫn chạy được |
| Thành viên tốt nghiệp/xã hội | Cam kết founder bằng thỏa thuận cổ phần, chia theo đóng góp (đã quen làm việc theo Issue/PR từ đồ án) |

---

## 3. CÂU 3 — Dự án có gì khác biệt? (Ý tưởng hay, sáng tạo)

1. **Đặt lịch bằng quét QR — không cần cài app:** mỗi phòng khám có QR riêng; bệnh nhân quét bằng camera điện thoại là đặt/đổi lịch được theo khung giờ trống thật (đọc trực tiếp DB phiếu/lịch hẹn). Giải quyết điểm yếu lớn nhất của mọi phần mềm hiện có: bắt bệnh nhân cài thêm app. Chi phí phòng khám = 1 mã QR dán quầy.
2. **AI phân loại triệu chứng trước khi khám (chatbot Zalo OA):** bệnh nhân chat tiếng Việt ("răng nhức buốt 2 đêm…") → AI gợi ý mức độ và dịch vụ phù hợp → xếp độ ưu tiên vào hàng chờ trước khi đến phòng. Giảm tải lễ tân và sàng lọc ca gấp.
3. **Nhắc tái khám thông minh theo dịch vụ:** hệ thống đọc chỉ định trong DB và nhắc tự động đúng chu kỳ nghề nghiệp (cạo vôi 6 tháng, niềng răng 4 tuần, tẩy trắng 1 năm, theo dõi răng khôn…) — cá nhân hóa, không phải nhắc đại trà. Tăng tỷ lệ khách quay lại — chỉ số sống còn của phòng khám nhỏ.
4. **Trợ lý báo cáo AI cho chủ phòng:** hỏi bằng tiếng Việt tự nhiên ("tháng này lượt khám tăng bao nhiêu so với tháng trước?", "dịch vụ nào ít khách nhất?") thay vì mò menu thống kê; AI tổng hợp từ dữ liệu phiếu khám thật.
5. **Offline-first — khác biệt chiến lược:** giữ kiến trúc đồ án (SQL Server tại chỗ) nên phòng khám **không chết khi mất mạng** — điểm mà SaaS cloud thuần (Medpro…) không làm được; dữ liệu bệnh nhân nằm tại phòng khám, hợp tâm lý bảo mật.
6. **Lộ trình sáng tạo dài hạn:** hồ sơ răng điện tử theo mã QR cá nhân (mang đi phòng khám khác), nhận diện ảnh răng bằng AI để cảnh báo sâu răng sớm, kênh đặt lịch cho mạng lưới nhiều cơ sở — mở rộng tự nhiên từ cùng một bộ dữ liệu.

**Vì sao nhóm tin làm được:** phần khó nhất của một startup phần mềm là có sản phẩm chạy thật — nhóm đã có kiến trúc và đang xây sản phẩm đó trong môn học; các thành phần khởi nghiệp (QR, AI API, SaaS) là mở rộng trên nền có sẵn, đi theo vòng Lean Startup với rủi ro thấp nhất ở mỗi giai đoạn.

---

## 4. Nguồn số liệu & tài liệu tham khảo

- [GlobeNewswire — Vietnam dental services market USD 4,21 tỷ (2024) → 5,5 tỷ (2030)](https://www.globenewswire.com/news-release/2025/06/04/3093470/28124/en/vietnam-emerges-as-usd-5-5-billion-dental-tourism-hub-competitive-intelligence-highlights-significant-market-expansion-through-2030.html) (Research and Markets, 06/2025)
- [Research and Markets — Vietnam Dental Equipment Market](https://www.researchandmarkets.com/reports/6114937/vietnam-dental-equipment-market-region)
- [Brands Vietnam — thị trường nha khoa & hơn 3.000 phòng khám (Tổng cục Thống kê 2024)](https://www.brandsvietnam.com/) — dẫn lại qua tổng hợp DMA Nha khoa 09/2024
- Sở Y tế TP.HCM (11/2025): ~2.000 cơ sở RHM đăng ký — tổng hợp báo chí trong nước
- Tổng quan đối thủ: [bacsi247.com — thị trường phần mềm quản lý phòng khám tại VN](https://bacsi247.com/tin-tuc/thi-truong-phan-mem-quan-ly-phong-kham-tai-viet-nam), [base.vn — top phần mềm quản lý phòng khám](https://base.vn/blog/phan-mem-quan-ly-phong-kham), [sannhakhoa.vn — top phần mềm nha khoa](https://sannhakhoa.vn)
- Tài liệu 1665 — *"Tài liệu hướng dẫn Đổi mới Sáng tạo và Khởi nghiệp dành cho sinh viên"* (khung kế hoạch kinh doanh, BMC 9 khối, Lean Startup, Design Thinking)
