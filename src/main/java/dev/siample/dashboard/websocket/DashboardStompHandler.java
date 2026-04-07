package dev.siample.dashboard.websocket;

import dev.siample.dashboard.dto.ArchiveMessageDto;
import dev.siample.dashboard.dto.StompMsgToClientDto;
import dev.siample.dashboard.dto.WSMessageFromClientDto;
import dev.siample.dashboard.service.WebSocketStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardStompHandler {

    private final WebSocketStatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/greet") // Maps to /app/greet
    public void handleStompMessage(@Payload WSMessageFromClientDto wSMessageFromClientDto,
                                   Message<?> message) {

        // Log the payload message
        String payload = wSMessageFromClientDto.getPayload();
        System.out.println("STOMP Message arrived from ws-client: " + payload);

        // Log the headers
        Map<String, Object> headers = message.getHeaders();
        
        // Retrieve the client-type from nativeHeaders
        @SuppressWarnings("unchecked")
        Map<String, Object> nativeHeaders = (Map<String, Object>) headers.get("nativeHeaders");
        String clientType = "Unknown";

        if (nativeHeaders != null) {
            @SuppressWarnings("unchecked")
            List<String> clientTypes = (List<String>) nativeHeaders.get("client-type");
            if (clientTypes != null && !clientTypes.isEmpty()) {
                clientType = clientTypes.get(0);
            }
        }

        System.out.println("Processing /greet request from clientType: " + clientType);

        // Tag: FE if frontend (Angular), else BE
        String tag = "frontend".equalsIgnoreCase(clientType) ? "FE" : "BE";
        
        // Get Real IP from attributes (captured by ClientIpInterceptor)
        @SuppressWarnings("unchecked")
        Map<String, Object> sessionAttributes = (Map<String, Object>) headers.get("simpSessionAttributes");
        String ip = (sessionAttributes != null) ? (String) sessionAttributes.get("IP_ADDRESS") : null;

        if (ip == null || ip.isEmpty()) {
            ip = "unknown";
        }

        // 1. Update backend history (source of truth for page refreshes)
        statusService.addFormattedMessage(tag, payload, ip);

        // 2. Broadcast to all subscribers (real-time sync for dashboards)
        messagingTemplate.convertAndSend("/topic/greetings", 
            new StompMsgToClientDto(
                payload, 
                OffsetDateTime.now(), 
                clientType, 
                ip, 
                "CET"
            ));
    }

    @MessageMapping("/archive")
    public void archiveMessages(@Payload List<ArchiveMessageDto> archiveMessages) {
        System.out.println("--- Archiving 10 messages ---");
        for (ArchiveMessageDto msg : archiveMessages) {
            System.out.println("[" + msg.getArrivalNumber() + "] " + msg.getSentFrom() + ": " 
                + msg.getSentFromTimezone() + ":" + msg.getTimestamp() + " ip:" + msg.getSentFromIP() 
                + " | message: " + msg.getOutMessage());
        }
        System.out.println("--- End of Archive ---");
    }
}
