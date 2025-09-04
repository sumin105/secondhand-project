package study.secondhand.module.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "system_message_read")
public class SystemMessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        readAt = LocalDateTime.now();
    }
}
