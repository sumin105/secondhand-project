package study.secondhand.module.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.secondhand.module.chat.entity.OrderMessage;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {

}
