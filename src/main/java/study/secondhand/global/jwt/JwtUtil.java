package study.secondhand.global.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // JWT 만료시간 : 기본 1시간
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // base64로 인코딩된 문자열을 SecretKey로 변환
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(String subject) {
        return createToken(subject, expiration);
    }

    public String createToken(String subject, long customExpiration) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + customExpiration);

        return Jwts.builder()
                .subject(subject)
                .issuer("secondhand-app") // 발급자
                .issuedAt(new Date(now)) // 발급 시간
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String subject) {
        long refreshTokenExp = 1000L * 60 * 60 * 24 * 7; // 7일
        return createToken(subject, refreshTokenExp);
    }

    public String getSubject(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();

        return parser
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(key)
                    .build();

            parser.parseSignedClaims(token); // JWT 유효성 검증 및 파싱
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}
