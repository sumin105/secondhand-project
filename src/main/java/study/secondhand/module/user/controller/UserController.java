package study.secondhand.module.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.user.dto.DeliveryInfoDto;
import study.secondhand.module.user.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delivery-info")
    public ResponseEntity<DeliveryInfoDto> getDeliveryInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        DeliveryInfoDto deliveryInfo = userService.getDeliveryInfo(userDetails.getUser().getId());
        System.out.println("deliveryInfo: " + deliveryInfo);
        return ResponseEntity.ok(deliveryInfo);
    }
}
