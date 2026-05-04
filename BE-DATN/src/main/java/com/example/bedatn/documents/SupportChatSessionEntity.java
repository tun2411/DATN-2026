package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "support_chat_session")
@Getter
@Setter
public class SupportChatSessionEntity extends BaseEntity {

    /** Khóa duy nhất từ trình duyệt khách (khách vãng lai); null nếu phiên đăng nhập */
    private String visitorKey;
    /** User đăng nhập (role USER); null = khách vãng lai */
    private Long userId;
    /** BĐS gắn hội thoại (bắt buộc với user đăng nhập; tùy chọn với khách) */
    private Long buildingId;
    /** null = đang chat với bot / chờ nhân viên */
    private Long assignedStaffId;
    /** OPEN | STAFF | CLOSED */
    private String status;
    /** Khách đã yêu cầu / bot đã chuyển cho nhân viên */
    private Boolean handoverRequested;
    /** Lead do tool capture_lead lưu */
    private String guestPhone;
    private String guestEmail;
    private String leadNote;
    private List<ChatMessageDoc> messages;

    public List<ChatMessageDoc> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }
}
