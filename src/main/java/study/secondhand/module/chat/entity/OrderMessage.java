package study.secondhand.module.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.product.entity.Product;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false, unique = true)
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(length = 2048)
    private String trackingUrl; // 배송조회 URL

    public String getImageUrl() {
        return order != null ? order.getProductThumbnailUrlSnapshot() : null;
    }

    public String resolveButtonText(Long loginUserId) {
        if (order == null || order.getBuyer() == null) return "";

        boolean isBuyer = order.getBuyer().getId().equals(loginUserId);
        boolean isDirectTransaction = order.getDelivery() == null;

        switch (orderStatus) {
            case PAID:
                if (isDirectTransaction) {
                    return isBuyer ? "거래 확정하기" : "거래 정보 확인";
                } else {
                    return isBuyer ? "거래 정보 확인" : "운송장 등록하기";
                }
            case SHIPPED:
                return isBuyer ? "택배 수령 완료" : "거래 정보 확인";
            case DELIVERED:
                return isBuyer ? "거래 확정하기" : "거래 정보 확인";
            case DONE:
                return "후기 작성하기";
            default:
                return "";
        }
    }
}
