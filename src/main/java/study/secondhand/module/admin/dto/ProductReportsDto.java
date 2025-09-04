package study.secondhand.module.admin.dto;

import lombok.Getter;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.report.entity.Report;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductReportsDto {
    private final Long productId;
    private final String productTitle;
    private final List<ReportDto> reports;

    public ProductReportsDto(Product product, List<Report> reports) {
        this.productId = product.getId();
        this.productTitle = product.getTitle();
        this.reports = reports.stream()
                .map(ReportDto::new)
                .collect(Collectors.toList());
    }
}
