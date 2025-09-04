package study.secondhand.module.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.chat.service.SystemMessageService;
import study.secondhand.module.order.service.DeliveryService;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.payment.dto.PortOnePaymentRequest;
import study.secondhand.module.order.entity.Delivery;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.payment.entity.Payment;
import study.secondhand.module.payment.repository.PaymentRepository;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PortOneApiService portOneApiService;
    private final DeliveryService deliveryService;
    private final OrderService orderService;
    private final SystemMessageService systemMessageService;
    private final ProductService productService;

    public PaymentService(PaymentRepository paymentRepository, PortOneApiService portOneApiService, DeliveryService deliveryService, OrderService orderService, SystemMessageService systemMessageService, @Lazy ProductService productService) {
        this.paymentRepository = paymentRepository;
        this.portOneApiService = portOneApiService;
        this.deliveryService = deliveryService;
        this.orderService = orderService;
        this.systemMessageService = systemMessageService;
        this.productService = productService;
    }

    @Transactional
    public Long verifyAndSavePayment(PortOnePaymentRequest request, User user) {
        // 1. PortOne API를 직접 호출하여 실제 결제 내역과 상태를 확인
        JsonNode info = validatePaymentFromPortOne(request);

        // 2. 상품 상태 검증 (판매/존재 여부, 정지된 판매자의 상품은 구매 x)
        Product product = validateProductAndSeller(request.getProductId());

        // 3. 결제 금액 검증 (서버에서 직접 계산한 금액과 실제 결제 금액 비교)
        int expectedAmount = validatePaymentAmount(product, request, info);

        // 4. 중복 결제 검증
        validateDuplicatePayment(request.getPaymentId());

        // 5. 배송 타입 필수 정보 검사
        validateRequestDetails(request);

        // 6. 결제 시간 변환
        String paidAtStr = info.get("paidAt").asText();
        LocalDateTime paidAt = parsePaidAt(paidAtStr);

        // 7. 결제 정보 저장
        Payment payment = createPayment(info, request, product, user, expectedAmount, paidAt);
        paymentRepository.save(payment);

        // 8. 상품 상태 변경
        productService.markAsSold(product);

        // 9. 택배거래시 배송 정보 저장 및 유저 배송지 업데이트
        Delivery delivery = null;
        if (!"direct".equals(request.getDeliveryType())) {
            delivery = deliveryService.createAndSaveDelivery(request, payment, user);
        }

        // 10. 주문 저장, 유저 배송 정보 업데이트, 메시지 전송
        Order order = orderService.createOrder(payment, product, user, delivery);
        systemMessageService.sendOrderMessage(order, product);

        return order.getId();
    }

    private JsonNode validatePaymentFromPortOne(PortOnePaymentRequest request) {
        // 1. v2 API로 결제 정보 조회
        JsonNode info = portOneApiService.getPaymentInfo(request.getPaymentId());

        if (info == null || info.isNull() || !info.hasNonNull("status") || !info.has("amount") || !info.get("amount").hasNonNull("total")) {
            throw new IllegalStateException("PortOne 결제 정보가 유효하지 않습니다.");
        }
        // 2. 상태 검사
        String status = info.get("status").asText();
        if (!"PAID".equals(status)) {
            throw new IllegalStateException("결제 상태가 완료되지 않음: " + status);
        }
        return info;
    }

    private Product validateProductAndSeller(Long productId) {
        Product product = productService.findById(productId);
        if (product.isDeleted() || product.getStatus() != Product.ProductStatus.SELLING) {
            throw new IllegalStateException("이미 판매되거나 삭제된 상품입니다.");
        }
        if (product.getSeller().getStatus() == User.UserStatus.BANNED) {
            throw new IllegalStateException("정지된 판매자의 상품은 구매할 수 없습니다.");
        }
        return product;
    }

    private int validatePaymentAmount(Product product, PortOnePaymentRequest request, JsonNode info) {
        int expectedAmount = calculateExpectedAmount(product, request.getDeliveryType());
        int totalAmount = info.get("amount").get("total").asInt();
        if (expectedAmount != totalAmount) {
            throw new IllegalArgumentException("결제 금액 불일치: 요청(" + expectedAmount + ") vs 실제(" + totalAmount + ")");
        }
        return expectedAmount;
    }

    private void validateDuplicatePayment(String paymentId) {
        if (paymentRepository.existsByPaymentId(paymentId)) {
            throw new IllegalArgumentException("이미 처리된 결제입니다.");
        }
    }

    private void validateRequestDetails(PortOnePaymentRequest request) {
        if ("normal".equals(request.getDeliveryType())) {
            if (request.getAddress() == null || request.getPostCode() == null || request.getPhone() == null || request.getName() == null) {
                throw new IllegalArgumentException("일반배송의 필수 정보가 없습니다.");
            }
        } else if ("cheap".equals(request.getDeliveryType())) {
            if (request.getStoreName() == null || request.getStoreAddress() == null || request.getPhone() == null || request.getName() == null) {
                throw new IllegalArgumentException("반값배송의 필수 정보가 없습니다.");
            }
        }
    }

    // 결제 금액 검사
    private int calculateExpectedAmount(Product product, String deliveryType) {
        int total = product.getPrice();

        if (("normal").equals(deliveryType)) {
            total += product.getNormalDeliveryFee();
        } else if (("cheap").equals(deliveryType)) {
            total += product.getCheapDeliveryFee();
        }
        return total;
    }

    private LocalDateTime parsePaidAt(String paidAtStr) {
        if (paidAtStr == null || paidAtStr.isEmpty()) {
            return null;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(paidAtStr);
            return odt.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception e) {
            throw new RuntimeException("결제 시간 파싱 실패", e);
        }
    }

    private Payment createPayment(JsonNode info, PortOnePaymentRequest request, Product product,
                                  User user, int expectedAmount, LocalDateTime paidAt) {
        Payment payment = new Payment();
        payment.setPaymentId(request.getPaymentId());
        payment.setAmount(product.getPrice());
        payment.setFinalAmount(expectedAmount);
        payment.setStatus(Payment.Status.PAID);
        payment.setPaidAt(paidAt);
        payment.setUser(user);
        payment.setProduct(product);

        JsonNode methodNode = info.get("method");
        payment.setPayMethod(methodNode != null && methodNode.has("provider")
                ? methodNode.get("provider").asText() : "UNKNOWN");

        if ("normal".equals(request.getDeliveryType())) {
            payment.setDeliveryMethod(Payment.DeliveryMethod.NORMAL);
            payment.setDeliveryFee(product.getNormalDeliveryFee());
        } else if ("cheap".equals(request.getDeliveryType())) {
            payment.setDeliveryMethod(Payment.DeliveryMethod.CHEAP);
            payment.setDeliveryFee(product.getCheapDeliveryFee());
        } else {
            payment.setDeliveryMethod(Payment.DeliveryMethod.DIRECT);
            payment.setDeliveryFee(null);
        }
        return payment;
    }

    public boolean existsByProduct(Product product) {
        return paymentRepository.existsByProduct(product);
    }
}
