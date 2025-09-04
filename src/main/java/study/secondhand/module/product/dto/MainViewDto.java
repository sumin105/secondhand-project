package study.secondhand.module.product.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.user.entity.User;

import java.util.List;

@Getter
public class MainViewDto {
    private final Long loginUserId;
    private final List<CategoryDto> categories;
    private final Page<ProductSummaryDto> products;
    private final String currentSort; // latest, likes

    public MainViewDto(User loginUser, List<Category> categories,
                       Page<ProductSummaryDto> productPage, String sort) {
        this.loginUserId = (loginUser != null) ? loginUser.getId() : null;
        this.categories = categories.stream()
                .map(CategoryDto::new)
                .toList();
        this.products = productPage;
        this.currentSort = sort;
    }

    @Getter
    public static class CategoryDto {
        private final Long id;
        private final String name;

        public CategoryDto(Category category) {
            this.id = category.getId();
            this.name = category.getName();
        }
    }
}
