-- CAI DAT TOAN BO CAC CHUC NANG HIEN CO: Java Swing -> BUS -> JDBC -> SQL Server.
-- Mo file bang SSMS, ket noi SQL Server, nhan Execute (F5) de chay toan bo.
-- Database rieng N15_NHAKHOA_DB; khong thay doi N15_PTUD_DB/N15_DUY_DB cu.
-- Moi bang 6 ban ghi mau; PhieuKham co 12 (6 luot hom nay + 6 lan kham cu).
-- Chay lai chi them ma con thieu, khong xoa/ghi de du lieu da sua.
-- Tai khoan TK2601010001..0005: mat khau 123456; TK2601010006 bi khoa.
USE master;
GO
IF DB_ID(N'N15_NHAKHOA_DB') IS NULL
    EXEC(N'CREATE DATABASE N15_NHAKHOA_DB');
GO
USE N15_NHAKHOA_DB;
GO
SET XACT_ABORT ON;
SET NOCOUNT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
BEGIN TRY
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
CREATE TABLE dbo.NhanVien (
    maNhanVien varchar(12) NOT NULL PRIMARY KEY,
    hoTen nvarchar(100) NOT NULL,
    soDienThoai varchar(10) NULL,
    ngaySinh date NULL,
    gioiTinh bit NULL,
    cccd varchar(12) NULL,
    chucVu nvarchar(10) NOT NULL CHECK(chucVu IN(N'QUAN_LY',N'LE_TAN',N'BAC_SI',N'PHU_TA')),
    conLamViec bit NOT NULL DEFAULT 1
);
IF OBJECT_ID(N'dbo.TaiKhoan', N'U') IS NULL
CREATE TABLE dbo.TaiKhoan (
    maTaiKhoan varchar(12) NOT NULL PRIMARY KEY,
    matKhau varchar(255) NOT NULL,
    trangThai nvarchar(10) NOT NULL CHECK(trangThai IN(N'HOAT_DONG',N'BI_KHOA')),
    maNhanVien varchar(12) NOT NULL UNIQUE REFERENCES dbo.NhanVien(maNhanVien),
    CONSTRAINT CK_TaiKhoan_MaTuSinh CHECK(LEN(maTaiKhoan)=12 AND LEFT(maTaiKhoan,2)='TK'
        AND SUBSTRING(maTaiKhoan,3,10) NOT LIKE '%[^0-9]%')
);
IF OBJECT_ID(N'dbo.DichVu', N'U') IS NULL
CREATE TABLE dbo.DichVu (
    maDichVu varchar(6) NOT NULL PRIMARY KEY,
    tenDichVu nvarchar(100) NOT NULL UNIQUE,
    donGiaNiemYet decimal(18,2) NOT NULL CHECK(donGiaNiemYet>0),
    trangThai nvarchar(15) NOT NULL CHECK(trangThai IN(N'DANG_PHUC_VU',N'NGUNG_PHUC_VU'))
);
IF OBJECT_ID(N'dbo.HangHoa', N'U') IS NULL
CREATE TABLE dbo.HangHoa (
    maHangHoa varchar(12) NOT NULL PRIMARY KEY,
    tenHangHoa nvarchar(100) NOT NULL UNIQUE,
    donViTinh nvarchar(30) NOT NULL,
    soLuongTon int NOT NULL CHECK(soLuongTon>=0),
    mucTonToiThieu int NOT NULL CHECK(mucTonToiThieu>=0)
);
IF OBJECT_ID(N'dbo.DuyBoDem', N'U') IS NULL
CREATE TABLE dbo.DuyBoDem (
    khoa varchar(10) NOT NULL PRIMARY KEY,
    giaTri int NOT NULL CHECK(giaTri BETWEEN 0 AND 9999)
);
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
IF OBJECT_ID(N'dbo.PhieuKham', N'U') IS NULL
CREATE TABLE dbo.PhieuKham (
    maPhieuKham varchar(12) NOT NULL PRIMARY KEY,
    ngayKham date NOT NULL,
    trangThai nvarchar(10) NOT NULL CHECK(trangThai IN(N'CHO_KHAM',N'DANG_KHAM',N'HOAN_TAT')),
    maNhanVien varchar(12) NOT NULL REFERENCES dbo.NhanVien(maNhanVien),
    maBenhNhan varchar(12) NULL REFERENCES dbo.BenhNhan(maBenhNhan),
    maPhong varchar(6) NULL REFERENCES dbo.PhongKham(maPhong),
    soThuTu int NULL,
    daThanhToanPhiKham bit NOT NULL DEFAULT 0,
    trangThaiHangCho varchar(12) NOT NULL DEFAULT 'CHO_KHAM',
    thoiDiemGoi datetime2(0) NULL,
    CONSTRAINT CK_PhieuKham_SoThuTu CHECK(soThuTu IS NULL OR
        (soThuTu>0 AND maPhong IS NOT NULL AND maBenhNhan IS NOT NULL)),
    CONSTRAINT CK_PhieuKham_TrangThaiCho CHECK(trangThaiHangCho IN('CHO_KHAM','DANG_GOI','DANG_KHAM','VANG_MAT'))
);
IF OBJECT_ID(N'dbo.ChiDinhDichVu', N'U') IS NULL
CREATE TABLE dbo.ChiDinhDichVu (
    maPhieuKham varchar(12) NOT NULL REFERENCES dbo.PhieuKham(maPhieuKham),
    maDichVu varchar(6) NOT NULL REFERENCES dbo.DichVu(maDichVu),
    PRIMARY KEY(maPhieuKham,maDichVu)
);
IF OBJECT_ID(N'dbo.HoSoKham', N'U') IS NULL
CREATE TABLE dbo.HoSoKham (
    maPhieuKham varchar(12) NOT NULL PRIMARY KEY REFERENCES dbo.PhieuKham(maPhieuKham),
    chanDoan nvarchar(1000) NOT NULL DEFAULT N'',
    dieuTri nvarchar(2000) NOT NULL DEFAULT N'',
    ghiChu nvarchar(1000) NOT NULL DEFAULT N''
);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='UX_PhieuKham_STT_Phong_Ngay'
        AND object_id=OBJECT_ID(N'dbo.PhieuKham'))
    CREATE UNIQUE INDEX UX_PhieuKham_STT_Phong_Ngay ON dbo.PhieuKham(maPhong,ngayKham,soThuTu)
        WHERE soThuTu IS NOT NULL AND maPhong IS NOT NULL;

