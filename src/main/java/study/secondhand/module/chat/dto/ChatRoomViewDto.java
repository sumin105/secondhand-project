package study.secondhand.module.chat.dto;

import lombok.Getter;
import study.secondhand.module.user.entity.User;

import java.util.List;

@Getter
public class ChatRoomViewDto {
    private final Long receiverId;
    private final String receiverNickname;
    private final boolean receiverWithdrawn;
    private final boolean receiverBanned;
    private final List<ChatRoomSummary> chatRooms;
    private final Long roomId;
    private final User loginUser;

    public ChatRoomViewDto(User receiver, List<ChatRoomSummary> chatRooms, Long roomId, User loginUser) {
        this.receiverId = receiver.getId();
        this.receiverNickname = receiver.getNickname() != null ? receiver.getNickname() : "상점 " + receiver.getId() + "호";
        this.receiverWithdrawn = receiver.isWithdrawn();
        this.receiverBanned = receiver.isBanned();
        this.chatRooms = chatRooms;
        this.roomId = roomId;
        this.loginUser = loginUser;
    }
}
