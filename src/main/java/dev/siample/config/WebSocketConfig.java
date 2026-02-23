package dev.siample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * The "/ws/stomp" as websocket connecting endpoint callable from http://localhost:4200
     * with SockJS as well.
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/stomp").setAllowedOrigins("http://localhost:4200");
        registry.addEndpoint("/ws/stomp").setAllowedOrigins("http://localhost:4200").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");// BE-ws-server sends to /topic/* for subscribed ws-clients
        registry.setApplicationDestinationPrefixes("/app");    // FE-ws-client sends to /app/<controller_endpoint>
    }
}
