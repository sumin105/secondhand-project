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
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Setter
    @Column(nullable = false, length = 20)
    private String recipientName;

    @Setter
    @Column(nullable = false, length = 15)
    private String recipientPhone;

    @Setter
    @Column(length = 255)
    private String address;

    @Setter
    @Column(length = 255)
    private String detailAddress;

    @Setter
    @Column(length = 6)
    private String postCode;

    @Setter
    @Column(length = 30)
    private String storeName;

    @Setter
    @Column(length = 255)
    private String storeAddress;

    @Setter
    @Column(length = 255)
    private String requestMessage;

    @Setter
    @Column(length = 30)
    private String trackingNumber; // 운송장 번호

    @Setter
    @Column(length = 50)
    private String deliveryCompany; // 택배 회사 (ex. CJ, 한진, 편의점, 반값 등)

    @Setter
    @Column(length = 2048)
    private String trackingUrl;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.READY;

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
