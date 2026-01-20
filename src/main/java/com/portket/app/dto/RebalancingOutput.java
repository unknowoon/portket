package com.portket.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingOutput {
    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalInvestmentAmount;
    private List<RebalancingItem> items;
    private BigDecimal totalCost; // 리밸런싱에 필요한 총 비용
    private boolean simulated; // 시뮬레이션 여부
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalancingItem {
        private Long instrumentId;
        private String ticker;
        private String instrumentName;
        private BigDecimal targetWeight; // 목표 비중 (%)
        private BigDecimal currentWeight; // 현재 비중 (%)
        private BigDecimal targetValue; // 목표 금액
        private BigDecimal currentValue; // 현재 보유 금액
        private BigDecimal currentQuantity; // 현재 보유 수량
        private BigDecimal currentPrice; // 현재 가격
        private BigDecimal requiredQuantity; // 필요 수량 (양수: 매수, 음수: 매도)
        private BigDecimal requiredValue; // 필요 금액 (양수: 매수, 음수: 매도)
        private String action; // BUY, SELL, HOLD
    }
}