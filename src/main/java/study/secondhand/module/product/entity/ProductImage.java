package study.secondhand.module.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setProduct(Product product) {
        if (this.product != null) {
            this.product.getImages().remove(this);
        }
        this.product = product;
        if (!product.getImages().contains(this)) {
            product.getImages().add(this);
        }
    }
}
