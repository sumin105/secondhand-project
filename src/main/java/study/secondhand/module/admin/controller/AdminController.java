package study.secondhand.module.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.admin.dto.AdminProductDto;
import study.secondhand.module.admin.dto.AdminUserDto;
import study.secondhand.module.admin.dto.ProductReportsDto;
import study.secondhand.module.admin.dto.UserReportsDto;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.report.entity.Report;
import study.secondhand.module.report.service.ReportService;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userService.count());
        model.addAttribute("productCount", productService.count());
        model.addAttribute("todayProductCount", productService.countToday());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(@RequestParam(name = "page", defaultValue = "0") int page,
                           Model model) {
        Page<AdminProductDto> productDtos = productService.findProductsForAdmin(page);
        model.addAttribute("products", productDtos);
        return "admin/products";
    }

    @GetMapping("/users")
    public String users(@RequestParam(name = "page", defaultValue = "0") int page,
                        Model model) {
        Page<AdminUserDto> userDtos = userService.findUsersForAdmin(page);
        model.addAttribute("users", userDtos);
        return "admin/users";
    }

    // 상품 삭제
    @DeleteMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "id") Long id) {
        try {
            productService.adminDeleteProduct(id);
            // 204 No Content
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 존재하지 않는 상품 삭제 시도 등
            return ResponseEntity.notFound().build();
        }
    }

    // 유저 정지 or 정지해제
    @PostMapping("/users/{id}/toggle-suspend")
    public String toggleSuspend(@PathVariable(name = "id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            String message = userService.toggleUserSuspend(id);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // 유저 탈퇴
    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") Long id,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            userService.withdrawUser(id, userDetails.getUser());
            // 204 No Content
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/products/{id}/reports")
    public String viewProductReports(@PathVariable("id") Long id,
                                     Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            List<Report> reports = reportService.findByProduct(product);

            ProductReportsDto dto = new ProductReportsDto(product, reports);
            model.addAttribute("viewData", dto);
            return "admin/product-reports";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @GetMapping("/users/{id}/reports")
    public String viewUserReports(@PathVariable("id") Long id,
                                  Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            List<Report> reports = reportService.findByUser(user);

            UserReportsDto dto = new UserReportsDto(user, reports);
            model.addAttribute("viewData", dto);
            return "admin/user-reports";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
