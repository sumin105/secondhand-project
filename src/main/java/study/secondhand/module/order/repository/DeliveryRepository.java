package study.secondhand.module.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.secondhand.module.order.entity.Delivery;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery>findByPaymentId(Long paymentId);
}
