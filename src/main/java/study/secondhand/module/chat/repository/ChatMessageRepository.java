package study.secondhand.module.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.secondhand.module.chat.entity.ChatMessage;
import study.secondhand.module.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    boolean existsByProduct(Product product);

    @Query("""
            SELECT count (m) FROM ChatMessage m
            WHERE m.chatRoom.id = :roomId AND m.sender.id != :senderId AND m.isRead = false 
            """)
    int countByChatRoomIdAndSenderNotAndIsReadFalse(@Param("roomId") Long roomId, @Param("senderId") Long senderId);

    @Modifying
    @Query("""
            UPDATE ChatMessage m SET m.isRead = true
            WHERE m.chatRoom.id = :roomId And m.sender.id != :userId AND m.isRead = false
            """)
    void updateIsReadByChatRoomIdAndSenderNot(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessage m
            WHERE m.chatRoom.sender.id = :userId AND m.sender.id != :userId AND m.isRead = false
            OR m.chatRoom.receiver.id = :userId AND m.sender.id != :userId AND m.isRead = false
            """)
    int countUnreadMessagesByUserId(@Param("userId") Long userId);

    @Query("SELECT m.id FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.sender.role = 'SYSTEM'")
    List<Long> findSystemMessageIdsByChatRoomId(@Param("roomId") Long roomId);

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessage m
            WHERE m.sender.role = 'SYSTEM'
            AND (
                m.chatRoom.sender.id = :userId OR m.chatRoom.receiver.id = :userId
            )
            AND NOT EXISTS (
                SELECT 1 FROM SystemMessageRead r
                WHERE r.message = m AND r.user.id = :userId
            )
            """)
    int countUnreadSystemMessageByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.chatRoom.id IN :roomIds
            AND m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.chatRoom.id IN :roomIds GROUP BY m2.chatRoom.id)
            """)
    List<ChatMessage> findLastMessagesByRoomIds(@Param("roomIds") List<Long> roomIds);
}