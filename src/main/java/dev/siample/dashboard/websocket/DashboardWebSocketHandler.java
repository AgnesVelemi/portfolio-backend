package dev.siample.dashboard.websocket;

import dev.siample.dashboard.service.WebSocketStatusService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketStatusService statusService;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public DashboardWebSocketHandler(WebSocketStatusService statusService) {
        this.statusService = statusService;
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

        // Echo the message back to the dashboard session for real-time update via its listener
        if (session.isOpen()) {
            session.sendMessage(message);
        }
    }
}
