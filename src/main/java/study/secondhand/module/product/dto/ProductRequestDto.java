package study.secondhand.module.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRequestDto {
    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "상품명은 2자 이상 30자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "설명은 필수 입력 값입니다.")
    @Size(min = 10, max = 2000, message = "설명은 10자 이상 2000자 이하로 입력해주세요.")
    private String description;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 500, message = "가격은 500원 이상이어야 합니다.")
    @Max(value = 100000000, message = "가격은 1억원 이하여야 합니다.")
    private int price;

    @NotBlank(message = "거래 방식은 필수 선택 값입니다.")
    private String dealMethod; // ex. DIRECT


    private String status; // ex. ON_SALE

    private MultipartFile[] image; // 단일 이미지 업로드용

    @Max(value = 30000, message = "배송비는 30,000원을 초과할 수 없습니다.")
    private Integer normalDeliveryFee;
    @Max(value = 30000, message = "배송비는 30,000원을 초과할 수 없습니다.")
    private Integer cheapDeliveryFee;

    @NotNull(message = "카테고리는 필수 선택 값입니다.")
    private Long categoryId;
}
