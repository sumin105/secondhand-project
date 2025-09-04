package study.secondhand.module.product.dto;

import lombok.Getter;
import study.secondhand.module.product.entity.ProductImage;

@Getter
public class ProductImageEditDto {
    private final Long id;
    private final String imageUrl;

    public ProductImageEditDto(ProductImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }
}
