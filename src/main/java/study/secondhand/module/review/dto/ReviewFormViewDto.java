package study.secondhand.module.review.dto;

import lombok.Getter;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.review.ReviewTag;
import study.secondhand.module.user.entity.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class ReviewFormViewDto {
    private final Long orderId;
    private final String productTitle;
    private final String formattedCompleteAt;
    private final String formattedAmount;
    private final String deliveryMethod;

    private final long partnerId;
    private final String partnerRole;
    private final String partnerNickname;

    private final List<ReviewTag> tags;

    public ReviewFormViewDto(Order order, User loginUser) {
        this.orderId = order.getId();
        this.productTitle = order.getProduct().getTitle();
        this.formattedCompleteAt = order.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.formattedAmount = String.format("%,d원", order.getPayment().getFinalAmount());
        this.deliveryMethod = order.getPayment().getDeliveryMethod().toString();

        boolean isCurrentUserBuyer = order.getBuyer().getId().equals(loginUser.getId());
        User partner = isCurrentUserBuyer ? order.getSeller() : order.getBuyer();

        this.partnerId = partner.getId();
        this.partnerRole = isCurrentUserBuyer ? "판매자" : "구매자";
        this.partnerNickname = partner.getNickname() != null ? partner.getNickname() : "상점 " + partner.getId() + "호";

        this.tags = ReviewTag.getTagsForRole(isCurrentUserBuyer);
    }
}
