package com.example.agentplatform.backend.listener;

import com.example.agentplatform.backend.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("Received a new web socket connection. Session ID: {}", sessionId);
        // Optionally, you can send a system message or log user connection
        // For example, if user principal is available:
        // String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "UnknownUser";
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            String sessionId = (String) sessionAttributes.get("sessionId"); // or headerAccessor.getSessionId()

            if (username != null) {
                logger.info("User Disconnected: {} (Session: {})", username, sessionId);

                ChatMessage chatMessage = ChatMessage.builder()
                        .type(ChatMessage.MessageType.LEAVE)
                        .sender(username)
                        .sessionId(sessionId)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Notify others in the public chat room
                messagingTemplate.convertAndSend("/topic/publicChatRoom", chatMessage);
            } else {
                 logger.info("Session Disconnected: {} (Username not found in session)", sessionId);
            }
        } else {
            logger.info("Session Disconnected: {} (No session attributes found)", headerAccessor.getSessionId());
        }
    }
}
