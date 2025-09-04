package study.secondhand.module.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.exception.DuplicateNicknameException;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.order.dto.PurchasedProductDto;
import study.secondhand.module.order.dto.SaledProductDto;
import study.secondhand.module.order.service.OrderService;
import study.secondhand.module.product.dto.FavoriteDto;
import study.secondhand.module.product.dto.ProductSummaryDto;
import study.secondhand.module.product.service.FavoriteService;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.review.dto.ReviewDto;
import study.secondhand.module.review.service.ReviewService;
import study.secondhand.module.user.dto.ShopHeaderDto;
import study.secondhand.module.user.dto.ShopViewDto;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

@Controller
@RequiredArgsConstructor
public class ShopController {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    @GetMapping("/shop/{id}")
    public String shop(@ModelAttribute("loginUser") User user,
                       @PathVariable("id") Long id,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        try {
            ShopViewDto viewData = userService.getShopViewDto(user, id, page);
            ShopHeaderDto shopHeaderDto = userService.getShopHeaderDto(user, id, "상품");
            model.addAttribute("headerData", shopHeaderDto);
            model.addAttribute("viewData", viewData);
            return "shop/shop";
        } catch (IllegalArgumentException e) {
            return "error/shop-not-found";
        }
    }

    @GetMapping("/shop/{id}/reviews")
    public String shopReviews(@ModelAttribute("loginUser") User user,
                              @PathVariable("id") Long id,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              Model model) {
        try {
            ShopHeaderDto shopHeaderDto = userService.getShopHeaderDto(user, id, "후기");
            Page<ReviewDto> reviewPage = reviewService.getRecentReviews(id, page);

            model.addAttribute("headerData", shopHeaderDto);
            model.addAttribute("reviews", reviewPage);
            model.addAttribute("userId", id);
            return "shop/review";
        } catch (IllegalArgumentException e) {
            return "error/shop-not-found";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/shop/{id}/wishlist")
    public String wishlist(@PathVariable("id") Long id,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           Model model) {
        try {
            ShopHeaderDto shopHeaderDto = userService.getShopHeaderDto(userDetails.getUser(), id, "찜");
            userService.isShopOwner(id, userDetails.getUser());
            Page<FavoriteDto> favorites = favoriteService.findUserFavoriteDto(id, page);
            System.out.println(favorites);

            model.addAttribute("headerData", shopHeaderDto);
            model.addAttribute("favorites", favorites);
            return "shop/wishlist";
        } catch (IllegalArgumentException e) {
            return "error/shop-not-found";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/shop/{id}/transactions")
    public String orderList(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable("id") Long id,
                            @RequestParam(name = "tab", defaultValue = "purchase") String tab,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            Model model) {

        ShopHeaderDto shopHeaderDto = userService.getShopHeaderDto(userDetails.getUser(), id, "거래내역");
        userService.isShopOwner(id, userDetails.getUser());
        Long userId = userDetails.getUser().getId();

        if ("purchase".equals(tab)) {
            Page<PurchasedProductDto> purchases = orderService.findPurchasedProductsByUserId(userId, page);
            model.addAttribute("purchases", purchases);
        } else if ("sale".equals(tab)) {
            Page<SaledProductDto> sales = orderService.findSaledProductsBySellerId(userId, page);
            model.addAttribute("sales", sales);
        }
        model.addAttribute("headerData", shopHeaderDto);
        model.addAttribute("currentTab", tab);
        return "shop/transaction-list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/products/manage")
    public String manageProduct(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                Model model) {

        Long userId = userDetails.getUser().getId();
        ShopHeaderDto shopHeaderDto = userService.getShopHeaderDto(userDetails.getUser(), userId, "상품관리");
        Page<ProductSummaryDto> products = productService.findUserProductSummaries(userDetails.getUser(), userId, page);
        model.addAttribute("headerData", shopHeaderDto);
        model.addAttribute("products", products);
        return "shop/product-manage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/update")
    public String updateUserInfo(@AuthenticationPrincipal CustomUserDetails loginUser,
                                 @RequestParam(name = "nickname", required = true) String nickname,
                                 @RequestParam(name = "intro", required = false) String intro,
                                 RedirectAttributes redirectAttributes) {

        User user = loginUser.getUser();

        try {
            userService.updateUserInfo(user.getId(), nickname, intro);
            redirectAttributes.addFlashAttribute("successMessage", "정보를 수정했습니다.");
            return "redirect:/shop/" + user.getId();
        } catch (IllegalArgumentException | DuplicateNicknameException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/shop/" + user.getId();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/user")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            userService.withdrawUser(userDetails.getUser().getId(), userDetails.getUser());
            // 204 No Content, 로그아웃 처리는 js
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
