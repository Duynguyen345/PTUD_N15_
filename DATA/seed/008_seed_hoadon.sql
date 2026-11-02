-- 008_seed_hoadon.sql (chủ file: Huy)
USE N15_PTUD_DB;
GO
DECLARE @d date = CAST(GETDATE() AS date);
DECLARE @t varchar(6) = FORMAT(@d, 'yyMMdd');

-- Hoa don phi kham da thu cho phieu DANG_KHAM hom nay
INSERT INTO HoaDon (maHoaDon, ngayThu, hinhThucThanhToan, daThanhToan, tienKhachDua, maBenhNhan, maNhanVien) VALUES
('HD' + @t + '0001', CAST(GETDATE() AS datetime2(0)), N'TIEN_MAT', 1, 50000.00, 'BN2611010007', 'NV2608010001');
INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGia) VALUES
('HD' + @t + '0001', 'DV0001', 1, 50000.00);
UPDATE PhieuKham SET maHoaDonPhiKham = 'HD' + @t + '0001'
WHERE maPhieuKham = 'PK' + @t + '0004';
GO
PRINT N'008_seed_hoadon OK';
GO
