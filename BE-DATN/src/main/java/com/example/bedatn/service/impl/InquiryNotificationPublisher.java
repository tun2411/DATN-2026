package com.example.bedatn.service.impl;

import com.example.bedatn.documents.InquiryEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InquiryNotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public InquiryNotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishCreated(InquiryEntity inquiry) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "INQUIRY_CREATED");
        payload.put("id", inquiry.getId());
        payload.put("buildingId", inquiry.getBuildingId());
        payload.put("status", inquiry.getStatus());
        messagingTemplate.convertAndSend("/topic/inquiries", (Object) payload);
    }

    public void publishUpdated(InquiryEntity inquiry, String event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("id", inquiry.getId());
        payload.put("status", inquiry.getStatus());
        payload.put("assignedStaffId", inquiry.getAssignedStaffId());
        messagingTemplate.convertAndSend("/topic/inquiries", (Object) payload);
    }
}

