package study.secondhand.module.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.chat.service.SystemMessageService;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.review.ReviewTag;
import study.secondhand.module.review.dto.ReviewDto;
import study.secondhand.module.review.dto.ReviewFormViewDto;
import study.secondhand.module.review.dto.ReviewRequestDto;
import study.secondhand.module.review.entity.Review;
import study.secondhand.module.review.repository.ReviewRepository;
import study.secondhand.module.user.entity.User;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final SystemMessageService systemMessageService;

    @Transactional
    public Review createReview(ReviewRequestDto dto, User user) {
        Order order = orderService.findOrder(dto.getOrderId());

        User target;
        if (Objects.equals(order.getBuyer().getId(), user.getId())) {
            target = order.getSeller();
        } else {
            target = order.getBuyer();
        }

        List<ReviewTag> tags = dto.getTags() != null ? dto.getTags() : Collections.emptyList();
        String content = dto.getContent() != null ? dto.getContent().trim() : "";

        Review review = new Review();
        review.setOrder(order);
        review.setWriter(user);
        review.setTarget(target);
        review.setRating(dto.getRating());
        review.setTags(tags);
        review.setContent(content);
        return reviewRepository.save(review);
    }

    public Page<ReviewDto> getRecentReviews(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByTargetId(userId, pageable)
                .map(ReviewDto::from);
    }

    public Review getOrderMyReview(User user, Order order) {
        Long orderId = order.getId();
        Long userId = user.getId();
        return reviewRepository.findByOrderIdAndWriterId(orderId, userId);
    }

    public Review getOrderOtherReview(User user, Order order) {
        Long orderId = order.getId();
        Long userId = user.getId();
        return reviewRepository.findByOrderIdAndTargetId(orderId, userId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long id) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (!Objects.equals(review.getWriter().getId(), id)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public ReviewFormViewDto getReviewFormData(Long orderId, User user) throws AccessDeniedException {
        Order order = orderService.findOrder(orderId);
        orderService.existsByUser(order, user);
        return new ReviewFormViewDto(order, user);
    }

    @Transactional
    public Review createReviewAndSend(Long orderId, ReviewRequestDto dto, User user) {
        Order order = orderService.findOrder(orderId);
        Review review = createReview(dto, user);
        systemMessageService.sendReviewMessage(review, order);
        return review;
    }
}
