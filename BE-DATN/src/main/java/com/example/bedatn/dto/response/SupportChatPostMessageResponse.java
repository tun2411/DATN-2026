package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportChatPostMessageResponse {
    private SupportChatSessionResponse session;
    /** Tin bot trả lời ngay (khi chưa có nhân viên); null nếu đã có nhân viên */
    private String botReply;
}
