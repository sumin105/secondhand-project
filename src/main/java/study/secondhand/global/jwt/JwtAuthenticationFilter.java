package study.secondhand.global.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import study.secondhand.global.oauth2.CustomAuthentication;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 쿠키에서 JWT 토큰 추출
        String jwtToken = getJwtFromCookies(request);

        // 1. 쿠키에 AccessToken이 존재하는 경우
        if (jwtToken != null) {
            // 2. 토큰이 유효한지 검증
            if (jwtUtil.isValidToken(jwtToken)) {
                // 2-1. 토큰이 유효하면, 사용자 정보를 찾아 인증 상태로 만들기
                try {
                    String userId = jwtUtil.getSubject(jwtToken);
                    User user = userRepository.findById(Long.valueOf(userId))
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다." + userId));

                    CustomUserDetails userDetails = new CustomUserDetails(user, null);
                    CustomAuthentication authentication = new CustomAuthentication(userDetails);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("[JwtAuthFilter] 인증 완료: userId = {}, nickname = {}", user.getId(), user.getNickname());
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    logger.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
                }
            } else {
                // 2-2. 토큰이 존재하지만 만료되었거나 유효하지 않은 경우 401 에러 응답, 필터 체인 중단
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access Token expired or invalid");
                System.out.println("401");
                return;
            }
        }

        // 3. 토큰이 아예 없거나, 유효한 토큰으로 인증 처리가 끝난 경우, 다음 필터로 요청 넘김
        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
