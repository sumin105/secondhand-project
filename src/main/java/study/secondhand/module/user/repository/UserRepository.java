package study.secondhand.module.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.secondhand.module.user.dto.DeliveryInfoDto;
import study.secondhand.module.user.dto.StoreSummaryDto;
import study.secondhand.module.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthId(String oauthId);

    Optional<User> findByOauthIdAndProvider(String oauthId, String provider);

    @Query("SELECT new study.secondhand.module.user.dto.DeliveryInfoDto(u.name, u.phoneNumber, u.address, u.detailAddress, u.postCode) " +
            "FROM User u WHERE u.id = :id")
    DeliveryInfoDto findDeliveryInfoById(@Param("id") Long id);

    boolean existsByOauthIdAndProvider(String oauthId, String provider);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.role != 'WITHDRAWN'")
    Page<User> findAllDeletedFalseUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdAndDeletedFalse(@Param("id") Long id);

    List<User> findAllByIdIn(List<Long> ids);

    @Query("""
            SELECT new study.secondhand.module.user.dto.StoreSummaryDto(
                u.id,
                u.nickname,
                u.status,
                (SELECT COUNT(p) FROM Product p WHERE p.seller = u AND p.deleted = false),
                COALESCE((SELECT AVG(r.rating) FROM Review r WHERE r.target = u), 0.0)
            )
            FROM User u
            WHERE (u.nickname LIKE concat('%', :keyword, '%') OR CAST(u.id AS string) LIKE concat('%', :keyword, '%'))
            AND u.status != 'WITHDRAWN'
            AND u.deleted = false
            AND u.role != 'ADMIN' AND u.role != 'SYSTEM'
            """)
    Page<StoreSummaryDto> findStoreSummariesByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
