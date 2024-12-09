package vives.bancovives.notifications.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.jwt.JwtService;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtService jwtService;
    private final UsersService usersService;

    @Autowired
    public WebSocketConfig(JwtService jwtService, UsersService usersService) {
        this.jwtService = jwtService;
        this.usersService = usersService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketNotificationsHandler(), "/ws/notifications")
                .addInterceptors(new HttpSessionHandshakeInterceptor(), new CustomHandshakeInterceptor());
    }

    @Bean
    public vives.bancovives.notifications.websocket.config.WebSocketHandler webSocketNotificationsHandler() {
        return new vives.bancovives.notifications.websocket.config.WebSocketHandler("Notification", jwtService, usersService);
    }

    public static class CustomHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request,
                                       ServerHttpResponse response,
                                       org.springframework.web.socket.WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) throws Exception {
            String token = extractToken(request);
            attributes.put("token", token);
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Exception ex) {
            //Nada, no clean up needed
        }

        private String extractToken(org.springframework.http.server.ServerHttpRequest request) {
            // Example: extract token from headers
            List<String> authorization = request.getHeaders().get("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                return authorization.get(0).replace("Bearer ", "");
            }
            return null;
        }
    }
}

