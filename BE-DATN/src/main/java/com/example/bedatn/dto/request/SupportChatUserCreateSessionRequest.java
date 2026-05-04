package com.example.bedatn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportChatUserCreateSessionRequest {
    private Long buildingId;
    /** true khi khách bấm "Quan tâm" — thêm tin bot chào đầu tiên nếu hội thoại còn trống */
    private Boolean interestOpening;
}
