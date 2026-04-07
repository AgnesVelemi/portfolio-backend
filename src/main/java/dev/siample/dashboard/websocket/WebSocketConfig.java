package dev.siample.dashboard.websocket;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker  // Enable STOMP
public class WebSocketConfig implements WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

    @Value("${app.websocket.allowed-origins}")
    private String allowedOrigins;

    private final DashboardWebSocketHandler handler;

    public WebSocketConfig(DashboardWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // for local raw websocket client+server
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                                 .map(String::trim)
                                 .toArray(String[]::new);
        registry.addHandler(handler, "/ws/dashboard")
                .setAllowedOrigins(origins)
                .addInterceptors(new ClientIpInterceptor());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { // for STOMP over websocket server
        // Register STOMP endpoint
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                                 .map(String::trim)
                                 .toArray(String[]::new);
        registry.addEndpoint("/ws/stomp") // STOMP endpoint
                .setAllowedOrigins(origins)
                .addInterceptors(new ClientIpInterceptor())
                .addInterceptors(new org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { // for STOMP over websocket server
        // Enable simple broker
        config.enableSimpleBroker("/topic"); // For messaging for subscribed ws-clients
        config.setApplicationDestinationPrefixes("/app"); // For sending messages by clients to /app/<controller_endpoint>
    }
}
