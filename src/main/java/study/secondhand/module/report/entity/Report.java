package study.secondhand.module.report.entity;

import jakarta.persistence.*;
import lombok.*;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // 신고자

    @ManyToOne
    @JoinColumn(name = "reported_product_id")
    private Product reportedProduct;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Column(nullable = false)
    private String reason; // 신고 사유

    private String description; // 상세 설명

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ReportType {
        PRODUCT,
        USER
    }
}
