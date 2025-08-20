package com.taskmanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;
    
    // Store active WebSocket sessions by user ID
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
        
        // Extract user ID from query parameters or session attributes
        String query = session.getUri().getQuery();
        if (query != null && query.contains("token=")) {
            String token = extractTokenFromQuery(query);
            try {
                Long userId = jwtUtil.extractUserId(token);
                if (userId != null) {
                    userSessions.put(userId, session);
                    session.getAttributes().put("userId", userId);
                    System.out.println("User " + userId + " connected to WebSocket");
                    
                    // Send connection confirmation
                    Map<String, Object> connectionMsg = new HashMap<>();
                    connectionMsg.put("type", "connection");
                    connectionMsg.put("message", "Connected successfully");
                    connectionMsg.put("userId", userId);
                    sendMessage(session, connectionMsg);
                }
            } catch (Exception e) {
                System.err.println("Invalid token in WebSocket connection: " + e.getMessage());
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            }
        } else {
            System.err.println("No token provided in WebSocket connection");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token required"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("User " + userId + " disconnected from WebSocket");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if needed (e.g., mark notifications as read)
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            
            if ("ping".equals(type)) {
                // Respond to ping with pong
                Map<String, Object> pongMsg = new HashMap<>();
                pongMsg.put("type", "pong");
                sendMessage(session, pongMsg);
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    // Send notification to specific user
    public void sendNotificationToUser(Long userId, Object notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> notificationMsg = new HashMap<>();
                notificationMsg.put("type", "notification");
                notificationMsg.put("data", notification);
                sendMessage(session, notificationMsg);
            } catch (Exception e) {
                System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
                // Remove invalid session
                userSessions.remove(userId);
            }
        }
    }

    // Send notification to multiple users
    public void sendNotificationToUsers(Iterable<Long> userIds, Object notification) {
        for (Long userId : userIds) {
            sendNotificationToUser(userId, notification);
        }
    }

    // Get count of connected users
    public int getConnectedUserCount() {
        return userSessions.size();
    }

    // Check if user is connected
    public boolean isUserConnected(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}
