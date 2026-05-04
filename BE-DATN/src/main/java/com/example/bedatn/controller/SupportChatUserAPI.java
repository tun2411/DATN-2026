package com.example.bedatn.controller;

import com.example.bedatn.dto.request.SupportChatUserCreateSessionRequest;
import com.example.bedatn.dto.request.SupportChatUserMessageRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.SupportChatPostMessageResponse;
import com.example.bedatn.dto.response.SupportChatSessionResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.SupportChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Hội thoại chat theo BĐS cho khách đã đăng nhập (role USER).
 */
@RestController
@RequestMapping("/api/support-chat/me")
public class SupportChatUserAPI {

    private final SupportChatService supportChatService;

    public SupportChatUserAPI(SupportChatService supportChatService) {
        this.supportChatService = supportChatService;
    }

    /**
     * Tạo hoặc lấy session tư vấn chung (không gắn buildingId) cho widget chat góc màn hình.
     * Session này KHÔNG xuất hiện trong danh sách MessagesPage.
     */
    @PostMapping("/sessions/general")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> getOrCreateGeneral() {
        Long userId = currentUserId();
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(supportChatService.getOrCreateGeneralSessionForUser(userId));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SupportChatSessionResponse>>> listSessions() {
        Long userId = currentUserId();
        ApiResponse<List<SupportChatSessionResponse>> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(supportChatService.listSessionsForUser(userId));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> getOrCreate(@RequestBody SupportChatUserCreateSessionRequest request) {
        Long userId = currentUserId();
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(supportChatService.getOrCreateSessionForUser(userId, request));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> getOne(@PathVariable Long id) {
        Long userId = currentUserId();
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(supportChatService.getSessionForUser(userId, id));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ApiResponse<SupportChatPostMessageResponse>> postMessage(
            @PathVariable Long id,
            @RequestBody SupportChatUserMessageRequest request) {
        Long userId = currentUserId();
        ApiResponse<SupportChatPostMessageResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(supportChatService.postUserMessage(userId, id, request));
        return ResponseEntity.ok(res);
    }

    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return ((MyUserDetail) auth.getPrincipal()).getId();
    }
}
