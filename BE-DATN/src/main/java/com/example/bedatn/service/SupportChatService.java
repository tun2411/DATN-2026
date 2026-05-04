package com.example.bedatn.service;

import com.example.bedatn.dto.request.SupportChatStaffMessageRequest;
import com.example.bedatn.dto.request.SupportChatUserCreateSessionRequest;
import com.example.bedatn.dto.request.SupportChatUserMessageRequest;
import com.example.bedatn.dto.request.SupportChatVisitorMessageRequest;
import com.example.bedatn.dto.response.SupportChatPostMessageResponse;
import com.example.bedatn.dto.response.SupportChatSessionResponse;

import java.util.List;

public interface SupportChatService {

    SupportChatPostMessageResponse postVisitorMessage(SupportChatVisitorMessageRequest request);

    SupportChatSessionResponse getSessionByVisitorKey(String visitorKey);

    List<SupportChatSessionResponse> listSessionsForUser(Long userId);

    SupportChatSessionResponse getSessionForUser(Long userId, Long sessionId);

    SupportChatSessionResponse getOrCreateSessionForUser(Long userId, SupportChatUserCreateSessionRequest request);

    SupportChatPostMessageResponse postUserMessage(Long userId, Long sessionId, SupportChatUserMessageRequest request);

    /**
     * Tạo hoặc lấy session "tư vấn chung" (không gắn buildingId) cho user đã đăng nhập.
     * Session này chỉ hiển thị trong widget chat góc màn hình, KHÔNG xuất hiện trong
     * danh sách MessagesPage (vốn chỉ lấy session có buildingId != null).
     */
    SupportChatSessionResponse getOrCreateGeneralSessionForUser(Long userId);

    List<SupportChatSessionResponse> listForStaff(Long actorStaffId, boolean managerViewAll);

    SupportChatSessionResponse claimSession(Long sessionId, Long staffId, String staffName, boolean managerMayOverride);

    SupportChatSessionResponse postStaffMessage(Long sessionId, Long staffId, SupportChatStaffMessageRequest request);

    /**
     * Trả phiên về bot: bỏ gán nhân viên, trạng thái OPEN. Nhân viên đang phụ trách hoặc quản lý (override).
     */
    SupportChatSessionResponse releaseSessionToBot(Long sessionId, Long actorStaffId, boolean managerMayOverride);
}
