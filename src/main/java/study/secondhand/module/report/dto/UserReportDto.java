package study.secondhand.module.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReportDto {
    private Long reportedUserId;
    private String reason;
    private String description;
}
