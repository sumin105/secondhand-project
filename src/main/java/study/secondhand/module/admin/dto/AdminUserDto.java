package study.secondhand.module.admin.dto;

import lombok.Getter;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;

@Getter
public class AdminUserDto {
    private final long id;
    private final String nickname;
    private final User.Role role;
    private final LocalDateTime createdAt;
    private final int reportCount;
    private final User.UserStatus status;

    public AdminUserDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname() != null ? user.getNickname() : "상점 " + user.getId() + "호";
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.reportCount = user.getReportCount();
        this.status = user.getStatus();
    }
}
