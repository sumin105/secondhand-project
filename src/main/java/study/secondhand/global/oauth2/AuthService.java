package study.secondhand.global.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.global.jwt.JwtUtil;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String reissueToken(String refreshToken) {
        log.info("\n--- [AuthService] 토큰 재발급 시작 ---");

        // 1. RefreshToken 유효성 검증
        if (refreshToken == null || !jwtUtil.isValidToken(refreshToken)) {
            log.warn("-> 재발급 실패: 쿠키에 담긴 Refresh Token이 유효하지 않습니다.");
            throw new SecurityException("유효하지 않은 RefreshToken입니다.");
        }
        log.info("-> 1. 토큰 유효 기간 검사 통과.");

        // 2. Token에서 UserId 추출
        String userId = jwtUtil.getSubject(refreshToken);
        log.info("-> 2. 토큰에서 추출한 UserId: {}", userId);

        // 3. DB에 저장된 RefreshToken과 일치하는지 확인
        User user = userRepository.findById(Long.parseLong(userId))
                .filter(u -> refreshToken.equals(u.getRefreshToken()))
                .orElseThrow(() -> {
                    log.warn("-> 재발급 실패: DB에 사용자가 없거나 Refresh Token이 일치하지 않습니다.");
                    userRepository.findById(Long.parseLong(userId))
                            .ifPresent(u -> log.warn("   - 쿠키 토큰: {}", refreshToken));
                    userRepository.findById(Long.parseLong(userId))
                            .ifPresent(u -> log.warn("   - DB 토큰: {}", u.getRefreshToken()));
                    return new SecurityException("RefreshToken이 일치하지 않습니다.");
                });
        log.info("-> 3. DB 토큰 일치 확인 통과.");

        // 4. 새로운 AccessToken 생성
        log.info("-> 4. 새로운 Access Token을 생성합니다.");
        return jwtUtil.createToken(user.getId().toString());
    }
}
