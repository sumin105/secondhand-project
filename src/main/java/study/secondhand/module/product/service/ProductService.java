package study.secondhand.module.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import study.secondhand.global.exception.ProductNotFoundException;
import study.secondhand.global.util.TimeUtil;
import study.secondhand.module.admin.dto.AdminProductDto;
import study.secondhand.module.chat.service.ChatService;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.payment.service.PaymentService;
import study.secondhand.module.product.dto.*;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.product.entity.ProductImage;
import study.secondhand.module.product.repository.ProductRepository;
import study.secondhand.module.user.dto.UserSummaryDto;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final FavoriteService favoriteService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ChatService chatService;
    private final UserService userService;

    public ProductService(ProductRepository productRepository, @Lazy FavoriteService favoriteService, CategoryService categoryService, ImageService imageService, PaymentService paymentService, OrderService orderService, @Lazy ChatService chatService, @Lazy UserService userService) {
        this.productRepository = productRepository;
        this.favoriteService = favoriteService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.chatService = chatService;
        this.userService = userService;
    }

    @Transactional
    public Long createProduct(ProductRequestDto dto, User seller) { // 상품 등록
        // 정지 유저는 등록 불가
        if (seller.getStatus() == User.UserStatus.BANNED) {
            throw new IllegalStateException("정지된 유저는 상품을 판매할 수 없습니다.");
        }

        MultipartFile[] files = dto.getImage();
        // 이미지가 최소 1장 이상 있는지 확인
        if (files == null || files.length == 0 || files[0].isEmpty()) {
            throw new IllegalArgumentException("이미지를 최소 1장 등록해야 합니다.");
        }
        // 이미지가 5장 초과인지 확인
        if (files.length > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지만 등록할 수 있습니다.");
        }

        Category category = categoryService.findId(dto.getCategoryId());

        // 1. Product 엔티티 생성 및 저장
        Product product = new Product(dto, seller, category);

        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile file : dto.getImage()) {
            if (!file.isEmpty()) {
                String filename = imageService.storeImage(file);
                String imageUrl = "/images/uploads/" + filename;
                productImages.add(new ProductImage(imageUrl));
            }
        }

        product.setImages(productImages);

        product.updateThumbnailUrl();
        productRepository.save(product);

        return product.getId();
    }

    @Transactional
    public void updateProduct(Long productId, ProductRequestDto dto, List<Long> deletedImageIds, User user) throws AccessDeniedException {
        log.info("상품 수정 요청 시작. productId: {}, 삭제할 이미지 ID 목록: {}", productId, deletedImageIds);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("상품을 수정할 권한이 없습니다.");
        }

        if ("DELIVERY".equals(dto.getDealMethod())) {
            if (dto.getNormalDeliveryFee() == null) {
                dto.setNormalDeliveryFee(0);
            }
            if (dto.getCheapDeliveryFee() == null) {
                dto.setCheapDeliveryFee(0);
            }
        }

        // 1. 과거 주문 기록에 사용된 썸네일 URL 목록을 미리 조회
        List<String> protectedThumbnailUrls = orderService.findThumbnailUrlsByProductId(productId);

        // 2. 삭제 요청된 기존 이미지 목록 순회
        if (deletedImageIds != null) {
            Iterator<ProductImage> iterator = product.getImages().iterator();
            while (iterator.hasNext()) {
                ProductImage image = iterator.next();
                if (deletedImageIds.contains(image.getId())) {

                    // 보호 목록에 포함되어 있지 않은 경우에만 실제 파일 삭제
                    if (!protectedThumbnailUrls.contains(image.getImageUrl())) {
                        // 실제 파일 삭제
                        imageService.deleteImageFile(image.getImageUrl());
                    }

                    iterator.remove();
                }
            }
        }

        // 3. 새로운 이미지 추가
        MultipartFile[] newImages = dto.getImage();
        if (newImages != null) {
            int currentImageCount = product.getImages().size(); // 연관된 이미지 수
            for (MultipartFile file : newImages) {
                if (!file.isEmpty()) {
                    if (currentImageCount >= 5) break;

                    String filename = imageService.storeImage(file);
                    String imageUrl = "/images/uploads/" + filename;

                    ProductImage productImage = new ProductImage(imageUrl);
                    productImage.setProduct(product);
                    currentImageCount++;
                }
            }
        }

        Category category = categoryService.findId(dto.getCategoryId());

        // 4. 상품 필드 업데이트
        product.update(dto, category);
        product.updateThumbnailUrl();
    }

    @Transactional
    public void deleteProduct(Long productId, User loginUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        if (!product.getSeller().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        handleProductDeleteLogic(product);
    }

    @Transactional
    public void adminDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        handleProductDeleteLogic(product);
    }

    // 회원 탈퇴시 해당 유저 상품 전체 삭제
    @Transactional
    public void deleteProductsByUser(User user) {
        List<Product> products = productRepository.findBySellerId(user.getId());

        for (Product product : products) {
            handleProductDeleteLogic(product);
        }
    }

    // 상품 삭제
    @Transactional
    void handleProductDeleteLogic(Product product) {
        boolean hasPayment = paymentService.existsByProduct(product);
        boolean hasOrder = orderService.existsByProduct(product);
        boolean hasMessage = chatService.existsByProduct(product);

        if (hasPayment || hasOrder || hasMessage) {
            product.setDeleted(true);
            productRepository.save(product);
        } else {
            productRepository.delete(product);
        }
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetailDto(Long productId, User loginUser, boolean allowDeleted) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("존재하지 않는 상품입니다."));

        // 삭제된 상품이고 주문서 통한 접근이 아닐시 -> 상품이 존재하지 않는 것처럼 처리
        if (product.isDeleted() && !allowDeleted) {
            throw new ProductNotFoundException("존재하지 않는 상품입니다.");
        }

        // 찜 여부 확인
        boolean isFavorite = false;
        if (loginUser != null) {
            isFavorite = favoriteService.isFavorite(loginUser, product);
        }

        // 찜 개수
        int favoriteCount = favoriteService.getFavoriteCount(productId);

        // 판매자가 본인 글을 보는 경우에만 찜한 유저 ID 목록 반환
        List<UserSummaryDto> wishlistUsers = new ArrayList<>();
        if (loginUser != null && loginUser.getId().equals(product.getSeller().getId())) {
            List<Long> userIds = favoriteService.getFavoriteUserIds(productId);

            if (!userIds.isEmpty()) {
                List<User> users = userService.findUserSummariesByIds(userIds);
                wishlistUsers = users.stream()
                        .map(user -> new UserSummaryDto(user.getId(), user.getNickname()))
                        .toList();
            }
        }

        Long userId = loginUser != null ? loginUser.getId() : null;
        boolean isOwner = (userId != null) && userId.equals(product.getSeller().getId());
        boolean isSelling = product.getStatus() == Product.ProductStatus.SELLING;

        return ProductDetailDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus().toString())
                .dealMethod(product.getDealMethod().toString())
                .createdAt(product.getCreatedAt())
                .favorite(isFavorite)
                .sellerId(product.getSeller().getId())
                .nickname(product.getSeller().getNickname() != null ? product.getSeller().getNickname() : "")
                .favoriteCount(favoriteCount)
                .wishlistUsers(wishlistUsers)
                .normalDeliveryFee(product.getNormalDeliveryFee() != null ? product.getNormalDeliveryFee() : 0)
                .cheapDeliveryFee(product.getCheapDeliveryFee() != null ? product.getCheapDeliveryFee() : 0)
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .images(product.getImages().stream()
                        .map(image -> ProductImageDto.builder()
                                .imageUrl(image.getImageUrl())
                                .build())
                        .toList()
                )
                .deleted(product.isDeleted())
                .sellerBanned(product.getSeller().getStatus() == User.UserStatus.BANNED)
                .isOwner(isOwner)
                .isSelling(isSelling)
                .formattedCreatedAt(TimeUtil.formatRelative(product.getCreatedAt()))
                .build();
    }

    public ProductEditViewDto getProductEditView(Long id, User user) throws AccessDeniedException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("상품을 수정할 권한이 없습니다.");
        }

        List<Category> categories = categoryService.findAll();

        return new ProductEditViewDto(product, categories);
    }

    @Transactional
    public void markAsSold(Product product) {
        product.setStatus(Product.ProductStatus.SOLD);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDto> findProductSummaries(String sort, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        if ("likes".equals(sort)) {
            return productRepository.findSummariesByOrderByLikes(pageable);
        } else {
            return productRepository.findSummariesByOrderByLatest(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDto> findProductByCategorySummaries(String sort, Long categoryId, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        if ("likes".equals(sort)) {
            return productRepository.findSummariesByCategoryByOrderByLikes(categoryId, pageable);
        } else {
            return productRepository.findSummariesByCategoryByOrderByLatest(categoryId, pageable);
        }
    }

    public Page<ProductSummaryDto> findUserProductSummaries(User loginUser, Long sellerId, int page) {
        Pageable pageable = PageRequest.of(page, 8);

        if (loginUser != null && loginUser.getId().equals(sellerId)) {
            return productRepository.findSummaryBySellerForOwner(sellerId, pageable);
        } else {
            return productRepository.findSummaryBySellerForPublic(sellerId, pageable);
        }
    }

    public Page<ProductSummaryDto> searchByKeyword(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        return productRepository.findSummariesByKeyword(keyword, pageable);
    }

    public Long count() {
        return productRepository.count();
    }

    public Long countToday() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay(); // 오늘 00:00:00
        return productRepository.countTodayProducts(todayStart);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }

    public Page<AdminProductDto> findProductsForAdmin(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findAllByDeletedFalse(pageable);
        return productPage.map(AdminProductDto::new);
    }
}
