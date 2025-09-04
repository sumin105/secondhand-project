package study.secondhand.module.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.secondhand.module.review.ReviewTag;
import study.secondhand.module.review.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long writerId;
    private String writerNickname;
    private int rating;
    private List<ReviewTag> tags;
    private String content;
    private LocalDateTime createdAt;
    private Long productId;
    private String productTitle;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .writerId(review.getWriter().getId())
                .writerNickname(review.getWriter().getNickname() != null ? review.getWriter().getNickname() : "상점 " + review.getWriter().getId() + "호")
                .rating(review.getRating())
                .tags(review.getTags())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .productId(review.getOrder().getProduct().getId())
                .productTitle(review.getOrder().getProduct().getTitle())
                .build();
    }
}
