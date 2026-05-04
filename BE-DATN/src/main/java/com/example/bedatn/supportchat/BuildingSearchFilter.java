package com.example.bedatn.supportchat;

import lombok.Data;

@Data
public class BuildingSearchFilter {
    /** CAN_HO | NGUYEN_CAN | DAT_NEN */
    private String typeCode;
    private String district;
    private String ward;
    private String street;
    /** Don vi dong */
    private Long minPrice;
    private Long maxPrice;
    private Double minArea;
    private Double maxArea;
    private Integer bedrooms;
    private Integer bathrooms;
    /** FOR_SALE | DEPOSIT | SOLD */
    private String saleStatus;
    /** GIAY_CHUNG_NHAN | PENDING | OTHER */
    private String legalStatus;
    /** THO_CU | NONG_NGHIEP | DAT_VUON | OTHER */
    private String landType;
    /** FULL | BASIC | RAW */
    private String furniture;
}
