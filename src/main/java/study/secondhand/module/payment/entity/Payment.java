package study.secondhand.module.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paymentId; // 포트원 결제 고유 ID

    @Column(nullable = false)
    private int amount; // 상품 금액

    private Integer deliveryFee; // 배송비

    @Column(nullable = false)
    private int finalAmount; // 최종 금액

    @Column(nullable = false)
    private String payMethod; // 결제 수단(토스, 카카오페이, 카드)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod deliveryMethod;

    private LocalDateTime paidAt; // 결제 완료 시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 구매한 유저

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 결제 요청 시각

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt; // 업데이트 시각


    @Getter
    public enum Status {
        PAID("결제 완료"),
        CANCELLED("결제 취소"),
        FAILED("결제 실패"),
        PARTIAL_CANCELLED("부분 취소"),
        PAY_PENDING("결제 완료 대기"),
        READY("준비"),
        VIRTUAL_ACCOUNT_ISSUED("가상계좌 발급 완료")
        ;

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
    }

    @Getter
    public enum DeliveryMethod {
        NORMAL("일반택배"),
        CHEAP("반값・알뜰택배"),
        DIRECT("직거래");

        private final String displayName;
        DeliveryMethod(String displayName) {
            this.displayName = displayName;
        }
        public String toString() {
            return displayName;
        }
    }
}
