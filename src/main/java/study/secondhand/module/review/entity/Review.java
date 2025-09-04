package study.secondhand.module.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import study.secondhand.module.chat.entity.ReviewMessage;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.review.ReviewTag;
import study.secondhand.module.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(nullable = false)
    private int rating;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private List<ReviewTag> tags = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ReviewMessage reviewMessage;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
