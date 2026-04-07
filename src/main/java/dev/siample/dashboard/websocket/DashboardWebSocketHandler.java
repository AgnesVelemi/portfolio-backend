package dev.siample.dashboard.websocket;

import dev.siample.dashboard.dto.StompMsgToClientDto;
import dev.siample.dashboard.service.WebSocketStatusService;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketStatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public DashboardWebSocketHandler(WebSocketStatusService statusService, 
                                     @Lazy SimpMessagingTemplate messagingTemplate) {
        this.statusService = statusService;
        this.messagingTemplate = messagingTemplate;
    }

    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        connected.set(true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        connected.set(false);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Get Real IP from attributes (captured by ClientIpInterceptor)
        Map<String, Object> sessionAttributes = session.getAttributes();
        String ip = (sessionAttributes != null) ? (String) sessionAttributes.get("IP_ADDRESS") : null;
        if (ip == null) {
            ip = session.getRemoteAddress() != null 
                ? session.getRemoteAddress().getHostString() 
                : "unknown";
        }

        // Add to history as BE
        statusService.addFormattedMessage("BE", payload, ip);

        // Broadcast to STOMP topic so all dashboards update in real-time
        messagingTemplate.convertAndSend("/topic/greetings", 
            new StompMsgToClientDto(payload, OffsetDateTime.now(), "BE", ip, "CET"));
    }
}
