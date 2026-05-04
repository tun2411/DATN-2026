package com.example.bedatn.service.impl;

import com.example.bedatn.documents.ChatMessageDoc;
import com.example.bedatn.documents.SupportChatSessionEntity;
import com.example.bedatn.service.SupportChatBotService;
import com.example.bedatn.supportchat.EziChatPrompts;
import com.example.bedatn.supportchat.SupportChatContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Trợ lý chat: ưu tiên Spring AI (Gemini) + {@link EziChatTools}; fallback từ khóa nếu không có model hoặc lỗi API.
 */
@Service
public class SupportChatAssistantService implements SupportChatBotService {

    private static final Logger log = LoggerFactory.getLogger(SupportChatAssistantService.class);

    private final ObjectProvider<ChatModel> chatModel;
    private final EziChatTools eziChatTools;
    private final SupportChatKeywordFallback keywordFallback;

    @Value("${spring.ai.google.genai.api-key:}")
    private String googleGenAiApiKey;

    public SupportChatAssistantService(ObjectProvider<ChatModel> chatModel,
                                       EziChatTools eziChatTools,
                                       SupportChatKeywordFallback keywordFallback) {
        this.chatModel = chatModel;
        this.eziChatTools = eziChatTools;
        this.keywordFallback = keywordFallback;
    }

    @Override
    public String generateReply(SupportChatSessionEntity session) {
        if (!isGeminiConfigured()) {
            return keywordFallback.replyFromLastUserMessage(session);
        }
        ChatModel model = chatModel.getIfAvailable();
        if (model == null) {
            return keywordFallback.replyFromLastUserMessage(session);
        }
        SupportChatContextHolder.setSessionId(session.getId());
        try {
            String transcript = buildTranscript(session);
            String userPrompt = """
                    Dưới đây là toàn bộ hội thoại (khách mới nhất ở cuối). Hãy trả lời **một** tin nhắn tiếp theo của trợ lý Ezi,
                    ngắn gọn, phù hợp ngữ cảnh. Không lặp lại tiền tố "Khách:" hay "Trợ lý".

                    """ + transcript;
            return ChatClient.create(model)
                    .prompt()
                    .system(EziChatPrompts.SYSTEM)
                    .user(userPrompt)
                    .tools(eziChatTools)
                    .call()
                    .content();
        } catch (Exception e) {
            if (isQuotaOrRateLimit(e)) {
                // 429 / quota: da xu ly bang fallback — mac dinh khong in ra console (chi khi bat DEBUG)
                log.debug("Gemini quota/rate limit, keyword fallback. {}", shortApiHint(e));
            } else {
                log.warn("Gemini API error (using keyword fallback). {} | root: {}",
                        e.getMessage(),
                        rootCauseMessage(e),
                        e);
            }
            return keywordFallback.replyFromLastUserMessage(session);
        } finally {
            SupportChatContextHolder.clear();
        }
    }

    private boolean isGeminiConfigured() {
        String k = googleGenAiApiKey;
        if (k == null || k.isBlank()) {
            return false;
        }
        return !k.startsWith("__MISSING_");
    }

    private static String buildTranscript(SupportChatSessionEntity session) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessageDoc m : session.getMessages()) {
            String label = switch (m.getRole()) {
                case "USER" -> "Khách";
                case "BOT" -> "Trợ lý Ezi";
                case "STAFF" -> "Nhân viên";
                default -> m.getRole();
            };
            sb.append(label).append(": ").append(m.getBody()).append("\n");
        }
        return sb.toString().trim();
    }

    /** Lấy message từ Throwable gốc (Google API thường bọc nhiều lớp). */
    private static String rootCauseMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String m = t.getMessage();
        return (m != null && !m.isBlank()) ? m : String.valueOf(t);
    }

    /** 429 / free tier het quota — log ngan, khong spam stack trace. */
    private static boolean isQuotaOrRateLimit(Throwable e) {
        Throwable t = e;
        while (t != null) {
            String m = t.getMessage();
            if (m != null) {
                String u = m.toUpperCase();
                if (u.contains("429") || m.contains("Quota exceeded") || m.contains("RESOURCE_EXHAUSTED")
                        || m.contains("rate limit") || m.contains("exceeded your current quota")) {
                    return true;
                }
            }
            String name = t.getClass().getName();
            if (name.contains("ClientException") || name.contains("ApiException")) {
                if (m != null && m.contains("429")) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    /** Mot dong goi y (retry) neu message Google co chua. */
    private static String shortApiHint(Throwable e) {
        String full = rootCauseMessage(e);
        if (full.contains("Please retry in")) {
            int i = full.indexOf("Please retry in");
            return full.substring(i).split("\\n")[0].trim();
        }
        return "See https://ai.google.dev/gemini-api/docs/rate-limits";
    }
}
