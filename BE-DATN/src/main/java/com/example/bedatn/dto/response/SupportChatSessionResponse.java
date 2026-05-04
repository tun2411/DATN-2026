package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SupportChatSessionResponse {
    private Long id;
    private String visitorKey;
    private Long userId;
    private String customerCode;
    private String customerName;
    private Long buildingId;
    private String buildingName;
    private String buildingAvatar;
    private Long assignedStaffId;
    private String assignedStaffName;
    private String status;
    private List<SupportChatMessageEntryResponse> messages;
}
