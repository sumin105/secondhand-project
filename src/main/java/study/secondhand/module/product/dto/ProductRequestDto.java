package study.secondhand.module.product.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRequestDto {
    private String title;
    private String description;
    private int price;
    private String dealMethod; // ex. DIRECT
    private String status; // ex. ON_SALE

    private MultipartFile[] image; // 단일 이미지 업로드용

    private Integer normalDeliveryFee;
    private Integer cheapDeliveryFee;

    private Long categoryId;
}
