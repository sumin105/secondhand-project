package study.secondhand.module.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import study.secondhand.module.review.ReviewTag;

import java.util.List;

@Getter
@Setter
public class ReviewRequestDto {
    private Long orderId;
    @NotNull(message = "별점은 필수 항목입니다.")
    @Min(1) @Max(5)
    private int rating;
    private List<ReviewTag> tags;
    @Size(max = 1000, message = "후기 내용은 1000자를 초과할 수 없습니다.")
    private String content;
}
