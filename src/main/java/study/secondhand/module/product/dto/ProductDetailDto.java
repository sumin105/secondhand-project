package study.secondhand.module.product.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import study.secondhand.module.user.dto.UserSummaryDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Builder
public class ProductDetailDto {
    private Long id;
    private String title;
    private String description;
    private int price;
    private String status;
    private String dealMethod;
    private LocalDateTime createdAt;
    private boolean favorite; // 로그인한 유저의 찜 여부
    private Long sellerId;
    private String nickname;
    private int favoriteCount; // 찜 개수 추가

    private Integer normalDeliveryFee;
    private Integer cheapDeliveryFee;

    private Long categoryId;
    private String categoryName;

    private List<ProductImageDto> images;
    private List<UserSummaryDto> wishlistUsers; // 판매자 본인이면 응답

    private boolean deleted;
    private boolean sellerBanned;

    // 추가
    private final boolean isOwner;
    private final boolean isSelling;
    private final String formattedCreatedAt;

    public String getProductJson() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", this.id);
        dataMap.put("title", this.title);
        dataMap.put("price", this.price);
        dataMap.put("dealMethod", this.dealMethod);
        dataMap.put("normalDeliveryFee", this.normalDeliveryFee);
        dataMap.put("cheapDeliveryFee", this.cheapDeliveryFee);
        dataMap.put("description", this.description);
        dataMap.put("status", this.status);
        dataMap.put("createdAt", this.formattedCreatedAt);
        dataMap.put("favorite", this.favorite);
        dataMap.put("sellerId", this.sellerId);
        dataMap.put("nickname", this.nickname);
        dataMap.put("favoriteCount", this.favoriteCount);
        dataMap.put("categoryId", this.categoryId);
        dataMap.put("categoryName", this.categoryName);
        dataMap.put("wishlistUsers", this.wishlistUsers);

        if (this.images != null) {
            dataMap.put("imageUrls", this.images.stream().map(ProductImageDto::getImageUrl).toList());
        } else {
            dataMap.put("imageUrls", List.of());
        }

        try {
            String jsonString = mapper.writeValueAsString(dataMap);
            log.info("Generated product json: {}", jsonString);
            return jsonString;
        } catch (JsonProcessingException e) {
            log.error("Failed to generate product json", e);
            return "{}"; // 오류 발생 시 빈 JSON 객체 반환
        }
    }
}
