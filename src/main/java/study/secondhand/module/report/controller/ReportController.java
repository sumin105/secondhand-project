package study.secondhand.module.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.report.dto.ProductReportDto;
import study.secondhand.module.report.dto.UserReportDto;
import study.secondhand.module.report.service.ReportService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user")
    public String reportUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute UserReportDto dto,
                             RedirectAttributes redirectAttributes) {
        try {
            reportService.userReport(userDetails.getUser(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "신고가 접수되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shop/" + dto.getReportedUserId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/product")
    public String reportProduct(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @ModelAttribute ProductReportDto dto,
                                RedirectAttributes redirectAttributes) {
        try {
            reportService.productReport(userDetails.getUser(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "신고가 접수되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/" + dto.getReportedProductId();
    }
}
