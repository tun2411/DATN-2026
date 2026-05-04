package com.example.bedatn.controller;

import com.example.bedatn.dto.request.EventRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.EventResponse;
import com.example.bedatn.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventAPI {

    private final EventService eventService;

    public EventAPI(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> listPublished() {
        ApiResponse<List<EventResponse>> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(eventService.listPublished());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getPublished(@PathVariable Long id) {
        ApiResponse<EventResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(eventService.getPublished(id));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/admin/list")
    public ResponseEntity<ApiResponse<List<EventResponse>>> listAll() {
        ApiResponse<List<EventResponse>> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(eventService.listAllForAdmin());
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> create(@RequestBody EventRequest request) {
        ApiResponse<EventResponse> res = new ApiResponse<>();
        res.setMessage("Created");
        res.setData(eventService.create(request));
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> update(@PathVariable Long id, @RequestBody EventRequest request) {
        ApiResponse<EventResponse> res = new ApiResponse<>();
        res.setMessage("Updated");
        res.setData(eventService.update(id, request));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        eventService.delete(id);
        ApiResponse<Void> res = new ApiResponse<>();
        res.setMessage("Deleted");
        return ResponseEntity.ok(res);
    }
}
