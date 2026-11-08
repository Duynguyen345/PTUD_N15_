-- Khoi tao MOT LAN tren database MOI; chi giu pham vi Duy.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO
IF DB_ID('N15_DUY_DB') IS NULL CREATE DATABASE N15_DUY_DB;
GO
USE N15_DUY_DB;
GO
CREATE TABLE NhanVien (
    maNhanVien varchar(12) NOT NULL PRIMARY KEY,
    hoTen nvarchar(100) NOT NULL,
    soDienThoai varchar(10) NULL,
    ngaySinh date NULL,
    gioiTinh bit NULL,
    cccd varchar(12) NULL,
    chucVu nvarchar(10) NOT NULL CHECK(chucVu IN(N'QUAN_LY',N'LE_TAN',N'BAC_SI',N'PHU_TA')),
    conLamViec bit NOT NULL DEFAULT 1
);
CREATE TABLE TaiKhoan (
    maTaiKhoan varchar(12) NOT NULL PRIMARY KEY,
    matKhau varchar(255) NOT NULL,
    trangThai nvarchar(10) NOT NULL CHECK(trangThai IN(N'HOAT_DONG',N'BI_KHOA')),
    maNhanVien varchar(12) NOT NULL UNIQUE REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CK_TaiKhoan_MaTuSinh CHECK(LEN(maTaiKhoan)=12 AND LEFT(maTaiKhoan,2)='TK' AND SUBSTRING(maTaiKhoan,3,10) NOT LIKE '%[^0-9]%')
);
CREATE TABLE DichVu (
    maDichVu varchar(6) NOT NULL PRIMARY KEY,
    tenDichVu nvarchar(100) NOT NULL UNIQUE,
    donGiaNiemYet decimal(18,2) NOT NULL CHECK(donGiaNiemYet>0),
    trangThai nvarchar(15) NOT NULL CHECK(trangThai IN(N'DANG_PHUC_VU',N'NGUNG_PHUC_VU'))
);
CREATE TABLE HangHoa (
    maHangHoa varchar(12) NOT NULL PRIMARY KEY,
    tenHangHoa nvarchar(100) NOT NULL UNIQUE,
    donViTinh nvarchar(30) NOT NULL,
    soLuongTon int NOT NULL CHECK(soLuongTon>=0),
    mucTonToiThieu int NOT NULL CHECK(mucTonToiThieu>=0)
);
CREATE TABLE DuyBoDem (
    khoa varchar(10) NOT NULL PRIMARY KEY,
    giaTri int NOT NULL CHECK(giaTri BETWEEN 0 AND 9999)
);
-- Chi giu hai bang nguon bao cao, khong trien khai luong kham benh.
CREATE TABLE PhieuKham (
    maPhieuKham varchar(12) NOT NULL PRIMARY KEY,
    ngayKham date NOT NULL,
    trangThai nvarchar(10) NOT NULL CHECK(trangThai IN(N'CHO_KHAM',N'DANG_KHAM',N'HOAN_TAT')),
    maNhanVien varchar(12) NOT NULL REFERENCES NhanVien(maNhanVien)
);
CREATE TABLE ChiDinhDichVu (
    maPhieuKham varchar(12) NOT NULL REFERENCES PhieuKham(maPhieuKham),
    maDichVu varchar(6) NOT NULL REFERENCES DichVu(maDichVu),
    PRIMARY KEY(maPhieuKham,maDichVu)
);
GO
