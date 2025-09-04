package study.secondhand.module.user.dto;

import lombok.Getter;
import study.secondhand.module.user.entity.User;

@Getter
public class StoreSummaryDto {
    private final Long id;
    private final String nickname;
    private final User.UserStatus status;
    private final long productCount;
    private final double averageRating;

    public StoreSummaryDto(Long userId, String nickname, User.UserStatus status, long productCount, double averageRating) {
        this.id = userId;
        this.nickname = nickname != null ? nickname : "상점 " + userId + "호";
        this.status = status;
        this.productCount = productCount;
        this.averageRating = (averageRating != 0) ? averageRating : 0.0;
    }
}
