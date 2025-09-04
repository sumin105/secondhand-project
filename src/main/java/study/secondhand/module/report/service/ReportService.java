package study.secondhand.module.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.report.dto.ProductReportDto;
import study.secondhand.module.report.dto.UserReportDto;
import study.secondhand.module.report.entity.Report;
import study.secondhand.module.report.repository.ReportRepository;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public void userReport(User loginUser, UserReportDto dto) {
        User reportedUser = userService.findById(dto.getReportedUserId());
        if (reportedUser.isDeleted() || reportedUser.isAdmin() || reportedUser.isSystem()) {
            throw new IllegalArgumentException("신고할 수 없는 사용자입니다.");
        }
        // 중복 신고 검사
        if (reportRepository.existsByReporterAndReportedUser(loginUser, reportedUser)) {
            throw new IllegalArgumentException("이미 신고한 사용자입니다.");
        }

        // 신고 등록
        Report report = createAndSaveUserReport(loginUser, reportedUser, dto.getReason(), dto.getDescription());
        reportedUser.increaseReportCount();
    }

    @Transactional
    public void productReport(User loginUser, ProductReportDto dto) {
        Product reportedProduct = productService.findById(dto.getReportedProductId());
        if (reportedProduct.isDeleted()) {
            throw new IllegalArgumentException("신고 대상 상품이 존재하지 않습니다.");
        }
        if (reportRepository.existsByReporterAndReportedProduct(loginUser, reportedProduct)) {
            throw new IllegalArgumentException("이미 신고한 상품입니다.");
        }

        Report report = createAndSaveProductReport(loginUser, reportedProduct, dto.getReason(), dto.getDescription());
        reportedProduct.increaseReportCount();
    }

    public List<Report> findByProduct(Product product) {
        return reportRepository.findAllByReportedProduct(product);
    }

    public List<Report> findByUser(User user) {
        return reportRepository.findAllByReportedUser(user);
    }

    private Report createAndSaveUserReport(User loginUser, User reportedUser, String reason, String description) {
        Report report = Report.builder()
                .reporter(loginUser)
                .reportedUser(reportedUser)
                .reason(reason)
                .description(description)
                .type(Report.ReportType.USER)
                .build();
        return reportRepository.save(report);
    }

    private Report createAndSaveProductReport(User loginUser, Product reportedProduct, String reason, String description) {
        Report report = Report.builder()
                .reporter(loginUser)
                .reportedProduct(reportedProduct)
                .reason(reason)
                .description(description)
                .type(Report.ReportType.PRODUCT)
                .build();
        return reportRepository.save(report);
    }
}
