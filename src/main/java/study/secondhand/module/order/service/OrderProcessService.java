package study.secondhand.module.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.chat.service.SystemMessageService;
import study.secondhand.module.order.dto.DeliveryFormDto;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.order.repository.OrderRepository;
import study.secondhand.module.user.entity.User;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderProcessService {

    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private final SystemMessageService systemMessageService;
    private final OrderRepository orderRepository;

    // 운송장 등록
    public void shipOrder(Long orderId, DeliveryFormDto dto, User user) throws AccessDeniedException {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문서를 찾을 수 없습니다."));
        // 판매자 검증
        if (!order.getSeller().getId().equals(user.getId())) {
            throw new AccessDeniedException("운송장 정보를 등록할 권한이 없습니다.");
        }

        // 운송장 등록 및 배송상태 변경
        deliveryService.registerShippingInfo(order, dto);
        // 배송사별 운송장 조회 URL 생성
        String trackingUrl = getTrackingUrl(dto.getDeliveryCompany(), dto.getTrackingNumber());
        // 주문 상태 업데이트
        order = orderService.markAsShipped(orderId);
        // 배송 조회 주소 업데이트
        deliveryService.updateTrackingUrl(order.getDelivery(), trackingUrl);
        // 주문 메시지 전송
        systemMessageService.sendShippingMessage(order, order.getProduct(), trackingUrl);
    }

    // 배송 완료
    public void completeDeliveryProcess(Long orderId, User user) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문서를 찾을 수 없습니다."));
        // 구매자 검증
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new AccessDeniedException("구매자만 배송 완료를 할 수 있습니다.");
        }
        // 배송 상태 업데이트
        deliveryService.markAsDelivered(order);
        // 주문 상태 업데이트
        order = orderService.markAsDelivered(orderId);
        // 주문 메시지 전송 (배송완료 알림, 거래 확정 버튼)
        systemMessageService.sendDeliveredMessage(order, order.getProduct());
    }

    // 거래 완료
    public void completeOrderProcess(Long orderId, User user) {
        Order order = orderService.markAsDone(orderId, user);
        // 주문 메시지 전송 (거래 완료 알림, 후기 작성 버튼 유도)
        systemMessageService.sendDoneMessage(order, order.getProduct());
    }

    private String getTrackingUrl(String courier, String invoiceNo) {
        return switch (courier) {
            case "CJ대한통운" -> "https://www.cjlogistics.com/ko/tool/parcel/newTracking?gnbInvcNo=" + invoiceNo;
            case "롯데택배" -> "https://www.lotteglogis.com/home/reservation/tracking/linkView?InvNo=" + invoiceNo;
            case "한진택배" ->
                    "https://www.hanjin.com/kor/CMS/DeliveryMgr/WaybillResult.do?mCode=MN038&schLang=KR&wblnumText2=" + invoiceNo;
            case "우체국택배" -> "https://service.epost.go.kr/trace.RetrieveDomRigiTraceList.comm?sid1=" + invoiceNo;
            case "로젠택배" -> "https://www.ilogen.com/web/personal/trace/" + invoiceNo;
            case "CU편의점택배" -> "https://www.cupost.co.kr/postbox/delivery/allResult.cupost?invoice_no=" + invoiceNo;
            case "GS25편의점택배" -> "https://www.cvsnet.co.kr/invoice/tracking.do?invoice_no=" + invoiceNo;
            default -> "";
        };
    }
}
