package study.secondhand.module.review.dto;

import lombok.Getter;
import lombok.Setter;
import study.secondhand.module.review.ReviewTag;

import java.util.List;

@Getter
@Setter
public class ReviewRequestDto {
    private Long orderId;
    private int rating;
    private List<ReviewTag> tags;
    private String content;
}
