package study.secondhand.module.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // 메시지를 처음 보낼 때 어떤 상품에서 보냈는지 남겨둠
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 500)
    @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다.")
    private String content; // 일반 메시지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;


    @Column(length = 2048)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderMessage orderMessage;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewMessage reviewMessage;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.type == MessageType.TEXT || this.type == MessageType.IMAGE) {
            this.isRead = false;
        } else {
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
