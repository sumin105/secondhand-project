package study.secondhand.module.user.dto;

import lombok.Getter;
import study.secondhand.module.user.entity.User;

@Getter
public class ShopHeaderDto {
    private final Long shopOwnerId;
    private final String shopOwnerNickname;
    private final String shopOwnerIntro;
    private final boolean isShopOwnerBanned;

    private final int soldCount;
    private final String activeTab;

    private final boolean isLoggedIn;
    private final boolean isMyStore;

    private final Long loginUserId;
    private final String loginUserNickname;
    private final String loginUserIntro;

    public ShopHeaderDto(User shopOwner, User loginUser, int soldCount, String activeTab) {
        this.shopOwnerId = shopOwner.getId();
        this.shopOwnerNickname = shopOwner.getNickname() != null ? shopOwner.getNickname() : "상점 " + shopOwner.getId() + "호";
        this.shopOwnerIntro = shopOwner.getIntro() != null ? shopOwner.getIntro() : "";
        this.isShopOwnerBanned = shopOwner.isBanned();

        this.soldCount = soldCount;
        this.activeTab = activeTab;

        this.isLoggedIn = (loginUser != null);
        this.isMyStore = (loginUser != null) && loginUser.getId().equals(shopOwner.getId());

        this.loginUserId = (loginUser != null) ? loginUser.getId() : null;
        this.loginUserNickname = (loginUser != null) ? loginUser.getNickname() : null;
        this.loginUserIntro = (loginUser != null) ? loginUser.getIntro() : null;
    }
}
