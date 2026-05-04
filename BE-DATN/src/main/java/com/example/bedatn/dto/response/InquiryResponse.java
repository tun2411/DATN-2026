package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryResponse extends AbstractResponse<InquiryResponse> {
    private Long buildingId;
    private String buildingName;
    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    private String type;
    private String note;
    private String status;
    private Long assignedStaffId;
    private String assignedStaffName;
}
