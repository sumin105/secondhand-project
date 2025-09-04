package study.secondhand.module.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductReportDto {
    private Long reportedProductId;
    private String reason;
    private String description;
}
