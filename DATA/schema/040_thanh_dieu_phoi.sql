-- Chay SAU schema 001 (hoac database da nang cap 020 + 030).
-- Chon dung database trong SSMS truoc khi chay; khong xoa/bien doi du lieu cu.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL OR OBJECT_ID(N'dbo.PhieuKham', N'U') IS NULL
    THROW 50001, 'Can tao schema goc truoc migration 040.', 1;
GO
IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
CREATE TABLE dbo.BenhNhan (
    maBenhNhan varchar(12) NOT NULL PRIMARY KEY,
    hoTen nvarchar(100) NOT NULL,
    ngaySinh date NOT NULL,
    gioiTinh nvarchar(10) NOT NULL,
    soDienThoai varchar(10) NOT NULL,
    diaChi nvarchar(250) NOT NULL,
    tienSu nvarchar(1000) NOT NULL DEFAULT N'',
    diUng nvarchar(500) NOT NULL DEFAULT N'',
    CONSTRAINT CK_BenhNhan_Ma CHECK(LEN(maBenhNhan)=12 AND LEFT(maBenhNhan,2)='BN'
        AND SUBSTRING(maBenhNhan,3,10) NOT LIKE '%[^0-9]%'),
    CONSTRAINT CK_BenhNhan_Sdt CHECK(LEN(soDienThoai)=10 AND soDienThoai NOT LIKE '%[^0-9]%')
);
IF OBJECT_ID(N'dbo.PhongKham', N'U') IS NULL
CREATE TABLE dbo.PhongKham (
    maPhong varchar(6) NOT NULL PRIMARY KEY,
    tenPhong nvarchar(100) NOT NULL UNIQUE,
    maBacSi varchar(12) NOT NULL REFERENCES dbo.NhanVien(maNhanVien),
    dangNhanBenh bit NOT NULL DEFAULT 0
);
GO
-- Cac cot nullable cho phep giu nguyen phieu bao cao cu chua gan benh nhan/phong.
IF COL_LENGTH('dbo.PhieuKham','maBenhNhan') IS NULL
    ALTER TABLE dbo.PhieuKham ADD maBenhNhan varchar(12) NULL;
IF COL_LENGTH('dbo.PhieuKham','maPhong') IS NULL
    ALTER TABLE dbo.PhieuKham ADD maPhong varchar(6) NULL;
IF COL_LENGTH('dbo.PhieuKham','soThuTu') IS NULL
    ALTER TABLE dbo.PhieuKham ADD soThuTu int NULL;
IF COL_LENGTH('dbo.PhieuKham','daThanhToanPhiKham') IS NULL
    ALTER TABLE dbo.PhieuKham ADD daThanhToanPhiKham bit NOT NULL
        CONSTRAINT DF_PhieuKham_DaTraPhi DEFAULT 0;
IF COL_LENGTH('dbo.PhieuKham','trangThaiHangCho') IS NULL
    ALTER TABLE dbo.PhieuKham ADD trangThaiHangCho varchar(12) NOT NULL
        CONSTRAINT DF_PhieuKham_HangCho DEFAULT 'CHO_KHAM';
IF COL_LENGTH('dbo.PhieuKham','thoiDiemGoi') IS NULL
    ALTER TABLE dbo.PhieuKham ADD thoiDiemGoi datetime2(0) NULL;
GO
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_PhieuKham_BenhNhan')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_PhieuKham_BenhNhan
        FOREIGN KEY(maBenhNhan) REFERENCES dbo.BenhNhan(maBenhNhan);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_PhieuKham_PhongKham')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_PhieuKham_PhongKham
        FOREIGN KEY(maPhong) REFERENCES dbo.PhongKham(maPhong);
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='CK_PhieuKham_SoThuTu')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT CK_PhieuKham_SoThuTu
        CHECK(soThuTu IS NULL OR (soThuTu>0 AND maPhong IS NOT NULL AND maBenhNhan IS NOT NULL));
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='CK_PhieuKham_TrangThaiCho')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT CK_PhieuKham_TrangThaiCho
        CHECK(trangThaiHangCho IN ('CHO_KHAM','DANG_GOI','DANG_KHAM','VANG_MAT'));
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='UX_PhieuKham_STT_Phong_Ngay'
        AND object_id=OBJECT_ID('dbo.PhieuKham'))
    CREATE UNIQUE INDEX UX_PhieuKham_STT_Phong_Ngay ON dbo.PhieuKham(maPhong,ngayKham,soThuTu)
        WHERE soThuTu IS NOT NULL AND maPhong IS NOT NULL;
IF OBJECT_ID(N'dbo.HoSoKham', N'U') IS NULL
CREATE TABLE dbo.HoSoKham (
    maPhieuKham varchar(12) NOT NULL PRIMARY KEY REFERENCES dbo.PhieuKham(maPhieuKham),
    chanDoan nvarchar(1000) NOT NULL DEFAULT N'',
    dieuTri nvarchar(2000) NOT NULL DEFAULT N'',
    ghiChu nvarchar(1000) NOT NULL DEFAULT N''
);
GO
-- Cac module cung ghi PhieuKham phai khoa PhongKham (UPDLOCK,HOLDLOCK) truoc.
-- Huy: gan maBenhNhan, maPhong, soThuTu va daThanhToanPhiKham sau khi thu phi.
-- Thang: hoan tat bang PhieuKham.trangThai='HOAN_TAT', ghi HoSoKham.