INSERT INTO dbo.NhanVien(maNhanVien,hoTen,soDienThoai,ngaySinh,gioiTinh,cccd,chucVu,conLamViec)
SELECT v.* FROM (VALUES
    ('NV2601010001',N'Lê Hoàng Minh','0901234501',CONVERT(date,'19850515'),1,'079000000001',N'QUAN_LY',1),
    ('NV2601010002',N'Nguyễn Thị Anh','0912345678',CONVERT(date,'19960220'),0,'079000000002',N'LE_TAN',1),
    ('NV2601010003',N'Nguyễn Văn An','0901234567',CONVERT(date,'19900310'),1,'079000000003',N'BAC_SI',1),
    ('NV2601010004',N'Trần Minh Bình','0987654321',CONVERT(date,'19980418'),1,'079000000004',N'PHU_TA',1),
    ('NV2601010005',N'Phạm Thu Hà','0901234505',CONVERT(date,'19890612'),0,'079000000005',N'BAC_SI',1),
    ('NV2601010006',N'Đặng Ngọc Mai','0901234506',CONVERT(date,'19990814'),0,'079000000006',N'LE_TAN',1)
) v(ma,ten,sdt,sinh,gt,cccd,cv,lam)
WHERE NOT EXISTS(SELECT 1 FROM dbo.NhanVien n WHERE n.maNhanVien=v.ma);

