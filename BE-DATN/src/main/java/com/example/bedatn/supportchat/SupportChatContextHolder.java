package com.example.bedatn.supportchat;

/**
 * Gắn session chat hiện tại cho luồng xử lý Spring AI / @Tool (ThreadLocal).
 */
public final class SupportChatContextHolder {

    private static final ThreadLocal<Long> SESSION_ID = new ThreadLocal<>();

    private SupportChatContextHolder() {
    }

    public static void setSessionId(Long id) {
        SESSION_ID.set(id);
    }

    public static Long getSessionId() {
        return SESSION_ID.get();
    }

    public static void clear() {
        SESSION_ID.remove();
    }
}
