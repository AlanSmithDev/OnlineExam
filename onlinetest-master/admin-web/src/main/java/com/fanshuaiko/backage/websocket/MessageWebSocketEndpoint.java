package com.fanshuaiko.backage.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fanshuaiko.backage.entity.ImMessage;
import com.fanshuaiko.backage.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint("/ws/message/{userNo}")
public class MessageWebSocketEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageWebSocketEndpoint.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    private static MessageService messageService;

    @Autowired
    public void setMessageService(MessageService service) {
        MessageWebSocketEndpoint.messageService = service;
    }

    private String userNo;

    @OnOpen
    public void onOpen(Session session, @PathParam("userNo") String userNo) {
        this.userNo = userNo;
        SESSION_MAP.put(userNo, session);
        LOGGER.info("WebSocket connected: {} (total: {})", userNo, SESSION_MAP.size());
        // 广播用户上线状态
        broadcastUserStatus(userNo, "online");
    }

    @OnClose
    public void onClose() {
        if (this.userNo != null) {
            SESSION_MAP.remove(this.userNo);
            LOGGER.info("WebSocket closed: {} (remaining: {})", this.userNo, SESSION_MAP.size());
            // 广播用户离线状态
            broadcastUserStatus(this.userNo, "offline");
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("WebSocket error for user: {}", this.userNo, throwable);
        if (this.userNo != null) {
            SESSION_MAP.remove(this.userNo);
            broadcastUserStatus(this.userNo, "offline");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userNo") String userNo) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        // 异步处理消息，避免阻塞 WebSocket 线程
        EXECUTOR.submit(() -> {
            try {
                ImMessage payload = parseMessage(message, userNo);
                payload.setCreateTime(new Date());
                payload.setTarget("ALL");
                if (messageService != null) {
                    messageService.save(payload);
                }
                broadcast(payload);
            } catch (Exception e) {
                LOGGER.error("Error processing message from {}", userNo, e);
            }
        });
    }

    private void broadcastUserStatus(String userNo, String status) {
        try {
            ImMessage statusMsg = new ImMessage();
            statusMsg.setSenderNo("SYSTEM");
            statusMsg.setSenderName("系统");
            statusMsg.setContent(userNo + " " + (status.equals("online") ? "已上线" : "已离线"));
            statusMsg.setCreateTime(new Date());
            statusMsg.setTarget("ALL");
            String json = OBJECT_MAPPER.writeValueAsString(statusMsg);
            
            SESSION_MAP.values().parallelStream().forEach(s -> {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        LOGGER.debug("Failed to send status message", e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error broadcasting user status", e);
        }
    }

    private void broadcast(ImMessage payload) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(payload);
            // 使用并行流提高广播效率
            SESSION_MAP.values().parallelStream().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        LOGGER.debug("Send message error to session", e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("Serialize message error", e);
        }
    }

    private ImMessage parseMessage(String message, String userNo) {
        try {
            ImMessage msg = OBJECT_MAPPER.readValue(message, ImMessage.class);
            if (msg.getSenderNo() == null) {
                msg.setSenderNo(userNo);
            }
            return msg;
        } catch (IOException e) {
            ImMessage fallback = new ImMessage();
            fallback.setSenderNo(userNo);
            fallback.setSenderName(userNo);
            fallback.setContent(message);
            return fallback;
        }
    }
}
