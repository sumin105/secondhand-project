package study.secondhand.module.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.secondhand.module.product.dto.ProductSummaryDto;
import study.secondhand.module.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> id(Long id);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.deleted = false
            AND p.status = 'SELLING'
            AND p.seller.status = 'ACTIVE'
            ORDER BY p.favoriteCount DESC, p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummariesByOrderByLikes(Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.deleted = false
            AND p.status = 'SELLING'
            AND p.seller.status = 'ACTIVE'
            ORDER BY p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummariesByOrderByLatest(Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.category.id = :categoryId
            AND p.deleted = false
            AND p.status = 'SELLING'
            AND p.seller.status = 'ACTIVE'
            ORDER BY p.favoriteCount DESC, p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummariesByCategoryByOrderByLikes(@Param("categoryId") Long CategoryId,
                                                                  Pageable pageable);

    @Query("""
            select new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.category.id = :categoryId
            AND p.deleted = false
            AND p.status = 'SELLING'
            AND p.seller.status = 'ACTIVE'
            ORDER BY p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummariesByCategoryByOrderByLatest(@Param("categoryId") Long categoryId,
                                                                   Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.seller.id = :sellerId
            AND p.deleted = false
            AND p.seller.status != 'WITHDRAWN'
            ORDER BY
                        CASE
                            WHEN p.status = 'SELLING' THEN 0
                            WHEN p.status = 'RESERVED' THEN 1
                            WHEN p.status = 'SOLD' THEN 2
                        END,
                        p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummaryBySellerForOwner(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE p.seller.id = :sellerId
            AND p.deleted = false
            AND p.seller.status = 'ACTIVE'
            ORDER BY
                        CASE
                            WHEN p.status = 'SELLING' THEN 0
                            WHEN p.status = 'RESERVED' THEN 1
                            WHEN p.status = 'SOLD' THEN 2
                        END,
                        p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummaryBySellerForPublic(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("""
            SELECT new study.secondhand.module.product.dto.ProductSummaryDto(
            p.id, p.title, p.price, p.thumbnailImageUrl, p.status, p.createdAt)
            FROM Product p
            WHERE lower(p.title) LIKE lower(concat('%', :keyword, '%'))
            AND p.deleted = false
            And p.seller.status = 'ACTIVE'
            ORDER BY p.createdAt DESC
            """)
    Page<ProductSummaryDto> findSummariesByKeyword(
            @Param("keyword") String keyword, Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :todayStart")
    Long countTodayProducts(@Param("todayStart") LocalDateTime todayStart);

    List<Product> findBySellerId(Long sellerId);

    Page<Product> findAllByDeletedFalse(Pageable pageable);
}
