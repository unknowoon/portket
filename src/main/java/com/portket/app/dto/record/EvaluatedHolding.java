package com.portket.app.dto.record;

import com.portket.app.domain.Balance;
import com.portket.app.domain.Holding;

import java.math.BigDecimal;

public record EvaluatedHolding(
        Holding holding,
        BigDecimal totalAmount,      // 해당 홀딩의 통화 기준 총 평가금액 = 수량 * 현재가
        BigDecimal evaluatedUsd,     // USD 환산 평가금액
        BigDecimal evaluatedKrw,     // KRW 환산 평가금액
        BigDecimal profitAmount,     // 수익금액 = (현재가 - 평단가) * 수량
        BigDecimal profitRatio
) {
}
