package study.secondhand.module.chat.dto;

import lombok.Getter;
import lombok.Setter;
import study.secondhand.module.chat.entity.ChatMessage;

@Getter
@Setter
public class ChatMessageDto {
    private Long roomId;
    private Long senderId;
    private String content;
    private String imageUrl;
    private ChatMessage.MessageType messageType;
}
