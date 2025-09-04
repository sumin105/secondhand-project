package study.secondhand.module.product.dto;

import lombok.Getter;
import study.secondhand.global.util.TimeUtil;
import study.secondhand.module.product.entity.Product;

import java.time.LocalDateTime;

@Getter
public class ProductSummaryDto {
    private final Long id;
    private final String title;
    private final int price;
    private final String thumbnailUrl;
    private final Product.ProductStatus status;
    private final String createdAt;

    public ProductSummaryDto(Long id, String title, int price, String thumbnailUrl,
                             Product.ProductStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.createdAt = TimeUtil.formatRelative(createdAt);
    }
}
