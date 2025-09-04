package study.secondhand.module.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.product.service.FavoriteService;

@RestController
@RequestMapping("/api/products/{productId}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Void> addFavorite(@PathVariable("productId") Long productId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        favoriteService.addFavorite(userDetails.getUser(), productId);
        // 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(@PathVariable("productId") Long productId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        favoriteService.removeFavorite(userDetails.getUser(), productId);
        // 204 No Content
        return ResponseEntity.noContent().build();
    }
}