package com.example.agentplatform.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enables a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" or "/queue".
        // Application-level messages (e.g., messages from @MessageMapping methods)
        // will be routed to these prefixes.
        config.enableSimpleBroker("/topic", "/queue");

        // Designates the "/app" prefix for messages that are bound for @MessageMapping-annotated methods.
        // For example, a message sent to "/app/chat.sendMessage" would be routed to a
        // @MessageMapping("/chat.sendMessage") method.
        config.setApplicationDestinationPrefixes("/app");

        // Optionally, configure user destination prefix if using user-specific messaging
        // config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling SockJS fallback options so that alternate transports
        // may be used if WebSocket is not available.
        // SockJS is used to enable fallback options for browsers that don’t support WebSocket.
        // withSockJS() is recommended for broader compatibility.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for development. Restrict in production.
                .withSockJS();

        // Another endpoint without SockJS if preferred for certain clients
        // registry.addEndpoint("/ws-native")
        //        .setAllowedOriginPatterns("*");
    }
}
