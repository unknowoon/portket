package com.portket.util.finance;

import com.portket.app.constant.TransactionType;
import com.portket.app.dto.record.UpdateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FinancialCalculator 테스트")
class FinancialCalculatorTest {
    
    @Test
    @DisplayName("평균 가격 계산 성공")
    void calculateAveragePrice_Success() {
        // given
        BigDecimal amount = new BigDecimal("1500.00");
        BigDecimal quantity = new BigDecimal("10");
        
        // when
        BigDecimal averagePrice = FinancialCalculator.calculateAveragePrice(amount, quantity);
        
        // then
        assertThat(averagePrice).isEqualByComparingTo("150.00");
    }
    
    @Test
    @DisplayName("평균 가격 계산 - 수량이 0인 경우")
    void calculateAveragePrice_ZeroQuantity() {
        // given
        BigDecimal amount = new BigDecimal("1500.00");
        BigDecimal quantity = BigDecimal.ZERO;
        
        // when
        BigDecimal averagePrice = FinancialCalculator.calculateAveragePrice(amount, quantity);
        
        // then
        assertThat(averagePrice).isEqualByComparingTo("0");
    }
    
    @Test
    @DisplayName("평균 가격 계산 - 수량이 null인 경우")
    void calculateAveragePrice_NullQuantity() {
        // given
        BigDecimal amount = new BigDecimal("1500.00");
        BigDecimal quantity = null;
        
        // when
        BigDecimal averagePrice = FinancialCalculator.calculateAveragePrice(amount, quantity);
        
        // then
        assertThat(averagePrice).isEqualByComparingTo("0");
    }
    
    @Test
    @DisplayName("총 가격 계산 성공")
    void calculateTotalPrice_Success() {
        // given
        BigDecimal averagePrice = new BigDecimal("150.00");
        BigDecimal quantity = new BigDecimal("10");
        
        // when
        BigDecimal totalPrice = FinancialCalculator.calculateTotalPrice(averagePrice, quantity);
        
        // then
        assertThat(totalPrice).isEqualByComparingTo("1500.00");
    }
    
    @Test
    @DisplayName("총 가격 계산 - null 입력 처리")
    void calculateTotalPrice_NullInputs() {
        // when & then
        assertThat(FinancialCalculator.calculateTotalPrice(null, new BigDecimal("10")))
                .isEqualByComparingTo("0");
        assertThat(FinancialCalculator.calculateTotalPrice(new BigDecimal("150"), null))
                .isEqualByComparingTo("0");
        assertThat(FinancialCalculator.calculateTotalPrice(new BigDecimal("150"), BigDecimal.ZERO))
                .isEqualByComparingTo("0");
    }
    
    @Test
    @DisplayName("잔고 업데이트 - 매수")
    void updateBalance_Buy() {
        // given
        BigDecimal nowQuantity = new BigDecimal("10");
        BigDecimal nowAveragePrice = new BigDecimal("100.00");
        BigDecimal transactionQuantity = new BigDecimal("5");
        BigDecimal transactionPrice = new BigDecimal("120.00");
        TransactionType transactionType = TransactionType.BUY;
        
        // when
        UpdateResult result = FinancialCalculator.updateBalance(
                nowQuantity, nowAveragePrice, 
                transactionQuantity, transactionPrice, 
                transactionType
        );
        
        // then
        assertThat(result.quantity()).isEqualByComparingTo("15"); // 10 + 5
        // 새로운 평균 가격 = (10 * 100 + 5 * 120) / 15 = 1600 / 15 = 106.67
        assertThat(result.averagePrice()).isEqualByComparingTo("106.67");
    }
    
    @Test
    @DisplayName("잔고 업데이트 - 매도")
    void updateBalance_Sell() {
        // given
        BigDecimal nowQuantity = new BigDecimal("10");
        BigDecimal nowAveragePrice = new BigDecimal("100.00");
        BigDecimal transactionQuantity = new BigDecimal("3");
        BigDecimal transactionPrice = new BigDecimal("120.00");
        TransactionType transactionType = TransactionType.SELL;
        
        // when
        UpdateResult result = FinancialCalculator.updateBalance(
                nowQuantity, nowAveragePrice, 
                transactionQuantity, transactionPrice, 
                transactionType
        );
        
        // then
        assertThat(result.quantity()).isEqualByComparingTo("7"); // 10 - 3
        // 매도 시 평균 가격은 변경되지 않음
        assertThat(result.averagePrice()).isEqualByComparingTo("100.00");
    }
    
    @Test
    @DisplayName("잔고 업데이트 - 첫 매수")
    void updateBalance_FirstBuy() {
        // given
        BigDecimal nowQuantity = BigDecimal.ZERO;
        BigDecimal nowAveragePrice = BigDecimal.ZERO;
        BigDecimal transactionQuantity = new BigDecimal("10");
        BigDecimal transactionPrice = new BigDecimal("150.00");
        TransactionType transactionType = TransactionType.BUY;
        
        // when
        UpdateResult result = FinancialCalculator.updateBalance(
                nowQuantity, nowAveragePrice, 
                transactionQuantity, transactionPrice, 
                transactionType
        );
        
        // then
        assertThat(result.quantity()).isEqualByComparingTo("10");
        assertThat(result.averagePrice()).isEqualByComparingTo("150.00");
    }
    
    @Test
    @DisplayName("잔고 업데이트 - 전량 매도")
    void updateBalance_SellAll() {
        // given
        BigDecimal nowQuantity = new BigDecimal("10");
        BigDecimal nowAveragePrice = new BigDecimal("100.00");
        BigDecimal transactionQuantity = new BigDecimal("10");
        BigDecimal transactionPrice = new BigDecimal("120.00");
        TransactionType transactionType = TransactionType.SELL;
        
        // when
        UpdateResult result = FinancialCalculator.updateBalance(
                nowQuantity, nowAveragePrice, 
                transactionQuantity, transactionPrice, 
                transactionType
        );
        
        // then
        assertThat(result.quantity()).isEqualByComparingTo("0");
        assertThat(result.averagePrice()).isEqualByComparingTo("100.00");
    }
    
    @Test
    @DisplayName("인스턴스 생성 방지")
    void preventInstantiation() {
        // when & then
        assertThatThrownBy(() -> {
            var constructor = FinancialCalculator.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class)
          .hasMessageContaining("Cannot instantiate utility class");
    }
}