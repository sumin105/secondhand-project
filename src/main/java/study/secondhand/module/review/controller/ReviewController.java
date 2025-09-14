package study.secondhand.module.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.review.dto.ReviewFormViewDto;
import study.secondhand.module.review.entity.Review;
import study.secondhand.module.review.dto.ReviewRequestDto;
import study.secondhand.module.review.service.ReviewService;

import java.nio.file.AccessDeniedException;


@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders/{id}/reviews/new")
    public String reviewForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("id") Long orderId,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            ReviewFormViewDto viewData = reviewService.getReviewFormData(orderId, userDetails.getUser());
            model.addAttribute("review", viewData);
            return "review/review-form";
        } catch (AccessDeniedException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/orders/{id}/reviews")
    public String createReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long orderId,
                               @Valid @ModelAttribute ReviewRequestDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/orders/" + orderId + "/reviews/new";
        }

        try {
            Review review = reviewService.createReviewAndSend(orderId, dto, userDetails.getUser());
            Long targetId = review.getTarget().getId();
            redirectAttributes.addFlashAttribute("successMessage", "후기가 등록되었습니다.");
            return "redirect:/shop/" + targetId + "/reviews";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orders/" + orderId + "/reviews/new";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/reviews/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteReview(@PathVariable("id") Long reviewId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            reviewService.deleteReview(reviewId, userDetails.getUser().getId());
            // 204 No Content
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
