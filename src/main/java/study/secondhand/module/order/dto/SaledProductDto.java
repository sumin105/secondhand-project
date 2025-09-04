package study.secondhand.module.order.dto;

import lombok.Getter;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter

public class SaledProductDto {
    private final Long orderId;
    private final String title;
    private final int amount;
    private final OrderStatus status;
    private final LocalDateTime updatedAt;
    private final Long buyerId;
    private final String buyerNickname;
    private final Payment.DeliveryMethod deliveryMethod;
    private final String imageUrl;

    public SaledProductDto(Long orderId, String title, int amount, OrderStatus status,
                           LocalDateTime updatedAt, Long buyerId, String buyerNickname,
                           Payment.DeliveryMethod deliveryMethod, String imageUrl) {
        this.orderId = orderId;
        this.title = title;
        this.amount = amount;
        this.status = status;
        this.updatedAt = updatedAt;
        this.buyerId = buyerId;
        this.buyerNickname = buyerNickname != null ? buyerNickname : "상점 " + buyerId + "호";
        this.deliveryMethod = deliveryMethod;
        this.imageUrl = imageUrl;
    }
}
