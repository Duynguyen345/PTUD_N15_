-- 005_benhnhan.sql (chủ file: Huy)
USE N15_PTUD_DB;
GO
CREATE TABLE BenhNhan (
    maBenhNhan   varchar(12)   NOT NULL PRIMARY KEY,  -- BNyyMMddNNNN
    hoTen        nvarchar(100) NOT NULL,
    soDienThoai  varchar(10)   NOT NULL CHECK (soDienThoai LIKE '0%' AND LEN(soDienThoai) = 10),
    cccd         varchar(12)   NULL UNIQUE CHECK (cccd IS NULL OR LEN(cccd) = 12),
    ngaySinh     date          NOT NULL,
    diaChi       nvarchar(200) NULL,
    tienSuDiUng  nvarchar(500) NULL
);
GO
PRINT N'005_benhnhan OK';
GO
