package study.secondhand.module.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PAID("결제 완료"),
    SHIPPED("운송장 번호 등록"),
    DELIVERED("택배 수령 완료"),
    DONE("거래 완료");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
