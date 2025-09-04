package study.secondhand.module.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 확장자 추출(.jpg, .png)
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        // UUID로 안전한 파일명 생성 (공백 제거)
        String safeFilename = UUID.randomUUID().toString() + extension;
        // 저장할 경로
        File destination = new File(uploadDir + safeFilename);

        try {
            file.transferTo(destination);
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
        return safeFilename; // DB에는 이 값을 저장
    }

    public void deleteImageFile(String imageUrl) {
        String filename = imageUrl.replace("/images/uploads/", "");
        File file = new File(uploadDir + filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("파일 삭제 " + (deleted ? "성공" : "실패") + ": " + filename);
        } else {
            System.out.println("파일 존재하지 않음: " + filename);
        }
    }
}
