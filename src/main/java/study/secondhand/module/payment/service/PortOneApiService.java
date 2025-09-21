package study.secondhand.module.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PortOneApiService {

    @Value("${portOne.api.secret}")
    private String apiSecret;

    private final ObjectMapper objectMapper;
    private static final String PAYMENT_URL = "https://api.portone.io/payments/";

    public JsonNode getPaymentInfo(String paymentId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + apiSecret);  // ✅ v2 인증 방식

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                PAYMENT_URL + paymentId,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("결제 정보 조회 실패: " + response.getBody());
        }

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            System.out.println("결제 정보 파싱 실패. 응답 본문: " + response.getBody());
            throw new RuntimeException("결제 정보 파싱 실패 ", e);
        }
    }
}

