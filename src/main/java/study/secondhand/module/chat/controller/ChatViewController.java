package study.secondhand.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.chat.dto.ChatRoomSummary;
import study.secondhand.module.chat.dto.ChatRoomViewDto;
import study.secondhand.module.chat.service.ChatService;
import study.secondhand.module.user.entity.User;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatViewController {
    private final ChatService chatService;

    // 채팅 목록
    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    public String chatRoomList(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User loginUser = userDetails.getUser();
        List<ChatRoomSummary> chatRooms = chatService.getRoomsByUser(loginUser);
        model.addAttribute("chatRooms", chatRooms);
        return "chat/chat-list";
    }

    // 채팅방 입장, 채팅방 생성 or 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{receiverId}")
    public String enterChatRoom(@PathVariable("receiverId") Long receiverId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        User loginUser = userDetails.getUser();
        ChatRoomViewDto dto = chatService.prepareChatRoom(loginUser, receiverId);
        model.addAttribute("viewData", dto);
        return "chat/chat";
    }
}
