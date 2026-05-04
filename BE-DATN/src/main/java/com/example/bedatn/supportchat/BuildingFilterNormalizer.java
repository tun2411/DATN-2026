package com.example.bedatn.supportchat;

import org.springframework.stereotype.Component;

@Component
public class BuildingFilterNormalizer {

    public BuildingSearchFilter normalize(BuildingSearchFilter f) {
        if (f == null) {
            return new BuildingSearchFilter();
        }

        f.setTypeCode(normalizeTypeCode(f.getTypeCode()));
        f.setSaleStatus(normalizeSaleStatus(f.getSaleStatus()));
        f.setLegalStatus(normalizeLegalStatus(f.getLegalStatus()));
        f.setFurniture(normalizeFurniture(f.getFurniture()));
        f.setLandType(normalizeLandType(f.getLandType()));
        return f;
    }

    private String normalizeTypeCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase().trim()) {
            case "căn hộ", "chung cư", "can_ho", "apartment" -> "CAN_HO";
            case "nhà", "nguyên căn", "nguyen_can", "house" -> "NGUYEN_CAN";
            case "đất", "đất nền", "dat_nen", "land" -> "DAT_NEN";
            default -> raw.toUpperCase().trim();
        };
    }

    private String normalizeSaleStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase().trim()) {
            case "đang bán", "mở bán", "for_sale" -> "FOR_SALE";
            case "đã cọc", "cọc", "deposit" -> "DEPOSIT";
            case "đã bán", "sold" -> "SOLD";
            default -> raw.toUpperCase().trim();
        };
    }

    private String normalizeLegalStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase().trim()) {
            case "giấy chứng nhận", "giay chung nhan", "giay_chung_nhan" -> "GIAY_CHUNG_NHAN";
            case "chờ sổ", "đang chờ", "pending" -> "PENDING";
            default -> raw.toUpperCase().trim();
        };
    }

    private String normalizeFurniture(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase().trim()) {
            case "đầy đủ", "full" -> "FULL";
            case "cơ bản", "basic" -> "BASIC";
            case "thô", "bàn giao thô", "raw" -> "RAW";
            default -> raw.toUpperCase().trim();
        };
    }

    private String normalizeLandType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase().trim()) {
            case "thổ cư", "tho cu", "tho_cu" -> "THO_CU";
            case "nông nghiệp", "nong_nghiep" -> "NONG_NGHIEP";
            case "đất vườn", "dat vuon", "dat_vuon" -> "DAT_VUON";
            default -> raw.toUpperCase().trim();
        };
    }
}
