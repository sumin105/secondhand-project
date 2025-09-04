package study.secondhand.module.order.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.order.dto.DeliveryFormDto;
import study.secondhand.module.payment.dto.PortOnePaymentRequest;
import study.secondhand.module.order.entity.Delivery;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.payment.entity.Payment;
import study.secondhand.module.order.repository.DeliveryRepository;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.util.Optional;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserService userService;

    public DeliveryService(DeliveryRepository deliveryRepository, @Lazy UserService userService) {
        this.deliveryRepository = deliveryRepository;
        this.userService = userService;
    }

    public Delivery createAndSaveDelivery(PortOnePaymentRequest request, Payment payment, User user) {
        Delivery delivery = new Delivery();
        delivery.setPayment(payment);
        delivery.setRecipientName(request.getName());
        delivery.setRecipientPhone(request.getPhone());
        delivery.setRequestMessage(Optional.ofNullable(request.getRequestMessage()).orElse(""));
        delivery.setDeliveryStatus(Delivery.DeliveryStatus.READY);

        if ("normal".equals(request.getDeliveryType())) {
            delivery.setAddress(request.getAddress());
            delivery.setDetailAddress(Optional.ofNullable(request.getDetailAddress()).orElse(""));
            delivery.setPostCode(request.getPostCode());
            userService.updateUserAddress(user, request);
        } else if ("cheap".equals(request.getDeliveryType())) {
            delivery.setStoreName(request.getStoreName());
            delivery.setStoreAddress(request.getStoreAddress());
            userService.updateUserStore(user, request);
        } else {
            throw new IllegalArgumentException("배송 유형이 올바르지 않습니다.");
        }
        deliveryRepository.save(delivery);
        return delivery;
    }

    // 운송장 등록
    public void registerShippingInfo(Order order, DeliveryFormDto dto) {
        // 운송장 정보 저장, 배송 상태 변경
        Payment payment = order.getPayment();
        Delivery delivery = deliveryRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 결제에 대한 배송 정보가 존재하지 않습니다. 결제 ID: " + payment.getId()));
        delivery.setDeliveryCompany(dto.getDeliveryCompany());
        delivery.setTrackingNumber(dto.getTrackingNumber());
        delivery.setDeliveryStatus(Delivery.DeliveryStatus.SHIPPING);
        deliveryRepository.save(delivery);
    }

    // 배송 상태 업데이트
    public void markAsDelivered(Order order) {
        Payment payment = order.getPayment();
        Delivery delivery = deliveryRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 결제에 대한 배송 정보가 존재하지 않습니다. 결제 ID: " + payment.getId()));

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.DELIVERED);
        deliveryRepository.save(delivery);
    }

    public void updateTrackingUrl(Delivery delivery, String trackingUrl) {
        delivery.setTrackingUrl(trackingUrl);
        deliveryRepository.save(delivery);
    }
}
