package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryRequest {
    private Long buildingId;
    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    /** INTEREST | CALLBACK */
    private String type;
    private String note;
}
