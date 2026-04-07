package dev.siample.dashboard.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import org.springframework.lang.Nullable;
import java.util.Map;

public class ClientIpInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // Primary check for Nginx X-Forwarded-For header
            String ip = servletRequest.getServletRequest().getHeader("X-Forwarded-For");
            
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                // Secondary check for X-Real-IP
                ip = servletRequest.getServletRequest().getHeader("X-Real-IP");
            }
            
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                // Final fallback to remote address
                ip = servletRequest.getServletRequest().getRemoteAddr();
            }

            // Extract only the first IP if multiple exist (comma separated)
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            // Store in session attributes for later retrieval
            attributes.put("IP_ADDRESS", ip);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        // No action needed after handshake
    }
}
