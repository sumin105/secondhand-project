package study.secondhand.module.chat;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지를 구독할 prefix, 구독경로
        config.enableSimpleBroker("/topic"); // 구독 경로
        // 클라이언트에서 보낼 prefix 경로
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 주소 (프론트는 여기에 연결 시도)
        registry.addEndpoint("/ws") // 클라이언트가 연결할 엔드포인트
                .setAllowedOriginPatterns("*") // 배포 시에는 도메인 명시
                .withSockJS(); // SockJS를 통해 fallback 지원
    }
}
