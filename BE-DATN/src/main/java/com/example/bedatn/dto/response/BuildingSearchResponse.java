package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BuildingSearchResponse extends AbstractResponse<Object> {
    private String name;
    private String address;
    private Long numberOfBasement;
    private String managerName;
    private String managerPhone;
    private Long floorArea;
    private String rentArea;
    private String emptyArea;
    private Long rentPrice;
    private String serviceFee;
    private Float brokerageFee;
    /** Loại BĐS (chuỗi mã, có thể phân tách bằng dấu phẩy) — khớp field `type` trên Mongo */
    private String type;
    private String note;
    /** Đường dẫn ảnh lưu trên server, ví dụ /building/ten-file.jpg */
    private String avatar;

    /** FOR_SALE | DEPOSIT | SOLD */
    private String saleStatus;

    private Long linkedCustomerId;

    /** Hiển thị nhanh: tên + SĐT khách (nếu có) */
    private String linkedCustomerSummary;
}
