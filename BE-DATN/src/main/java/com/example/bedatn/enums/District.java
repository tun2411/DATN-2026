package com.example.bedatn.enums;

import java.util.Map;
import java.util.TreeMap;

public enum District {

    HOAN_KIEM("Hoàn Kiếm"),
    BA_DINH("Ba Đình"),
    DONG_DA("Đống Đa"),
    HAI_BA_TRUNG("Hai Bà Trưng"),
    HOANG_MAI("Hoàng Mai"),
    LONG_BIEN("Long Biên"),
    TAY_HO("Tây Hồ"),
    CAU_GIAY("Cầu Giấy"),
    THANH_XUAN("Thanh Xuân"),
    HA_DONG("Hà Đông"),
    BAC_TU_LIEM("Bắc Từ Liêm"),
    NAM_TU_LIEM("Nam Từ Liêm");

    private final String districtName;

    District(String districtName) {
        this.districtName = districtName;
    }

    public String getName() {
        return districtName;
    }

    public static Map<String, String> getDistrict() {
        Map<String, String> districts = new TreeMap<>();
        for (District district : District.values()) {
            districts.put(district.toString(), district.districtName);
        }
        return districts;
    }

}
