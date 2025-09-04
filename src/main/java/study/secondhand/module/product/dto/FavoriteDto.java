package study.secondhand.module.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class FavoriteDto {

    private Long favoriteId;
    private Long productId;
    private String productTitle;
    private int productPrice;
    private String productStatus;
    private LocalDateTime productCreatedAt;

    private List<ProductImageDto> images;
}
