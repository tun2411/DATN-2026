package com.example.bedatn.enums;

/**
 * Trạng thái giao dịch bán/thuê BĐS trên hệ thống.
 */
public enum BuildingSaleStatus {
    /** Đang mở bán / cho thuê */
    FOR_SALE,
    /** Đã đặt cọc */
    DEPOSIT,
    /** Đã bán (hoặc đã chốt giao dịch) */
    SOLD
}
