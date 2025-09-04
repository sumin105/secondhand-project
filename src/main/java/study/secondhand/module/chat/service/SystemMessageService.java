package study.secondhand.module.chat.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.chat.entity.ChatMessage;
import study.secondhand.module.chat.entity.ChatRoom;
import study.secondhand.module.chat.entity.OrderMessage;
import study.secondhand.module.chat.entity.ReviewMessage;
import study.secondhand.module.chat.repository.ChatMessageRepository;
import study.secondhand.module.chat.repository.OrderMessageRepository;
import study.secondhand.module.chat.repository.ReviewMessageRepository;
import study.secondhand.module.order.entity.Order;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.review.entity.Review;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

@Service
public class SystemMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final OrderMessageRepository orderMessageRepository;
    private final ReviewMessageRepository reviewMessageRepository;
    private final ChatService chatService;
    private final UserService userService;

    public SystemMessageService(ChatMessageRepository chatMessageRepository, OrderMessageRepository orderMessageRepository, ReviewMessageRepository reviewMessageRepository, ChatService chatService, @Lazy UserService userService) {
        this.chatMessageRepository = chatMessageRepository;
        this.orderMessageRepository = orderMessageRepository;
        this.reviewMessageRepository = reviewMessageRepository;
        this.chatService = chatService;
        this.userService = userService;
    }

    // 주문 생성 메시지
    @Transactional
    public void sendOrderMessage(Order order, Product product) {
        sendOrderSystemMessage(order, product, null);
    }

    // 배송 시작 메시지
    @Transactional
    public void sendShippingMessage(Order order, Product product, String trackingUrl) {
        sendOrderSystemMessage(order, product, trackingUrl);
    }

    // 배송 완료 메시지
    @Transactional
    public void sendDeliveredMessage(Order order, Product product) {
        sendOrderSystemMessage(order, product, null);
    }

    // 거래 완료 메시지
    @Transactional
    public void sendDoneMessage(Order order, Product product) {
        sendOrderSystemMessage(order, product, null);
    }

    // 후기 메시지
    @Transactional
    public void sendReviewMessage(Review review, Order order) {
        User systemUser = userService.getSystemUser();
        Product product = order.getProduct();
        ChatRoom room = chatService.getOrCreateRoom(order.getBuyer(), order.getSeller());

        ChatMessage message = buildChatMessage(systemUser, room, product, ChatMessage.MessageType.REVIEW);
        chatMessageRepository.save(message);

        ReviewMessage reviewMessage = ReviewMessage.builder()
                .chatMessage(message)
                .review(review)
                .product(product)
                .build();
        reviewMessageRepository.save(reviewMessage);
    }

    private void sendOrderSystemMessage(Order order, Product product, String trackingUrl) {
        User systemUser = userService.getSystemUser();
        ChatRoom room = chatService.getOrCreateRoom(order.getBuyer(), order.getSeller());

        ChatMessage message = buildChatMessage(systemUser, room, product, ChatMessage.MessageType.ORDER);
        chatMessageRepository.save(message);

        OrderMessage orderMessage = OrderMessage.builder()
                .chatMessage(message)
                .product(product)
                .order(order)
                .orderStatus(order.getStatus())
                .trackingUrl(trackingUrl) // null일 경우 자동 무시됨
                .build();
        orderMessageRepository.save(orderMessage);
    }

    private ChatMessage buildChatMessage(User systemUser, ChatRoom room, Product product, ChatMessage.MessageType type) {
        return ChatMessage.builder()
                .chatRoom(room)
                .sender(systemUser)
                .product(product)
                .type(type)
                .build();
    }
}
