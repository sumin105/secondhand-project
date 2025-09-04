package study.secondhand.module.user.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import study.secondhand.module.product.dto.ProductSummaryDto;
import study.secondhand.module.user.entity.User;

@Getter
public class ShopViewDto {

    private final ShopOwnerInfo shopOwner;
    private final Page<ProductSummaryDto> products;

    public ShopViewDto(User shopOwner, Page<ProductSummaryDto> products) {
        this.shopOwner = new ShopOwnerInfo(shopOwner);
        this.products = products;

    }

    @Getter
    public static class ShopOwnerInfo {
        private final Long id;
        private final String nickname;

        public ShopOwnerInfo(User user) {
            this.id = user.getId();
            this.nickname = user.getNickname() != null ? user.getNickname() : "상점 " + user.getId() + "호";
        }
    }
}
