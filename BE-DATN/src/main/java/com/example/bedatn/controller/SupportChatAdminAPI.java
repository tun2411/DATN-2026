package com.example.bedatn.controller;

import com.example.bedatn.dto.request.SupportChatStaffMessageRequest;
import com.example.bedatn.dto.response.ApiResponse;
import com.example.bedatn.dto.response.SupportChatSessionResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.SupportChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support-chat/admin")
public class SupportChatAdminAPI {

    private final SupportChatService supportChatService;

    public SupportChatAdminAPI(SupportChatService supportChatService) {
        this.supportChatService = supportChatService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SupportChatSessionResponse>>> list() {
        MyUserDetail user = currentUser();
        boolean manager = hasRole(user, "MANAGER");
        List<SupportChatSessionResponse> data = supportChatService.listForStaff(user.getId(), manager);
        ApiResponse<List<SupportChatSessionResponse>> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(data);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/sessions/{id}/claim")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> claim(@PathVariable Long id) {
        MyUserDetail user = currentUser();
        boolean manager = hasRole(user, "MANAGER");
        SupportChatSessionResponse data = supportChatService.claimSession(id, user.getId(), user.getFullName(), manager);
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("Claimed");
        res.setData(data);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> staffMessage(
            @PathVariable Long id,
            @RequestBody SupportChatStaffMessageRequest request) {
        MyUserDetail user = currentUser();
        SupportChatSessionResponse data = supportChatService.postStaffMessage(id, user.getId(), request);
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("OK");
        res.setData(data);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/sessions/{id}/release-to-bot")
    public ResponseEntity<ApiResponse<SupportChatSessionResponse>> releaseToBot(@PathVariable Long id) {
        MyUserDetail user = currentUser();
        boolean manager = hasRole(user, "MANAGER");
        SupportChatSessionResponse data = supportChatService.releaseSessionToBot(id, user.getId(), manager);
        ApiResponse<SupportChatSessionResponse> res = new ApiResponse<>();
        res.setMessage("Released");
        res.setData(data);
        return ResponseEntity.ok(res);
    }

    private static boolean hasRole(MyUserDetail user, String roleWithoutPrefix) {
        String want = "ROLE_" + roleWithoutPrefix;
        for (GrantedAuthority a : user.getAuthorities()) {
            if (want.equalsIgnoreCase(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static MyUserDetail currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetail)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (MyUserDetail) auth.getPrincipal();
    }
}
