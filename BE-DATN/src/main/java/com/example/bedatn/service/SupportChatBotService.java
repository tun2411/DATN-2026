package com.example.bedatn.service;

import com.example.bedatn.documents.SupportChatSessionEntity;

/**
 * Trợ lý tự động khi chưa có nhân viên tiếp quản (Spring AI + tool; có fallback).
 */
public interface SupportChatBotService {

    String generateReply(SupportChatSessionEntity session);
}
