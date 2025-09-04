package study.secondhand.module.product.service;

import org.springframework.stereotype.Service;
import study.secondhand.module.product.entity.Category;
import study.secondhand.module.product.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByIdAsc();
    }

    public Category findId(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }
}
