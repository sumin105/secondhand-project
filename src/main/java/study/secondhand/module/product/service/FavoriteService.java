package study.secondhand.module.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.secondhand.module.product.dto.FavoriteDto;
import study.secondhand.module.product.dto.ProductImageDto;
import study.secondhand.module.product.entity.Favorite;
import study.secondhand.module.product.entity.Product;
import study.secondhand.module.product.repository.FavoriteRepository;
import study.secondhand.module.user.entity.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductService productService;

    @Transactional
    public void addFavorite(User user, Long productId) {
        Product product = productService.findById(productId);

        if (!favoriteRepository.existsByUserAndProduct(user, product)) {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .product(product)
                    .build();
            favoriteRepository.save(favorite);
            product.increaseFavoriteCount();
        }
    }

    @Transactional
    public void removeFavorite(User user, Long productId) {
        Product product = productService.findById(productId);

        Optional<Favorite> existing = favoriteRepository.findByUserAndProduct(user, product);

        existing.ifPresent(favorite -> {
            favoriteRepository.delete(favorite);
            product.decreaseFavoriteCount();
        });
    }

    public boolean isFavorite(User user, Product product) {
        return favoriteRepository.existsByUserAndProduct(user, product);
    }

    public int getFavoriteCount(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }

    public List<Long> getFavoriteUserIds(Long productId) {
        return favoriteRepository.findUserIdsByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Page<FavoriteDto> findUserFavoriteDto(Long id, int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favorites = favoriteRepository.findFavoritesByUserId(id, pageable);

        return favorites.map(favorite -> {
            Product product = favorite.getProduct();
            return new FavoriteDto(
                    favorite.getId(),
                    product.getId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getStatus().toString(),
                    product.getCreatedAt(),
                    product.getImages().isEmpty() ? List.of() :
                            List.of(new ProductImageDto(product.getThumbnailImageUrl()))
            );
        });
    }
}
