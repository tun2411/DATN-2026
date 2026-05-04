package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryStatusRequest {
    /** ASSIGNED (Đã tiếp nhận) | CLOSED (Đã xử lý) */
    private String status;
}
