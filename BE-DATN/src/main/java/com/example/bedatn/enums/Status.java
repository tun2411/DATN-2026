package com.example.bedatn.enums;

import java.util.Map;
import java.util.TreeMap;

public enum Status {
    CHUA_XU_LY("Chưa xử lý"),
    DA_XU_LY("Đã xử lý");


    private final String statusName;

    Status(String statusName) {
        this.statusName = statusName;
    }

    public String getName() {
        return statusName;
    }

    public static Map<String,String> getStatus(){
        Map<String,String> statuses = new TreeMap<>();
        for(Status status:Status.values()){
            statuses.put(status.statusName,status.statusName);
        }
        return statuses;
    }

    // Phương thức mới: Trả về statusName dựa trên Status
    public static String getNameByStatus(Status status) {
        if (status == null) {
            return null;
        }
        return status.getName();
    }

}
