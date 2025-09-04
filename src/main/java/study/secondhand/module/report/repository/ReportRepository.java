package study.secondhand.module.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.report.entity.Report;
import study.secondhand.module.user.entity.User;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndReportedUser(User reporter, User reportedUser);

    boolean existsByReporterAndReportedProduct(User reporter, Product reportedProduct);

    List<Report> findAllByReportedProduct(Product reportedProduct);

    List<Report> findAllByReportedUser(User reportedUser);
}
