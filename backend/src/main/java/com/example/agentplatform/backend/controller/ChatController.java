package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles messages sent to "/app/chat.sendMessage".
     * The received message is broadcast to all subscribers of "/topic/publicChatRoom".
     *
     * @param chatMessage The incoming chat message.
     * @return The processed chat message (which will be sent to the topic).
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/publicChatRoom")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        // Here, you could save the message to a database if needed
        // For now, just forwarding it
        return chatMessage;
    }

    /**
     * Handles messages sent to "/app/chat.addUser" when a user joins.
     * It adds the username to the WebSocket session and broadcasts a JOIN message.
     *
     * @param chatMessage The message containing sender information.
     * @param headerAccessor Accessor for WebSocket message headers.
     * @return The JOIN message to be broadcast.
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/publicChatRoom")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username to WebSocket session attributes
        if (headerAccessor.getSessionAttributes() != null && chatMessage.getSender() != null) {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            headerAccessor.getSessionAttributes().put("sessionId", headerAccessor.getSessionId());
        }

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());
        // Content could be something like "User X has joined"
        // For now, the client can interpret the JOIN type with the sender info.

        return chatMessage;
    }

    // Example of sending a message to a specific user (not used in public chat directly yet)
    // public void sendPrivateMessage(ChatMessage chatMessage) {
    //     if (chatMessage.getRecipient() != null && !chatMessage.getRecipient().isEmpty()) {
    //         chatMessage.setTimestamp(LocalDateTime.now());
    //         messagingTemplate.convertAndSendToUser(
    //                 chatMessage.getRecipient(), // This should be the user's name (principal name)
    //                 "/queue/privateMessages",    // Destination queue for this user
    //                 chatMessage
    //         );
    //     }
    // }

    // We might also need a way to handle disconnections.
    // This can be done by listening to SessionDisconnectEvent. See WebSocketEventListener example.
}
