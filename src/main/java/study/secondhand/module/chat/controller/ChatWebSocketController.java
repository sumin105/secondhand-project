package study.secondhand.module.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import study.secondhand.module.chat.dto.*;
import study.secondhand.module.chat.service.ChatService;


@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 메시지 전송
    @MessageMapping("/chat/{roomId}")
    public void handleMessage(@DestinationVariable("roomId") Long roomId,
                              @Valid @Payload ChatMessageDto dto) {
        chatService.processAndBroadcastMessage(roomId, dto);
    }

    @MessageExceptionHandler
    public void handleValidationException(MethodArgumentNotValidException exception, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        if (exception.getBindingResult().hasErrors()) {
            String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();

            if (errorMessage != null) {
                messagingTemplate.convertAndSendToUser(sessionId, "/queue/error", errorMessage, headerAccessor.getMessageHeaders());
            }
        }
    }
}
