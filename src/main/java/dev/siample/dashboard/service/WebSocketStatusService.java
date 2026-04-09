package dev.siample.dashboard.service;

import dev.siample.dashboard.dto.DashboardStatusDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WebSocketStatusService {

    private final AtomicInteger connectedClients = new AtomicInteger(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final ZonedDateTime serverStartTime = ZonedDateTime.now();

    private final List<String> currentMessages = Collections.synchronizedList(new ArrayList<>());
    private final List<String> archivedMessages = Collections.synchronizedList(new ArrayList<>());

    private static final DateTimeFormatter MSG_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss",
            Locale.ENGLISH);

    public void incrementClients() {
        connectedClients.incrementAndGet();
    }

    public void decrementClients() {
        int current;
        do {
            current = connectedClients.get();
            if (current == 0) {
                return; // prevent negative values
            }
        } while (!connectedClients.compareAndSet(current, current - 1));
    }

    public int getConnectedClients() {
        return connectedClients.get();
    }

    public void incrementMessages() {
        totalMessages.incrementAndGet();
    }

    public synchronized void addFormattedMessage(String type, String payload, String ip) {
        long currentCount = totalMessages.incrementAndGet();

        // Numbering within the batch 1-10
        int displayNum = (int) ((currentCount - 1) % 10) + 1;

        String displayPayload = payload;
        String displayIp = ip;

        // If it's a BE message and looks like JSON, try to extract values
        if ("BE".equals(type) && payload != null && payload.trim().startsWith("{")) {
            try {
                // Using simple string parsing to avoid adding Jackson dependency if not needed,
                // but since it's Spring, let's assume Jackson is fine.
                // However, for simplicity and robustness in this specific format:
                if (payload.contains("\"outMessage\":\"") && payload.contains("\"sentFromIP\":\"")) {
                    String outMsg = payload.split("\"outMessage\":\"")[1].split("\"")[0];
                    String fromIp = payload.split("\"sentFromIP\":\"")[1].split("\"")[0];
                    displayPayload = outMsg;
                    displayIp = fromIp;
                }
            } catch (Exception e) {
                // Fallback to original if parsing fails
            }
        }

        // Format: [1] BE: 2026.04.08.07:25:44 received from 13.63.37.93 | message: 123
        String timestamp = ZonedDateTime.now().format(MSG_FORMATTER);
        String formattedMsg = String.format("<div>[%d] %s: %s received from %s | message: %s</div>",
                displayNum, type, timestamp, displayIp, displayPayload);

        // Reset batch if we just hit 10
        if (currentMessages.size() >= 10) {
            archivedMessages.clear();
            archivedMessages.addAll(currentMessages);
            currentMessages.clear();
        }

        // Add to the top
        currentMessages.add(0, formattedMsg);
    }

    public String getCurrentMessagesHtml() {
        return String.join("", currentMessages);
    }

    public String getArchivedMessagesHtml() {
        return String.join("", archivedMessages);
    }

    public DashboardStatusDto getCurrentStats() {
        return DashboardStatusDto.builder()
                .connectedClients(connectedClients.get())
                .totalMessages(totalMessages.get())
                .serverStartTime(serverStartTime)
                .timestamp(LocalDateTime.now())
                .build();
    }
}