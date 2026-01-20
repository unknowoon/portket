package com.portket.app.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Currency {
    // Enum constants with assigned priority (lower number indicates higher priority)
    EUR("EUR", 1),
    USD("USD", 2), // 내가 이런게 아니에요 야후파이낸스가 이런거에요
    GBP("GBP", 3),
    AUD("AUD", 4),
    NZD("NZD", 5),
    CAD("CAD", 6),
    CHF("CHF", 7),
    HKD("HKD", 8),
    SGD("SGD", 9),
    NOK("NOK", 10),
    TRY("TRY", 11),
    ZAR("ZAR", 12),
    CNY("CNY", 13),
    JPY("JPY", 14),
    KRW("KRW", 15);

    private final String code;
    private final int priority;

    /**
     * 두 통화의 우선순위를 비교하여, 우선순위가 더 높은(숫자가 더 낮은) 통화를 앞에 배치합니다.
     * 만약 두 통화가 동일하다면, 해당 통화 코드만 반환합니다.
     */
    public String buildCurrencyPair(Currency targetCurrency) {
        if (this == targetCurrency) {
            return null;
        }
        // 우선순위 비교: priority 값이 낮을수록 앞에 위치
        if (this.priority <= targetCurrency.priority) {
            return this.code + targetCurrency.getCode();
        } else {
            return targetCurrency.getCode() + this.code;
        }
    }
}