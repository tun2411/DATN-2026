package com.example.bedatn.service;

import com.example.bedatn.dto.request.EventRequest;
import com.example.bedatn.dto.response.EventResponse;

import java.util.List;

public interface EventService {

    List<EventResponse> listPublished();

    EventResponse getPublished(Long id);

    List<EventResponse> listAllForAdmin();

    EventResponse create(EventRequest request);

    EventResponse update(Long id, EventRequest request);

    void delete(Long id);
}
