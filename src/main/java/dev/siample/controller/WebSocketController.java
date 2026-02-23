package dev.siample.controller;

import dev.siample.dto.WSMessageFromClientDTO;
import dev.siample.dto.WSMessageToClientDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Controller
public class WebSocketController {

    // private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/greet") // when client sent to app/greet url
    @SendTo("/topic/greetings") // return to url
    public WSMessageToClientDTO greeting(WSMessageFromClientDTO wSMessageFromClientDTO) {
        System.out.println("Message arrived from ws-client: " + wSMessageFromClientDTO.getPayload());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new WSMessageToClientDTO('"' + wSMessageFromClientDTO.getPayload() + '"', OffsetDateTime.now());
    }
}
