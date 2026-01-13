package com.fanshuaiko.backage.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fanshuaiko.backage.entity.Notice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/notice/{role}")
public class NoticeWebSocketEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeWebSocketEndpoint.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, Set<Session>> ROLE_SESSIONS = new ConcurrentHashMap<>();

    private Session currentSession;
    private String currentRole;

    @OnOpen
    public void onOpen(Session session, @PathParam("role") String role) {
        this.currentSession = session;
        this.currentRole = normalizeRole(role);
        ROLE_SESSIONS.computeIfAbsent(this.currentRole, k -> ConcurrentHashMap.newKeySet()).add(session);
        LOGGER.info("Notice websocket connected role={}, sessions={}", this.currentRole, ROLE_SESSIONS.get(this.currentRole).size());
    }

    @OnClose
    public void onClose() {
        removeCurrent();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.warn("Notice websocket error role={}", this.currentRole, throwable);
        removeCurrent();
    }

    @OnMessage
    public void onMessage(String message) {
        // 当前仅服务端推送，客户端消息忽略
    }

    private void removeCurrent() {
        if (this.currentRole == null || this.currentSession == null) {
            return;
        }
        Set<Session> sessions = ROLE_SESSIONS.get(this.currentRole);
        if (sessions != null) {
            sessions.remove(this.currentSession);
        }
    }

    private static String normalizeRole(String role) {
        if (role == null) return "all";
        String r = role.trim().toLowerCase();
        if ("student".equals(r) || "teacher".equals(r) || "all".equals(r)) {
            return r;
        }
        return "all";
    }

    public static void broadcastNotice(Notice notice) {
        broadcastEvent("create", notice);
    }

    public static void broadcastEvent(String action, Notice notice) {
        if (notice == null) return;
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("action", action == null ? "create" : action);
            event.put("notice", notice);
            String payload = OBJECT_MAPPER.writeValueAsString(event);
            Set<String> targetRoles = resolveTargetRoles(notice.getTargetRole());
            for (String role : targetRoles) {
                Set<Session> sessions = ROLE_SESSIONS.get(role);
                if (sessions == null || sessions.isEmpty()) continue;
                sessions.removeIf(session -> !session.isOpen());
                for (Session session : sessions) {
                    try {
                        session.getBasicRemote().sendText(payload);
                    } catch (IOException e) {
                        LOGGER.debug("Send notice websocket failed role={}", role, e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Broadcast notice websocket failed", e);
        }
    }

    private static Set<String> resolveTargetRoles(Integer targetRole) {
        if (targetRole == null) {
            return new HashSet<>(Arrays.asList("all", "student", "teacher"));
        }
        if (targetRole == 1 || targetRole == 9) {
            return new HashSet<>(Arrays.asList("student", "all"));
        }
        if (targetRole == 2) {
            return new HashSet<>(Arrays.asList("teacher", "all"));
        }
        return new HashSet<>(Arrays.asList("all", "student", "teacher"));
    }
}
