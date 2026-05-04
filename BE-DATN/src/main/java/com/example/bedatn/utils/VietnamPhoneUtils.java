package com.example.bedatn.utils;

/**
 * Chuẩn hóa và kiểm tra số điện thoại di động Việt Nam (10 chữ số, bắt đầu bằng 0).
 */
public final class VietnamPhoneUtils {

    private VietnamPhoneUtils() {
    }

    /** Bỏ khoảng trắng, dấu gạch ngang, dấu chấm thường gặp khi nhập form. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("[\\s\\-\\.]", "");
    }

    /**
     * Di động VN 10 số: 0 + chữ số thứ 2 từ 3–9 + 8 chữ số (ví dụ 03x, 05x, 06x, 07x, 08x, 09x…).
     * Không khớp cố định mã 03|05|07|08|09 vì một số đầu số hợp lệ có thể bị loại nhầm.
     */
    public static boolean isValidMobile10Digits(String raw) {
        String p = normalize(raw);
        return p.matches("^0[3-9]\\d{8}$");
    }
}
