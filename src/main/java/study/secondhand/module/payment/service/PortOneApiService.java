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
    // 인증 관련 API를 제외한 모든 API는 HTTP Authorization 헤더로 아래 형식의 인증 정보를 전달해주셔야 합니다.
    //Authorization: PortOne MY_API_SECRET
    // GET 요청 시에 Body를 전달해야 하는 경우, Body 대신 Query를 사용할 수 있습니다.
    //이 경우, Body 객체를 requestBody Query 필드에 넣어주시면 됩니다.
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

