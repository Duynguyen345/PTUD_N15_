
package model;

public class NhanVien {

    // 1. Các thuộc tính
    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private ChucVu chucVu;

    // 2. Constructor mặc định
    public NhanVien() {
    }

    // 3. Constructor tạo nhân viên mới (chưa có mã)
    public NhanVien(String hoTen, String soDienThoai,
                    ChucVu chucVu) {

        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setChucVu(chucVu);
    }

    // 4. Constructor cho nhân viên đã có mã
    public NhanVien(String maNhanVien, String hoTen,
                    String soDienThoai, ChucVu chucVu) {

        this(hoTen, soDienThoai, chucVu);

        if (maNhanVien == null ||
                !maNhanVien.matches("^NV[0-9]{6}(?!0000)[0-9]{4}$")) {
            throw new IllegalArgumentException(
                    "Mã nhân viên không hợp lệ."
            );
        }

        this.maNhanVien = maNhanVien;
    }

    // 5. Copy constructor
    public NhanVien(NhanVien original) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "Nhân viên nguồn không được null."
            );
        }

        this.maNhanVien = original.maNhanVien;
        this.hoTen = original.hoTen;
        this.soDienThoai = original.soDienThoai;
        this.chucVu = original.chucVu;
    }

    // 6. Getter và Setter
    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException(
                    "Họ tên nhân viên không hợp lệ."
            );
        }

        String chuanHoa =
                hoTen.trim().replaceAll("\\s+", " ");

        if (chuanHoa.codePoints()
                .noneMatch(Character::isLetter)) {
            throw new IllegalArgumentException(
                    "Họ tên nhân viên không hợp lệ."
            );
        }

        this.hoTen = chuanHoa;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null ||
                !soDienThoai.matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException(
                    "Số điện thoại nhân viên không hợp lệ."
            );
        }

        this.soDienThoai = soDienThoai;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        if (chucVu == null) {
            throw new IllegalArgumentException(
                    "Chức vụ nhân viên không hợp lệ."
            );
        }

        this.chucVu = chucVu;
    }

    // 7. Phương thức toString()
    @Override
    public String toString() {
        return "NhanVien [maNhanVien=" + maNhanVien
                + ", hoTen=" + hoTen
                + ", soDienThoai=" + soDienThoai
                + ", chucVu=" + chucVu + "]";
    }
}
