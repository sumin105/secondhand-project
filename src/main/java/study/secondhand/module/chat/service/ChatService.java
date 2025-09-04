package study.secondhand.module.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import study.secondhand.global.util.TimeUtil;
import study.secondhand.module.chat.dto.*;
import study.secondhand.module.chat.entity.ChatMessage;
import study.secondhand.module.chat.entity.ChatRoom;
import study.secondhand.module.chat.entity.SystemMessageRead;
import study.secondhand.module.chat.repository.ChatMessageRepository;
import study.secondhand.module.chat.repository.ChatRoomRepository;
import study.secondhand.module.chat.repository.SystemMessageReadRepository;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class ChatService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SystemMessageReadRepository systemMessageReadRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TimeUtil timeUtil;

    public ChatService(@Lazy UserService userService, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, SystemMessageReadRepository systemMessageReadRepository, SimpMessagingTemplate messagingTemplate, TimeUtil timeUtil) {
        this.userService = userService;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.systemMessageReadRepository = systemMessageReadRepository;
        this.messagingTemplate = messagingTemplate;
        this.timeUtil = timeUtil;
    }

    @Transactional
    public ChatRoomViewDto prepareChatRoom(User loginUser, Long receiverId) {
        if (loginUser.getId().equals(receiverId)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다.");
        }

        User receiver = userService.findById(receiverId);

        if (receiver.isAdmin() | receiver.isSystem() | receiver.isDeleted()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        ChatRoom room = getOrCreateRoom(loginUser, receiver);
        markMessagesAsRead(room.getId(), loginUser.getId());
        List<ChatRoomSummary> chatRooms = getRoomsByUser(loginUser);

        return new ChatRoomViewDto(receiver, chatRooms, room.getId(), loginUser);
    }

    public ChatRoom getOrCreateRoom(User userA, User userB) {
        if (userA.getId().equals(userB.getId())) {
            throw new IllegalArgumentException("자기 자신과의 채팅방은 만들 수 없습니다.");
        }

        if (userB.isSystem() || userB.isAdmin() || userB.isDeleted()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return chatRoomRepository.findByChatRoomByUserPair(userA.getId(), userB.getId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .sender(userA)
                        .receiver(userB)
                        .build()));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesByRoomId(Long roomId, Long loginUserId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(msg -> {
                    boolean isRead;
                    if ("SYSTEM".equals(msg.getSender().getRole().name())) {
                        isRead = isSystemMessageReadByUser(msg.getId(), loginUserId);
                    } else {
                        isRead = msg.isRead();
                    }
                    return ChatMessageResponse.fromEntity(msg, roomId, loginUserId, isRead);
                })
                .collect(Collectors.toList());
    }

    public List<ChatRoomSummary> getRoomsByUser(User user) {
        // 1. 채팅방 기본 정보 + 안 읽은 메시지 수를 한번에 조회
        List<ChatRoomBasicInfo> roomInfos = chatRoomRepository.findChatRoomInfosByUserId(user.getId());

        if (roomInfos.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 모든 채팅방 마지막 메시지를 한번에 조회
        // 채팅방 ID 목록 추출
        List<Long> roomIds = roomInfos.stream().map(ChatRoomBasicInfo::roomId).toList();
        List<ChatMessage> lastMessages = chatMessageRepository.findLastMessagesByRoomIds(roomIds);
        Map<Long, ChatMessage> lastMessaageMap = lastMessages.stream()
                .collect(Collectors.toMap(msg -> msg.getChatRoom().getId(), msg -> msg));

        // 3. 두 결과를 조합하여 최종 DTO 생성
        return roomInfos.stream().map(info -> {
            ChatRoom room = info.room();
            User otherUser = room.getSender().getId().equals(user.getId()) ? room.getReceiver() : room.getSender();
            ChatMessage lastMessage = lastMessaageMap.get(room.getId());

            String lastMessageContent = "";
            LocalDateTime lastMessageTime = lastMessage.getCreatedAt();

            switch (lastMessage.getType()) {
                case TEXT:
                    lastMessageContent = lastMessage.getContent();
                    break;
                case ORDER:
                    lastMessageContent = "[주문 메시지]";
                    break;
                case REVIEW:
                    lastMessageContent = "[후기 메시지]";
                    break;
                case IMAGE:
                    lastMessageContent = "[이미지]";
                    break;
            }

            return new ChatRoomSummary(
                    room.getId(),
                    otherUser.getId(),
                    otherUser.getNickname() != null ? otherUser.getNickname() : "상점 " + otherUser.getId() + "호",
                    otherUser.isBanned(),
                    otherUser.isWithdrawn(),
                    lastMessageContent,
                    lastMessageTime,
                    info.unreadCount().intValue()
            );
        }).collect(Collectors.toList());
    }


    // 읽음 처리
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        // 일반 메시지 읽음 처리
        chatMessageRepository.updateIsReadByChatRoomIdAndSenderNot(roomId, userId);

        // 시스템 메시지 읽음 처리
        List<Long> systemMessageIds = chatMessageRepository.findSystemMessageIdsByChatRoomId(roomId);
        if (systemMessageIds.isEmpty()) {
            return;
        }

        // 이미 읽은 시스템 메시지 조회
        List<Long> alreadyReadMessageIds = systemMessageReadRepository.findExistingMessageIds(systemMessageIds, userId);

        // 새로 읽음 처리해야 할 메시지만 필터링
        List<SystemMessageRead> newReads = systemMessageIds.stream()
                .filter(id -> !alreadyReadMessageIds.contains(id))
                .map(id -> SystemMessageRead.builder()
                        .message(chatMessageRepository.getReferenceById(id))
                        .user(userService.getReferenceById(userId))
                        .build())
                .toList();

        if (!newReads.isEmpty()) {
            systemMessageReadRepository.saveAll(newReads);
        }
    }

    // 시스템 메시지 읽음 여부 체크
    public boolean isSystemMessageReadByUser(Long messageId, Long userId) {
        return systemMessageReadRepository.existsByMessageIdAndUserId(messageId, userId);
    }

    public Long findReceiverId(Long roomId, Long senderId) {
        return chatRoomRepository.findByOtherUserId(roomId, senderId);
    }

    public int countUnreadMessages(Long roomId, Long receiverId) {
        return chatMessageRepository.countByChatRoomIdAndSenderNotAndIsReadFalse(roomId, receiverId);
    }

    public ChatMessage findLastMessage(Long roomId) {
        return chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(roomId)
                .orElse(null);
    }

    public String saveImage(ChatImageRequestDto dto) {
        MultipartFile file = dto.getImage();
        String fileName = chatImage(file);
        return "/images/uploads/" + fileName;
    }

    private String chatImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        // 확장자 추출 (.jpg, .png)
        String extension = "";
        int doIndex = originalFilename.lastIndexOf('.');
        if (doIndex > 0) {
            extension = originalFilename.substring(doIndex);
        }

        // UUID로 안전한 파일명 생성 (공백 제거)
        String safeFileName = UUID.randomUUID().toString() + extension;

        // 저장 경로 분리
        File chatDir = new File(uploadDir + "chat/");
        if (!chatDir.exists()) chatDir.mkdirs();

        File destination = new File(chatDir, safeFileName);

        try {
            file.transferTo(destination);
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }

        return "chat/" + safeFileName;
    }

    public int getTotalUnreadMessageCount(Long userId) {
        int normalCount = chatMessageRepository.countUnreadMessagesByUserId(userId);
        int systemCount = chatMessageRepository.countUnreadSystemMessageByUserId(userId);
        return normalCount + systemCount;
    }

    @Transactional
    public void processAndBroadcastMessage(Long roomId, ChatMessageDto dto) {
        // 1. 메시지 생성하고 db에 저장
        ChatMessage savedMessage = createAndSaveMessage(roomId, dto);

        // 2. 해당 채팅방의 모든 구독자에게 메시지를 전송
        broadcastChatMessage(roomId, savedMessage);

        // 3. 발신자, 수신자 채팅방 목록 UI 업데이트
        broadcastRoomListUpdates(savedMessage);
    }

    // 채팅 메시지를 생성하고 저장, 기존 채팅 메시지 저장 메서드, roomId가 이미 존재한다고 가정하고 바로 메시지 저장
    private ChatMessage createAndSaveMessage(Long roomId, ChatMessageDto dto) {
        User sender = userService.findById(dto.getSenderId());
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .type(dto.getMessageType())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .build();

        System.out.println("저장된 메시지: " + message.getContent());
        return chatMessageRepository.save(message);
    }

    // 채팅방에 실시간 메시지 전송
    private void broadcastChatMessage(Long roomId, ChatMessage savedMessage) {
        // 클라이언트에게 전송: /topic/chat/{roomId}
        boolean isRead = savedMessage.isRead();
        ChatMessageResponse response = ChatMessageResponse.fromEntity(
                savedMessage,
                roomId,
                savedMessage.getSender().getId(),
                isRead);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }

    // 발신자, 수신자 모두의 채팅방 목록 업데이트
    private void broadcastRoomListUpdates(ChatMessage savedMessage) {
        // 채팅방 목록 업데이트 메시지 전송
        Long roomId = savedMessage.getChatRoom().getId();
        Long senderId = savedMessage.getSender().getId();
        Long receiverId = findReceiverId(roomId, senderId);

        // 수신자를 위한 업데이트 DTO 생성
        int unreadCount = countUnreadMessages(roomId, receiverId);
        ChatRoomUpdateDto updateForReceiver = createRoomUpdateDto(savedMessage, unreadCount);

        // 발신자를 위한 업데이트 DTO 생성 (안 읽은 메시지 수: 0)
        ChatRoomUpdateDto updateForSender = createRoomUpdateDto(savedMessage, 0);

        messagingTemplate.convertAndSend("/topic/chat-room/" + receiverId, updateForReceiver);
        messagingTemplate.convertAndSend("/topic/chat-room/" + senderId, updateForSender);
    }

    private ChatRoomUpdateDto createRoomUpdateDto(ChatMessage message, int unreadCount) {
        return new ChatRoomUpdateDto(
                message.getChatRoom().getId(),
                message.getContent(),
                message.getImageUrl(),
                timeUtil.formatTime(message.getCreatedAt()),
                unreadCount,
                message.getType()
        );
    }

    @Transactional
    public void processReadReceipt(Long roomId, Long userId) {
        // 읽음 처리
        markMessagesAsRead(roomId, userId);

        // 채팅방 리스트 UI 갱신
        ChatMessage lastMessage = findLastMessage(roomId);
        if (lastMessage != null) {
            ChatRoomUpdateDto roomUpdate = new ChatRoomUpdateDto(
                    roomId,
                    lastMessage.getContent(),
                    lastMessage.getImageUrl(),
                    timeUtil.formatTime(lastMessage.getCreatedAt()),
                    0,
                    lastMessage.getType()
            );
            messagingTemplate.convertAndSend("/topic/chat-room/" + userId, roomUpdate);
        }

        // 읽음 알림 전송
        Long partnerId = findReceiverId(roomId, userId);
        messagingTemplate.convertAndSend("/topic/chat-read/" + partnerId, "READ");

    }

    @Transactional
    public ChatMessage saveAndReturn(ChatMessage message) {
        ChatRoom room = chatRoomRepository.findById(message.getChatRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        message.setChatRoom(room);
        message.setCreatedAt(LocalDateTime.now());
        System.out.println("저장된 메시지: " + message.getContent());
        return chatMessageRepository.save(message);
    }

    public boolean existsByProduct(Product product) {
        return chatMessageRepository.existsByProduct(product);
    }

    @Transactional(readOnly = true)
    public boolean existsBySenderOrReceiver(User user) {
        return chatRoomRepository.existsBySender(user) ||
                chatRoomRepository.existsByReceiver(user);
    }
}
