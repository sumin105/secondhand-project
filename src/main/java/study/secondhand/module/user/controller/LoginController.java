package study.secondhand.module.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/api/test")
    public ResponseEntity<String> test(Authentication auth) {
        return ResponseEntity.ok("안녕하세요, " + auth.getName());
    }
}
