package com.example.bedatn.enums;

import java.util.Map;
import java.util.TreeMap;

public enum RentType {
    TANG_TRET("Tầng trệt"),
    NGUYEN_CAN("Nguyên căn"),
    NOI_THAT("Nội thất");


    private final String name;

    RentType(String name) {
        this.name = name;
    }

    public static Map<String,String> getType(){
        Map<String,String> types = new TreeMap<>();
        for (RentType type : RentType.values()){
            types.put(type.toString(), type.name);
        }
        return types;
    }
}
