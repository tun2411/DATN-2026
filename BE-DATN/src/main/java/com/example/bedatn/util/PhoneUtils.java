package com.example.bedatn.util;

/**
 * Chuẩn hóa SĐT Việt Nam để lưu và kiểm tra trùng (một số chỉ một tài khoản).
 */
public final class PhoneUtils {

    private PhoneUtils() {
    }

    /** Bỏ khoảng trắng, +84 → 0… */
    public static String normalizeVietnamPhone(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().replaceAll("[\\s.\\-()]", "");
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        if (s.startsWith("84") && s.length() >= 10) {
            s = "0" + s.substring(2);
        }
        return s;
    }

    /** Di động VN: 0 + 9–10 chữ số (vd. 0912345678) */
    public static boolean isValidVietnamMobile(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        return normalized.matches("^0[0-9]{9,10}$");
    }
}
