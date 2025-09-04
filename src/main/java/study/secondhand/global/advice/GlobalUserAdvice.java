package study.secondhand.global.advice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

import java.util.Map;

@ControllerAdvice
public class GlobalUserAdvice {

    private final UserRepository userRepository;

    public GlobalUserAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("loginUser")
    public User loginUser(@AuthenticationPrincipal Object principal) {
        if (principal == null) return null;

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        if (principal instanceof OAuth2User oauth2User) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            if (attributes == null) return null;

            Object idAttr = attributes.get("id");
            if (idAttr == null) {
                // 네이버의 경우 response 내부에 id가 있음
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                if (response != null) {
                    idAttr = response.get("id");
                }
            }
            if (idAttr == null) return null;

            String oauthId = String.valueOf(idAttr);
            return userRepository.findByOauthId(oauthId).orElse(null);
        }

        return null;
    }
}
