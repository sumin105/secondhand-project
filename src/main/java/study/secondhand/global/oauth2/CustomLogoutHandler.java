package study.secondhand.global.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.global.jwt.JwtUtil;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(CustomLogoutHandler.class);
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.error("--- [CustomLogoutHandler] 실행---");

        String accessToken = extractCookie(request, "ACCESS_TOKEN");
        if (accessToken != null) {
            try {
                // 토큰에서 사용자 ID 추출
                String userId = jwtUtil.getSubject(accessToken);
                log.info("-> 로그아웃 요청 사용자 ID (from Token)");

                // DB에서 해당 사용자를 찾아 RefreshToken을 null로 설정
                userRepository.findById(Long.parseLong(userId)).ifPresent(foundUser -> {
                    log.info("-> DB에서 사용자 조회 성공. RefreshToken 삭제 시작");
                    foundUser.setRefreshToken(null);
                    userRepository.save(foundUser);
                    log.info("-> DB RefreshToken 삭제 완료.");
                });
            } catch (Exception e) {
                log.warn("-> 로그아웃 처리 중 오류 발생 (유효하지 않은 토큰 등): {}", e.getMessage());
            }
        } else {
            log.warn("-> AccessToken 쿠키를 찾을 수 없어 DB 작업을 건너뜁니다.");
        }
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() ==  null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
