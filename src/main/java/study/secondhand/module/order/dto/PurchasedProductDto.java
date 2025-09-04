package study.secondhand.module.order.dto;

import lombok.Getter;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter
public class PurchasedProductDto { // 구매된 상품
    private final Long orderId;
    private final String title;
    private final int amount;
    private final OrderStatus status;
    private final LocalDateTime updatedAt;
    private final Long sellerId;
    private final String sellerNickname;
    private final Payment.DeliveryMethod deliveryMethod;
    private final String imageUrl;

    public PurchasedProductDto(Long orderId, String title, int amount, OrderStatus status,
                               LocalDateTime updatedAt, Long sellerId, String sellerNickname,
                               Payment.DeliveryMethod deliveryMethod, String imageUrl) {
        this.orderId = orderId;
        this.title = title;
        this.amount = amount;
        this.status = status;
        this.updatedAt = updatedAt;
        this.sellerId = sellerId;
        this.sellerNickname = sellerNickname != null ? sellerNickname : "상점 " + sellerId + "호";
        this.deliveryMethod = deliveryMethod;
        this.imageUrl = imageUrl;
    }
}
