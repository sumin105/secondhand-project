package study.secondhand.module.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.payment.dto.PortOnePaymentRequest;
import study.secondhand.module.payment.service.PaymentService;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> completePayment(@RequestBody PortOnePaymentRequest request,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println("request: " + request);
        try {
            Long orderId = paymentService.verifyAndSavePayment(request, userDetails.getUser());
            // 생성 성공 시 201 Created 상태 코드랑 주문서 URI를 반환
            URI location = URI.create("/orders/" + orderId);
            return ResponseEntity.created(location).body(orderId);
        } catch (IllegalArgumentException e) {
            // 결제 검증 실패 등 예상된 오류는 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 서버 오류는 500
            log.error("Payment processing failed unexpectedly", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류가 발생했습니다.");
        }
    }
}
