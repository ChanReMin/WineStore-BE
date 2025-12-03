package com.example.demo.configs.websocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // ✅ Skip nếu message không mutable (immutable)
        if (!accessor.isMutable()) {
            log.debug("⚠️ Message is not mutable, skipping user set");
            return message;
        }

        // ✅ Skip DISCONNECT event
        StompCommand command = accessor.getCommand();
        if (command != null && command == StompCommand.DISCONNECT) {
            log.debug("⚠️ Skipping DISCONNECT command");
            return message;
        }

        try {
            if (accessor.getSessionAttributes() != null) {
                Object auth = accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");

                if (auth instanceof UsernamePasswordAuthenticationToken token
                        && token.getPrincipal() instanceof JwtHandshakeInterceptor.WebSocketPrincipal wsPrincipal) {
                    accessor.setUser(wsPrincipal);
                    log.debug("✅ WebSocket user set: {}", wsPrincipal.getName());
                }
            }
        } catch (IllegalStateException e) {
            // ✅ Bắt lỗi "Already immutable" và bỏ qua
            log.warn("⚠️ Cannot set user - message already immutable: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error setting WebSocket user: {}", e.getMessage(), e);
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.debug("⚠️ Error clearing security context: {}", e.getMessage());
        }
    }
}