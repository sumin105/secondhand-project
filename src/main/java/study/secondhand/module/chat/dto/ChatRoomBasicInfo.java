package study.secondhand.module.chat.dto;

import study.secondhand.module.chat.entity.ChatRoom;

public record ChatRoomBasicInfo(
        Long roomId,
        ChatRoom room,
        Long unreadCount
) {}
