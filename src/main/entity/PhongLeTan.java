package entity;

public record PhongLeTan(String maPhong, String tenPhong, boolean dangNhanBenh,
        String maBacSi, String tenBacSi, String maPhuTa, String tenPhuTa,
        int soCho, int soDangKham) {
    public boolean sanSang() {
        return dangNhanBenh && maBacSi != null && maPhuTa != null;
    }
}
