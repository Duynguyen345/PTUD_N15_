-- Chạy trên database của ứng dụng Duy sau 001_fresh_database.sql và 030_account_ids.sql.
-- Chỉ bổ sung dữ liệu UC002/003/005, thanh toán và ca thu ngân; giữ các bảng quản lý hiện có.


use N15_DUY_DB
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
    CREATE TABLE dbo.BenhNhan (
        maBenhNhan varchar(12) NOT NULL PRIMARY KEY,
        hoTen nvarchar(100) NOT NULL,
        soDienThoai varchar(10) NOT NULL,
        ngaySinh date NULL,
        cccd varchar(12) NULL,
        diaChi nvarchar(200) NULL,
        tienSuDiUng nvarchar(300) NULL
    );
GO
IF COL_LENGTH('dbo.BenhNhan','cccd') IS NULL ALTER TABLE dbo.BenhNhan ADD cccd varchar(12) NULL;
IF COL_LENGTH('dbo.BenhNhan','tienSuDiUng') IS NULL ALTER TABLE dbo.BenhNhan ADD tienSuDiUng nvarchar(1500) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.BenhNhan') AND name='UQ_LeTan_SDT')
    CREATE UNIQUE INDEX UQ_LeTan_SDT ON dbo.BenhNhan(soDienThoai);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.BenhNhan') AND name='UQ_LeTan_CCCD')
    CREATE UNIQUE INDEX UQ_LeTan_CCCD ON dbo.BenhNhan(cccd) WHERE cccd IS NOT NULL;
GO
IF OBJECT_ID(N'dbo.PhongKham', N'U') IS NULL
    CREATE TABLE dbo.PhongKham (
        maPhong varchar(6) NOT NULL PRIMARY KEY,
        tenPhong nvarchar(60) NOT NULL UNIQUE,
        dangNhanBenh bit NOT NULL DEFAULT 0,
        maBacSi varchar(12) NULL REFERENCES dbo.NhanVien(maNhanVien),
        maPhuTa varchar(12) NULL REFERENCES dbo.NhanVien(maNhanVien)
    );
GO
IF COL_LENGTH('dbo.PhongKham','maPhuTa') IS NULL ALTER TABLE dbo.PhongKham ADD maPhuTa varchar(12) NULL REFERENCES dbo.NhanVien(maNhanVien);
GO
IF COL_LENGTH('dbo.PhieuKham','soThuTu') IS NULL ALTER TABLE dbo.PhieuKham ADD soThuTu int NULL;
IF COL_LENGTH('dbo.PhieuKham','maBenhNhan') IS NULL ALTER TABLE dbo.PhieuKham ADD maBenhNhan varchar(12) NULL;
IF COL_LENGTH('dbo.PhieuKham','maPhong') IS NULL ALTER TABLE dbo.PhieuKham ADD maPhong varchar(6) NULL;
IF COL_LENGTH('dbo.PhieuKham','lyDoKham') IS NULL ALTER TABLE dbo.PhieuKham ADD lyDoKham nvarchar(300) NULL;
IF COL_LENGTH('dbo.PhieuKham','maHoaDonPhiKham') IS NULL ALTER TABLE dbo.PhieuKham ADD maHoaDonPhiKham varchar(12) NULL;
GO
IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL
    CREATE TABLE dbo.HoaDon (
        maHoaDon varchar(12) NOT NULL PRIMARY KEY,
        maBenhNhan varchar(12) NOT NULL REFERENCES dbo.BenhNhan(maBenhNhan),
        maPhieuKham varchar(12) NULL REFERENCES dbo.PhieuKham(maPhieuKham),
        maNhanVien varchar(12) NOT NULL REFERENCES dbo.NhanVien(maNhanVien),
        ngayThu datetime2(0) NOT NULL,
        hinhThucThanhToan varchar(15) NOT NULL CHECK(hinhThucThanhToan IN('TIEN_MAT','CHUYEN_KHOAN')),
        tongTien decimal(18,2) NOT NULL CHECK(tongTien>0),
        tienKhachDua decimal(18,2) NOT NULL,
        CONSTRAINT CK_LeTan_HoaDon_TienKhachDua CHECK(tienKhachDua>=tongTien)
    );
GO
IF OBJECT_ID(N'dbo.ChiTietHoaDon',N'U') IS NULL
    CREATE TABLE dbo.ChiTietHoaDon (
        maHoaDon varchar(12) NOT NULL REFERENCES dbo.HoaDon(maHoaDon),
        maDichVu varchar(6) NOT NULL REFERENCES dbo.DichVu(maDichVu),
        donGia decimal(18,2) NOT NULL CHECK(donGia>0),
        CONSTRAINT PK_LeTan_ChiTietHoaDon PRIMARY KEY(maHoaDon,maDichVu)
    );
GO
IF COL_LENGTH('dbo.ChiDinhDichVu','donGia') IS NULL
    ALTER TABLE dbo.ChiDinhDichVu ADD donGia decimal(18,2) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name='FK_LeTan_PK_BN')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_LeTan_PK_BN FOREIGN KEY(maBenhNhan) REFERENCES dbo.BenhNhan(maBenhNhan);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name='FK_LeTan_PK_Phong')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_LeTan_PK_Phong FOREIGN KEY(maPhong) REFERENCES dbo.PhongKham(maPhong);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name='FK_LeTan_PK_HD')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_LeTan_PK_HD FOREIGN KEY(maHoaDonPhiKham) REFERENCES dbo.HoaDon(maHoaDon);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhieuKham') AND name='UQ_LeTan_STT')
    CREATE UNIQUE INDEX UQ_LeTan_STT ON dbo.PhieuKham(ngayKham,maPhong,soThuTu) WHERE maPhong IS NOT NULL AND soThuTu IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhieuKham') AND name='UQ_LeTan_Phi')
    CREATE UNIQUE INDEX UQ_LeTan_Phi ON dbo.PhieuKham(maHoaDonPhiKham) WHERE maHoaDonPhiKham IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.HoaDon') AND name='UQ_LeTan_HoaDonPhieu')
    CREATE UNIQUE INDEX UQ_LeTan_HoaDonPhieu ON dbo.HoaDon(maPhieuKham) WHERE maPhieuKham IS NOT NULL;
GO
IF OBJECT_ID(N'dbo.SoThuTuPhong',N'U') IS NULL
    CREATE TABLE dbo.SoThuTuPhong (
        ngayKham date NOT NULL,
        maPhong varchar(6) NOT NULL REFERENCES dbo.PhongKham(maPhong),
        giaTri int NOT NULL CHECK(giaTri>0),
        CONSTRAINT PK_LeTan_SoThuTuPhong PRIMARY KEY(ngayKham,maPhong)
    );
IF OBJECT_ID(N'dbo.CaThuNgan',N'U') IS NULL
    CREATE TABLE dbo.CaThuNgan (
        maCa varchar(12) NOT NULL PRIMARY KEY,
        maNhanVien varchar(12) NOT NULL REFERENCES dbo.NhanVien(maNhanVien),
        thoiGianMo datetime2(0) NOT NULL,
        thoiGianChot datetime2(0) NULL,
        doanhThuDichVu decimal(18,2) NULL,
        phiKhamThuHo decimal(18,2) NULL
    );
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.CaThuNgan') AND name='UQ_LeTan_CaDangMo')
    CREATE UNIQUE INDEX UQ_LeTan_CaDangMo ON dbo.CaThuNgan(thoiGianChot) WHERE thoiGianChot IS NULL;
GO
