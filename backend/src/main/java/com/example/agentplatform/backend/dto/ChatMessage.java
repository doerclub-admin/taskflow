package com.example.agentplatform.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender; // Could be username or agent ID
    private String recipient; // Optional, for private messages or specific agent interactions
    private String sessionId; // WebSocket session ID, useful for tracking
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT, // A standard chat message
        JOIN, // A user or agent joining the chat/session
        LEAVE, // A user or agent leaving the chat/session
        SYSTEM, // System messages, e.g., errors, notifications
        AGENT_REQUEST, // A message from user to an agent
        AGENT_RESPONSE // A message from an agent to a user
        // Add more types as needed, e.g., IMAGE, FILE
    }
}