-- Hash SHA-256 seed duoc BoBamMatKhau ho tro. Tai khoan tao tu GUI dung PBKDF2.
INSERT INTO dbo.TaiKhoan(maTaiKhoan,matKhau,trangThai,maNhanVien)
SELECT v.ma,CONVERT(varchar(64),HASHBYTES('SHA2_256','123456'),2),v.tt,v.nv FROM (VALUES
    ('TK2601010001',N'HOAT_DONG','NV2601010001'),
    ('TK2601010002',N'HOAT_DONG','NV2601010002'),
    ('TK2601010003',N'HOAT_DONG','NV2601010003'),
    ('TK2601010004',N'HOAT_DONG','NV2601010004'),
    ('TK2601010005',N'HOAT_DONG','NV2601010005'),
    ('TK2601010006',N'BI_KHOA','NV2601010006')
) v(ma,tt,nv)
WHERE NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan t WHERE t.maTaiKhoan=v.ma OR t.maNhanVien=v.nv);

INSERT INTO dbo.DichVu(maDichVu,tenDichVu,donGiaNiemYet,trangThai)
SELECT v.* FROM (VALUES
    ('DV0001',N'Khám ban đầu',100000,N'DANG_PHUC_VU'),
    ('DV0002',N'Lấy cao răng',250000,N'DANG_PHUC_VU'),
    ('DV0003',N'Trám răng thẩm mỹ',350000,N'DANG_PHUC_VU'),
    ('DV0004',N'Nhổ răng thường',500000,N'DANG_PHUC_VU'),
    ('DV0005',N'Chụp X-quang răng',150000,N'DANG_PHUC_VU'),
    ('DV0006',N'Tẩy trắng răng',1500000,N'NGUNG_PHUC_VU')
) v(ma,ten,gia,tt)
WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVu d WHERE d.maDichVu=v.ma);

INSERT INTO dbo.HangHoa(maHangHoa,tenHangHoa,donViTinh,soLuongTon,mucTonToiThieu)
SELECT v.* FROM (VALUES
    ('HH2601010001',N'Găng tay y tế',N'Hộp',8,10),
    ('HH2601010002',N'Khẩu trang y tế',N'Hộp',30,10),
    ('HH2601010003',N'Gạc vô trùng',N'Gói',0,5),
    ('HH2601010004',N'Thuốc tê nha khoa',N'Hộp',12,10),
    ('HH2601010005',N'Vật liệu trám',N'Tuýp',5,5),
    ('HH2601010006',N'Ống hút nước bọt',N'Túi',20,8)
) v(ma,ten,dv,ton,minTon)
WHERE NOT EXISTS(SELECT 1 FROM dbo.HangHoa h WHERE h.maHangHoa=v.ma);

INSERT INTO dbo.BenhNhan(maBenhNhan,hoTen,ngaySinh,gioiTinh,soDienThoai,diaChi,tienSu,diUng)
SELECT v.* FROM (VALUES
    ('BN2601010001',N'Nguyễn Minh Châu',CONVERT(date,'19970312'),N'Nữ','0911110001',N'Quận 3, TP.HCM',N'Đã điều trị tủy răng 36',N'Penicillin'),
    ('BN2601010002',N'Trần Quốc Bảo',CONVERT(date,'19880722'),N'Nam','0911110002',N'Quận 10, TP.HCM',N'Chưa ghi nhận',N'Chưa ghi nhận'),
    ('BN2601010003',N'Lê Ngọc Anh',CONVERT(date,'20010105'),N'Nữ','0911110003',N'Bình Thạnh, TP.HCM',N'Đang chỉnh nha',N'Chưa ghi nhận'),
    ('BN2601010004',N'Phạm Hoàng Nam',CONVERT(date,'19921116'),N'Nam','0911110004',N'Thủ Đức, TP.HCM',N'Viêm nướu tái phát',N'Chưa ghi nhận'),
    ('BN2601010005',N'Võ Thanh Hà',CONVERT(date,'19780909'),N'Nữ','0911110005',N'Quận 5, TP.HCM',N'Tăng huyết áp',N'Chưa ghi nhận'),
    ('BN2601010006',N'Đặng Gia Huy',CONVERT(date,'20040618'),N'Nam','0911110006',N'Quận 7, TP.HCM',N'Chưa ghi nhận',N'Latex')
) v(ma,ten,sinh,gt,sdt,dc,ts,du)
WHERE NOT EXISTS(SELECT 1 FROM dbo.BenhNhan b WHERE b.maBenhNhan=v.ma);

