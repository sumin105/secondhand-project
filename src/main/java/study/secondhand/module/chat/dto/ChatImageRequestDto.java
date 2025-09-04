package study.secondhand.module.chat.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ChatImageRequestDto {
    private MultipartFile image;
    private Long roomId;
    private Long senderId;
}
