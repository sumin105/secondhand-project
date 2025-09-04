package study.secondhand.module.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content; // 일반 메시지

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    // 메시지를 처음 보낼 때 어떤 상품에서 보냈는지 남겨둠
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String imageUrl;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderMessage orderMessage;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewMessage reviewMessage;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.type != MessageType.TEXT && this.type != MessageType.IMAGE) {
            this.isRead = true;
        }
    }

    @Getter
    @Setter
    @Transient
    private Long roomId;

    public enum MessageType {
        TEXT,
        IMAGE,
        ORDER,
        REVIEW,
        DELETED_REVIEW
    }
}
