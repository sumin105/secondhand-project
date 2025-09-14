package study.secondhand.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.product.repository.CategoryRepository;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AdminConfig adminConfig;

    private static final String SYSTEM_OAUTH_ID = "system-id";
    private static final String SYSTEM_PROVIDER = "system";
    private static final String SYSTEM_NAME = "SYSTEM";
    private static final String SYSTEM_EMAIL = "system@system.com";

    @Override
    public void run(String... args) throws Exception {
        // 시스템 유저 생성
        createSystemUserIfNotExists();

        // 카테고리 생성
        createCategoriesIfNotExists();

        // 어드민 유저 생성
        promoteAdminUser();
    }

    private void createSystemUserIfNotExists() {
        boolean exists = userRepository.existsByOauthIdAndProvider(SYSTEM_OAUTH_ID, SYSTEM_PROVIDER);
        if (!exists) {
            User systemUser = User.builder()
                    .oauthId(SYSTEM_OAUTH_ID)
                    .provider(SYSTEM_PROVIDER)
                    .name(SYSTEM_NAME)
                    .email(SYSTEM_EMAIL)
                    .role(User.Role.SYSTEM)
                    .build();

            userRepository.save(systemUser);
            System.out.println("System user has been created");
        }
    }

    private void createCategoriesIfNotExists() {
        if (categoryRepository.count() == 0) {
            List<String> categoryNames = Arrays.asList(
                    "의류", "전자기기", "가구/인테리어", "생활/주방용품",
                    "유아용품", "뷰티/미용", "스포츠/레저", "취미/게임/음반",
                    "도서", "티켓/교환권", "가공식품", "반려동물용품", "기타"
            );

            List<Category> categories = categoryNames.stream()
                    .map(Category::new)
                    .toList();

            categoryRepository.saveAll(categories);
            System.out.println("Initial categories have been created");
        }
    }

    private void promoteAdminUser() {
        Optional<User> userOptional = userRepository.findByEmail(adminConfig.getEmail());

        if (userOptional.isPresent() && userOptional.get().getRole() == User.Role.USER) {
            User adminUser = userOptional.get();
            adminUser.setRole(User.Role.ADMIN);
            userRepository.save(adminUser);
            System.out.println("Admin user '" + adminConfig.getEmail() + "' has been promoted.");
        }
    }
}
