package com.example.demo.configs.websocket;

import com.example.demo.configs.security.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    public static class WebSocketPrincipal implements Principal {
        private final UserPrincipal user;

        public WebSocketPrincipal(UserPrincipal user) {
            this.user = user;
        }

        @Override
        public String getName() {
            return user.getId().toString();
        }

        public UserPrincipal getUser() {
            return user;
        }
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {

            WebSocketPrincipal wsPrincipal = new WebSocketPrincipal(userPrincipal);

            Authentication stompAuth = new UsernamePasswordAuthenticationToken(
                    wsPrincipal,
                    authentication.getCredentials(),
                    userPrincipal.getAuthorities()
            );
            attributes.put("SPRING_SECURITY_CONTEXT", stompAuth);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}