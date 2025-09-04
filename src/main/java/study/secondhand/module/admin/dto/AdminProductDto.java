package study.secondhand.module.admin.dto;

import lombok.Getter;
import study.secondhand.module.product.entity.Product;

import java.time.LocalDateTime;

@Getter
public class AdminProductDto {
    private final Long id;
    private final String title;
    private final Long sellerId;
    private final String sellerNickname;
    private final LocalDateTime createdAt;
    private final Product.ProductStatus status;
    private final int reportCount;

    public AdminProductDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.sellerId = product.getSeller().getId();
        this.sellerNickname = product.getSeller().getNickname() != null ? product.getSeller().getNickname() : "상점 " + product.getSeller().getId() + "호";
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
        this.reportCount = product.getReportCount();
    }
}
