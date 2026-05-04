package com.example.bedatn.enums;

public enum LegalStatus {
    GIAY_CHUNG_NHAN,
    // Legacy values kept only so old Mongo documents can still be deserialized.
    SO_DO,
    SO_HONG,
    PENDING,
    OTHER
}
