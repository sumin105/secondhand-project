package study.secondhand.module.order.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.order.dto.OrderViewDto;
import study.secondhand.module.order.dto.PurchasedProductDto;
import study.secondhand.module.order.dto.SaledProductDto;
import study.secondhand.module.order.entity.Delivery;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.payment.entity.Payment;
import study.secondhand.module.order.repository.OrderRepository;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.review.entity.Review;
import study.secondhand.module.review.service.ReviewService;
import study.secondhand.module.user.entity.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ReviewService reviewService;

    public OrderService(OrderRepository orderRepository, @Lazy ReviewService reviewService) {
        this.orderRepository = orderRepository;
        this.reviewService = reviewService;
    }

    @Transactional
    public Order createOrder(Payment payment, Product product, User buyer, Delivery delivery) {
        Order order = Order.builder()
                .buyer(buyer)
                .seller(product.getSeller())
                .product(product)
                .productTitleSnapshot(product.getTitle())
                .productThumbnailUrlSnapshot(product.getThumbnailImageUrl())
                .payment(payment)
                .delivery(delivery)
                .status(OrderStatus.PAID)
                .build();

        return orderRepository.save(order);
    }

    public Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
    }

    public Page<PurchasedProductDto> findPurchasedProductsByUserId(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return orderRepository.findPurchasedProductByUserId(userId, pageable);
    }

    public Page<SaledProductDto> findSaledProductsBySellerId(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return orderRepository.findSaledProductByUserid(userId, pageable);
    }

    @Transactional
    public Order markAsShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.setStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markAsDone(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        }

        // 거래 방식에 따른 상태 검증
        if (order.getDelivery() == null) {
            // 직거래: 결제 완료 상태에서만 거래 확정 가능
            if (order.getStatus() != OrderStatus.PAID) {
                throw new IllegalStateException("직거래는 결제 완료 상태에서만 거래를 확정할 수 있습니다.");
            }
        } else {
            // 택배거래: 배송 완료 상태에서만 거래 확정 가능
            if (order.getStatus() != OrderStatus.DELIVERED) {
                throw new IllegalStateException("택배 거래는 배송 완료 상태에서만 거래를 확정할 수 있습니다.");
            }
        }

        // 주문 상태 변경, 완료 시간 업데이트
        order.setStatus(OrderStatus.DONE);
        order.setCompletedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public int countCompletedOrdersBySeller(Long sellerId) {
        return orderRepository.countCompletedSalesBySeller(sellerId);
    }

    public OrderViewDto getOrderView(Long id, User user) {
        Order order = findOrder(id);

        if (!user.getId().equals(order.getBuyer().getId())) {
            if (!user.getId().equals(order.getSeller().getId())) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
        }

        Review myReview = reviewService.getOrderMyReview(user, order);
        Review otherReview = reviewService.getOrderOtherReview(user, order);

        return new OrderViewDto(order, myReview, otherReview, user);
    }

    public List<String> findThumbnailUrlsByProductId(Long productId) {
        return orderRepository.findThumbnailUrlsByProductId(productId);
    }

    public boolean existsByProduct(Product product) {
        return orderRepository.existsByProduct(product);
    }

    public void existsByUser(Order order, User user) throws AccessDeniedException {
        if (!order.getBuyer().getId().equals(user.getId())) {
            if (!order.getSeller().getId().equals(user.getId())) {
                throw new AccessDeniedException("리뷰 작성 권한이 없습니다.");
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean hasIncompleteOrders(User user) {
        return orderRepository.existsByBuyerAndStatusNot(user, OrderStatus.DONE) ||
                orderRepository.existsBySellerAndStatusNot(user, OrderStatus.DONE);
    }

    @Transactional(readOnly = true)
    public boolean existsByBuyerOrSeller(User user) {
        return orderRepository.existsByBuyer(user) ||
                orderRepository.existsBySeller(user);
    }
}
