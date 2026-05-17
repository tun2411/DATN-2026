package com.example.bedatn.utils;

import java.util.Map;
import java.util.TreeMap;

public enum DistrictCode {
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

    DistrictCode(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public static Map<String, String> type() {
        Map<String, String> listType = new TreeMap<>();
        for (DistrictCode item : DistrictCode.values()) {
            listType.put(item.toString(), item.districtName);
        }
        return listType;
    }

}
