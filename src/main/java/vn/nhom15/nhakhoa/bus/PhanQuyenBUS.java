package vn.nhom15.nhakhoa.bus;

import java.util.Set;
import vn.nhom15.nhakhoa.entity.ChucVu;
import vn.nhom15.nhakhoa.entity.Quyen;

/** Chính sách vai trò dùng chung cho menu và kiểm tra nghiệp vụ. */
public final class PhanQuyenBUS {
    private PhanQuyenBUS() { }

    /** Trả tập quyền bất biến của vai trò; không nhận vai trò từ form đăng nhập. */
    public static Set<Quyen> layQuyen(ChucVu chucVu) {
        if (chucVu == null) return Set.of();
        return switch (chucVu) {
            case QUAN_LY -> Set.of(Quyen.values());
            case LE_TAN -> Set.of(Quyen.TRA_CUU_HO_SO);
            case BAC_SI -> Set.of(Quyen.XEM_HANG_CHO, Quyen.BAT_TAT_PHONG, Quyen.TRA_CUU_HO_SO);
            case PHU_TA -> Set.of(Quyen.XEM_HANG_CHO, Quyen.DIEU_PHOI_HANG_CHO, Quyen.BAT_TAT_PHONG);
        };
    }
}
