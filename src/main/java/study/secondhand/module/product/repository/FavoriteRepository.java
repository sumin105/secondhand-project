package study.secondhand.module.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.secondhand.module.product.entity.Favorite;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndProduct(User user, Product product);
    Optional<Favorite> findByUserAndProduct(User user, Product product);

    Page<Favorite> findFavoritesByUserId(Long shopId, Pageable pageable);

    int countByProductId(Long productId);

    @Query("SELECT f.user.id FROM Favorite f WHERE f.product.id = :productId")
    List<Long> findUserIdsByProductId(@Param("productId") Long productId);
}
