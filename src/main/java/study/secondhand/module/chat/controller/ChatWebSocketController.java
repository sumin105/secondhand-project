package study.secondhand.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import study.secondhand.module.chat.dto.*;
import study.secondhand.module.chat.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    // 메시지 전송
    @MessageMapping("/chat/{roomId}")
    public void handleMessage(@DestinationVariable("roomId") Long roomId, ChatMessageDto dto) {
        chatService.processAndBroadcastMessage(roomId, dto);
    }
}
