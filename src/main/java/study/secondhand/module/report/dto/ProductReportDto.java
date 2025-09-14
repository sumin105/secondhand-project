package study.secondhand.module.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductReportDto {
    @NotNull(message = "신고할 상품 ID는 필수입니다.")
    private Long reportedProductId;
    @NotBlank(message = "신고 사유는 필수 선택 항목입니다.")
    @Size(max = 100)
    private String reason;
    @Size(max = 500, message = "상세 설명은 500자를 초과할 수 없습니다.")
    private String description;
}
