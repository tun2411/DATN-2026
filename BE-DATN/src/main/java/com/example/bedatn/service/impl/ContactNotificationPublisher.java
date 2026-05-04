package com.example.bedatn.service.impl;

import com.example.bedatn.documents.CustomerEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ContactNotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public ContactNotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishContactLead(CustomerEntity customer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "CONTACT_LEAD");
        payload.put("customerId", customer.getId());
        payload.put("fullName", customer.getFullName());
        payload.put("phone", customer.getPhone());
        payload.put("email", customer.getEmail());
        messagingTemplate.convertAndSend("/topic/admin-notifications", (Object) payload);
    }
}
