package study.secondhand.module.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import study.secondhand.module.payment.entity.Payment;

@Entity
@Getter @Setter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Setter
    private String deliveryCompany; // 택배 회사 (ex. CJ, 한진, 편의점, 반값 등)

    @Setter
    private String trackingNumber; // 운송장 번호

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.READY;

    @Setter
    private String trackingUrl;

    @Setter
    @Column(nullable = false)
    private String recipientName;

    @Setter
    @Column(nullable = false)
    private String recipientPhone;

    @Setter
    private String address;

    @Setter
    private String detailAddress;

    @Setter
    private String postCode;

    @Setter
    private String storeName;

    @Setter
    private String storeAddress;

    @Setter
    private String requestMessage;

    public enum DeliveryStatus {
        READY("배송 준비 중"),
        SHIPPING("배송 중"),
        DELIVERED("배송 완료");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
