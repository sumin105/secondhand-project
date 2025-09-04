package study.secondhand.module.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.secondhand.module.chat.dto.ChatRoomBasicInfo;
import study.secondhand.module.chat.entity.ChatRoom;
import study.secondhand.module.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.sender.id = :userAId AND cr.receiver.id = :userBId) OR " +
            "(cr.sender.id = :userBId AND cr.receiver.id = :userAId)")
    Optional<ChatRoom> findByChatRoomByUserPair(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    @Query("""
            SELECT
            CASE
            WHEN cr.sender.id =:userId THEN cr.receiver.id
            WHEN cr.receiver.id =:userId THEN cr.sender.id
            END
            FROM ChatRoom cr
            WHERE cr.id = :roomId
            """)
    Long findByOtherUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    boolean existsBySender(User sender);

    boolean existsByReceiver(User receiver);

    @Query("""
            SELECT new study.secondhand.module.chat.dto.ChatRoomBasicInfo(
                cr.id,
                cr,
                (SELECT
                    SUM(CASE WHEN cm.sender.id != :userId AND cm.isRead = false THEN 1 ELSE 0 END)
                    +
                    SUM(CASE WHEN cm.sender.role = 'SYSTEM' AND NOT EXISTS (
                            SELECT 1 FROM SystemMessageRead smr
                            WHERE smr.message = cm AND smr.user.id = :userId) THEN 1 ELSE 0 END)
                FROM ChatMessage cm WHERE cm.chatRoom = cr)
            )
            FROM ChatRoom cr
            WHERE (cr.sender.id = :userId OR cr.receiver.id = :userId)
            AND EXISTS (SELECT 1 FROM ChatMessage cm WHERE cm.chatRoom = cr)
            """)
    List<ChatRoomBasicInfo> findChatRoomInfosByUserId(@Param("userId") Long id);
}

