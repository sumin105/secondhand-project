package study.secondhand.module.admin.dto;

import lombok.Getter;
import study.secondhand.module.report.entity.Report;

import java.time.LocalDateTime;

@Getter
public class ReportDto {
    private final Long reporterId;
    private final String reporterNickname;
    private final String reason;
    private final String description;
    private final LocalDateTime createdAt;

    public ReportDto(Report report) {
        this.reporterId = report.getReporter().getId();
        this.reporterNickname = report.getReporter().getNickname() != null ? report.getReporter().getNickname() : "상점 " + report.getReporter().getId() + "호";
        this.reason = report.getReason();
        this.description = report.getDescription() != null && !report.getDescription().isBlank() ? report.getDescription() : "";
        this.createdAt = report.getCreatedAt();
    }
}
