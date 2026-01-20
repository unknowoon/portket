package com.portket.util.finance;

import com.portket.app.constant.TransactionType;
import com.portket.app.dto.record.UpdateResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 금융 관련 계산 유틸리티 클래스.
 * 인스턴스 생성을 막기 위해 private 생성자를 가지고 있으며,
 * 모든 메서드는 stateless 하게 static 으로 제공됩니다.
 */
public final class FinancialCalculator {

    // 인스턴스화를 막습니다.
    private FinancialCalculator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 거래금액과 거래수량을 이용해 평균 가격을 계산합니다. (반올림 2자리)
     *
     * @param amount 거래 총 금액
     * @param quantity 거래 수량
     * @return 평균 가격 (수량이 0이면 0 반환)
     */
    public static BigDecimal calculateAveragePrice(BigDecimal amount, BigDecimal quantity) {
        if (quantity == null || BigDecimal.ZERO.compareTo(quantity) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(quantity, 2, RoundingMode.HALF_UP);
    }

    /**
     * 거래 평균 가격과 거래 수량을 이용하여 거래 총 금액을 계산합니다.
     *
     * @param averagePrice 거래 평균 가격
     * @param quantity 거래 수량
     * @return 거래 총 금액 (평균 가격이나 수량이 null 이거나 수량이 0이면 0 반환)
     */
    public static BigDecimal calculateTotalPrice(BigDecimal averagePrice, BigDecimal quantity) {
        if (averagePrice == null || quantity == null || BigDecimal.ZERO.compareTo(quantity) == 0) {
            return BigDecimal.ZERO;
        }
        return averagePrice.multiply(quantity);
    }

    /**
     * 현재 잔고와 거래 정보를 바탕으로 잔고를 업데이트합니다.
     *
     * @param nowQuantity 현재 수량
     * @param nowAveragePrice 현재 평균 가격
     * @param transactionQuantity 거래 수량
     * @param transactionPrice 거래 가격
     * @param transactionType 거래 유형 (매수/매도)
     * @return 업데이트 결과(새로운 수량 및 평균 가격)
     */
    public static UpdateResult updateBalance(BigDecimal nowQuantity, BigDecimal nowAveragePrice,
                                             BigDecimal transactionQuantity, BigDecimal transactionPrice,
                                             TransactionType transactionType) {
        BigDecimal nowTotalPrice = nowAveragePrice.multiply(nowQuantity);
        BigDecimal transactionTotalPrice = transactionPrice.multiply(transactionQuantity);

        BigDecimal quantity;
        BigDecimal averagePrice;

        if (TransactionType.BUY.equals(transactionType)) {
            quantity = nowQuantity.add(transactionQuantity);
            BigDecimal totalPrice = nowTotalPrice.add(transactionTotalPrice);
            // 평균 가격 = 총 금액 / 총 수량
            averagePrice = calculateAveragePrice(totalPrice, quantity);
        } else {
            quantity = nowQuantity.subtract(transactionQuantity);
            // 매도 시 평균 가격은 변경되지 않음
            averagePrice = nowAveragePrice;
        }

        return new UpdateResult(quantity, averagePrice);
    }
}