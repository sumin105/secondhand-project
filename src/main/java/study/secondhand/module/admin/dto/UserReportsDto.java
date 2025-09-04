package study.secondhand.module.admin.dto;

import lombok.Getter;
import study.secondhand.module.report.entity.Report;
import study.secondhand.module.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserReportsDto {
    private final Long userId;
    private final String userNickname;
    private final List<ReportDto> reports;

    public UserReportsDto(User user, List<Report> reports) {
        this.userId = user.getId();
        this.userNickname = user.getNickname() != null ? user.getNickname() : "상점 " + user.getId() + "호";
        this.reports = reports.stream()
                .map(ReportDto::new)
                .collect(Collectors.toList());
    }
}
