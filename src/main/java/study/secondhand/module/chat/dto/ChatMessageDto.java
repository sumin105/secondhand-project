package study.secondhand.module.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import study.secondhand.module.chat.entity.ChatMessage;

@Getter
@Setter
public class ChatMessageDto {
    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long roomId;
    @NotNull(message = "보내는 사람 ID는 필수입니다.")
    private Long senderId;
    @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다.")
    private String content;
    private String imageUrl;
    @NotNull(message = "메시지 타입은 필수입니다.")
    private ChatMessage.MessageType messageType;
}
