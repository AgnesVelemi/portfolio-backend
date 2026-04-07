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

    private static final DateTimeFormatter MSG_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm:ss", Locale.ENGLISH);

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
        
        // Format: [8] BE: CET:2026.04.06. 16:48:48 ip:192.168.1.10:8080 | message: rembaba
        String timestamp = ZonedDateTime.now().format(MSG_FORMATTER);
        String formattedMsg = String.format("[%d] %s: CET:%s ip:%s | message: %s", 
                displayNum, type, timestamp, ip, payload);

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
        return String.join("<br/>", currentMessages);
    }

    public String getArchivedMessagesHtml() {
        return String.join("<br/>", archivedMessages);
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