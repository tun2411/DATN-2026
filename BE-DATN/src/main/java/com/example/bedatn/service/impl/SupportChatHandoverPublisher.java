package com.example.bedatn.service.impl;

import com.example.bedatn.documents.SupportChatSessionEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SupportChatHandoverPublisher {

    public static final String TOPIC_HANDOVER = "/topic/support-handover";

    private final SimpMessagingTemplate messagingTemplate;

    public SupportChatHandoverPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishHandover(SupportChatSessionEntity session, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "SUPPORT_CHAT_HANDOVER");
        payload.put("sessionId", session.getId());
        payload.put("visitorKey", session.getVisitorKey());
        payload.put("reason", reason);
        messagingTemplate.convertAndSend(TOPIC_HANDOVER, (Object) payload);
    }
}
