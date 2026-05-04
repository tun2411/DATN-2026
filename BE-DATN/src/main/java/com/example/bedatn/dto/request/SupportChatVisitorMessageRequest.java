package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportChatVisitorMessageRequest {
    private String visitorKey;
    private String text;
    /** Khi chat từ trang chi tiết BĐS */
    private Long buildingId;
}
