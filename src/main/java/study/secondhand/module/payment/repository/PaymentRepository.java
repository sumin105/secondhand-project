package study.secondhand.module.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import study.secondhand.module.payment.entity.Payment;
import study.secondhand.module.product.entity.Product;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaymentId(String paymentId);

    boolean existsByProduct(Product product);
}
