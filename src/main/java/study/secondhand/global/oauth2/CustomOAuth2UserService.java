package study.secondhand.global.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;
import study.secondhand.global.jwt.JwtUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final HttpServletResponse response;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2 유저 정보 가져오기
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        if (oAuth2User.getAttributes() == null) {
            throw new OAuth2AuthenticationException("OAuth2 user attributes are missing");
        }

        // 2. 제공자 및 사용자 정보 파싱
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, attributes);

        // 3. DB에 사용자 저장 또는 업데이트
        User user = saveOrUpdate(oAuthAttributes, registrationId);

        // 4. 탈퇴한 계정인지 확인
        if (user.isDeleted() || user.isWithdrawn()) {
            throw new DisabledException("탈퇴한 계정입니다.");
        }

        // 5. JWT 토큰 생성 후 쿠키에 저장
        String accessToken = jwtUtil.createToken(user.getId().toString());

        // DB에 저장된 Refresh Token
        String refreshToken = user.getRefreshToken();

        // RefreshToken이 없다면 (최초 로그인) 새로 생성하고 DB에 저장
        if (refreshToken == null) {
            refreshToken = jwtUtil.createRefreshToken(user.getId().toString());
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        // RefreshToken 쿠키 저장
        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        refreshCookie.setSecure(false); // 배포 시 true로
        response.addCookie(refreshCookie);

        // AccessToken 쿠키 저장
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60); // 브라우저에 저장된 쿠키 유효 시간: 1시간
        accessCookie.setSecure(false); // 배포 시 true로\
        response.addCookie(accessCookie);

        return new CustomUserDetails(user, attributes);
    }

    private User saveOrUpdate(OAuthAttributes attributes, String provider) {
        System.out.println("DEBUG - oauthId: " + attributes.getOauthId());

        return userRepository.findByOauthIdAndProvider(attributes.getOauthId(), provider)
                .orElseGet(() -> {
                    User newUser = new User(
                            attributes.getOauthId(),
                            provider,
                            attributes.getName(),
                            attributes.getEmail()
                    );
                    return userRepository.save(newUser);
                });
    }
}
