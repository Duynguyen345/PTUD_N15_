package util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DinhDangLeTan {
    private DinhDangLeTan() { }
    public static String tien(BigDecimal so) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(so) + " ₫";
    }
    public static String gio(LocalDateTime luc) {
        return luc == null ? "" : luc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
