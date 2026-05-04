package com.example.bedatn.service.impl;

import com.example.bedatn.documents.EventEntity;
import com.example.bedatn.dto.request.EventRequest;
import com.example.bedatn.dto.response.EventResponse;
import com.example.bedatn.repository.EventRepository;
import com.example.bedatn.service.EventService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final List<String> VALID_STATUS = Arrays.asList("DRAFT", "PUBLISHED");

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventResponse> listPublished() {
        return eventRepository.findByStatusOrderByStartDateDesc("PUBLISHED").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getPublished(Long id) {
        EventEntity e = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!"PUBLISHED".equals(e.getStatus())) {
            throw new IllegalArgumentException("Event not found");
        }
        return toResponse(e);
    }

    @Override
    public List<EventResponse> listAllForAdmin() {
        return eventRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getStartDate() == null && b.getStartDate() == null) {
                        return 0;
                    }
                    if (a.getStartDate() == null) {
                        return 1;
                    }
                    if (b.getStartDate() == null) {
                        return -1;
                    }
                    return b.getStartDate().compareTo(a.getStartDate());
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse create(EventRequest request) {
        validate(request, true);
        EventEntity e = new EventEntity();
        e.setId(System.currentTimeMillis());
        apply(e, request);
        return toResponse(eventRepository.save(e));
    }

    @Override
    public EventResponse update(Long id, EventRequest request) {
        validate(request, false);
        EventEntity e = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        apply(e, request);
        return toResponse(eventRepository.save(e));
    }

    @Override
    public void delete(Long id) {
        EventEntity e = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        eventRepository.delete(e);
    }

    private void validate(EventRequest request, boolean creating) {
        if (request == null) {
            throw new IllegalArgumentException("Payload is invalid");
        }
        if (creating && (request.getTitle() == null || request.getTitle().trim().isEmpty())) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            normalizeStatus(request.getStatus());
        }
    }

    private void apply(EventEntity e, EventRequest r) {
        if (r.getTitle() != null) {
            e.setTitle(r.getTitle().trim());
        }
        if (r.getSummary() != null) {
            e.setSummary(r.getSummary().trim());
        }
        if (r.getContent() != null) {
            e.setContent(r.getContent());
        }
        if (r.getImageUrl() != null) {
            e.setImageUrl(r.getImageUrl().trim());
        }
        if (r.getLocation() != null) {
            e.setLocation(r.getLocation().trim());
        }
        if (r.getStartDate() != null) {
            e.setStartDate(r.getStartDate());
        }
        if (r.getEndDate() != null) {
            e.setEndDate(r.getEndDate());
        }
        if (r.getStatus() != null && !r.getStatus().trim().isEmpty()) {
            e.setStatus(normalizeStatus(r.getStatus()));
        } else if (e.getStatus() == null || e.getStatus().isEmpty()) {
            e.setStatus("DRAFT");
        }
    }

    private String normalizeStatus(String status) {
        String v = status.trim().toUpperCase(Locale.ROOT);
        if (!VALID_STATUS.contains(v)) {
            throw new IllegalArgumentException("status must be DRAFT or PUBLISHED");
        }
        return v;
    }

    private EventResponse toResponse(EventEntity e) {
        EventResponse r = new EventResponse();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        r.setSummary(e.getSummary());
        r.setContent(e.getContent());
        r.setImageUrl(e.getImageUrl());
        r.setLocation(e.getLocation());
        r.setStartDate(e.getStartDate());
        r.setEndDate(e.getEndDate());
        r.setStatus(e.getStatus());
        return r;
    }
}