INSERT INTO dbo.PhongKham(maPhong,tenPhong,maBacSi,dangNhanBenh)
SELECT v.* FROM (VALUES
    ('P01',N'Phòng khám 01','NV2601010003',1),
    ('P02',N'Phòng khám 02','NV2601010005',1),
    ('P03',N'Phòng khám 03','NV2601010005',0),
    ('P04',N'Phòng khám 04','NV2601010003',1),
    ('P05',N'Phòng khám 05','NV2601010003',1),
    ('P06',N'Phòng khám 06','NV2601010005',0)
) v(ma,ten,bs,nhan)
WHERE NOT EXISTS(SELECT 1 FROM dbo.PhongKham p WHERE p.maPhong=v.ma);

-- Ngay tham chieu la ngay Viet Nam tai lan nap dau; khong tu doi ngay khi chay lai.
DECLARE @homNay date=CONVERT(date,SYSDATETIMEOFFSET() AT TIME ZONE 'SE Asia Standard Time');
INSERT INTO dbo.PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,maPhong,
    soThuTu,daThanhToanPhiKham,trangThaiHangCho,thoiDiemGoi)
SELECT v.ma,@homNay,v.tt,v.bs,v.bn,v.phong,v.stt,v.phi,v.cho,
    CASE WHEN v.cho IN('DANG_GOI','DANG_KHAM','VANG_MAT') THEN CONVERT(datetime2(0),@homNay) ELSE NULL END
FROM (VALUES
    ('PK2601010001',N'CHO_KHAM','NV2601010003','BN2601010001','P01',1,1,'CHO_KHAM'),
    ('PK2601010002',N'CHO_KHAM','NV2601010003','BN2601010002','P01',2,1,'CHO_KHAM'),
    ('PK2601010003',N'CHO_KHAM','NV2601010003','BN2601010003','P01',3,0,'CHO_KHAM'),
    ('PK2601010004',N'CHO_KHAM','NV2601010003','BN2601010004','P01',4,1,'VANG_MAT'),
    ('PK2601010005',N'DANG_KHAM','NV2601010005','BN2601010005','P02',1,1,'DANG_KHAM'),
    ('PK2601010006',N'CHO_KHAM','NV2601010005','BN2601010006','P03',1,1,'CHO_KHAM')
) v(ma,tt,bs,bn,phong,stt,phi,cho)
WHERE NOT EXISTS(SELECT 1 FROM dbo.PhieuKham p WHERE p.maPhieuKham=v.ma);

-- Moi benh nhan co mot lan kham hoan tat de tra cuu chan doan/dieu tri.
INSERT INTO dbo.PhieuKham(maPhieuKham,ngayKham,trangThai,maNhanVien,maBenhNhan,daThanhToanPhiKham)
SELECT v.ma,DATEADD(day,v.lech,@homNay),N'HOAN_TAT',v.bs,v.bn,1 FROM (VALUES
    ('PK2601010101',-6,'NV2601010003','BN2601010001'),
    ('PK2601010102',-5,'NV2601010005','BN2601010002'),
    ('PK2601010103',-4,'NV2601010003','BN2601010003'),
    ('PK2601010104',-3,'NV2601010005','BN2601010004'),
    ('PK2601010105',-2,'NV2601010003','BN2601010005'),
    ('PK2601010106',-1,'NV2601010005','BN2601010006')
) v(ma,lech,bs,bn)
WHERE NOT EXISTS(SELECT 1 FROM dbo.PhieuKham p WHERE p.maPhieuKham=v.ma);

