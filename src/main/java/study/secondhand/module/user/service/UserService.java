package study.secondhand.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.global.exception.DuplicateNicknameException;
import study.secondhand.module.admin.dto.AdminUserDto;
import study.secondhand.module.chat.service.ChatService;
import study.secondhand.module.payment.dto.PortOnePaymentRequest;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.product.dto.ProductSummaryDto;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.user.dto.DeliveryInfoDto;
import study.secondhand.module.user.dto.ShopHeaderDto;
import study.secondhand.module.user.dto.ShopViewDto;
import study.secondhand.module.user.dto.StoreSummaryDto;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final ChatService chatService;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    public DeliveryInfoDto getDeliveryInfo(Long id) {
        return Optional.ofNullable(userRepository.findDeliveryInfoById(id))
                .orElseGet(DeliveryInfoDto::new);
    }

    @Transactional
    public void updateUserAddress(User user, PortOnePaymentRequest request) {
        user.setPhoneNumber(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDetailAddress(Optional.ofNullable(request.getDetailAddress()).orElse(""));
        user.setPostCode(request.getPostCode());
        userRepository.save(user);
    }

    @Transactional
    public void updateUserStore(User user, PortOnePaymentRequest request) {
        user.setPhoneNumber(request.getPhone());
        user.setStoreName(request.getStoreName());
        user.setStoreAddress(request.getStoreAddress());
        userRepository.save(user);
    }

    public Page<StoreSummaryDto> searchStoreSummaries(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 20);

        // 상점 ()호 패턴 감지
        Pattern shopIdPattern = Pattern.compile("^상점\\s*(\\d+)호$");
        Matcher matcher = shopIdPattern.matcher(keyword);

        // 패턴 일치 확인
        if (matcher.find()) {
            // 일치하면 숫자 부분만 추출
            Long userId = Long.parseLong(matcher.group(1));

            // 추출된 ID로 상점 직접 검색
            return userRepository.findStoreSummaryById(userId, pageable);
        } else {
            // 패턴이 안 맞으면 일반 닉네임 검색
            return userRepository.findStoreSummariesByKeyword(keyword, pageable);
        }
    }


    @Transactional
    public void updateUserInfo(Long id, String newNickname, String newIntro) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (newNickname == null || newNickname.isBlank()) {
            if (user.getNickname() == null || user.getNickname().isBlank()) {
                throw new IllegalArgumentException("닉네임은 필수 입력 값입니다.");
            }
            newNickname = user.getNickname(); // 기존 닉네임 유지
        }

        // '상점..호' 패턴 검사
        Pattern shopIdPattern = Pattern.compile("^상점\\s*(\\d+)호$");
        Matcher matcher = shopIdPattern.matcher(newNickname);
        if (matcher.find()) {
            throw new IllegalArgumentException("'상점'으로 시작하고 '호'로 끝나는 형식의 닉네임은 사용할 수 없습니다.");
        }

        // 형식 유효성 검사
        if (!isValidNickname(newNickname)) {
            throw new IllegalArgumentException("닉네임은 2~12자의 한글/영문 또는 숫자 조합이어야 하며, 숫자만은 사용할 수 없습니다.");
        }

        // 닉네임 중복 검사 (본인 제외)
        if (!newNickname.equals(user.getNickname())) {
            boolean isDuplicate = userRepository.existsByNicknameAndIdNot(newNickname, user.getId());
            if (isDuplicate) {
                throw new DuplicateNicknameException("이미 있는 닉네임입니다.");
            }
            user.setNickname(newNickname);
        }

        // 소개글 수정 (지우면 null 처리)
        if (newIntro != null && !newIntro.isBlank()) {
            if (newIntro.length() > 500) {
                throw new IllegalArgumentException("소개글은 최대 500자까지 작성할 수 있습니다.");
            }
            user.setIntro(newIntro);
        } else {
            user.setIntro(null);
        }
    }

    private boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) return false;

        String regex = "^(?=.*[a-zA-Z가-힣])[a-zA-Z0-9가-힣]{2,12}$";
        return nickname.matches(regex);
    }

    public Long count() {
        return userRepository.count();
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawUser(Long targetUserId, User loginUser) {
        if (!targetUserId.equals(loginUser.getId()) && !loginUser.isAdmin()) {
            throw new AccessDeniedException("회원 탈퇴 권환이 없습니다.");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 거래 완료되지 않은 주문이 있는지 확인
        boolean hasIncompleteOrders = orderService.hasIncompleteOrders(user);
        if (hasIncompleteOrders) {
            throw new IllegalStateException("진행 중인 거래가 있어 탈퇴할 수 없습니다.");
        }

        // 유저의 등록된 모든 상품 삭제
        productService.deleteProductsByUser(user);

        // 기타 연관 여부 판단
        boolean hasRelations = orderService.existsByBuyerOrSeller(user) ||
                chatService.existsBySenderOrReceiver(user);

        // 탈퇴 처리 방식 선택
        if (hasRelations) {
            user.setDeleted(true);
            user.setStatus(User.UserStatus.WITHDRAWN);
            userRepository.save(user);
        } else {
            userRepository.delete(user);
        }
    }

    // 유저 정지 or 정지해제
    @Transactional
    public String toggleUserSuspend(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() == User.UserStatus.BANNED) {
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
            return "ID: " + user.getId() + " 회원의 정지가 해제되었습니다.";
        } else {
            user.setStatus(User.UserStatus.BANNED);
            userRepository.save(user);
            return "ID: " + user.getId() + " 회원이 정지되었습니다.";
        }
    }

    public User findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .filter(user -> user.getRole() == User.Role.USER) // ADMIN, SYSTEM 제외
                .orElse(null);
    }

    public Page<AdminUserDto> findUsersForAdmin(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAllDeletedFalseUsers(pageable);
        return userPage.map(AdminUserDto::new);
    }

    public User getReferenceById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public User getSystemUser() {
        return userRepository.findByOauthIdAndProvider("system-id", "system")
                .orElseThrow(() -> new IllegalStateException("시스템 유저가 존재하지 않습니다."));
    }

    public List<User> findUserSummariesByIds(List<Long> userIds) {
        return userRepository.findAllByIdIn(userIds);
    }

    @Transactional(readOnly = true)
    public ShopViewDto getShopViewDto(User loginUser, Long shopOwnerId, int page) {
        // 상점 주인 정보
        User shopOwner = findActiveUserById(shopOwnerId);
        if (shopOwner == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Page<ProductSummaryDto> products = productService.findUserProductSummaries(loginUser, shopOwnerId, page);
        return new ShopViewDto(shopOwner, products);
    }

    @Transactional(readOnly = true)
    public ShopHeaderDto getShopHeaderDto(User loginUser, Long id, String activeTab) {
        User shopOwner = findActiveUserById(id);
        if (shopOwner == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        int soldCount = orderService.countCompletedOrdersBySeller(id);

        return new ShopHeaderDto(shopOwner, loginUser, soldCount, activeTab);
    }

    public void isShopOwner(Long id, User user) {
        if (!id.equals(user.getId())) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }
}
