package study.secondhand.module.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.secondhand.global.oauth2.CustomUserDetails;
import study.secondhand.module.product.dto.*;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.product.service.CategoryService;
import study.secondhand.module.product.service.ProductService;
import study.secondhand.module.user.dto.StoreSummaryDto;
import study.secondhand.module.user.entity.User;
import study.secondhand.module.user.service.UserService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    @Value("${kakao.javascript.key}")
    private String kakaoJavascriptKey;

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String mainPage(@ModelAttribute("loginUser") User user,
                           @RequestParam(name = "sort", defaultValue = "latest") String sort,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           Model model) {

        Page<ProductSummaryDto> product = productService.findProductSummaries(sort, page);
        List<Category> categories = categoryService.findAll();
        MainViewDto viewData = new MainViewDto(user, categories, product, sort);
        model.addAttribute("viewData", viewData);
        return "main";
    }

    @GetMapping("/products/category/{id}")
    public String categoryProduct(@ModelAttribute("loginUser") User user,
                                  @PathVariable("id") Long categoryId,
                                  @RequestParam(name = "sort", defaultValue = "latest") String sort,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  Model model) {
        Category category = categoryService.findId(categoryId);
        Page<ProductSummaryDto> product = productService.findProductByCategorySummaries(sort, categoryId, page);
        List<Category> categories = categoryService.findAll();
        MainViewDto viewData = new MainViewDto(user, categories, product, sort);
        model.addAttribute("viewData", viewData);
        model.addAttribute("category", category);
        return "product/category-product";
    }

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         Model model) {
        SearchViewDto viewData;
        if (keyword.startsWith("@")) {
            String newKeyword = keyword.substring(1);
            Page<StoreSummaryDto> stores = userService.searchStoreSummaries(newKeyword, page);
            viewData = SearchViewDto.forStores(keyword, stores);
        } else {
            Page<ProductSummaryDto> products = productService.searchByKeyword(keyword, page);
            viewData = SearchViewDto.forProducts(keyword, products);
        }
        model.addAttribute("viewData", viewData);
        return "product/search";
    }

    @GetMapping("/products/{id}")
    public String productView(@PathVariable("id") Long id,
                              @RequestParam(value = "fromOrder", defaultValue = "false") boolean fromOrder,
                              @ModelAttribute("loginUser") User user,
                              Model model) {
        ProductDetailDto productDetailDto = productService.getProductDetailDto(id, user, fromOrder);
        model.addAttribute("product", productDetailDto);
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);

        if (productDetailDto.isDeleted()) {
            return "product/product-deleted";
        }
        return "product/product-view";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/products/new")
    public String productForm(Model model) {
        List<Category> categories = categoryService.findAll();
        ProductFormViewDto viewData = new ProductFormViewDto(categories);
        model.addAttribute("viewData", viewData);
        return "product/product-form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute ProductRequestDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/products/new";
        }

        try {
            Long productId = productService.createProduct(dto, userDetails.getUser());
            return "redirect:/products/" + productId;
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/new";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model, RedirectAttributes redirectAttributes) {

        try {
            ProductEditViewDto viewData = productService.getProductEditView(id, userDetails.getUser());
            model.addAttribute("viewData", viewData);
            return "product/product-edit";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + id;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/products/{id}")
    public String updateProduct(@PathVariable("id") Long productId,
                                @Valid @ModelAttribute ProductRequestDto dto,
                                BindingResult bindingResult,
                                @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/products/" + productId + "/edit";
        }

        try {
            productService.updateProduct(productId, dto, deletedImageIds, userDetails.getUser());
            return "redirect:/products/" + productId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다." + e.getMessage());
            return "redirect:/products/" + productId + "/edit";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long productId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        productService.deleteProduct(productId, userDetails.getUser());
        // 204 No Content
        return ResponseEntity.noContent().build();
    }
}
