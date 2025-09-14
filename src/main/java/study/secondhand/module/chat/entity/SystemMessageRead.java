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
@Table(name = "system_message_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"}))
public class SystemMessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        readAt = LocalDateTime.now();
    }
}
