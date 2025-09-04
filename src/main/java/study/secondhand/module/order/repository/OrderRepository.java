package study.secondhand.module.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.secondhand.module.order.entity.OrderStatus;
import study.secondhand.module.order.dto.PurchasedProductDto;
import study.secondhand.module.order.dto.SaledProductDto;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByProduct(Product product);

    @Query("SELECT o.productThumbnailUrlSnapshot FROM  Order o WHERE o.product.id = :productId")
    List<String> findThumbnailUrlsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.seller.id =:sellerId AND o.status = 'DONE'")
    int countCompletedSalesBySeller(@Param("sellerId") Long sellerId);

    boolean existsByBuyer(User buyer);

    boolean existsBySeller(User seller);

    boolean existsByBuyerAndStatusNot(User buyer, OrderStatus status);

    boolean existsBySellerAndStatusNot(User seller, OrderStatus status);

    @Query("""
            SELECT new study.secondhand.module.order.dto.PurchasedProductDto(
            o.id, o.productTitleSnapshot, o.payment.finalAmount, o.status,
            o.updatedAt, o.seller.id, o.seller.nickname, o.payment.deliveryMethod,
            o.productThumbnailUrlSnapshot)
            FROM Order o
            WHERE o.buyer.id = :userId
            ORDER BY
                        CASE WHEN o.status = 'DONE' THEN 1 ELSE 0 END ASC,
                        o.updatedAt DESC
            """)
    Page<PurchasedProductDto> findPurchasedProductByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.order.dto.SaledProductDto(
            o.id, o.productTitleSnapshot, o.payment.finalAmount,o.status,
            o.updatedAt, o.buyer.id, o.buyer.nickname, o.payment.deliveryMethod,
            o.productThumbnailUrlSnapshot)
            FROM Order o
            WHERE o.seller.id = :userId
            ORDER BY
                         CASE WHEN o.status = 'DONE' THEN 1 ELSE 0 END ASC,
                         o.updatedAt DESC
            """)
    Page<SaledProductDto> findSaledProductByUserid(@Param("userId") Long userId, Pageable pageable);
}
