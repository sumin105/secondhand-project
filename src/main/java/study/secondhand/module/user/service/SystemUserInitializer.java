package study.secondhand.module.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class SystemUserInitializer {
    private final UserRepository userRepository;

    private static final String SYSTEM_OAUTH_ID = "system-id";
    private static final String SYSTEM_PROVIDER = "system";
    private static final String SYSTEM_NAME = "시스템";
    private static final String SYSTEM_EMAIL = "system@system.com";

    @PostConstruct
    public void createSystemUserIfNotExist() {
        boolean exists = userRepository.existsByOauthIdAndProvider(SYSTEM_OAUTH_ID, SYSTEM_PROVIDER);
        if (!exists) {
            User systemUser = User.builder()
                    .oauthId(SYSTEM_OAUTH_ID)
                    .provider(SYSTEM_PROVIDER)
                    .name(SYSTEM_NAME)
                    .email(SYSTEM_EMAIL)
                    .role(User.Role.SYSTEM)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            userRepository.save(systemUser);
        }
    }
}
