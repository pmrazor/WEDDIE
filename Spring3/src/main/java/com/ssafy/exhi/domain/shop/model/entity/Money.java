package com.ssafy.exhi.domain.shop.model.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private static final String CURRENCY = "KRW"; // 통화 단위
    private BigDecimal amount; // 금액

    // 정적 팩토리 메서드
    public static Money wons(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    // 비즈니스 로직 메서드들
    public Money plus(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(double multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    // 비교 메서드

    // 현재 Money가 다른 Money보다 크다면
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    // 현재 Money가 다른 Money보다 작다면
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    // Value Object 패턴을 위한 메서드들
    // 같은 금액이면 같은 객체로 간주
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + CURRENCY;
    }

    // 금액 접근자. 금액값 반환 메서드
    public BigDecimal getAmount() {
        return amount;
    }
}