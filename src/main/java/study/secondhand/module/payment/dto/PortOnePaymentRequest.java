package study.secondhand.module.payment.dto;

import lombok.Getter;
import lombok.Setter;

// 결제 검증 요청 시 클라이언트로부터 전달받는 DTO

@Getter @Setter
public class PortOnePaymentRequest {

    private String paymentId; // 클라이언트에서 생성한 고유 결제 ID
    private Long productId;

    private String name;
    private String phone;
    private String address;
    private String detailAddress;
    private String postCode;

    private String storeName;
    private String storeAddress;

    private String requestMessage;

    private String deliveryType;
}
