package study.secondhand.module.review;

import java.util.Arrays;
import java.util.List;

public enum ReviewTag {
    FAST_RESPONSE("빠른 답변"),
    KINDNESS("친절왕"),
    DETAILED_DESCRIPTION("상세한 상품 설명"),
    FAST_DELIVERY("빠른 배송"),
    SAFE_PACKAGING("안전 포장"),
    QUICK_PURCHASE_CONFIRM("빠른 구매 확정"),
    SIMPLE_INQUIRY("간단 문의"),
    COOL_DEAL("쿨거래");

    private final String label;

    ReviewTag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<ReviewTag> getTagsForRole(boolean isBuyer) {
        if (isBuyer) {
            return Arrays.asList(FAST_RESPONSE, KINDNESS, DETAILED_DESCRIPTION, FAST_DELIVERY, SAFE_PACKAGING);
        } else {
            return Arrays.asList(FAST_RESPONSE, KINDNESS, SIMPLE_INQUIRY, QUICK_PURCHASE_CONFIRM, COOL_DEAL);
        }
    }
}
