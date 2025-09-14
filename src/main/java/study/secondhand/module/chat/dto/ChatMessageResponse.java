package study.secondhand.module.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import study.secondhand.module.chat.entity.ChatMessage;
import study.secondhand.module.chat.entity.OrderMessage;
import study.secondhand.module.chat.entity.ReviewMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        Long senderId,
        String message,
        String chatImageUrl,
        LocalDateTime sentAt,
        String messageType, // "TEXT", "ORDER"
        @JsonProperty("read")
        boolean isRead,

        // 아래는 order 메시지용
        String imageUrl,
        String orderStatus,
        String buttonText,
        Long productId,
        Long orderId,
        String userRole,
        String trackingUrl,

        // review 메시지용
        Long reviewId,
        Long targetId,
        Long writerId,
        String writerNickname

) {

    // fromEntity 메서드가 isRead 값을 직접 받음
    public static ChatMessageResponse fromEntity(ChatMessage entity, Long roomId, Long loginUserId, boolean isRead) {

        if (entity.getType() == ChatMessage.MessageType.ORDER) {
            OrderMessage orderMsg = entity.getOrderMessage();
            var order = orderMsg.getOrder();
            String role = order.getBuyer().getId().equals(loginUserId) ? "BUYER" : "SELLER";

            return new ChatMessageResponse(
                    entity.getId(),
                    roomId,
                    entity.getSender().getId(),
                    entity.getContent(),
                    entity.getImageUrl(),
                    entity.getCreatedAt(),
                    entity.getType().name(),
                    isRead,
                    orderMsg.getImageUrl(),
                    orderMsg.getOrderStatus().toString(),
                    orderMsg.resolveButtonText(loginUserId),
                    orderMsg.getProduct().getId(),
                    orderMsg.getOrder().getId(),
                    role,
                    orderMsg.getTrackingUrl(),
                    null, null, null, null
            );
        } else if (entity.getType() == ChatMessage.MessageType.REVIEW) {
            ReviewMessage reviewMsg = entity.getReviewMessage();

            // reviewMsg가 null인지 확인
            if (reviewMsg == null || reviewMsg.getReview() == null) {
                // 리뷰가 삭제된 경우, 일반 텍스트 메시지처럼 처리
                return new ChatMessageResponse(
                        entity.getId(),
                        roomId,
                        entity.getSender().getId(),
                        "[삭제된 후기 메시지입니다.]", // 내용을 변경하여 표시
                        entity.getImageUrl(),
                        entity.getCreatedAt(),
                        ChatMessage.MessageType.DELETED_REVIEW.name(), // 타입을 TEXT로 변경
                        isRead,
                        null, null, null,
                        null, null, null, null,
                        null, null, null, null
                );
            }

            // 리뷰가 존재하는 경우
            return new ChatMessageResponse(
                    entity.getId(),
                    roomId,
                    entity.getSender().getId(),
                    entity.getContent(),
                    entity.getImageUrl(),
                    entity.getCreatedAt(),
                    entity.getType().name(),
                    isRead,
                    null, null, null,
                    null, null, null, null,
                    reviewMsg.getReview().getId(),
                    reviewMsg.getReview().getTarget().getId(),
                    reviewMsg.getReview().getWriter().getId(),
                    reviewMsg.getReview().getWriter().getNickname() != null ?
                            reviewMsg.getReview().getWriter().getNickname() :
                            "상점 " + reviewMsg.getReview().getWriter().getId() + "호"
            );
        }
        // 일반 텍스트 메시지
        return new ChatMessageResponse(
                entity.getId(),
                roomId,
                entity.getSender().getId(),
                entity.getContent(),
                entity.getImageUrl(),
                entity.getCreatedAt(),
                entity.getType().name(),
                isRead,
                null, null, null,
                null, null, null, null,
                null, null, null, null
        );
    }
}