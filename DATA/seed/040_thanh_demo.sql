-- Generated from src/main/resources/demo/dieu_phoi.tsv by DATA/GenerateThanhSeed.java
-- Chay SAU schema 040 va seed 001_duy_demo.sql trong database demo/test.
-- Khong ghi de du lieu cu. Ngay kham duoc tinh tai lan chay seed dau tien.
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
BEGIN TRANSACTION;
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010001')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010001',N'Nguyễn Minh Châu',N'1997-03-12',N'Nữ',N'0911110001',N'Quận 3, TP.HCM',N'Đã điều trị tủy răng 36',N'Penicillin');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010002')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010002',N'Trần Quốc Bảo',N'1988-07-22',N'Nam',N'0911110002',N'Quận 10, TP.HCM',N'Chưa ghi nhận',N'Chưa ghi nhận');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010003')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010003',N'Lê Ngọc Anh',N'2001-01-05',N'Nữ',N'0911110003',N'Bình Thạnh, TP.HCM',N'Đang chỉnh nha',N'Chưa ghi nhận');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010004')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010004',N'Phạm Hoàng Nam',N'1992-11-16',N'Nam',N'0911110004',N'Thủ Đức, TP.HCM',N'Viêm nướu tái phát',N'Chưa ghi nhận');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010005')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010005',N'Võ Thanh Hà',N'1978-09-09',N'Nữ',N'0911110005',N'Quận 5, TP.HCM',N'Tăng huyết áp',N'Chưa ghi nhận');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010006')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010006',N'Đặng Gia Huy',N'2004-06-18',N'Nam',N'0911110006',N'Quận 7, TP.HCM',N'Chưa ghi nhận',N'Chưa ghi nhận');
IF NOT EXISTS(SELECT 1 FROM BenhNhan WHERE maBenhNhan=N'BN2601010007')
INSERT INTO BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng) VALUES(N'BN2601010007',N'Bùi Thảo Vy',N'1999-12-01',N'Nữ',N'0911110007',N'Tân Bình, TP.HCM',N'Chưa ghi nhận',N'Latex');
IF NOT EXISTS(SELECT 1 FROM PhongKham WHERE maPhong=N'P01')
INSERT INTO PhongKham(maPhong,tenPhong,maBacSi,dangNhanBenh) VALUES(N'P01',N'Phòng khám 01',N'NV2601010003',1);
IF NOT EXISTS(SELECT 1 FROM PhongKham WHERE maPhong=N'P02')
INSERT INTO PhongKham(maPhong,tenPhong,maBacSi,dangNhanBenh) VALUES(N'P02',N'Phòng khám 02',N'NV2601010008',1);
IF NOT EXISTS(SELECT 1 FROM PhongKham WHERE maPhong=N'P03')
INSERT INTO PhongKham(maPhong,tenPhong,maBacSi,dangNhanBenh) VALUES(N'P03',N'Phòng khám 03',N'NV2601010005',0);
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019001')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019001',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010001',maPhong,1,1,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P01';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019002')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019002',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010002',maPhong,2,1,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P01';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019003')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019003',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010003',maPhong,3,0,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P01';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019004')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019004',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010004',maPhong,4,1,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P01';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019005')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019005',DATEADD(day,0,CONVERT(date,GETDATE())),N'DANG_KHAM',maBacSi,N'BN2601010005',maPhong,1,1,N'DANG_KHAM',DATEADD(hour,8,CONVERT(datetime2,DATEADD(day,0,CONVERT(date,GETDATE())))) FROM PhongKham WHERE maPhong=N'P02';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019006')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019006',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010006',maPhong,2,1,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P02';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019007')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT N'PK2601019007',DATEADD(day,0,CONVERT(date,GETDATE())),N'CHO_KHAM',maBacSi,N'BN2601010007',maPhong,1,1,N'CHO_KHAM',NULL FROM PhongKham WHERE maPhong=N'P03';
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019101')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan) VALUES(N'PK2601019101',DATEADD(day,-30,CONVERT(date,GETDATE())),N'HOAN_TAT',N'NV2601010003',N'BN2601010001');
IF NOT EXISTS(SELECT 1 FROM HoSoKham WHERE maPhieuKham=N'PK2601019101')
INSERT INTO HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu) VALUES(N'PK2601019101',N'Sâu răng 36',N'Điều trị tủy',N'Đã hoàn tất; hẹn tái khám.');
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019102')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan) VALUES(N'PK2601019102',DATEADD(day,-7,CONVERT(date,GETDATE())),N'HOAN_TAT',N'NV2601010003',N'BN2601010001');
IF NOT EXISTS(SELECT 1 FROM HoSoKham WHERE maPhieuKham=N'PK2601019102')
INSERT INTO HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu) VALUES(N'PK2601019102',N'Kiểm tra sau điều trị',N'Kiểm tra khớp cắn',N'Tiến triển ổn định.');
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019103')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan) VALUES(N'PK2601019103',DATEADD(day,-15,CONVERT(date,GETDATE())),N'HOAN_TAT',N'NV2601010008',N'BN2601010002');
IF NOT EXISTS(SELECT 1 FROM HoSoKham WHERE maPhieuKham=N'PK2601019103')
INSERT INTO HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu) VALUES(N'PK2601019103',N'Cao răng',N'Lấy cao răng',N'Hướng dẫn vệ sinh răng miệng.');
IF NOT EXISTS(SELECT 1 FROM PhieuKham WHERE maPhieuKham=N'PK2601019104')
INSERT INTO PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan) VALUES(N'PK2601019104',DATEADD(day,-10,CONVERT(date,GETDATE())),N'HOAN_TAT',N'NV2601010003',N'BN2601010004');
IF NOT EXISTS(SELECT 1 FROM HoSoKham WHERE maPhieuKham=N'PK2601019104')
INSERT INTO HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu) VALUES(N'PK2601019104',N'Viêm nướu',N'Vệ sinh nha chu',N'Hẹn tái khám sau điều trị.');
COMMIT;
GO
