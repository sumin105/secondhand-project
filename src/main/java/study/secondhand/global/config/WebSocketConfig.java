package study.secondhand.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsConfig corsConfig;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지를 구독할 prefix, 구독경로
        config.enableSimpleBroker("/topic", "/queue"); // 구독 경로
        // 클라이언트에서 서버로 보낼 prefix 경로
        config.setApplicationDestinationPrefixes("/app");
        // 사용자 개인 베시지를 위한 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 주소 (프론트는 여기에 연결 시도)
        registry.addEndpoint("/ws") // 클라이언트가 연결할 엔드포인트
                .setAllowedOriginPatterns(corsConfig.getAllowedOrigins()) // 배포 시에는 도메인 명시
                .withSockJS(); // SockJS를 통해 fallback 지원
    }
}
