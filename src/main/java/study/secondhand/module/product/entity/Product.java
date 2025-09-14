package study.secondhand.module.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import study.secondhand.module.product.dto.ProductRequestDto;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    @Size(min = 2, max = 30, message = "상품명은 2자 이상 30자 이하여야 합니다.")
    private String title; // 상품명

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(min = 10, max = 2000, message = "설명은 10자 이상 2000자 이내여야 합니다.")
    private String description; // 상품설명

    @Column(nullable = false)
    @Min(value = 500, message = "가격은 500원 이상이어야 합니다.")
    @Max(value = 100000000, message = "가격은 1억 원 이하여야 합니다.")
    private int price; // 가격

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Setter
    @Column(nullable = false)
    private boolean deleted = false; // 결제나 채팅에 연관된 상품이면 true, 숨김 처리

    @Column(nullable = false)
    private int reportCount = 0;

    @Column(nullable = false)
    private int favoriteCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 시각

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 수정 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealMethod dealMethod; // 거래 방식 (직거래 / 택배)

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status; // 판매 상태 (판매중 / 예약중 / 판매완료)

    // 엔테티 연관관계 필드에는 @Column 사용 x
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false) // 외래 키 컬럼 지정
    private User seller; // 판매자 (user와 연관관계)

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false, length = 2048)
    private String thumbnailImageUrl;

    @Max(value = 30000, message = "일반택배비는 30,000원을 초과할 수 없습니다.")
    private Integer normalDeliveryFee; // 일반 택배 배송비

    @Max(value = 30000, message = "반값택배비는 30,000원을 초과할 수 없습니다.")
    private Integer cheapDeliveryFee; // 반값 택배 배송비

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Product(ProductRequestDto dto, User seller, Category category) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.price = dto.getPrice();
        this.dealMethod = DealMethod.valueOf(dto.getDealMethod());
        this.status = ProductStatus.SELLING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.seller = seller;
        this.category = category;

        this.thumbnailImageUrl = "/images/default.jpg";

        // 택배거래 선택시 배송비 저장
        if (this.dealMethod == DealMethod.DELIVERY) {
            this.normalDeliveryFee = dto.getNormalDeliveryFee();
            this.cheapDeliveryFee = dto.getCheapDeliveryFee();
        } else { // 직거래 선택시 배송비 저장 x
            this.normalDeliveryFee = null;
            this.cheapDeliveryFee = null;
        }
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
        images.forEach(image -> image.setProduct(this));
    }

    public void updateThumbnailUrl() {
        if (this.images != null && !this.images.isEmpty()) {
            this.thumbnailImageUrl = this.images.get(0).getImageUrl();
        } else {
            this.thumbnailImageUrl = "/images/default.jpg";
        }
    }

    // 썸네일 이미지 로직 사용시 삭제 가능
    public String getThumbnailImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return "/images/default.jpg"; // 썸네일 이미지 없을 경우 기본 이미지 경로 설정해줘야함
    }

    public void update(ProductRequestDto dto, Category category) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.price = dto.getPrice();
        this.dealMethod = DealMethod.valueOf(dto.getDealMethod());
        this.category = category;
        if (dto.getStatus() != null) {
            this.status = ProductStatus.valueOf(dto.getStatus());
        }
        // 택배거래 선택시 배송비 저장
        if (this.dealMethod == DealMethod.DELIVERY) {
            this.normalDeliveryFee = dto.getNormalDeliveryFee();
            this.cheapDeliveryFee = dto.getCheapDeliveryFee();
        } else { // 직거래 선택시 배송비 저장 x
            this.normalDeliveryFee = null;
            this.cheapDeliveryFee = null;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    public void decreaseFavoriteCount() {
        this.favoriteCount = Math.max(0, this.favoriteCount - 1);
    }

    public enum DealMethod {
        DIRECT("직거래"),
        DELIVERY("택배거래");

        private final String displayName;

        DealMethod(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum ProductStatus {
        SELLING("판매중"),
        RESERVED("예약중"),
        SOLD("판매완료");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
