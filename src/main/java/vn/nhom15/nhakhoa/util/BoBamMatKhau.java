package vn.nhom15.nhakhoa.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash mới có salt riêng. Chấp nhận SHA-256 cũ để dùng dữ liệu seed của nhóm. */
public final class BoBamMatKhau {
    private final SecureRandom boNgauNhien = new SecureRandom();

    public String bamMatKhau(char[] matKhau) {
        byte[] muoi = new byte[16];
        boNgauNhien.nextBytes(muoi);
        return "pbkdf2$210000$" + Base64.getEncoder().encodeToString(muoi) + "$"
                + Base64.getEncoder().encodeToString(taoKhoaDanXuat(matKhau, muoi, 210000));
    }

    public boolean kiemTraMatKhau(char[] matKhau, String chuoiDaLuu) {
        if (chuoiDaLuu == null)
            return false;
        try {
            if (chuoiDaLuu.matches("[0-9a-fA-F]{64}")) {
                byte[] mangByte = new String(matKhau).getBytes(StandardCharsets.UTF_8);
                try {
                    return MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(mangByte),
                            HexFormat.of().parseHex(chuoiDaLuu));
                } finally {
                    Arrays.fill(mangByte, (byte) 0);
                }
            }
            String[] cacPhan = chuoiDaLuu.split("\\$");
            if (cacPhan.length != 4 || !cacPhan[0].equals("pbkdf2"))
                return false;
            int soLanLap = Integer.parseInt(cacPhan[1]);
            if (soLanLap < 10000 || soLanLap > 1000000)
                return false;
            return MessageDigest.isEqual(
                    taoKhoaDanXuat(matKhau, Base64.getDecoder().decode(cacPhan[2]), soLanLap),
                    Base64.getDecoder().decode(cacPhan[3]));
        } catch (Exception loi) {
            return false;
        }
    }

    private byte[] taoKhoaDanXuat(char[] matKhau, byte[] muoi, int soVong) {
        PBEKeySpec dacTaKhoa = new PBEKeySpec(matKhau, muoi, soVong, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(dacTaKhoa)
                    .getEncoded();
        } catch (Exception loi) {
            throw new IllegalStateException("Không khởi tạo được bộ băm mật khẩu.", loi);
        } finally {
            dacTaKhoa.clearPassword();
        }
    }
}
