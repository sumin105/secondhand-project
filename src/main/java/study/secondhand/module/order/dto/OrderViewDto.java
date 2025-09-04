package study.secondhand.module.order.dto;

import lombok.Getter;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.order.entity.Delivery;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.payment.entity.Payment;
import study.secondhand.module.review.entity.Review;
import study.secondhand.module.user.entity.User;

import java.time.format.DateTimeFormatter;

@Getter
public class OrderViewDto {
    // 최상위 정보
    private final Long orderId;
    private final String formattedCreatedAt;
    private final String orderStatus;

    // 중첩 dto
    private final ProductInfo product;
    private final PaymentInfo payment;
    private final DeliveryInfo delivery; // null 가능
    private final SellerInfo seller;
    private final BuyerInfo buyer;

    // UI 렌더링을 위한 플래그
    private final boolean showWriteReviewButton;
    private final boolean showViewMyReviewButton;
    private final boolean showViewOtherReviewButton;

    // 구매자용
    private final boolean showTrackingDeliveryButton;
    private final boolean showCompleteDeliveryButton;
    private final boolean showConfirmOrderButton;

    // 판매자용
    private final boolean showDeliveryInfoSection;
    private final boolean showTrackingInputForm;
    private final boolean isNormalDeliveryInput; // 일반/반택 입력폼 구분
    private final boolean showEnteredTrackingInfo;

    public OrderViewDto(Order order, Review myReview, Review otherReview, User loginUser) {
        this.orderId = order.getId();
        this.formattedCreatedAt = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        this.orderStatus = order.getStatus().toString();

        this.product = new ProductInfo(order);
        this.payment = new PaymentInfo(order.getPayment());
        this.delivery = order.getDelivery() != null ? new DeliveryInfo(order.getDelivery()) : null;
        this.seller = new SellerInfo(order.getSeller());
        this.buyer = new BuyerInfo(order.getBuyer());

        this.showWriteReviewButton = order.getStatus() == OrderStatus.DONE && myReview == null;
        this.showViewMyReviewButton = myReview != null;
        this.showViewOtherReviewButton = otherReview != null;

        // 버튼 렌더링 여부 결정하는 플래그 로직
        boolean isDeliveryOrder = order.getDelivery() != null;
        if (isDeliveryOrder) {
            Delivery.DeliveryStatus deliveryStatus = order.getDelivery().getDeliveryStatus();
            this.showDeliveryInfoSection = deliveryStatus != Delivery.DeliveryStatus.DELIVERED;
            this.showTrackingInputForm = deliveryStatus == Delivery.DeliveryStatus.READY;
            this.isNormalDeliveryInput = order.getDelivery().getAddress() != null;
            this.showEnteredTrackingInfo = deliveryStatus != Delivery.DeliveryStatus.READY;
        } else {
            this.showDeliveryInfoSection = false;
            this.showTrackingInputForm = false;
            this.isNormalDeliveryInput = false;
            this.showEnteredTrackingInfo = false;
        }
        boolean hasTrackingNumber = isDeliveryOrder && order.getDelivery().getTrackingNumber() != null && !order.getDelivery().getTrackingNumber().isEmpty();

        this.showTrackingDeliveryButton = hasTrackingNumber;
        this.showCompleteDeliveryButton = hasTrackingNumber && order.getStatus() == OrderStatus.SHIPPED;
        this.showConfirmOrderButton = order.getStatus() == OrderStatus.DELIVERED || (!isDeliveryOrder && order.getStatus() == OrderStatus.PAID);
    }

    @Getter
    public static class ProductInfo {
        private final Long id;
        private final String thumbnailUrl;
        private final String title;
        private final int price;

        ProductInfo(Order order) {
            this.id = order.getProduct().getId();
            this.thumbnailUrl = order.getProductThumbnailUrlSnapshot();
            this.title = order.getProductTitleSnapshot();
            this.price = order.getPayment().getAmount();
        }
    }

    @Getter
    public static class PaymentInfo {
        private final int amount;
        private final Integer deliveryFee;
        private final int finalAmount;
        private final String payMethod;
        private final String deliveryMethod;

        PaymentInfo(Payment payment) {
            this.amount = payment.getAmount();
            this.deliveryFee = payment.getDeliveryFee();
            this.finalAmount = payment.getFinalAmount();
            this.payMethod = payment.getPayMethod();
            this.deliveryMethod = payment.getDeliveryMethod() == Payment.DeliveryMethod.DIRECT ? "직거래" : (payment.getDeliveryMethod() == Payment.DeliveryMethod.NORMAL ? "일반택배" : "반값•알뜰택배");
        }
    }

    @Getter
    public static class DeliveryInfo {
        private final String recipientName;
        private final String recipientPhone;
        private final String postCode;
        private final String address;
        private final String detailAddress;
        private final String storeName;
        private final String storeAddress;
        private final String requestMessage;
        private final String deliveryStatus;
        private final String deliveryCompany;
        private final String trackingNumber;
        private final String trackingUrl;

        DeliveryInfo(Delivery delivery) {
            this.recipientName = delivery.getRecipientName();
            this.recipientPhone = delivery.getRecipientPhone();
            this.postCode = delivery.getPostCode();
            this.address = delivery.getAddress();
            this.detailAddress = delivery.getDetailAddress();
            this.storeName = delivery.getStoreName();
            this.storeAddress = delivery.getStoreAddress();
            this.requestMessage = delivery.getRequestMessage();
            this.deliveryStatus = delivery.getDeliveryStatus().toString();
            this.deliveryCompany = delivery.getDeliveryCompany();
            this.trackingNumber = delivery.getTrackingNumber();
            this.trackingUrl = delivery.getTrackingUrl();
        }
    }

    @Getter
    public static class SellerInfo {
        private final Long id;
        private final String nickname;

        SellerInfo(User seller) {
            this.id = seller.getId();
            this.nickname = seller.getNickname() != null ? seller.getNickname() : "상점 " + seller.getId() + "호";
        }
    }

    @Getter
    public static class BuyerInfo {
        private final Long id;
        private final String nickname;

        BuyerInfo(User buyer) {
            this.id = buyer.getId();
            this.nickname = buyer.getNickname() != null ? buyer.getNickname() : "상점 " + buyer.getId() + "호";
        }
    }
}
