package study.secondhand.module.review.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.secondhand.module.review.entity.Review;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByTargetId(Long targetId, Pageable pageable);

    Review findByOrderIdAndWriterId(Long orderId, Long writerId);

    Review findByOrderIdAndTargetId(Long orderId, Long targetId);

}
