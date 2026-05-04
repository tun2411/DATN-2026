package com.example.bedatn.service.impl;

import com.example.bedatn.documents.*;
import com.example.bedatn.dto.request.SupportChatStaffMessageRequest;
import com.example.bedatn.dto.request.SupportChatUserCreateSessionRequest;
import com.example.bedatn.dto.request.SupportChatUserMessageRequest;
import com.example.bedatn.dto.request.SupportChatVisitorMessageRequest;
import com.example.bedatn.dto.response.SupportChatMessageEntryResponse;
import com.example.bedatn.dto.response.SupportChatPostMessageResponse;
import com.example.bedatn.dto.response.SupportChatSessionResponse;
import com.example.bedatn.repository.AssignmentBuildingRepository;
import com.example.bedatn.repository.BuildingRepository;
import com.example.bedatn.repository.SupportChatSessionRepository;
import com.example.bedatn.repository.UserRepository;
import com.example.bedatn.service.SupportChatBotService;
import com.example.bedatn.service.SupportChatService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupportChatServiceImpl implements SupportChatService {

    private static final int MAX_MESSAGES = 80;
    private static final List<String> LIST_STATUSES = List.of("OPEN", "STAFF");

    private final SupportChatSessionRepository sessionRepository;
    private final AssignmentBuildingRepository assignmentBuildingRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final SupportChatBotService botService;

    public SupportChatServiceImpl(SupportChatSessionRepository sessionRepository,
                                  AssignmentBuildingRepository assignmentBuildingRepository,
                                  BuildingRepository buildingRepository,
                                  UserRepository userRepository,
                                  SupportChatBotService botService) {
        this.sessionRepository = sessionRepository;
        this.assignmentBuildingRepository = assignmentBuildingRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.botService = botService;
    }

    @Override
    public SupportChatPostMessageResponse postVisitorMessage(SupportChatVisitorMessageRequest request) {
        if (request == null || request.getVisitorKey() == null || request.getVisitorKey().trim().isEmpty()) {
            throw new IllegalArgumentException("visitorKey is required");
        }
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("text is required");
        }
        String key = request.getVisitorKey().trim();
        String text = request.getText().trim();
        if (text.length() > 4000) {
            throw new IllegalArgumentException("text is too long");
        }

        Long reqBuildingId = request.getBuildingId();
        SupportChatSessionEntity session = sessionRepository.findByVisitorKey(key).orElseGet(() -> newGuestSession(key, reqBuildingId));
        appendUserMessage(session, text);

        SupportChatPostMessageResponse out = new SupportChatPostMessageResponse();
        if (session.getAssignedStaffId() == null && "OPEN".equals(session.getStatus())) {
            String botReply = botService.generateReply(session);
            appendBotMessage(session, botReply);
            sessionRepository.save(session);
            out.setBotReply(botReply);
        } else {
            sessionRepository.save(session);
            out.setBotReply(null);
        }
        out.setSession(toResponse(session, null));
        return out;
    }

    @Override
    public SupportChatSessionResponse getSessionByVisitorKey(String visitorKey) {
        if (visitorKey == null || visitorKey.trim().isEmpty()) {
            throw new IllegalArgumentException("visitorKey is required");
        }
        SupportChatSessionEntity session = sessionRepository.findByVisitorKey(visitorKey.trim())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        return toResponse(session, null);
    }

    @Override
    public List<SupportChatSessionResponse> listSessionsForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return sessionRepository.findByUserIdOrderByCreatedDateDesc(userId).stream()
                // Chỉ lấy session gắn buildingId — session "general" (buildingId null) chỉ dùng cho widget
                .filter(s -> s.getBuildingId() != null)
                .sorted(Comparator.comparing(SupportChatSessionEntity::getCreatedDate,
                        Comparator.nullsLast(Date::compareTo)).reversed())
                .map(s -> toResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    public SupportChatSessionResponse getSessionForUser(Long userId, Long sessionId) {
        SupportChatSessionEntity s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (s.getUserId() == null || !s.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session not found");
        }
        return toResponse(s, null);
    }

    @Override
    public SupportChatSessionResponse getOrCreateSessionForUser(Long userId, SupportChatUserCreateSessionRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request == null || request.getBuildingId() == null) {
            throw new IllegalArgumentException("buildingId is required");
        }
        Long buildingId = request.getBuildingId();
        buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));
        boolean wantInterestOpening = Boolean.TRUE.equals(request.getInterestOpening());

        return sessionRepository.findByUserIdAndBuildingId(userId, buildingId)
                .map(s -> {
                    if (wantInterestOpening && s.getMessages().isEmpty()) {
                        appendInterestOpeningBot(s);
                        sessionRepository.save(s);
                    }
                    return toResponse(s, null);
                })
                .orElseGet(() -> {
                    SupportChatSessionEntity e = newUserSession(userId, buildingId);
                    if (wantInterestOpening) {
                        appendInterestOpeningBot(e);
                    }
                    SupportChatSessionEntity saved = sessionRepository.save(e);
                    return toResponse(saved, null);
                });
    }

    private void appendInterestOpeningBot(SupportChatSessionEntity session) {
        if (session.getBuildingId() == null) {
            return;
        }
        BuildingEntity b = buildingRepository.findById(session.getBuildingId()).orElse(null);
        String name = (b != null && b.getName() != null && !b.getName().isBlank()) ? b.getName().trim() : "bất động sản này";
        String district = (b != null && b.getDistrict() != null && !b.getDistrict().isBlank()) ? b.getDistrict().trim() : "";
        String loc = district.isEmpty() ? "" : " — " + district;
        String body = "Chào bạn! Cảm ơn bạn đã quan tâm tới " + name + loc
                + ". Mình là trợ lý ảo EziSolution — bạn muốn hỏi thêm về giá, diện tích, hay đặt lịch xem nhà?";
        appendBotMessage(session, body);
    }

    @Override
    public SupportChatPostMessageResponse postUserMessage(Long userId, Long sessionId, SupportChatUserMessageRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("text is required");
        }
        String text = request.getText().trim();
        if (text.length() > 4000) {
            throw new IllegalArgumentException("text is too long");
        }
        SupportChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getUserId() == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session not found");
        }
        appendUserMessage(session, text);
        SupportChatPostMessageResponse out = new SupportChatPostMessageResponse();
        if (session.getAssignedStaffId() == null && "OPEN".equals(session.getStatus())) {
            String botReply = botService.generateReply(session);
            appendBotMessage(session, botReply);
            sessionRepository.save(session);
            out.setBotReply(botReply);
        } else {
            sessionRepository.save(session);
            out.setBotReply(null);
        }
        out.setSession(toResponse(session, null));
        return out;
    }

    @Override
    public SupportChatSessionResponse getOrCreateGeneralSessionForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return sessionRepository.findByUserIdAndBuildingIdIsNull(userId)
                .map(s -> toResponse(s, null))
                .orElseGet(() -> {
                    SupportChatSessionEntity e = new SupportChatSessionEntity();
                    e.setId(System.currentTimeMillis());
                    e.setUserId(userId);
                    e.setBuildingId(null);
                    e.setStatus("OPEN");
                    e.setHandoverRequested(false);
                    String greeting = "Xin chào! Mình là trợ lý AI EziSolution. Bạn muốn tìm hiểu về dự án, chính sách hay cần tư vấn gì không?";
                    appendBotMessage(e, greeting);
                    SupportChatSessionEntity saved = sessionRepository.save(e);
                    return toResponse(saved, null);
                });
    }

    @Override
    public List<SupportChatSessionResponse> listForStaff(Long actorStaffId, boolean managerViewAll) {
        if (actorStaffId == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        Set<Long> allowedBuildingIds = managerViewAll ? null : assignedBuildingIds(actorStaffId);
        return sessionRepository.findByStatusInOrderByCreatedDateDesc(LIST_STATUSES).stream()
                .filter(s -> s.getUserId() != null)
                // Phiên general của widget AI không gắn BĐS, không thuộc trung tâm tin nhắn/tư vấn theo BĐS.
                .filter(s -> s.getBuildingId() != null)
                .filter(s -> managerViewAll || (s.getBuildingId() != null && allowedBuildingIds.contains(s.getBuildingId())))
                .sorted(Comparator.comparing(SupportChatSessionEntity::getCreatedDate,
                        Comparator.nullsLast(Date::compareTo)).reversed())
                .map(s -> toResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    public SupportChatSessionResponse claimSession(Long sessionId, Long staffId, String staffName, boolean managerMayOverride) {
        if (staffId == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        SupportChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!managerMayOverride && !canStaffAccessSession(staffId, session)) {
            throw new IllegalArgumentException("Bạn không có quyền tiếp quản đoạn chat này");
        }
        if (session.getUserId() == null) {
            throw new IllegalArgumentException("Không thể tiếp quản phiên khách vãng lai (chỉ hỗ trợ khách đã đăng nhập)");
        }
        if (session.getAssignedStaffId() != null) {
            throw new IllegalArgumentException("Session already assigned");
        }
        session.setAssignedStaffId(staffId);
        session.setStatus("STAFF");
        String label = (staffName != null && !staffName.isBlank()) ? staffName : "Nhân viên";
        appendBotMessage(session,
                "Nhân viên " + label + " đã tham gia hội thoại. Bạn có thể tiếp tục trao đổi.");
        sessionRepository.save(session);
        return toResponse(session, label);
    }

    @Override
    public SupportChatSessionResponse postStaffMessage(Long sessionId, Long staffId, SupportChatStaffMessageRequest request) {
        if (staffId == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("text is required");
        }
        String body = request.getText().trim();
        if (body.length() > 4000) {
            throw new IllegalArgumentException("text is too long");
        }
        SupportChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!canStaffAccessSession(staffId, session)) {
            throw new IllegalArgumentException("Bạn không có quyền xử lý đoạn chat này");
        }
        if (session.getAssignedStaffId() == null || !session.getAssignedStaffId().equals(staffId)) {
            throw new IllegalArgumentException("Not assigned to this staff");
        }
        appendStaffMessage(session, body);
        sessionRepository.save(session);
        return toResponse(session, null);
    }

    @Override
    public SupportChatSessionResponse releaseSessionToBot(Long sessionId, Long actorStaffId, boolean managerMayOverride) {
        if (actorStaffId == null) {
            throw new IllegalArgumentException("staffId is required");
        }
        SupportChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Long assigned = session.getAssignedStaffId();
        if (assigned == null) {
            throw new IllegalArgumentException("Phiên không đang do nhân viên phụ trách");
        }
        boolean isAssignee = assigned.equals(actorStaffId);
        if (!isAssignee && !managerMayOverride) {
            throw new IllegalArgumentException("Chỉ nhân viên đang phụ trách mới có thể trả phiên về bot");
        }
        session.setAssignedStaffId(null);
        session.setStatus("OPEN");
        session.setHandoverRequested(false);
        appendBotMessage(session,
                "Cuộc trò chuyện đã được chuyển lại cho trợ lý ảo. Bạn có thể tiếp tục nhắn tin để được tư vấn.");
        sessionRepository.save(session);
        return toResponse(session, null);
    }

    private SupportChatSessionEntity newGuestSession(String visitorKey, Long buildingId) {
        SupportChatSessionEntity e = new SupportChatSessionEntity();
        e.setId(System.currentTimeMillis());
        e.setVisitorKey(visitorKey);
        e.setUserId(null);
        e.setBuildingId(buildingId);
        e.setStatus("OPEN");
        e.setAssignedStaffId(null);
        e.setHandoverRequested(false);
        return e;
    }

    private SupportChatSessionEntity newUserSession(Long userId, Long buildingId) {
        SupportChatSessionEntity e = new SupportChatSessionEntity();
        e.setId(System.currentTimeMillis());
        e.setVisitorKey(null);
        e.setUserId(userId);
        e.setBuildingId(buildingId);
        e.setStatus("OPEN");
        e.setAssignedStaffId(null);
        e.setHandoverRequested(false);
        return e;
    }

    private void appendUserMessage(SupportChatSessionEntity session, String body) {
        ChatMessageDoc m = new ChatMessageDoc();
        m.setRole("USER");
        m.setBody(body);
        m.setCreatedAt(new Date());
        session.getMessages().add(m);
        trimMessages(session);
    }

    private void appendBotMessage(SupportChatSessionEntity session, String body) {
        ChatMessageDoc m = new ChatMessageDoc();
        m.setRole("BOT");
        m.setBody(body);
        m.setCreatedAt(new Date());
        session.getMessages().add(m);
        trimMessages(session);
    }

    private void appendStaffMessage(SupportChatSessionEntity session, String body) {
        ChatMessageDoc m = new ChatMessageDoc();
        m.setRole("STAFF");
        m.setBody(body);
        m.setCreatedAt(new Date());
        session.getMessages().add(m);
        trimMessages(session);
    }

    private void trimMessages(SupportChatSessionEntity session) {
        List<ChatMessageDoc> list = session.getMessages();
        while (list.size() > MAX_MESSAGES) {
            list.remove(0);
        }
    }

    private Set<Long> assignedBuildingIds(Long staffId) {
        return assignmentBuildingRepository.findByStaffId(staffId).stream()
                .map(AssignmentBuildingEntity::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean canStaffAccessSession(Long staffId, SupportChatSessionEntity session) {
        Long buildingId = session.getBuildingId();
        if (buildingId == null) {
            return false;
        }
        return assignedBuildingIds(staffId).contains(buildingId);
    }

    private SupportChatSessionResponse toResponse(SupportChatSessionEntity e, String assignedStaffNameOverride) {
        SupportChatSessionResponse r = new SupportChatSessionResponse();
        r.setId(e.getId());
        r.setVisitorKey(e.getVisitorKey());
        r.setUserId(e.getUserId());
        if (e.getUserId() != null) {
            r.setCustomerCode("KH-" + e.getUserId());
            UserEntity customerUser = userRepository.findById(e.getUserId()).orElse(null);
            if (customerUser != null) {
                r.setCustomerName(customerUser.getFullName());
            }
        }
        r.setBuildingId(e.getBuildingId());
        if (e.getBuildingId() != null) {
            BuildingEntity b = buildingRepository.findById(e.getBuildingId()).orElse(null);
            if (b != null) {
                r.setBuildingName(b.getName());
                r.setBuildingAvatar(b.getAvatar());
            }
        }
        r.setAssignedStaffId(e.getAssignedStaffId());
        r.setStatus(e.getStatus());
        if (assignedStaffNameOverride != null) {
            r.setAssignedStaffName(assignedStaffNameOverride);
        } else if (e.getAssignedStaffId() != null) {
            UserEntity staff = userRepository.findById(e.getAssignedStaffId()).orElse(null);
            if (staff != null) {
                r.setAssignedStaffName(staff.getFullName());
            }
        }
        List<SupportChatMessageEntryResponse> msgs = new ArrayList<>();
        for (ChatMessageDoc d : e.getMessages()) {
            SupportChatMessageEntryResponse row = new SupportChatMessageEntryResponse();
            row.setRole(d.getRole());
            row.setBody(d.getBody());
            row.setCreatedAt(d.getCreatedAt());
            msgs.add(row);
        }
        r.setMessages(msgs);
        return r;
    }
}
