package study.secondhand.module.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.secondhand.module.chat.entity.SystemMessageRead;

import java.util.List;

public interface SystemMessageReadRepository extends JpaRepository<SystemMessageRead, Long> {
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    @Query("SELECT smr.message.id FROM SystemMessageRead smr WHERE smr.message.id IN :messageIds AND smr.user.id = :userId")
    List<Long> findExistingMessageIds(@Param("messageIds") List<Long> systemMessageIds,
                                      @Param("userId") Long userId);
}
