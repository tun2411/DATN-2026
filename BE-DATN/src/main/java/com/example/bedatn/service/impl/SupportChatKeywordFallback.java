package com.example.bedatn.service.impl;

import com.example.bedatn.documents.ChatMessageDoc;
import com.example.bedatn.documents.SupportChatSessionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Fallback khi không có Gemini hoặc gọi API lỗi — trả lời theo từ khóa đơn giản.
 */
@Component
public class SupportChatKeywordFallback {

    private static final String DEFAULT =
            "Cảm ơn bạn đã liên hệ EziSolution. Bạn có thể hỏi về dự án, giá, hoặc để lại nhu cầu — "
                    + "nhân viên sẽ tiếp nhận hội thoại sớm nhất có thể.";

    public String replyFromLastUserMessage(SupportChatSessionEntity session) {
        String userMessage = lastUserText(session);
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Bạn vui lòng nhập nội dung cần hỗ trợ.";
        }
        String t = userMessage.toLowerCase(Locale.ROOT);
        if (t.contains("giá") || t.contains("gia ") || t.contains("bao nhiêu") || t.contains("gia ban")) {
            return "Giá từng căn/dự án phụ thuộc vị trí và thời điểm. Bạn cho mình biết khu vực hoặc mã dự án quan tâm, "
                    + "hoặc xem mục Sản phẩm trên website.";
        }
        if (t.contains("dự án") || t.contains("du an") || t.contains("bất động sản") || t.contains("can ho") || t.contains("căn hộ")) {
            return "EziSolution có nhiều dự án căn hộ, nhà phố, biệt thự. Bạn ưu tiên mua hay thuê, và khu vực nào?";
        }
        if (t.contains("liên hệ") || t.contains("hotline") || t.contains("sdt") || t.contains("điện thoại") || t.contains("phone")) {
            return "Bạn có thể để lại SĐT hoặc email trong khung chat; team sẽ gọi lại.";
        }
        if (t.contains("xin chào") || t.contains("chào") || t.contains("hello") || t.contains("hi ")) {
            return "Xin chào! Mình là trợ lý EziSolution. Bạn cần tư vấn về dự án, pháp lý hay lịch xem nhà?";
        }
        if (t.contains("cảm ơn") || t.contains("cam on")) {
            return "Rất vui được hỗ trợ bạn. Nếu cần thêm thông tin, cứ nhắn tiếp nhé!";
        }
        return DEFAULT;
    }

    private static String lastUserText(SupportChatSessionEntity session) {
        List<ChatMessageDoc> list = session.getMessages();
        for (int i = list.size() - 1; i >= 0; i--) {
            ChatMessageDoc m = list.get(i);
            if ("USER".equals(m.getRole())) {
                return m.getBody();
            }
        }
        return "";
    }
}
