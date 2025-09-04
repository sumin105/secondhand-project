package study.secondhand.global.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.secondhand.global.jwt.JwtUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @GetMapping("/status")
    public ResponseEntity<Void> checkLoginStatus(
            @CookieValue(value = "ACCESS_TOKEN", required = false) String accessToken,
            @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        // 1. AccessToken 유효한지 확인
        if (accessToken != null && jwtUtil.isValidToken(accessToken)) {
            // 유효하면 200 OK 응답
            return ResponseEntity.ok().build();
        }

        // 2. AccessToken 만료 시, RefreshToken으로 재발급 시도
        try {
            String newAccessToken = authService.reissueToken(refreshToken);

            // 재발급 성공 시, 새로운 AccessToken을 쿠키에 담아 201 Created 응답
            Cookie accessCookie = new Cookie("ACCESS_TOKEN", newAccessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setMaxAge(60 * 60);
            accessCookie.setPath("/");
            response.addCookie(accessCookie);

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (SecurityException e) {
            // 3. RefreshToken도 유효하지 않으면 401 Unauthorized 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
