package study.secondhand.module.product.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import study.secondhand.module.user.dto.StoreSummaryDto;

@Getter
public class SearchViewDto {
    private final String keyword;
    private final boolean isStoreSearch;
    private final Page<StoreSummaryDto> stores;
    private final Page<ProductSummaryDto> products;

    public static SearchViewDto forStores(String keyword, Page<StoreSummaryDto> stores) {
        return new SearchViewDto(keyword, true, stores, null);
    }

    public static SearchViewDto forProducts(String keyword, Page<ProductSummaryDto> products) {
        return new SearchViewDto(keyword, false, null, products);
    }

    private SearchViewDto(String keyword, boolean isStoreSearch,
                          Page<StoreSummaryDto> stores, Page<ProductSummaryDto> products) {
        this.keyword = keyword;
        this.isStoreSearch = isStoreSearch;
        this.stores = stores;
        this.products = products;
    }
}
