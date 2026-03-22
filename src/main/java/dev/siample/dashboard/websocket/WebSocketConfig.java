package dev.siample.dashboard.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker  // Enable STOMP
public class WebSocketConfig implements WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

    private final DashboardWebSocketHandler handler;

    public WebSocketConfig(DashboardWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // for local raw websocket client+server
        registry.addHandler(handler, "/ws/dashboard")
                .setAllowedOrigins("*");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { // for STOMP over websocket server
        // Register STOMP endpoint
        registry.addEndpoint("/ws/stomp") // STOMP endpoint
                .setAllowedOrigins("http://localhost:4200", "http://localhost:1025", "http://localhost:8080", "*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { // for STOMP over websocket server
        // Enable simple broker
        config.enableSimpleBroker("/topic"); // For messaging for subscribed ws-clients
        config.setApplicationDestinationPrefixes("/app"); // For sending messages by clients to /app/<controller_endpoint>
    }
}
