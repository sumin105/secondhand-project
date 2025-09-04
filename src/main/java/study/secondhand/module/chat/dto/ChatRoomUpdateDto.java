package study.secondhand.module.chat.dto;

import study.secondhand.module.chat.entity.ChatMessage;

public record ChatRoomUpdateDto(
        Long roomId,
        String lastMessage,
        String imageUrl,
        String formattedTime,
        Integer unreadCount,
        ChatMessage.MessageType messageType
) {}
