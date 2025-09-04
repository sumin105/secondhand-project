package study.secondhand.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.chat.dto.ChatImageRequestDto;
import study.secondhand.module.chat.dto.ChatMessageResponse;
import study.secondhand.module.chat.dto.ImageUploadResponse;
import study.secondhand.module.chat.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatService chatService;

    // 메시지 목록 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesByRoomId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                         @PathVariable("roomId") Long roomId) {
        List<ChatMessageResponse> messages = chatService.getMessagesByRoomId(roomId, userDetails.getUser().getId());
        return ResponseEntity.ok(messages);
    }

    // 안 읽은 메시지 수 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadMessageCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        int unreadMessageCount = chatService.getTotalUnreadMessageCount(userDetails.getUser().getId());
        return ResponseEntity.ok(unreadMessageCount);
    }

    // 메시지 읽음 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{roomId}/messages/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable("roomId") Long roomId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.processReadReceipt(roomId, userDetails.getUser().getId());
        // 204 No Content 응답
        return ResponseEntity.noContent().build();
    }

    // 이미지 업로드
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{roomId}/images")
    public ResponseEntity<ImageUploadResponse> uploadImage(@ModelAttribute ChatImageRequestDto dto) {
        String imageUrl = chatService.saveImage(dto);
        // 201 Created 및 JSON 응답
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(imageUrl));
    }
}