INSERT INTO dbo.HoSoKham(maPhieuKham,chanDoan,dieuTri,ghiChu)
SELECT v.* FROM (VALUES
    ('PK2601010101',N'Sâu răng 36',N'Điều trị tủy',N'Đã hoàn tất; hẹn tái khám.'),
    ('PK2601010102',N'Cao răng',N'Lấy cao răng',N'Hướng dẫn vệ sinh răng miệng.'),
    ('PK2601010103',N'Sâu răng 16',N'Trám răng thẩm mỹ',N'Kiểm tra khớp cắn sau trám.'),
    ('PK2601010104',N'Răng 48 lung lay',N'Nhổ răng thường',N'Tái khám sau 7 ngày.'),
    ('PK2601010105',N'Đau răng hàm',N'Chụp X-quang và tư vấn',N'Theo dõi huyết áp trước điều trị.'),
    ('PK2601010106',N'Khám răng định kỳ',N'Khám và hướng dẫn chăm sóc',N'Sử dụng găng không chứa latex.')
) v(ma,cd,dt,gc)
WHERE NOT EXISTS(SELECT 1 FROM dbo.HoSoKham h WHERE h.maPhieuKham=v.ma);

INSERT INTO dbo.ChiDinhDichVu(maPhieuKham,maDichVu)
SELECT v.* FROM (VALUES
    ('PK2601010101','DV0001'),('PK2601010102','DV0002'),
    ('PK2601010103','DV0003'),('PK2601010104','DV0004'),
    ('PK2601010105','DV0005'),('PK2601010106','DV0001')
) v(pk,dv)
WHERE NOT EXISTS(SELECT 1 FROM dbo.ChiDinhDichVu c WHERE c.maPhieuKham=v.pk AND c.maDichVu=v.dv);

INSERT INTO dbo.DuyBoDem(khoa,giaTri)
SELECT v.* FROM (VALUES
    ('NV260101',6),('TK260101',6),('DV',6),('HH260101',6),('BN260101',6),('PK260101',106)
) v(khoa,gt)
WHERE NOT EXISTS(SELECT 1 FROM dbo.DuyBoDem d WHERE d.khoa=v.khoa);

COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    THROW;
END CATCH;
GO
-- Ket qua kiem tra: 9 bang co 6 dong, PhieuKham co 12 dong tren database moi.
SELECT N'NhanVien' AS Bang,COUNT(*) AS SoDong FROM dbo.NhanVien
UNION ALL SELECT N'TaiKhoan',COUNT(*) FROM dbo.TaiKhoan
UNION ALL SELECT N'DichVu',COUNT(*) FROM dbo.DichVu
UNION ALL SELECT N'HangHoa',COUNT(*) FROM dbo.HangHoa
UNION ALL SELECT N'DuyBoDem',COUNT(*) FROM dbo.DuyBoDem
UNION ALL SELECT N'BenhNhan',COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT N'PhongKham',COUNT(*) FROM dbo.PhongKham
UNION ALL SELECT N'PhieuKham',COUNT(*) FROM dbo.PhieuKham
UNION ALL SELECT N'HoSoKham',COUNT(*) FROM dbo.HoSoKham
UNION ALL SELECT N'ChiDinhDichVu',COUNT(*) FROM dbo.ChiDinhDichVu;
SELECT t.maTaiKhoan,n.hoTen,n.chucVu,t.trangThai
FROM dbo.TaiKhoan t JOIN dbo.NhanVien n ON n.maNhanVien=t.maNhanVien ORDER BY t.maTaiKhoan;
GO
