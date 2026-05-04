package com.example.bedatn.controller;

import com.example.bedatn.dto.request.SupportChatVisitorMessageRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.SupportChatPostMessageResponse;
import com.example.bedatn.dto.response.SupportChatSessionResponse;
import com.example.bedatn.service.SupportChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support-chat")
public class SupportChatAPI {

    private final SupportChatService supportChatService;

    public SupportChatAPI(SupportChatService supportChatService) {
        this.supportChatService = supportChatService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<SupportChatPostMessageResponse>> postMessage(@RequestBody SupportChatVisitorMessageRequest request) {
        SupportChatPostMessageResponse data = supportChatService.postVisitorMessage(request);
        ApiResponse<SupportChatPostMessageResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(data);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> getSession(@RequestParam("visitorKey") String visitorKey) {
        SupportChatSessionResponse data = supportChatService.getSessionByVisitorKey(visitorKey);
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(data);
        return ResponseEntity.ok(res);
    }
}
