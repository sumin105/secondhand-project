package study.secondhand.module.chat.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
public class ChatRoomSummary {
    private final Long roomId;
    private final Long otherUserId;
    private final String otherUserNickname;
    private final boolean otherUserBanned;
    private final boolean otherUserWithdrawn;

    private final String lastMessage;
    private final String formattedTime;
    private final int unreadCount;

    public ChatRoomSummary(Long roomId, Long otherUserId, String otherUserNickname,
                           Boolean otherUserBanned, Boolean otherUserWithdrawn,
                           String lastMessage,
                           LocalDateTime lastMessageTime, int unreadCount) {
        this.roomId = roomId;
        this.otherUserId = otherUserId;
        this.otherUserNickname = otherUserNickname != null ? otherUserNickname : "상점 " + otherUserId + "호";
        this.otherUserBanned = otherUserBanned;
        this.otherUserWithdrawn = otherUserWithdrawn;
        this.lastMessage = lastMessage;
        this.formattedTime = formattedMessageTime(lastMessageTime);
        this.unreadCount = unreadCount;
    }

    private String formattedMessageTime(LocalDateTime messageTime) {
        if (messageTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();

        if (messageTime.toLocalDate().equals(now.toLocalDate())) {
            return messageTime.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN));
        } else if (messageTime.getYear() == now.getYear()) {
            return messageTime.format(DateTimeFormatter.ofPattern("M월 d일"));

        } else {
            return messageTime.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
        }

    }
}
