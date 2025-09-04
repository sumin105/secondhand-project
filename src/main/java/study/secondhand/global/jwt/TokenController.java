package study.secondhand.global.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

import java.util.Arrays;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("--- [API] /api/token/refresh 요청 도착 ---");

        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = extractCookie(request, "REFRESH_TOKEN");
        log.info("1. 쿠키에서 추출한 Refresh Token: {}", refreshToken);

        if (refreshToken == null || !jwtUtil.isValidToken(refreshToken)) {
            log.warn("-> 실패: Refresh Token이 없거나 유효하지 않음.");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        log.info("-> 성공: Refresh Token 유효성 검사 통과");

        // 2. refreshToken에서 userId 추출
        String userId = jwtUtil.getSubject(refreshToken);
        log.info("2. 토큰에서 추출한 UserId: {}", userId);

        // 3. DB에서 사용자 조회
        User user = userRepository.findById(Long.parseLong(userId))
                .orElse(null);

        if (user == null || user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            log.warn("-> 실패: DB에 사용자가 없거나 RefreshToken 불일치. DB Token: {}", user != null ? user.getRefreshToken() : "N/A");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        log.info("-> 성공: DB 토큰 일치 확인");

        // 4. 새 AccessToken 생성
        String newAccessToken = jwtUtil.createToken(userId);
        log.info("4. 새로운 AccessToken 생성 완료");

        // 5. AccessToken을 쿠키로 재전송
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(60 * 60);
        accessCookie.setPath("/");
        accessCookie.setSecure(false); // 배포 시 true로
        response.addCookie(accessCookie);
        response.setStatus(HttpStatus.OK.value());
        log.info("5. 새로운 AccessToken 쿠키 설정 완료. 재발급 성공.");
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
