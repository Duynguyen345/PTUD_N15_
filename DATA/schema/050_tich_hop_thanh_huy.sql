-- Chon database hien tai trong SSMS. Chay SAU 040_le_tan.sql va 040_thanh_dieu_phoi.sql.
-- Khong reset, khong nap seed, khong doi tai khoan/du lieu quan ly.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
IF COL_LENGTH('dbo.BenhNhan','gioiTinh') IS NULL
    ALTER TABLE dbo.BenhNhan ADD gioiTinh nvarchar(10) NULL;
IF COL_LENGTH('dbo.BenhNhan','tienSu') IS NULL
    ALTER TABLE dbo.BenhNhan ADD tienSu nvarchar(1000) NULL;
IF COL_LENGTH('dbo.BenhNhan','diUng') IS NULL
    ALTER TABLE dbo.BenhNhan ADD diUng nvarchar(500) NULL;
GO
-- Ho so le tan cho phep chua biet ngay sinh/gioi tinh/dia chi.
ALTER TABLE dbo.BenhNhan ALTER COLUMN ngaySinh date NULL;
ALTER TABLE dbo.BenhNhan ALTER COLUMN gioiTinh nvarchar(10) NULL;
ALTER TABLE dbo.BenhNhan ALTER COLUMN diaChi nvarchar(250) NULL;
ALTER TABLE dbo.BenhNhan ALTER COLUMN tienSu nvarchar(1000) NULL;
ALTER TABLE dbo.BenhNhan ALTER COLUMN diUng nvarchar(500) NULL;
ALTER TABLE dbo.BenhNhan ALTER COLUMN tienSuDiUng nvarchar(1500) NULL;
GO
-- Xac nhan phi cua cac phieu le tan da co hoa don phi kham hop le.
UPDATE pk SET daThanhToanPhiKham=1
FROM dbo.PhieuKham pk JOIN dbo.HoaDon hd ON hd.maHoaDon=pk.maHoaDonPhiKham
WHERE hd.maBenhNhan=pk.maBenhNhan AND hd.maPhieuKham IS NULL
  AND EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon ct
              WHERE ct.maHoaDon=hd.maHoaDon AND ct.maDichVu='DV0001');
GO
