package study.secondhand.module.product.dto;

import lombok.Getter;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.product.entity.Product;

import java.util.List;

@Getter
public class ProductEditViewDto {
    private final Long id;
    private final String title;
    private final int price;
    private final String description;
    private final Product.DealMethod dealMethod;
    private final Product.ProductStatus status;

    private final Integer normalDeliveryFee;
    private final Integer cheapDeliveryFee;

    private final Long categoryId;
    private final String categoryName;

    private final List<ProductImageEditDto> images;
    private final List<MainViewDto.CategoryDto> categories;

    public ProductEditViewDto(Product product, List<Category> categories) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.dealMethod = product.getDealMethod();
        this.status = product.getStatus();
        this.normalDeliveryFee = product.getNormalDeliveryFee();
        this.cheapDeliveryFee = product.getCheapDeliveryFee();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();
        this.images = product.getImages().stream()
                .map(ProductImageEditDto::new)
                .toList();
        this.categories = categories.stream()
                .map(MainViewDto.CategoryDto::new)
                .toList();
    }
}
