-- DATABASE CU: chay 020 truoc, chon dung database trong SSMS.
-- Doi cot tenDangNhap thanh maTaiKhoan va cap ma cho tai khoan cu.
-- Khong thay mat khau, nhan vien hoac trang thai. Luu bang doi ma duoc tra ve.
SET XACT_ABORT ON;
IF COL_LENGTH('dbo.TaiKhoan','maTaiKhoan') IS NULL
   AND COL_LENGTH('dbo.TaiKhoan','tenDangNhap') IS NOT NULL
    EXEC sp_rename 'dbo.TaiKhoan.tenDangNhap','maTaiKhoan','COLUMN';
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @key varchar(10)='TK'+CONVERT(char(6),GETDATE(),12), @number int=0;
DECLARE @old nvarchar(50), @new varchar(12);
DECLARE @mapping TABLE(maCu nvarchar(50),maMoi varchar(12));
SELECT @number=giaTri FROM dbo.DuyBoDem WITH(UPDLOCK,HOLDLOCK) WHERE khoa=@key;
DECLARE old_accounts CURSOR LOCAL STATIC FOR
SELECT maTaiKhoan FROM dbo.TaiKhoan WITH(UPDLOCK,HOLDLOCK)
WHERE LEN(maTaiKhoan)<>12 OR LEFT(maTaiKhoan,2)<>'TK' OR SUBSTRING(maTaiKhoan,3,10) LIKE '%[^0-9]%'
ORDER BY maTaiKhoan;
OPEN old_accounts;
FETCH NEXT FROM old_accounts INTO @old;
WHILE @@FETCH_STATUS=0
BEGIN
    WHILE 1=1
    BEGIN
        SET @number=@number+1;
        IF @number>9999 THROW 51000,'Het dai ma tai khoan trong ngay',1;
        SET @new=@key+RIGHT('0000'+CAST(@number AS varchar(4)),4);
        IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE maTaiKhoan=@new) BREAK;
    END;
    UPDATE dbo.TaiKhoan SET maTaiKhoan=@new WHERE maTaiKhoan=@old;
    INSERT INTO @mapping VALUES(@old,@new);
    FETCH NEXT FROM old_accounts INTO @old;
END;
CLOSE old_accounts;
DEALLOCATE old_accounts;
IF EXISTS(SELECT 1 FROM dbo.DuyBoDem WHERE khoa=@key) UPDATE dbo.DuyBoDem SET giaTri=@number WHERE khoa=@key;
ELSE INSERT INTO dbo.DuyBoDem(khoa,giaTri) VALUES(@key,@number);
IF OBJECT_ID('dbo.CK_TaiKhoan_MaTuSinh','C') IS NULL
    ALTER TABLE dbo.TaiKhoan WITH CHECK ADD CONSTRAINT CK_TaiKhoan_MaTuSinh
    CHECK(LEN(maTaiKhoan)=12 AND LEFT(maTaiKhoan,2)='TK' AND SUBSTRING(maTaiKhoan,3,10) NOT LIKE '%[^0-9]%');
COMMIT;
SELECT maCu,maMoi FROM @mapping;
SELECT maTaiKhoan,maNhanVien,trangThai FROM dbo.TaiKhoan ORDER BY maTaiKhoan;
GO
