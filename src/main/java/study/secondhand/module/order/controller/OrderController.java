package study.secondhand.module.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.order.dto.DeliveryFormDto;
import study.secondhand.module.order.dto.OrderViewDto;
import study.secondhand.module.order.service.OrderProcessService;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.user.entity.User;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderProcessService orderProcessService;

    // 주문서 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public String viewPurchase(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long id,
                               Model model, RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        OrderViewDto dto = orderService.getOrderView(id, user);
        model.addAttribute("viewData", dto);

        if (dto.getSeller().getId().equals(user.getId())) {
            return "order/order-seller";
        } else {
            return "order/order-buyer";
        }
    }

    // 운송장 번호 등록
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/delivery")
    public String submitDeliveryForm(@ModelAttribute DeliveryFormDto dto,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("id") Long orderId,
                                     RedirectAttributes redirectAttributes) throws AccessDeniedException {
        User user = userDetails.getUser();
        orderProcessService.shipOrder(orderId, dto, user);
        redirectAttributes.addFlashAttribute("successMessage", "운송장 정보가 등록되었습니다.");
        return "redirect:/orders/" + orderId;
    }

    // 주문 상태 변경 (배송완료, 거래완료)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/status")
    public String updateOrderStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable("id") Long orderId,
                                    @RequestParam("action") String action,
                                    RedirectAttributes redirectAttributes) throws AccessDeniedException {
        User user = userDetails.getUser();

        if ("completeDelivery".equals(action)) { // 배송 완료
            orderProcessService.completeDeliveryProcess(orderId, user);
            redirectAttributes.addFlashAttribute("successMessage", "배송이 완료되었습니다.");
        } else if ("completeOrder".equals(action)) { // 거래 완료
            orderProcessService.completeOrderProcess(orderId, user);
            redirectAttributes.addFlashAttribute("successMessage", "거래가 완료되었습니다.");
        }

        return "redirect:/orders/" + orderId;
    }
}
