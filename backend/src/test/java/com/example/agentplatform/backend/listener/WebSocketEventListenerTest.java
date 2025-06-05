package com.example.agentplatform.backend.listener;

import com.example.agentplatform.backend.dto.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private WebSocketEventListener eventListener;

    @Captor
    ArgumentCaptor<ChatMessage> chatMessageCaptor;

    private SessionDisconnectEvent disconnectEvent;
    private SimpMessageHeaderAccessor headerAccessor;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        // Mock the event and its headers
        Message<byte[]> message = mock(Message.class);
        MessageHeaders messageHeaders = mock(MessageHeaders.class);
        when(message.getHeaders()).thenReturn(messageHeaders);

        disconnectEvent = new SessionDisconnectEvent(this, message, "test-session-id", null);

        // Setup SimpMessageHeaderAccessor for the test
        headerAccessor = SimpMessageHeaderAccessor.create();
        sessionAttributes = new HashMap<>();
        headerAccessor.setSessionAttributes(sessionAttributes);
        headerAccessor.setSessionId("test-session-id");

        // This part is tricky because SessionDisconnectEvent's message headers are not easily mocked
        // to be picked up by StompHeaderAccessor.wrap(event.getMessage()).
        // A more direct approach for testing the listener's logic is to prepare the
        // sessionAttributes that would have been extracted by StompHeaderAccessor.
    }

    @Test
    void handleWebSocketDisconnectListener_userDisconnected_sendsLeaveMessage() {
        // Prepare session attributes as if a user was connected
        sessionAttributes.put("username", "testUser");
        sessionAttributes.put("sessionId", "test-session-id");

        // Create a StompHeaderAccessor manually for the test, as wrapping event.getMessage() is complex to mock
        StompHeaderAccessor stompAccessor = StompHeaderAccessor.create();
        stompAccessor.setSessionId("test-session-id");
        stompAccessor.setSessionAttributes(sessionAttributes);

        // Simulate the event by creating a new SessionDisconnectEvent with a message
        // whose headers can be controlled or by directly invoking the listener method
        // with a mocked StompHeaderAccessor if the method could accept it.
        // For simplicity, we'll assume the listener can somehow get these attributes.
        // The actual event listener uses StompHeaderAccessor.wrap(event.getMessage()).
        // We will mock the behavior that this wrapping would achieve.

        // To test the listener directly, we might need to refactor it slightly to be more testable,
        // or use a more involved Spring integration test.
        // For a unit test, let's simulate the scenario where attributes are available.

        // Create a new event with a message that can be wrapped by StompHeaderAccessor if needed,
        // but it's easier to test the logic by ensuring the attributes are set.
        // The current WebSocketEventListener directly uses StompHeaderAccessor.wrap(event.getMessage()).
        // This makes direct unit testing of the extraction harder without Spring context.

        // Let's assume for this unit test, we can create a StompHeaderAccessor
        // that reflects the state we want to test. We can't directly pass this into the
        // @EventListener method, so this test will be more conceptual or would require
        // a helper method in the listener or an integration test.

        // Given the structure, a true unit test would involve:
        // 1. Creating a SessionDisconnectEvent.
        // 2. When StompHeaderAccessor.wrap(event.getMessage()) is called (implicitly by Spring or explicitly if refactored),
        //    return a pre-configured StompHeaderAccessor. This requires mocking static methods or using PowerMockito,
        //    or refactoring the listener to accept a HeaderAccessor.

        // Simpler approach for this demonstration:
        // If the listener was: handleWebSocketDisconnectListener(StompHeaderAccessor accessor)
        // Then we could pass our mock.
        // With @EventListener, Spring invokes it.

        // Let's assume the listener successfully extracts these attributes:
        WebSocketEventListener listener = new WebSocketEventListener();
        // Manually inject the mock (Spring would do this with @Autowired)
        try {
            java.lang.reflect.Field field = WebSocketEventListener.class.getDeclaredField("messagingTemplate");
            field.setAccessible(true);
            field.set(listener, messagingTemplate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject mock messagingTemplate: " + e.getMessage());
        }


        // We need to manually create a SessionDisconnectEvent where getMessage() can be wrapped
        // by StompHeaderAccessor to return our desired attributes.
        // This is where pure unit testing hits limits without complex mocking or refactoring.

        // For now, this test will be more of a placeholder showing the intent.
        // A proper test would likely be an @SpringBootTest or involve refactoring.

        // Let's try to make StompHeaderAccessor.wrap work by providing a message with attributes
        SimpMessageHeaderAccessor eventHeaders = SimpMessageHeaderAccessor.create();
        eventHeaders.setSessionId("test-session-id");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("username", "testUser");
        attrs.put("sessionId", "test-session-id");
        eventHeaders.setSessionAttributes(attrs);

        org.springframework.messaging.support.GenericMessage<byte[]> disconnectMessage =
            new org.springframework.messaging.support.GenericMessage<>(new byte[0], eventHeaders.getMessageHeaders());
        SessionDisconnectEvent testEvent = new SessionDisconnectEvent(this, disconnectMessage, "test-session-id", null);


        listener.handleWebSocketDisconnectListener(testEvent);

        verify(messagingTemplate).convertAndSend(eq("/topic/publicChatRoom"), chatMessageCaptor.capture());
        ChatMessage sentMessage = chatMessageCaptor.getValue();
        assertEquals(ChatMessage.MessageType.LEAVE, sentMessage.getType());
        assertEquals("testUser", sentMessage.getSender());
        assertEquals("test-session-id", sentMessage.getSessionId());
        assertNotNull(sentMessage.getTimestamp());
    }

    @Test
    void handleWebSocketDisconnectListener_noUsername_logsAndDoesNotSend() {
         SimpMessageHeaderAccessor eventHeaders = SimpMessageHeaderAccessor.create();
        eventHeaders.setSessionId("test-session-id");
        Map<String, Object> attrs = new HashMap<>();
        // Username is NOT set
        attrs.put("sessionId", "test-session-id");
        eventHeaders.setSessionAttributes(attrs);

        org.springframework.messaging.support.GenericMessage<byte[]> disconnectMessage =
            new org.springframework.messaging.support.GenericMessage<>(new byte[0], eventHeaders.getMessageHeaders());
        SessionDisconnectEvent testEvent = new SessionDisconnectEvent(this, disconnectMessage, "test-session-id", null);

        // Inject mock manually for this test instance
        WebSocketEventListener listener = new WebSocketEventListener();
        try {
            java.lang.reflect.Field field = WebSocketEventListener.class.getDeclaredField("messagingTemplate");
            field.setAccessible(true);
            field.set(listener, messagingTemplate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject mock messagingTemplate: " + e.getMessage());
        }

        listener.handleWebSocketDisconnectListener(testEvent);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(ChatMessage.class));
        // Add logger verification if SLF4j test support is used
    }
}
