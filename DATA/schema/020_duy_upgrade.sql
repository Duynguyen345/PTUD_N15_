-- Chon database dich trong SSMS (N15_DUY_DB neu cai moi).
-- Khong DROP/TRUNCATE du lieu. Chay sau 001_fresh_database.sql.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL OR OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
    THROW 51000, 'Thieu schema goc: chay 001_fresh_database.sql tren database moi truoc.', 1;

-- Tang kich thuoc de luu PBKDF2 (hash SHA-256 cu van dang nhap duoc).
ALTER TABLE dbo.TaiKhoan ALTER COLUMN matKhau varchar(255) NOT NULL;

IF OBJECT_ID(N'dbo.DuyBoDem',N'U') IS NULL
    CREATE TABLE dbo.DuyBoDem (
        khoa varchar(10) NOT NULL PRIMARY KEY,
        giaTri int NOT NULL CHECK(giaTri BETWEEN 0 AND 9999)
    );

IF OBJECT_ID(N'dbo.HangHoa',N'U') IS NULL
    CREATE TABLE dbo.HangHoa (
        maHangHoa varchar(12) NOT NULL PRIMARY KEY,
        tenHangHoa nvarchar(100) NOT NULL UNIQUE,
        donViTinh nvarchar(30) NOT NULL,
        soLuongTon int NOT NULL CHECK(soLuongTon >= 0),
        mucTonToiThieu int NOT NULL CHECK(mucTonToiThieu >= 0)
    );
-- Kho cu co the dung varchar(10), trong khi ma HHyyMMddNNNN dai 12.
IF COL_LENGTH('dbo.HangHoa','maHangHoa') < 12
    ALTER TABLE dbo.HangHoa ALTER COLUMN maHangHoa varchar(12) NOT NULL;
COMMIT;
GO
