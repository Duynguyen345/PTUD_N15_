-- 005_seed_benhnhan.sql (chủ file: Huy)
USE N15_PTUD_DB;
GO
INSERT INTO BenhNhan (maBenhNhan, hoTen, soDienThoai, cccd, ngaySinh, diaChi, tienSuDiUng) VALUES
('BN2611010001', N'Nguyễn Văn An',      '0901111001', '079199001001', '1990-05-04', N'Q. Thủ Đức, TP.HCM',   N'Dịu thuốc Penicillin'),
('BN2611010002', N'Trần Thị Bình',      '0901111002', '079198502002', '1985-08-19', N'Q. Bình Thạnh, TP.HCM', NULL),
('BN2611010003', N'Lê Hoàng Cương',     '0901111003', NULL,           '2001-12-01', N'TP. Thủ Đức',           N'Dị ứng tê Lidocaine'),
('BN2611010004', N'Phạm Mỹ Duyên',      '0901111004', '079199803004', '1998-02-14', N'Q. Gò Vấp, TP.HCM',     NULL),
('BN2611010005', N'Hoàng Gia Đức',      '0901111005', '079200104005', '2001-04-27', N'Dĩ An, Bình Dương',     NULL),
('BN2611010006', N'Đỗ Khánh Giang',     '0901111006', NULL,           '1979-10-09', N'Q. Tân Bình, TP.HCM',   N'Tiêu chảy khi dùng kháng sinh'),
('BN2611010007', N'Bùi Thanh Hà',       '0901111007', '079199205007', '1992-06-30', N'Q. Phú Nhuận, TP.HCM',  NULL),
('BN2611010008', N'Ngô Trí Hùng',       '0901111008', '079198806008', '1988-01-11', N'Thu Duc, TP.HCM',       NULL),
('BN2611010009', N'Lý Tú Kiên',         '0901111009', NULL,           '2005-03-22', N'Q. 9, TP.HCM',          NULL),
('BN2611010010', N'Trương Ngọc Lam',    '0901111010', '079199407010', '1994-11-17', N'Q. Bình Tân, TP.HCM',   NULL);
GO
PRINT N'005_seed_benhnhan OK';
GO
