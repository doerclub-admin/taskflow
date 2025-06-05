package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Kept for potential future use or alternative tests

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate; // Mocked, though current methods use @SendTo

    @InjectMocks // This will inject messagingTemplate if ChatController had it as a field and constructor.
                 // If constructor is not taking it, then this test setup might need adjustment
                 // For @SendTo, the test becomes more about checking the output of the method.
    private ChatController chatController;

    private ChatMessage chatMessage;

    @Captor
    ArgumentCaptor<ChatMessage> chatMessageCaptor;

    @BeforeEach
    void setUp() {
        // Re-initialize chatController if it doesn't take SimpMessagingTemplate in constructor
        // For this example, assuming it might be used or for consistency in testing.
        // If ChatController's constructor doesn't take it, manual instantiation is fine.
        chatController = new ChatController(messagingTemplate); // Ensure this matches actual constructor

        chatMessage = new ChatMessage();
        chatMessage.setSender("testUser");
        chatMessage.setContent("Hello");
        chatMessage.setType(ChatMessage.MessageType.CHAT);
    }

    @Test
    void sendMessage_shouldSetTimestampAndReturnMessage() {
        ChatMessage result = chatController.sendMessage(chatMessage);

        assertNotNull(result.getTimestamp());
        assertEquals("testUser", result.getSender());
        assertEquals("Hello", result.getContent());
        assertEquals(ChatMessage.MessageType.CHAT, result.getType());
        // If we were testing a method that uses messagingTemplate.convertAndSend():
        // verify(messagingTemplate).convertAndSend(eq("/topic/publicChatRoom"), chatMessageCaptor.capture());
        // ChatMessage sentMsg = chatMessageCaptor.getValue();
        // assertNotNull(sentMsg.getTimestamp());
    }

    @Test
    void addUser_shouldSetTypeJoinAndSessionAttributes() {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        headerAccessor.setSessionAttributes(sessionAttributes); // Ensure sessionAttributes is not null
        headerAccessor.setSessionId("test-session-id");


        chatMessage.setType(null); // Clear type to test setting it
        ChatMessage result = chatController.addUser(chatMessage, headerAccessor);

        assertEquals(ChatMessage.MessageType.JOIN, result.getType());
        assertNotNull(result.getTimestamp());
        assertEquals("testUser", result.getSender());
        assertEquals("testUser", headerAccessor.getSessionAttributes().get("username"));
        assertEquals("test-session-id", headerAccessor.getSessionAttributes().get("sessionId"));
    }
}
