package study.secondhand.module.product.dto;

import lombok.Getter;
import study.secondhand.module.product.entity.Category;

import java.util.List;

@Getter
public class ProductFormViewDto {
    private final List<CategoryDto> categories;

    public ProductFormViewDto(List<Category> categories) {
        this.categories = categories.stream()
                .map(CategoryDto::new)
                .toList();
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
