package study.secondhand.module.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.secondhand.module.chat.entity.ReviewMessage;

@Repository
public interface ReviewMessageRepository extends JpaRepository<ReviewMessage, Integer> {

}
